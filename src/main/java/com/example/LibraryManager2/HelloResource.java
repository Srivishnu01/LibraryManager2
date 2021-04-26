package com.example.LibraryManager2;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Path("health")
public class HelloResource {
    @GET
    @Produces("text/html")
    public String hello() {
        return "API health is Ok";
    }
}