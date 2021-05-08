// Copyright [2019] [Payara Foundation and/or its affiliates]
package org.javaee7.jaxrs.ejb.lookup.sbn.war2;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/same-bean-name-app")
public class SameBeanNameApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> resources = new HashSet<>();
        resources.add(SameBeanName.class);
        return resources;
    }
}
