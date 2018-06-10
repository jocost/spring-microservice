package com.project.microservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A RestController that can create, read, update and delete resources.
 * Each resource shall have an Id and maintains a Map of String Key and String Value pairs.
 *
 * @author jocost
 */
@RestController
@ExposesResourceFor(ApplicationEntity.class)
@RequestMapping("/resources")
public class ApplicationController {

    @Autowired
    private ResourceRepository repo;

    @Autowired
    private ApplicationResourceAssembler assembler;

    /**
     * GET request to read all available resources
     * @return List of ApplicationResources with HttpStatus.OK
     */
    @GetMapping
    public ResponseEntity<Resources<ApplicationResource>> getResources() {

        Link link = linkTo(methodOn(ApplicationController.class).getResources()).withSelfRel();

        Resources<ApplicationResource> result;
        result = new Resources<>(assembler.toResources(repo.findAll()), link);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * POST request to create a new resource with a generated id and an empty Map
     * @return List of ApplicationResources containing only the newly created resource
     */
    @PostMapping
    protected ResponseEntity<Resources<ApplicationResource>> createResource() {

        ApplicationEntity entity = new ApplicationEntity();
        repo.save(entity);

        List<ApplicationResource> resources = new ArrayList<>();
        resources.add(assembler.toResource(entity));

        Link link = linkTo(methodOn(ApplicationController.class).getResources()).withSelfRel();

        Resources<ApplicationResource> result = new Resources<>(resources, link);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * GET request to read a resource specified by its Id
     * @param resourceId : specifies resource
     * @return The requested ApplicationResource with HttpStatus.OK
     */
    @GetMapping("/{resourceId}")
    public ResponseEntity<Resource<ApplicationResource>> getResourceById(@PathVariable Integer resourceId) {

        if (!repo.exists(resourceId)) {
            return ResponseEntity.notFound().build();
        }

        Resource<ApplicationResource> result = new Resource<>(assembler.toResource(repo.findOne(resourceId)));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * DELETE request to remove a specific resource
     * @param resourceId : specifies the resource
     * @return ResponseEntity: 200 OK if successful, or 404 Not Found if resource doesn't exist
     */
    @DeleteMapping("/{resourceId}")
    public ResponseEntity<?> deleteResource(@PathVariable Integer resourceId) {

        if (!repo.exists(resourceId)) {
            return ResponseEntity.notFound().build();
        }

        repo.delete(repo.findOne(resourceId));
        return ResponseEntity.ok().build();
    }

    /**
     * GET request to read all keys on a Resource's Map
     * @param resourceId : specifies the Resource
     * @return List of the Resources Keys, or 404 Not Found if Resource doesn't exist
     */
    @GetMapping("/{resourceId}/keys")
    public ResponseEntity<Resources<Resource>> getResourceKeys(@PathVariable Integer resourceId) {

        if (!repo.exists(resourceId)) {
            return ResponseEntity.notFound().build();
        }

        ApplicationEntity entity = repo.findOne(resourceId);

        List<Resource> resourceList = new ArrayList<>();

        entity.getMap().keySet().forEach((String key) -> {
            Resource<String> resource = new Resource<>(key);
            resource.add(Arrays.asList(
                    linkTo(methodOn(ApplicationController.class).getResourceById(resourceId)).slash(key).withSelfRel(),
                    linkTo(methodOn(ApplicationController.class).getResourceKeys(resourceId)).withRel("siblings"),
                    linkTo(methodOn(ApplicationController.class).getResourceById(resourceId)).withRel("parent")
            ));
            resourceList.add(resource);
        });

        Resources<Resource> results = new Resources<>(resourceList, Arrays.asList(linkTo(methodOn(ApplicationController.class).getResourceKeys(resourceId)).withSelfRel(),
                linkTo(methodOn(ApplicationController.class).getResourceById(resourceId)).withRel("parent"),
                linkTo(methodOn(ApplicationController.class).getResourceValueByKey(resourceId, "key")).slash("?value=val").withRel("update")));

        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    /**
     * GET request to Read the Value at a specific Key on the Map
     * @param resourceId : specifies the Resource
     * @param key : specifies the Key on the Map
     * @return The Value on the Map, or 404 Not Found if either the Resource, or Key doesn't exist
     */
    @GetMapping("/{resourceId}/{key}")
    public ResponseEntity<Resource> getResourceValueByKey(@PathVariable Integer resourceId,
                                        @PathVariable String key) {

        if (!repo.exists(resourceId)) {
            return ResponseEntity.notFound().build();
        }

        ApplicationEntity entity = repo.findOne(resourceId);
        String entityValue = entity.getValue(key);

        if (entityValue == null) {
            return ResponseEntity.notFound().build();
        }

        Resource<String> result = new Resource<>(entityValue);

        result.add(Arrays.asList(
                linkTo(methodOn(ApplicationController.class).getResourceValueByKey(resourceId, key)).withSelfRel(),
                linkTo(methodOn(ApplicationController.class).getResourceValueByKey(resourceId, key)).slash("?value=" + entityValue).withRel("update"),
                linkTo(methodOn(ApplicationController.class).getResourceById(resourceId)).withRel("parent"),
                linkTo(methodOn(ApplicationController.class).getResourceValueByKey(resourceId, key)).withRel("siblings")));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * PUT request to Create or Update a Key-Value pair on a Resource's Map
     * @param resourceId : specifies the resource
     * @param key : specifies the key
     * @param value : speficies the new value
     * @return The new Value on the Map, or 404 Not Found if Resource doesn't exist
     */
    @PutMapping("/{resourceId}/{key}")
    public ResponseEntity<Resource> setResourceValueByKey(@PathVariable Integer resourceId,
                                                   @PathVariable String key,
                                                   @RequestParam String value) {

        if (!repo.exists(resourceId)) {
            return ResponseEntity.notFound().build();
        }

        ApplicationEntity entity = repo.findOne(resourceId);
        entity.setValue(key, value);
        repo.save(entity);

        String entityValue = entity.getValue(key);

        Resource<String> result = new Resource<>(entity.getValue(key));

        result.add(Arrays.asList(
                linkTo(methodOn(ApplicationController.class).getResourceValueByKey(resourceId, key)).withSelfRel(),
                linkTo(methodOn(ApplicationController.class).getResourceValueByKey(resourceId, key)).slash("?value=" + entityValue).withRel("update"),
                linkTo(methodOn(ApplicationController.class).getResourceById(resourceId)).withRel("parent"),
                linkTo(methodOn(ApplicationController.class).getResourceKeys(resourceId)).withRel("siblings")));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * DELETE request to remove a Key-Value pair from a Resource's Map
     * @param resourceId : specifies the Resource
     * @param key : specifies the Key to remove from the Map
     * @return ResponseEntity: 200 OK, or 404 Not Found if either the Resource or Key doesn't exist
     */
    @DeleteMapping("/{resourceId}/{key}")
    public ResponseEntity<?> removeResourceKey(@PathVariable Integer resourceId,
                                               @PathVariable String key) {

        if (!repo.exists(resourceId)) {
            return ResponseEntity.notFound().build();
        }

        ApplicationEntity entity = repo.findOne(resourceId);

        if (!entity.keyExists(key)) {
            return ResponseEntity.notFound().build();
        }

        entity.remove(key);
        repo.save(entity);

        return ResponseEntity.ok().build();
    }
}
