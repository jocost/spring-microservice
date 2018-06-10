package com.project.microservice;

import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Class to create a Resource from an Entity and add appropriate links
 *
 * @author jocost
 */
@Component
public class ApplicationResourceAssembler extends ResourceAssemblerSupport<ApplicationEntity, ApplicationResource> {

    public  ApplicationResourceAssembler(Class<ApplicationController> controllerClass, Class<ApplicationResource> resourceClass) {
        super(controllerClass, resourceClass);
    }

    public ApplicationResourceAssembler() {
        super(ApplicationController.class, ApplicationResource.class);
    }

    public ApplicationResource toResource(ApplicationEntity entity) {
        ApplicationResource resource = createResourceWithId(entity.getAppId(), entity);

        resource.setAppId(entity.getAppId());
        resource.setMap(entity.getMap());

        resource.add(Arrays.asList(
                linkTo(methodOn(ApplicationController.class).getResourceKeys(entity.getAppId())).withRel("keys"),
                linkTo(methodOn(ApplicationController.class).getResourceValueByKey(entity.getAppId(), "key")).slash("?value=val").withRel("update")
        ));

        return resource;
    }
}
