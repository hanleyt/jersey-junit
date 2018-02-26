package com.github.hanleyt;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class DummyResource {

    public static final String DEFAULT_VALUES = "value1, value2";
    private final String values;

    public DummyResource() {
        this(DEFAULT_VALUES);
    }

    public DummyResource(String values) {
        this.values = values;
    }

    @GET
    @Path("/values")
    @Produces(MediaType.APPLICATION_JSON)
    public String get() {
        return values;
    }

}