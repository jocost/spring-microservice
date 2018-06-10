package com.project.microservice;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.Relation;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * Class Mapping a Resource to an Entity
 *
 * @author jocost
 */
@XmlRootElement
@Relation(value = "resource", collectionRelation = "resources")
public class ApplicationResource extends ResourceSupport {

    @XmlAttribute
    private Integer appId;
    @XmlAttribute
    private Map<String, String> map;

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public Integer getAppId() {
        return this.appId;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public Map<String, String> getMap() {
        return this.map;
    }
}
