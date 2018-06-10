package com.project.microservice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest

/**
 * @author jocost
 */
public class ApplicationControllerTests {

    private MockMvc mockMvc;

    private ApplicationEntity entity;

    private Integer entityId;

    private ArrayList<String> mapKeys = new ArrayList<>();

    private ArrayList<String> mapValues = new ArrayList<>();

    @Autowired
    private ResourceRepository repo;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setup() throws Exception {

        mapKeys.addAll(Arrays.asList("key1", "key2"));
        mapValues.addAll(Arrays.asList("val1", "val2"));

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        repo.deleteAll();

        entity = repo.save(new ApplicationEntity());
        entityId = entity.getAppId();

        IntStream.range(0, mapKeys.size()).forEach(i -> entity.setValue(mapKeys.get(i), mapValues.get(i)));

        repo.save(entity);
    }

    @After
    public void teardown() throws Exception {
        repo.deleteAll();
    }

    @Test
    public void contextLoads() {
    }

    @Test
    public void getResources() throws Exception {
        int resourceCount = repo.findAll().size();

        mockMvc.perform(get("/resources"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.resources", hasSize(resourceCount)))
        .andExpect(jsonPath("$._embedded.resources[0].appId", is(this.entityId)))
        .andExpect(jsonPath("$._links.self.href", is("http://localhost/resources")));
    }

    @Test
    public void createResource() throws Exception {
        int resourceCount = repo.findAll().size();

        mockMvc.perform(post("/resources"))
                .andExpect(status().isOk());

        resourceCount++;

        assertTrue(repo.findAll().size() == resourceCount);
    }

    @Test
    public void getResourceById() throws Exception {
        ResultActions response  = mockMvc.perform(get(String.format("/resources/%s", this.entityId)));

        response.andExpect(status().isOk())
        .andExpect(jsonPath("$.appId", is(this.entityId)))
        .andExpect(jsonPath("$.map.*", hasSize(2)));

        IntStream.range(0, mapKeys.size()).forEach(i -> {
            try {
                response.andExpect(jsonPath(String.format("$.map.%s", mapKeys.get(i)), is(mapValues.get(i))));
            } catch (Exception e) {
                fail();
            }
        });
    }

    @Test
    public void getResourceKeys() throws Exception {
        mockMvc.perform(get(String.format("/resources/%s/keys", entityId)))
                .andExpect(jsonPath("$._embedded.stringList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.stringList[0].content", is(mapKeys.get(0))))
                .andExpect(jsonPath("$._embedded.stringList[0]._links.self.href", is(String.format("http://localhost/resources/%s/%s", entityId, mapKeys.get(0)))))
                .andExpect(jsonPath("$._links.self.href", is(String.format("http://localhost/resources/%s/keys", entityId))));
    }

    @Test
    public void getResourceValueByKey() throws Exception {
        mockMvc.perform(get(String.format("/resources/%s/%s", entityId, mapKeys.get(0))))
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$.content", is(mapValues.get(0))))
                .andExpect(jsonPath("$._links.self.href", is(String.format("http://localhost/resources/%s/%s", entityId, mapKeys.get(0)))));
    }

    @Test
    public void createResourceValueWithKey() throws Exception {
        String newKey = "testCreateKey";
        String newValue = "testCreateVal";

        mapKeys.add(newKey);
        mapValues.add(newValue);

        mockMvc.perform(put(String.format("/resources/%s/%s?value=%s", entityId, newKey, newValue)))
                .andExpect(jsonPath("$.content", is(newValue)))
                .andExpect(jsonPath("$._links.self.href", is(String.format("http://localhost/resources/%s/%s", entityId, newKey))));
    }

    @Test
    public void updateResourceValueByKey() throws Exception {

        String updateKey = "testUpdateKey";
        String updateValue = "testUpdateValue";
        String newUpdateValue = "newTestUpdateValue";

        ApplicationEntity updateEntity = repo.save(new ApplicationEntity());

        updateEntity.setValue(updateKey, updateValue);
        repo.save(updateEntity);

        mockMvc.perform(put(String.format("/resources/%s/%s?value=%s", entityId, updateKey, newUpdateValue)))
                .andExpect(jsonPath("$.content", is(newUpdateValue)))
                .andExpect(jsonPath("$._links.self.href", is(String.format("http://localhost/resources/%s/%s", entityId, updateKey))));
    }

    @Test
    public void removeResourceKey() throws Exception {
        int keyCount = repo.findOne(entityId).getMap().size();

        mockMvc.perform(delete(String.format("/resources/%s/%s", entityId, mapKeys.get(0))))
                .andExpect(status().isOk());

        keyCount--;

        assertTrue(repo.findOne(entityId).getMap().keySet().size() == keyCount);
    }

    @Test
    public void deleteResourceById() throws Exception {
        int resourceCount = repo.findAll().size();

        mockMvc.perform(delete("/resources/" + this.entityId))
                .andExpect(status().isOk());

        resourceCount--;

        assertTrue(repo.findAll().size() == resourceCount);
    }
}
