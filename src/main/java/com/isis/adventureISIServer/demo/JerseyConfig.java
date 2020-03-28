package com.isis.adventureISIServer.demo;



import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cbason
 */
@Component
@ApplicationPath("/demo")
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(Webservice.class);
        register(CORSResponseFilter.class);
    }
}
