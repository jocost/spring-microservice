package com.project.microservice;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

/**
 * Resource Entity which is persisted using PostgreSQL
 *
 * @author jocost
 */
@Entity
public class ApplicationEntity implements Serializable {

    @Id
    @GeneratedValue
    private Integer appId;

    @ElementCollection
    @MapKeyColumn(name="key")
    @Column(name="value")
    @CollectionTable(name="resource_map", joinColumns=@JoinColumn(name="appId"))
    private Map<String, String> map;

    public Integer getAppId() {
        return appId;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public String getValue(String key) {
        return this.map.get(key);
    }

    public String setValue(String key, String value) {
        return this.map.put(key, value);
    }

    public Boolean keyExists(String key) {
        return this.map.containsKey(key);
    }

    public String remove(String key) {
        return this.map.remove(key);
    }

}
