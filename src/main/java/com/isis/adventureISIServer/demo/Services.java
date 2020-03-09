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
import generated.World;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.OutputStream;


public class Services {
    
    World world = new World();
    //String path ="c:/temp";

    //@Context
    //private UriInfo context;

    /**
     * Creates a new instance of GenericResource
     */
    public Services() {
        readWorldFromXml();
    }
    
    private World readWorldFromXml(){
        JAXBContext jaxbContext;

        try {
            InputStream input=getClass().getClassLoader().getResourceAsStream("world.xml");
            jaxbContext = JAXBContext.newInstance(World.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            //File f = new File("/world.xml");
            world = (World) jaxbUnmarshaller.unmarshal(input);
        } catch (JAXBException ex) {
            System.out.println("Erreur lecture du fichier:"+ex.getMessage());
            ex.printStackTrace();
        }
        return world;
    }
    
    private void saveWorldToXml() {
        JAXBContext jaxbContext;

        try {
            OutputStream output = new FileOutputStream("world.xml");
            jaxbContext = JAXBContext.newInstance(World.class);
            Marshaller march = jaxbContext.createMarshaller();
            march.marshal(world, new File("/world.xml"));
        } catch (Exception ex) {
            System.out.println("Erreur écriture du fichier:"+ex.getMessage());
            ex.printStackTrace();
       }
    }

    World getWorld() {
        return readWorldFromXml(); //To change body of generated methods, choose Tools | Templates.
    }
}
