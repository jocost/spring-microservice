package com.project.microservice;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by jocost on 6/8/2018.
 */
@Entity
public class ApplicationResource implements Serializable {
    @Id
    @GeneratedValue
    private Integer id;

    @ElementCollection
    @MapKeyColumn(name="key")
    @Column(name="value")
    @CollectionTable(name="resource_map", joinColumns=@JoinColumn(name="id"))
    private Map<String, String> map;

    public Integer getId() {
        return id;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public String getValue(String key) {
        return this.map.get(key);
    }

    public String setValue(String key, String value) {
        return this.map.put(key, value);
    }

    public String remove(String key) {
        return this.map.remove(key);
    }

}
