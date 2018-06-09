package com.project.microservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by jocost on 6/8/2018.
 */
@RestController
public class ApplicationController {

    @Autowired
    private ResourceRepository repo;

    /**
     * Resource: READ list of resources
     *
     * VALIDATED
     * @return
     */
    @GetMapping("/resources")
    public List<Integer> getResources() {
        List<ApplicationResource> all = repo.findAll();

        Stream<ApplicationResource> stream = all.stream();

        return stream.map(ApplicationResource::getId).collect(Collectors.toList());
    }

    /**
     * Resource: CREATE a resource
     *
     * VALIDATED
     * @return
     */
    @PostMapping("/resources")
    protected ApplicationResource createResource() {
        ApplicationResource resource = new ApplicationResource();
        return repo.save(resource);
    }

    @GetMapping("/resources/{resourceId}")
    public ApplicationResource getResourceById(@PathVariable Integer resourceId) {

        return repo.findOne(resourceId);
    }

    /**
     * Resource: READ a resources list of keys
     * Key: READ list of keys
     *
     * VALIDATED
     * @param resourceId
     * @return
     */
    @GetMapping("/resources/{resourceId}/keys")
    public List<String> getResourceKeys(@PathVariable Integer resourceId) {

        ApplicationResource resource = repo.findOne(resourceId);

        return new ArrayList<>(resource.getMap().keySet());
    }

    /**
     * Resource->Key->Value: READ value of a Resource map by Key
     *
     * VALIDATED
     * @param resourceId
     * @param key
     * @return
     */
    @GetMapping("/resources/{resourceId}/{key}")
    public String getResourceValueByKey(@PathVariable Integer resourceId,
                                        @PathVariable String key) {
        ApplicationResource resource = repo.findOne(resourceId);

        return resource.getValue(key);
    }

    /**
     * Resource->Key->Value: CREATE Key with Value on a Resource map if it doesn't exist
     * Resource->Key->Value: UPDATE Key with new Value if it previously existed
     *
     * VALIDATED
     * @param resourceId
     * @param key
     * @param value
     * @return
     */
    @PutMapping("/resources/{resourceId}/{key}/{value}")
    public ApplicationResource setResourceValueByKey(@PathVariable Integer resourceId,
                                        @PathVariable String key,
                                        @PathVariable String value) {
        ApplicationResource resource = repo.findOne(resourceId);

        resource.setValue(key, value);

        return repo.save(resource);
    }

    /**
     * Resource: READ complete Resource map with Keys and Values
     *
     * VALIDATED
     * @param resourceId
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping("/resources/{resourceId}/map")
    public String getResourceMap(@PathVariable Integer resourceId) throws JsonProcessingException {

        ApplicationResource resource = repo.findOne(resourceId);
        Map<String, String> payload = resource.getMap();

        return new ObjectMapper().writeValueAsString(payload);
    }

    /**
     * Resource: UPDATE a Resource by removing a Key on the map
     * Key: DELETE a Key on the Map
     *
     * VALIDATED
     * @param resourceId
     * @param key
     * @return
     */
    @DeleteMapping("/resources/{resourceId}/{key}")
    public ApplicationResource removeResourceKey(@PathVariable Integer resourceId,
                                    @PathVariable String key) {

        ApplicationResource resource = repo.findOne(resourceId);

        resource.remove(key);

        return repo.save(resource);
    }

    /**
     * Resource: DELETE a Resource
     *
     * VALIDATED
     * @return
     */
    @DeleteMapping("/resources/{resourceId}")
    public ResponseEntity<?> deleteResource(@PathVariable Integer resourceId) {

        if (repo.exists(resourceId)) {
            repo.delete(repo.findOne(resourceId));
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.notFound().build();
    }
}
