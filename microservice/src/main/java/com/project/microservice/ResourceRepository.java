package com.project.microservice;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Repository for persisting Entities
 *
 * @author jocost 
 */
public interface ResourceRepository extends CrudRepository<ApplicationEntity, Integer> {

    List<ApplicationEntity> findAll();
}
