package com.gadgetgalaxy.dao;

import java.util.List;

/**
 * Generic Data Access Object (DAO) interface to define standard CRUD contract.
 * Demonstrates Interfaces and Generics.
 */
public interface DAO<T> {
    T findById(int id) throws Exception;
    List<T> findAll() throws Exception;
    boolean insert(T entity) throws Exception;
    boolean update(T entity) throws Exception;
    boolean delete(int id) throws Exception;
}
