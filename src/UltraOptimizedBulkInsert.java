import configuration.DbConnection;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class UltraOptimizedBulkInsert {

    // Configuration parameters
    private static int TOTAL_ROWS;
    private static final String[] CATEGORIES = {"Electronics", "Books", "Clothing"};
    private static int BATCH_SIZE;
    private static int BUFFER_SIZE;
    private static int UUID_POOL_SIZE;
    private static int QUEUE_CAPACITY;
    private static int NUM_PRODUCERS;
    private static int PRECOMPUTED_TEMPLATE_SIZE;
    private static final int MAX_ROW_BYTES_ESTIMATE = 264;
    private static final int PROGRESS_REPORT_INTERVAL = 1_000_000;

    // Atomic flags and counters
    private static final AtomicBoolean errorOccurred = new AtomicBoolean(false);
    private static final AtomicLong rowsGenerated = new AtomicLong(0);
    private static final AtomicLong bytesWritten = new AtomicLong(0);

    public static void main(String[] args) {
        loadConfiguration();

        // Pre-compute all static data
        String fixedTimestamp = LocalDateTime.now().toString();
        String[] uuidPool = generateUuidPool();
        byte[][] precomputedRows;
        try {
            precomputedRows = precomputeRowData(uuidPool, fixedTimestamp);
        } catch (UnsupportedEncodingException e) {
            System.err.println("❌ Error pre-computing row data: " + e.getMessage());
            e.printStackTrace();
            return;
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Configuration Error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        long startTime = System.currentTimeMillis();

        try (Connection conn = DbConnection.getDatabaseConnection()) {
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                optimizePostgreSQL(stmt);
                dropConstraintsAndIndexes(stmt);

                // Setup data pipeline
                BlockingQueue<ByteBuffer> dataQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
                PipedOutputStream pipedOut = new PipedOutputStream();
                PipedInputStream pipedIn = new PipedInputStream(pipedOut, BUFFER_SIZE);

                // COPY operation thread
                Thread copyThread = new Thread(() -> {
                    try {
                        CopyManager copyManager = new CopyManager((BaseConnection) conn);
                        long insertedRows = copyManager.copyIn(
                                "COPY products (p_name, category, price, qty, is_deleted, p_uuid, created_at) " +
                                        "FROM STDIN WITH (FORMAT text, FREEZE true, DELIMITER E'\\t')",
                                pipedIn
                        );
                        System.out.printf("✅ COPY operation inserted %,d rows\n", insertedRows);
                        // Validate row count in database
                        try (Statement countStmt = conn.createStatement();
                             ResultSet rs = countStmt.executeQuery("SELECT COUNT(*) FROM products")) {
                            if (rs.next()) {
                                long dbRowCount = rs.getLong(1);
                                System.out.printf("✅ Database reports %,d rows in products table\n", dbRowCount);
                                if (dbRowCount != TOTAL_ROWS) {
                                    System.err.printf("❌ Row count mismatch: expected %,d, got %,d\n", TOTAL_ROWS, dbRowCount);
                                    errorOccurred.set(true);
                                }
                            }
                        }
                    } catch (Exception e) {
                        handleError("COPY thread", e);
                    } finally {
                        closeQuietly(pipedIn);
                    }
                }, "CopyThread");
                copyThread.start();

                // Producer threads
                ExecutorService producerExecutor = Executors.newFixedThreadPool(NUM_PRODUCERS);
                CountDownLatch producerLatch = new CountDownLatch(NUM_PRODUCERS);

                for (int p = 0; p < NUM_PRODUCERS; p++) {
                    final int producerId = p;
                    producerExecutor.submit(() -> {
                        try {
                            // Distribute rows evenly, handling remainder
                            int rowsPerProducer = TOTAL_ROWS / NUM_PRODUCERS;
                            int start = producerId * rowsPerProducer;
                            int end = (producerId == NUM_PRODUCERS - 1) ? TOTAL_ROWS : start + rowsPerProducer;

                            for (int batch = start; batch < end && !errorOccurred.get(); batch += BATCH_SIZE) {
                                ByteBuffer buffer = ByteBuffer.allocateDirect((int) Math.min(BATCH_SIZE, end - batch) * MAX_ROW_BYTES_ESTIMATE);
                                int endIdx = Math.min(batch + BATCH_SIZE, end);

                                for (int i = batch; i < endIdx; i++) {
                                    buffer.put(precomputedRows[i % precomputedRows.length]);
                                }
                                buffer.flip();
                                dataQueue.put(buffer);
                                long rowsInBatch = endIdx - batch;
                                rowsGenerated.addAndGet(rowsInBatch);

                                if (batch % PROGRESS_REPORT_INTERVAL == 0 || batch + rowsInBatch >= end) {
                                    System.out.printf("✅ Producer %d: Generated %,d rows (%.1f%%)\n",
                                            producerId, rowsGenerated.get(), (rowsGenerated.get() * 100.0 / TOTAL_ROWS));
                                }
                            }
                            System.out.printf("✅ Producer %d: Completed, generated %,d rows\n",
                                    producerId, end - start);
                        } catch (Exception e) {
                            handleError("Producer thread " + producerId, e);
                        } finally {
                            producerLatch.countDown();
                        }
                    });
                }

                // Consumer thread
                Thread consumerThread = new Thread(() -> {
                    try (PipedOutputStream out = pipedOut) {
                        int buffersProcessed = 0;
                        while (!errorOccurred.get()) {
                            ByteBuffer buffer = dataQueue.poll(100, TimeUnit.MILLISECONDS);
                            if (buffer == null) {
                                if (producerLatch.getCount() == 0 && dataQueue.isEmpty()) {
                                    break;
                                }
                                continue;
                            }

                            if (buffer.hasRemaining()) {
                                try {
                                    byte[] array;
                                    if (buffer.hasArray()) {
                                        array = buffer.array();
                                        out.write(array, buffer.arrayOffset(), buffer.remaining());
                                    } else {
                                        array = new byte[buffer.remaining()];
                                        buffer.get(array);
                                        out.write(array);
                                    }
                                    bytesWritten.addAndGet(buffer.remaining());
                                } catch (IOException e) {
                                    handleError("Consumer write", e);
                                }
                            }

                            buffersProcessed++;
                            if (buffersProcessed % 100 == 0) {
                                System.out.printf("✅ Consumer processed %d buffers (%,d bytes)\n",
                                        buffersProcessed, bytesWritten.get());
                            }
                        }
                        out.flush();
                        System.out.printf("✅ Consumer completed, processed %,d bytes\n", bytesWritten.get());
                    } catch (Exception e) {
                        handleError("Consumer thread", e);
                    }
                }, "ConsumerThread");
                consumerThread.start();

                // Wait for completion
                producerExecutor.shutdown();
                try {
                    if (!producerExecutor.awaitTermination(60, TimeUnit.MINUTES)) {
                        System.err.println("❌ Producer threads did not complete in time");
                        errorOccurred.set(true);
                    }
                } catch (InterruptedException e) {
                    handleError("Producer executor await", e);
                }

                // Send end-of-data marker
                if (!errorOccurred.get()) {
                    try {
                        dataQueue.put(ByteBuffer.allocate(0).asReadOnlyBuffer());
                    } catch (InterruptedException e) {
                        handleError("End-of-data marker", e);
                    }
                }

                try {
                    consumerThread.join(TimeUnit.MINUTES.toMillis(10));
                    copyThread.join(TimeUnit.MINUTES.toMillis(10));
                } catch (InterruptedException e) {
                    handleError("Thread join", e);
                }

                if (errorOccurred.get()) {
                    throw new RuntimeException("One or more threads encountered errors");
                }

                restoreTableStructure(stmt);
                System.out.println("⚠️ Skipping index creation to optimize insertion time");

                conn.commit();
                double readTimeSeconds = measureReadTime(stmt);
                resetPostgreSQL(stmt);

                printResults(startTime, rowsGenerated.get(), readTimeSeconds);

            } catch (Exception e) {
                handleError("Main operation", e);
                try { conn.rollback(); } catch (SQLException ex) { handleError("Rollback", ex); }
            }
        } catch (Exception e) {
            handleError("Database connection", e);
        }
    }

    private static void loadConfiguration() {
        Properties props = System.getProperties();
        TOTAL_ROWS = Integer.parseInt(props.getProperty("totalRows", "10000000"));
        BATCH_SIZE = Integer.parseInt(props.getProperty("batchSize", "100000"));
        BUFFER_SIZE = Integer.parseInt(props.getProperty("bufferSize", "10000000")); // 10MB
        UUID_POOL_SIZE = Integer.parseInt(props.getProperty("uuidPoolSize", "10000"));
        QUEUE_CAPACITY = Integer.parseInt(props.getProperty("queueCapacity", "5000"));
        NUM_PRODUCERS = Integer.parseInt(props.getProperty("numProducers",
                String.valueOf(Math.max(2, Runtime.getRuntime().availableProcessors()))));
        PRECOMPUTED_TEMPLATE_SIZE = Integer.parseInt(props.getProperty("precomputedTemplateSize", "10000"));

        System.out.println("--- Configuration ---");
        System.out.println("TOTAL_ROWS: " + TOTAL_ROWS);
        System.out.println("BATCH_SIZE: " + BATCH_SIZE);
        System.out.println("BUFFER_SIZE: " + BUFFER_SIZE + " bytes");
        System.out.println("UUID_POOL_SIZE: " + UUID_POOL_SIZE);
        System.out.println("QUEUE_CAPACITY: " + QUEUE_CAPACITY);
        System.out.println("NUM_PRODUCERS: " + NUM_PRODUCERS);
        System.out.println("PRECOMPUTED_TEMPLATE_SIZE: " + PRECOMPUTED_TEMPLATE_SIZE);
        System.out.println("MAX_ROW_BYTES_ESTIMATE: " + MAX_ROW_BYTES_ESTIMATE);
        System.out.println("---------------------");
    }

    private static String[] generateUuidPool() {
        System.out.println("🔧 Pre-generating UUID pool...");
        String[] uuidPool = new String[UUID_POOL_SIZE];
        for (int i = 0; i < uuidPool.length; i++) {
            uuidPool[i] = UUID.randomUUID().toString();
        }
        return uuidPool;
    }

    private static byte[][] precomputeRowData(String[] uuidPool, String fixedTimestamp)
            throws UnsupportedEncodingException {
        System.out.println("🔧 Pre-computing row templates...");
        if (PRECOMPUTED_TEMPLATE_SIZE <= 0) {
            throw new IllegalArgumentException("PRECOMPUTED_TEMPLATE_SIZE must be > 0");
        }

        byte[][] templates = new byte[PRECOMPUTED_TEMPLATE_SIZE][];
        for (int i = 0; i < templates.length; i++) {
            templates[i] = String.format("Product %d\t%s\t%.1f\t%d\tfalse\t%s\t%s\n",
                    i + 1,
                    CATEGORIES[i % CATEGORIES.length],
                    10.0 + (i & 255),
                    30 + (i & 63),
                    uuidPool[i % UUID_POOL_SIZE],
                    fixedTimestamp).getBytes(StandardCharsets.UTF_8);
        }
        return templates;
    }

    private static void optimizePostgreSQL(Statement stmt) throws SQLException {
        System.out.println("🔧 Applying PostgreSQL optimizations...");
        try { stmt.execute("SET synchronous_commit = OFF"); } catch (SQLException e) { System.out.println("⚠️ Could not set synchronous_commit: " + e.getMessage()); }
        try { stmt.execute("SET wal_compression = ON"); } catch (SQLException e) { System.out.println("⚠️ Could not set wal_compression: " + e.getMessage()); }
        try { stmt.execute("SET maintenance_work_mem = '512MB'"); } catch (SQLException e) { System.out.println("⚠️ Could not set maintenance_work_mem: " + e.getMessage()); }
        try { stmt.execute("SET work_mem = '64MB'"); } catch (SQLException e) { System.out.println("⚠️ Could not set work_mem: " + e.getMessage()); }
        try { stmt.execute("SET temp_buffers = '64MB'"); } catch (SQLException e) { System.out.println("⚠️ Could not set temp_buffers: " + e.getMessage()); }
        try { stmt.execute("SET max_parallel_workers_per_gather = 0"); } catch (SQLException e) { System.out.println("⚠️ Could not set max_parallel_workers_per_gather: " + e.getMessage()); }
    }

    private static void dropConstraintsAndIndexes(Statement stmt) throws SQLException {
        System.out.println("🗑️ Dropping constraints and indexes...");
        stmt.execute("ALTER TABLE products DROP CONSTRAINT IF EXISTS products_pkey CASCADE");
        stmt.execute("ALTER TABLE products DROP CONSTRAINT IF EXISTS products_price_check");
        stmt.execute("ALTER TABLE products DROP CONSTRAINT IF EXISTS products_qty_check");
        stmt.execute("DROP INDEX IF EXISTS idx_products_name");
        stmt.execute("DROP INDEX IF EXISTS idx_products_category");
        stmt.execute("DROP INDEX IF EXISTS idx_products_puuid");
        stmt.execute("DROP INDEX IF EXISTS idx_products");
        stmt.execute("DROP INDEX IF EXISTS idx_products_composite");

        stmt.execute("ALTER TABLE products ALTER COLUMN p_uuid DROP DEFAULT");
        stmt.execute("ALTER TABLE products ALTER COLUMN created_at DROP DEFAULT");
        stmt.execute("ALTER TABLE products ALTER COLUMN is_deleted DROP DEFAULT");

        stmt.execute("TRUNCATE TABLE products RESTART IDENTITY");
    }

    private static void restoreTableStructure(Statement stmt) throws SQLException {
        System.out.println("🔧 Restoring table structure...");
        stmt.execute("ALTER TABLE products ADD CONSTRAINT products_pkey PRIMARY KEY (id)");
        stmt.execute("ALTER TABLE products ADD CONSTRAINT products_price_check CHECK (price >= 0)");
        stmt.execute("ALTER TABLE products ADD CONSTRAINT products_qty_check CHECK (qty >= 0)");

        stmt.execute("ALTER TABLE products ALTER COLUMN p_uuid SET DEFAULT gen_random_uuid()");
        stmt.execute("ALTER TABLE products ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP");
        stmt.execute("ALTER TABLE products ALTER COLUMN is_deleted SET DEFAULT FALSE");
    }

    private static double measureReadTime(Statement stmt) throws SQLException {
        System.out.println("📖 Measuring read time for SELECT * FROM products...");
        long startTime = System.currentTimeMillis();
        try (ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {
            while (rs.next()) {
                // Iterate through rows to ensure data is read
            }
        }
        long endTime = System.currentTimeMillis();
        double seconds = (endTime - startTime) / 1000.0;
        System.out.printf("✅ Read completed in %.2f seconds\n", seconds);
        return seconds;
    }

    private static void resetPostgreSQL(Statement stmt) throws SQLException {
        System.out.println("🔄 Restoring PostgreSQL settings...");
        try { stmt.execute("RESET ALL"); } catch (SQLException e) { System.out.println("⚠️ Could not reset settings: " + e.getMessage()); }
        try { stmt.execute("SET synchronous_commit = ON"); } catch (SQLException e) { System.out.println("⚠️ Could not set synchronous_commit: " + e.getMessage()); }
    }

    private static void printResults(long startTime, long insertedRows, double readTimeSeconds) {
        long endTime = System.currentTimeMillis();
        double insertSeconds = (endTime - startTime) / 1000.0;
        double rowsPerSecond = insertedRows / insertSeconds;

        System.out.println("\n" + "=".repeat(70));
        System.out.println("🚀 ULTRA-OPTIMIZED BULK INSERT COMPLETE!");
        System.out.printf("✅ Successfully inserted: %,d rows\n", insertedRows);
        System.out.printf("⏱️  Insert time: %.2f seconds (%.1f minutes)\n", insertSeconds, insertSeconds / 60);
        System.out.printf("🚀 Insert speed: %,.0f rows/second\n", rowsPerSecond);
        System.out.printf("💾 Insert data rate: %.2f MB/second\n", (rowsPerSecond * MAX_ROW_BYTES_ESTIMATE) / (1024 * 1024));
        System.out.printf("📖 Read time: %.2f seconds\n", readTimeSeconds);
        System.out.println("=".repeat(70));
    }

    private static void handleError(String context, Exception e) {
        System.err.printf("❌ Error in %s: %s\n", context, e.getMessage());
        e.getCause().getMessage();
        errorOccurred.set(true);
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) closeable.close();
        } catch (IOException e) {
            System.err.println("❌ Error closing resource: " + e.getMessage());
            e.printStackTrace();
        }
    }
}