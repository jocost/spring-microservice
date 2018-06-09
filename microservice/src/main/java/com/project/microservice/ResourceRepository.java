package com.project.microservice;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by jocost on 6/8/2018.
 */
public interface ResourceRepository extends CrudRepository<ApplicationResource, Integer> {

    @Override
    List<ApplicationResource> findAll();
}
