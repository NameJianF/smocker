package com.jenetics.smocker.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;

public interface IDaoManager<T extends Serializable> {

	T findById(final Long id);

	T create(T entity);

	void setEm(EntityManager em);

	List<T> listAll(Integer startPosition, Integer maxResult);

	List<T> listAll();
	
	long count();

	void deleteById(Long id);

	T update(T entity);
	
	void delete(T entity);

	void deleteAll();
	
	public List<T> queryList(String querySql);

}
