package model.repository;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

public class ProductRepositoryInsertion {

    private final Connection connection;

    public ProductRepositoryInsertion(Connection connection) {
        this.connection = connection;
    }

    // Drop indexes and constraints before bulk insert to speed up
    public void dropConstraintsAndIndexes() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            System.out.println("Dropping constraints and indexes...");
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
            System.out.println("Truncated products table.");
        }
    }

    // Restore constraints and defaults after insert
    public void restoreConstraintsAndIndexes() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            System.out.println("Restoring constraints and indexes...");
            stmt.execute("ALTER TABLE products ADD CONSTRAINT products_pkey PRIMARY KEY (id)");
            stmt.execute("ALTER TABLE products ADD CONSTRAINT products_price_check CHECK (price >= 0)");
            stmt.execute("ALTER TABLE products ADD CONSTRAINT products_qty_check CHECK (qty >= 0)");

            stmt.execute("ALTER TABLE products ALTER COLUMN p_uuid SET DEFAULT gen_random_uuid()");
            stmt.execute("ALTER TABLE products ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP");
            stmt.execute("ALTER TABLE products ALTER COLUMN is_deleted SET DEFAULT FALSE");

            stmt.execute("CREATE INDEX idx_products_name ON products(p_name)");
            stmt.execute("CREATE INDEX idx_products_category ON products(category)");
            stmt.execute("CREATE INDEX idx_products_puuid ON products(p_uuid)");
            System.out.println("Indexes restored.");
        }
    }

    // Insert 10 million records with binary COPY and category randomly chosen from 3 options
    public void insert10Million() throws Exception {
        System.out.println("Starting insertion of 10 million records...");
        long startTime = System.currentTimeMillis();

        CopyManager copyManager = new CopyManager((BaseConnection) connection);

        // We use Piped streams for streaming data to COPY
        try (PipedOutputStream pout = new PipedOutputStream();
             PipedInputStream pin = new PipedInputStream(pout)) {

            Thread writerThread = new Thread(() -> {
                try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(pout))) {
                    writeBinaryCopyData(dos, 10_000_000);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writerThread.start();

            String copySql = "COPY products (p_name, category, price, qty, is_deleted, p_uuid, created_at) FROM STDIN WITH (FORMAT binary)";
            long insertedRows = copyManager.copyIn(copySql, pin);

            writerThread.join();
            long endTime = System.currentTimeMillis();
            System.out.printf("Insertion completed: %,d rows inserted in %.2f seconds.%n", insertedRows, (endTime - startTime) / 1000.0);
        }
    }

    private void writeBinaryCopyData(DataOutputStream dos, int totalRows) throws IOException {
        // Write PostgreSQL COPY binary header
        dos.writeBytes("PGCOPY\n\377\r\n\0");
        dos.writeInt(0); // Flags field
        dos.writeInt(0); // Header extension area length

        String[] categories = {"Book", "Technology", "Skin Care"};
        Random rand = new Random();
        Instant nowUtc;
        long epochMillisUtc;

        for (int i = 1; i <= totalRows; i++) {
            dos.writeShort(7); // Number of columns for each row

            // p_name (text)
            writeText(dos, "Product-" + i);
            // category (text)
            writeText(dos, categories[rand.nextInt(categories.length)]);
            // price (float8)
            writeFloat8(dos, rand.nextDouble() * 100);
            // qty (int4)
            writeInt4(dos, rand.nextInt(1000));
            // is_deleted (bool)
            writeBool(dos, false);
            // p_uuid (uuid)
            writeUUID(dos, UUID.randomUUID());
            // created_at (timestamp without time zone)
            nowUtc = Instant.now(); // UTC by default
            epochMillisUtc = nowUtc.toEpochMilli();
            writeTimestamp(dos, epochMillisUtc);


            if (i % 1_000_000 == 0) {
                System.out.printf("Inserted %,d rows...\n", i);
            }
        }
        dos.flush();

        // Write trailer
        dos.writeShort(-1);
    }

    // Write helper methods for PostgreSQL binary protocol

    private void writeText(DataOutputStream dos, String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        dos.writeInt(bytes.length);
        dos.write(bytes);
    }

    private void writeFloat8(DataOutputStream dos, double val) throws IOException {
        dos.writeInt(8);
        dos.writeLong(Double.doubleToRawLongBits(val));
    }

    private void writeInt4(DataOutputStream dos, int val) throws IOException {
        dos.writeInt(4);
        dos.writeInt(val);
    }

    private void writeBool(DataOutputStream dos, boolean val) throws IOException {
        dos.writeInt(1);
        dos.writeByte(val ? 1 : 0);
    }

    private void writeUUID(DataOutputStream dos, UUID uuid) throws IOException {
        String uuidString = uuid.toString(); // "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
        writeText(dos, uuidString); // write as UTF-8 string like other text fields
    }


    private void writeTimestamp(DataOutputStream dos, long epochMillisUtc) throws IOException {
        // PostgreSQL timestamp is microseconds since 2000-01-01 00:00:00 UTC
        // Convert Java millis since 1970-01-01 to microseconds since 2000-01-01
        long POSTGRES_EPOCH_DIFF = 946684800000L; // 2000-01-01 in millis
        long microsSince2000 = (epochMillisUtc - POSTGRES_EPOCH_DIFF) * 1000;
        dos.writeInt(8);
        dos.writeLong(microsSince2000);
    }

    // Read all rows and print progress every 1 million reads
    public void readAll() throws SQLException {
        System.out.println("Starting reading all records...");
        String sql = "SELECT * FROM products";
        int count = 0;

        long startTime = System.currentTimeMillis();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                count++;
                if (count % 1_000_000 == 0) {
                    System.out.printf("Read %,d rows...\n", count);
                }
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("Reading completed: %,d rows read in %.2f seconds.%n", count, (endTime - startTime) / 1000.0);
    }
}