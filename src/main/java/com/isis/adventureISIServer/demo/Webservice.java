package com.isis.adventureISIServer.demo;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cbason
 */
import generated.PallierType;
import generated.ProductType;
import java.io.FileNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import static javax.ws.rs.HttpMethod.POST;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import org.springframework.stereotype.Component;

@Component
@Path("generic")
public class Webservice {

    Services services;

    public Webservice() {
        services = new Services();
    }

    @GET
    @Path("world")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getXml(@Context HttpServletRequest request) {
        String username = request.getHeader("X-User");
        //String username ="Camille";
        System.out.println("GetXml "+ username);      
        return Response.ok(services.readWorldFromXml(username)).build();
    }
    
    @PUT
    @Path("product")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void PutProduct(@Context HttpServletRequest request, ProductType product) throws FileNotFoundException, JAXBException{
        String username = request.getHeader("X-User");
        services.updateProduct(username, product);
        System.out.println("Put sur le produit: " +product.getName());      
    }
    
    
    @PUT
    @Path("manager")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void PutManager(@Context HttpServletRequest request, PallierType manager) throws JAXBException, FileNotFoundException{
        String username = request.getHeader("X-User");
        services.updateManager(username, manager);
        System.out.println("Put sur le manager:"+manager.getName());      
    }
    
    @PUT
    @Path("upgrade")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void PutUpgrade(@Context HttpServletRequest request, PallierType upgrade) throws JAXBException, FileNotFoundException{
        String username = request.getHeader("X-User");
        services.updateUpgrade(username, upgrade);
        System.out.println("Put sur le upgrade: "+upgrade.getName());      
    }
}
