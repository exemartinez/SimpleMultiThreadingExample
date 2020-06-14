package com.foodfactory.builders;

import com.foodfactory.controllers.Kitchen;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Knows how to assemble a kitchen from a .properties file.
 * This is a micture between a Builder and a Factory pattern.
 */
public class KitchenBuilder {

    private static final String PROPERTIES_SEPARATOR = ",";
    private static KitchenBuilder kitchenBuilder = null;
    private String[] ovensToBuild;
    private String[] storesToBuild;

    /**
     * Loads up how to build the kitchen: how many Ovens and Stores, and their sizes.
     * @return
     * @throws IOException
     */
    private void readKitchenProperties() {

        final InputStream inputStream;

        try {
            Properties properties = new Properties();
            String propFileName = "config.properties";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            try(inputStream) {

                if (inputStream != null) {
                    properties.load(inputStream);
                } else {
                    throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
                }

                ovensToBuild = properties.getProperty("OvensSize").split(PROPERTIES_SEPARATOR);
                storesToBuild = properties.getProperty("StoresSize").split(PROPERTIES_SEPARATOR);
            }

        } catch (Exception e) {
            System.out.println("Can't read the 'config.properties' file. Exception: " + e.getMessage());
        }
    }
    
    /**
     * Builds itself from a .properties file
     */
    private KitchenBuilder(){
        readKitchenProperties();
    }

    /**
     * Singleton kind of implementation as usually a builder/factory pattern could be implemented.
     * @return
     */
    public static KitchenBuilder getInstance() {
        if (kitchenBuilder == null) kitchenBuilder = new KitchenBuilder();
        return kitchenBuilder;
    }

    /**
     * Takes the due properties and builds up a kitchen as it's been requested in
     * the properties files.
     * @return
     */
    public Kitchen buildKitchenStructure() {

        Kitchen kitchen = new Kitchen();

        OvenBuilder ovenBuilder = OvenBuilder.getInstance();
        StoreBuilder storeBuilder = StoreBuilder.getInstance();

        //Old fashioned for; we do not want to overburden the setup with streaming or foreach 'pirotechnics'.
        for(int i = 0; i < ovensToBuild.length; i++){
            kitchen.addOven(ovenBuilder.build(Integer.parseInt(ovensToBuild[i])));
        }

        for(int i = 0; i < storesToBuild.length; i++){
            kitchen.addStore(storeBuilder.build(Integer.parseInt(storesToBuild[i])));
        }

        return kitchen;
    }
}
