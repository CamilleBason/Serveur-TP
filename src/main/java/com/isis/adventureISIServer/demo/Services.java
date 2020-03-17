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
import generated.PalliersType;
import generated.ProductType;
import generated.ProductsType;
import javax.servlet.http.HttpServletRequest;
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
import java.util.List;

public class Services {

    World world = new World();

    //String path ="c:/temp";
    //@Context
    //private UriInfo context;
    /**
     * Creates a new instance of GenericResource
     */
    public Services() {

    }

    private World readWorldFromXml(String username) {
        JAXBContext jaxbContext;

        try {
            try {
                File f = new File(username + "-world.xml");
                jaxbContext = JAXBContext.newInstance(World.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                //File f = new File("/world.xml");
                world = (World) jaxbUnmarshaller.unmarshal(f);
            } catch (Exception e) {
                System.out.println("Pas de monde pour le joueur:" + username);
                InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
                jaxbContext = JAXBContext.newInstance(World.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                //File f = new File("/world.xml");
                world = (World) jaxbUnmarshaller.unmarshal(input);
            }
        } catch (JAXBException ex) {
            System.out.println("Erreur lecture du fichier:" + ex.getMessage());
            ex.printStackTrace();
        }
        return world;
    }

    private void saveWorldToXml(String username, World world) {
        JAXBContext jaxbContext;

        try {
            OutputStream output = new FileOutputStream(username + "-world.xml");
            jaxbContext = JAXBContext.newInstance(World.class);
            Marshaller march = jaxbContext.createMarshaller();
            march.marshal(world, output);
        } catch (Exception ex) {
            System.out.println("Erreur écriture du fichier:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    World getWorld(String username) {

        return readWorldFromXml(username); //To change body of generated methods, choose Tools | Templates.
    }

// prend en paramètre le pseudo du joueur et le produit
// sur lequel une action a eu lieu (lancement manuel de production ou 
// achat d’une certaine quantité de produit)
// renvoie false si l’action n’a pas pu être traitée  
    public Boolean updateProduct(String username, ProductType newproduct) {

        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le produit équivalent à celui passé en paramètr
        ProductType product = findProductById(world, newproduct.getId());
        if (product == null) {
            return false;
        }
        // calculer la variation de quantité. Si elle est positive c'est
        // que le joueur a acheté une certaine quantité de ce produit
        // sinon c’est qu’il s’agit d’un lancement de production.
        int qtchange = newproduct.getQuantite() - product.getQuantite();
        if (qtchange > 0) {
            // soustraire del'argent du joueur le cout de la quantité
            // achetée et mettre à jour la quantité de product 
            double somme = product.getCout() * qtchange;
            world.setMoney(world.getMoney() - somme);
            product.setQuantite(product.getQuantite() + qtchange);
        } else {
            // initialiser product.timeleft à product.vitesse
            // pour lancer la production
            product.setTimeleft(product.getVitesse());
        }
        // sauvegarder les changements du monde
        saveWorldToXml(username, world);
        return true;
    }

    private ProductType findProductById(World world, int id) {
        ProductsType ps = world.getProducts();
        ProductType Goodproduct = null;
        for (ProductType p : ps.getProduct()) {
            if (id == p.getId()) {
                Goodproduct = p;
            }
        }
        return Goodproduct;
    }

    // prend en paramètre le pseudo du joueur et le manager acheté.
    // renvoie false si l’action n’a pas pu être traitée
    public Boolean updateManager(String username, PallierType newmanager) {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le manager équivalent à celui passé
        // en paramètre
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) {
            return false;
        }

        // débloquer ce manager
        manager.setUnlocked(true);
        // trouver le produit correspondant au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            return false;
        }
        // débloquer le manager de ce produit
        product.isManagerUnlocked();
        // soustraire de l'argent du joueur le cout du manager
        world.setMoney(world.getMoney()-manager.getSeuil());
        // sauvegarder les changements au monde
        saveWorldToXml(username, world);
        return true;
    }

    private PallierType findManagerByName(World world, String name) {
        PalliersType ms = world.getManagers();
        PallierType Goodmanager = null;
        for (PallierType p : ms.getPallier()) {
            if (name == p.getName()) {
                Goodmanager= p;
            }
        }
        return Goodmanager;
    }

}
