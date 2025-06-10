package model.repository;

import java.util.List;

public interface Repository<T,K> {
    T save (T t);
    List<T> findAll();
    K findById(K id);

    Integer delete(Integer id);
}
