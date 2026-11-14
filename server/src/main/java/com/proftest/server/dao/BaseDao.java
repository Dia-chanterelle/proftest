package com.proftest.server.dao;

import com.proftest.server.model.BaseEntity;

import java.util.List;
import java.util.Optional;

/**
 * Базовый интерфейс DAO — абстрактный тип данных для работы с хранилищем.
 */
public interface BaseDao<T extends BaseEntity> {
    T save(T entity);
    Optional<T> findById(Long id);
    List<T> findAll();
    T update(T entity);
    void delete(T entity);
    void deleteById(Long id);
}
