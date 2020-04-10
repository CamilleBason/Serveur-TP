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
import generated.TyperatioType;
import static generated.TyperatioType.ANGE;
import static generated.TyperatioType.GAIN;
import static generated.TyperatioType.VITESSE;
import javax.servlet.http.HttpServletRequest;
import generated.World;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.Math.floor;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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

    World readWorldFromXml(String username) {
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

    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public World getWorld(String username) throws JAXBException, FileNotFoundException {
        //recuperer le world
        World wo = readWorldFromXml(username);
        if (wo.getLastupdate() == System.currentTimeMillis()) {
            return wo;
        }
        //calculer le temps ecoulé depuis la dernière mise a jour du monde
        long ecoule = System.currentTimeMillis() - wo.getLastupdate();
        //System.out.println("ecoule " + ecoule);
        //pour tous les produits      
        ProductsType ps = wo.getProducts();
        for (ProductType p : ps.getProduct()) {
            //si pas de manager
            //System.out.println("produit " + p.getName() + "difference " + (ecoule - p.getTimeleft()));
            if (!p.isManagerUnlocked()) {
                //verifier que le joueur possède le produit
                if (p.getTimeleft() > 0) {
                    if (p.getTimeleft() > ecoule) {
                        p.setTimeleft(p.getTimeleft() - ecoule);
                        ecoule = 0;
                    } else {
                        System.out.println("ok");
                        double money = wo.getMoney() + p.getRevenu() * p.getQuantite();
                        wo.setMoney(money);
                        wo.setScore(money);
                        p.setTimeleft(0);
                        ecoule = ecoule - p.getTimeleft();
                    }
                }
            } //si manager
            else {
                    if (p.getTimeleft() > ecoule) {
                        p.setTimeleft(p.getTimeleft() - ecoule);
                    } else {
                        int nb = (int) (ecoule  / p.getVitesse());
                        //int nb = (int) (ecoule  / p.getVitesse());
                        int rest = (int) (ecoule % p.getVitesse());
                        //int rest = (int) (ecoule  % p.getVitesse());
                        double money =  (p.getRevenu() * p.getQuantite()) * nb;
                        wo.setMoney(wo.getMoney() + money);
                        wo.setScore(wo.getScore() + money);
                        p.setTimeleft(rest);
                    }
            }
        }
        wo.setLastupdate(System.currentTimeMillis());
        //enregistre les modifications
        //saveWorldToXml(username, wo);
        System.out.println("Monnaie " + wo.getMoney());
        return wo;

    }   
        // prend en paramètre le pseudo du joueur et le produit
    // sur lequel une action a eu lieu (lancement manuel de production ou 
    // achat d’une certaine quantité de produit)
    // renvoie false si l’action n’a pas pu être traitée  
    public Boolean updateProduct(String username, ProductType newproduct) throws FileNotFoundException, FileNotFoundException, JAXBException {
        System.out.println("Strat update product " + newproduct.getName());
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le produit équivalent à celui passé en paramètr
        ProductType product = findProductById(world, newproduct.getId());
        if (product == null) {
            System.out.println("product null");
            return false;
        }
        // calculer la variation de quantité. Si elle est positive c'est
        // que le joueur a acheté une certaine quantité de ce produit
        // sinon c’est qu’il s’agit d’un lancement de production.
        int qtchange = newproduct.getQuantite() - product.getQuantite();
        System.out.println("variation de quantité " + qtchange);
        if (qtchange > 0) {
            // soustraire del'argent du joueur le cout de la quantité
            // achetée et mettre à jour la quantité de product 
            double somme = ((newproduct.getCout() * qtchange) / product.getCroissance());
            world.setMoney(world.getMoney() - somme);
            System.out.println("Nouvelle money " + world.getMoney());
            product.setQuantite(product.getQuantite() + qtchange);
            product.setCout(newproduct.getCout());
            System.out.println("Nouvelle quantité " + product.getQuantite());
            System.out.println("Nouveaux couts " + product.getCout());
            //verification des unlocks du produit
            PalliersType pallier = product.getPalliers();
            for (PallierType p : pallier.getPallier()) {
                //cas de l'upgrade d'un seul produit
                if (p.isUnlocked() == false & p.getIdcible() != 0 & product.getQuantite() >= p.getSeuil()) {
                    p.setUnlocked(true);
                    System.out.println("unlock produit activé");
                    //gain
                    if (p.getTyperatio() == GAIN) {
                        product.setRevenu(product.getRevenu() * p.getRatio());
                        System.out.println("gain");
                    }
                    //vitesse
                    if (p.getTyperatio() == VITESSE) {
                        product.setVitesse((int) (product.getVitesse() / p.getRatio()));
                        product.setTimeleft((long) (product.getTimeleft() / p.getRatio()));
                        System.out.println("vitesse");
                    }
                }
                //cas de l'upgrade de tous les produits
                if (p.isUnlocked() == false & p.getIdcible() == 0 & product.getQuantite() >= p.getSeuil()) {
                    p.setUnlocked(true);
                    System.out.println("unlock produit général activé");

                    //gain
                    if (p.getTyperatio() == GAIN) {
                        ProductsType ps = world.getProducts();
                        for (ProductType pro : ps.getProduct()) {
                            pro.setRevenu(pro.getRevenu() * p.getRatio());
                        }
                        System.out.println("general gain");
                    }
                    //vitesse
                    if (p.getTyperatio() == VITESSE) {
                        ProductsType ps = world.getProducts();
                        for (ProductType pro : ps.getProduct()) {
                            pro.setVitesse((int) (pro.getVitesse() / p.getRatio()));
                            pro.setTimeleft((long) (pro.getTimeleft() / p.getRatio()));
                        }
                        System.out.println("general vitesse");

                    }
                }
            }
            PalliersType palliers;
            palliers = world.getAllunlocks();
            for (PallierType p : palliers.getPallier()) {
                //cas de l'upgrade d'un seul produit
                if (p.getIdcible() == product.getId() & p.getSeuil() <= product.getQuantite()) {
                    p.setUnlocked(true);
                    System.out.println("unlock activé");
                    //gain
                    if (p.getTyperatio() == GAIN) {
                        product.setRevenu(product.getRevenu() * p.getRatio());
                        System.out.println("gain");
                    }
                    //vitesse
                    if (p.getTyperatio() == VITESSE) {
                        product.setVitesse((int) (product.getVitesse() / p.getRatio()));
                        product.setTimeleft((long) (product.getTimeleft() / p.getRatio()));
                        System.out.println("vitesse");
                    }
                    if (p.getTyperatio() == ANGE) {
                        world.setAngelbonus((int) (world.getAngelbonus() + p.getRatio()));
                        System.out.println("ange");
                    }
                }
                //cas de l'upgrade de tous les produits
                if (p.getIdcible() == 0 & p.getSeuil() >= product.getQuantite()) {
                    p.setUnlocked(true);
                    System.out.println("unlock général activé");

                    //gain
                    if (p.getTyperatio() == GAIN) {
                        ProductsType ps = world.getProducts();
                        for (ProductType pro : ps.getProduct()) {
                            pro.setRevenu(pro.getRevenu() * p.getRatio());
                        }
                        System.out.println("general gain");
                    }
                    //vitesse
                    if (p.getTyperatio() == VITESSE) {
                        ProductsType ps = world.getProducts();
                        for (ProductType pro : ps.getProduct()) {
                            pro.setVitesse((int) (pro.getVitesse() * p.getRatio()));
                            pro.setTimeleft((long) (pro.getTimeleft() / p.getRatio()));
                        }
                        System.out.println("general vitesse");

                    }
                }
            }
            //cas d'un nouveau produit
        } else {
            // initialiser product.timeleft à product.vitesse
            // pour lancer la production
            product.setTimeleft(product.getVitesse());
            product.setQuantite(newproduct.getQuantite());
        }
        // sauvegarder les changements du monde
        this.saveWorldToXml(username, world);
        return true;
    }

    //retrouver un produit a partir de son id
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
    public Boolean updateManager(String username, PallierType newmanager) throws JAXBException, FileNotFoundException {
        // aller chercher le monde qui correspond au joueur
        System.out.println("Strat update manager" + newmanager.getName() + " avec username " + username);
        World world = getWorld(username);
        // trouver dans ce monde, le manager équivalent à celui passé
        // en paramètre
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) {
            System.out.println("manager null");
            return false;
        }
        // débloquer ce manager
        manager.setUnlocked(true);
        // trouver le produit correspondant au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            System.out.println("product null");
            return false;
        }
        // débloquer le manager de ce produit
        product.setManagerUnlocked(true);
        // soustraire de l'argent du joueur le cout du manager
        System.out.println("argent avant soustraction" + world.getMoney());
        world.setMoney(world.getMoney() - manager.getSeuil());
        System.out.println("argent après soustraction" + world.getMoney());
        // sauvegarder les changements au monde
        saveWorldToXml(username, world);
        return true;
    }

    //retrouve un manager a partir de son nom
    private PallierType findManagerByName(World world, String name) {
        PalliersType ms = world.getManagers();
        PallierType Goodmanager = null;
        for (PallierType p : ms.getPallier()) {
            if (p.getName().equals(name)) {
                Goodmanager = p;
            }
        }
        return Goodmanager;
    }

    public Boolean updateUpgrade(String username, PallierType newupgrade) throws JAXBException, FileNotFoundException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, l'upgrade équivalent à celui passé
        // en paramètre
        PallierType upgrade = findUpgradeByName(world, newupgrade.getName());
        System.out.println("new upgrade " + upgrade.getName());
        if (upgrade == null) {
            return false;
        }
        // débloquer cet upgrade
        if (world.getMoney() > upgrade.getSeuil()) {
            upgrade.setUnlocked(true);
            //cas d'un seul produit
            if (upgrade.getIdcible() != 0) {
                ProductType product = findProductById(world, upgrade.getIdcible());
                if (product == null) {
                    return false;
                }
                //gain
                if (upgrade.getTyperatio() == GAIN) {
                    product.setRevenu(product.getRevenu() * upgrade.getRatio());
                }
                //vitesse
                if (upgrade.getTyperatio() == VITESSE) {
                    product.setVitesse((int) (product.getVitesse() * upgrade.getRatio()));
                    product.setTimeleft((long) (product.getTimeleft() / upgrade.getRatio()));
                }
                //cas de plusieurs produits
            } else {
                //gain
                if (upgrade.getTyperatio() == GAIN) {
                    ProductsType ps = world.getProducts();
                    for (ProductType pro : ps.getProduct()) {
                        pro.setRevenu(pro.getRevenu() * upgrade.getRatio());
                    }
                }
                //vitesse
                if (upgrade.getTyperatio() == VITESSE) {
                    ProductsType ps = world.getProducts();
                    for (ProductType pro : ps.getProduct()) {
                        pro.setVitesse((int) (pro.getVitesse() * upgrade.getRatio()));
                        pro.setTimeleft((long) (pro.getTimeleft() / upgrade.getRatio()));
                    }
                }
            }
            //payer l'upgrade
            world.setMoney(world.getMoney() - upgrade.getSeuil());
        }
        //sauvegarder le monde
        this.saveWorldToXml(username, world);
        return true;
    }

    //trouve un upgrade a partir de son nom
    private PallierType findUpgradeByName(World world, String name) {
        PalliersType up = world.getUpgrades();
        PallierType Goodup = null;
        for (PallierType p : up.getPallier()) {
            if (p.getName().equals(name)) {
                    Goodup = p;
                }
            }
            return Goodup;
        }

    }
