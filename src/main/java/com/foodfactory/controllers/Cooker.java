package com.foodfactory.controllers;

import com.foodfactory.exceptions.CapacityExceededException;
import com.foodfactory.model.*;

import javax.swing.plaf.basic.BasicGraphicsUtils;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

/**
 * Handles the thread of execution of the Kitchen.
 * What needs to be cooked, in which order.
 */
public class Cooker {

    private static final int CACHE_INITIAL_CAPACITY = 11; // A prime number for good luck. :)
    private static final Integer FACTOR_FOR_LANE_ID = 1000000; //We use this to form a unique identifier for a product in the cache that combines AssemblyLaneID + product.OrdenNumber.

    private final List<Oven> ovens;
    private final List<Store> stores;

    private final Comparator<Product> orderNumberSorter = Comparator.comparing(product -> ((FACTOR_FOR_LANE_ID * ((Food)product).getAssemblyLineId()) + ((Food)product).getOrderNumber()));

    private final PriorityBlockingQueue<Product> cacheFinishedProducts = new PriorityBlockingQueue<>(CACHE_INITIAL_CAPACITY,orderNumberSorter);
    private final List<Executor> timers = new CopyOnWriteArrayList<>();

    /**
     * This constructor allows us to maintain a reference
     * to the Kitchens ovens & stores for the cooker to
     * manage them.
     * @param ovens
     * @param stores
     */
    public Cooker(List<Oven> ovens, List<Store> stores) {
        this.ovens = ovens;
        this.stores = stores;
    }

    public synchronized void  turnOnAllOvens() {
        ovens.forEach(Oven::turnOn);
    }

    public synchronized void turnOffAllOvens() {
        ovens.forEach(Oven::turnOff);
    }

    /**
     * Here lies the main logic of how to put to cook a given product
     * @param product
     */
    public final Boolean cook(Product product) throws InterruptedException {

        // tries and puts the product in the due oven
        Boolean cooking = placeProductInOven(product);

        // if there is not space in the ovens, places the product into a store.
        if(!cooking){
            if(placeProductInStorage(product)){
                return true;
            } else {
                return false;
            }
        }else{
            return true;
        }

    }

    /**
     * Tries to place the product in a Storage, if not, it returns false.
     * @param product
     * @return
     */
    private Boolean placeProductInStorage(Product product) {

        Product nullableProduct  = product; // we copy the reference
        Boolean productPlacedInStore = false;

        //Tries to get the product into a storage
        for (Store store: stores) {

            store.put(nullableProduct);

            if (nullableProduct == null){ // WE ARE DOING THIS BECAUSE WE ASSUME YOU WANT US TO DEAL WITH THE CURRENT INTERFACE FOR STORE.
                continue; // redundant, but is more readable like this.
            }else{
                productPlacedInStore = true;
                System.out.println("STORED product #: " + ((Food)product).getOrderNumber() + " from lane #: " + ((Food)product).getAssemblyLineId() + " into Store of SIZE: " + ((StoreImpl)store).getSize());
                break;
            }
        }

        return productPlacedInStore;
    }

    /**
     * Tries and places a product in an Oven, if it is successful it starts a timer that simulates
     * the cooking time.
     * @param product
     * @return
     */
    private Boolean placeProductInOven(Product product) {
        Executor timer = Executors.newSingleThreadExecutor();
        Boolean productPlacedInOven = true;

        // Trying to get the product into the Oven //TODO add functionality to test if the Oven is turned on or not.
        for (Oven oven: ovens) {
            try {
                oven.put(product);
                break;
            } catch (CapacityExceededException e) {
                productPlacedInOven = false;
            }
        }

        // If the product was placed in an oven we start "cooking it", and place it in the cooker's "cache".
        if (productPlacedInOven){

            timer.execute(()->{
                try {

                    System.out.println("COOKING product #: " + ((Food)product).getOrderNumber() + " from lane #: " + ((Food)product).getAssemblyLineId() + " - size: " + product.size() + " cooking time: " + product.cookTime().getSeconds());
                    TimeUnit.SECONDS.sleep(product.cookTime().getSeconds());

                    //Take the product from the oven.
                    ovens.forEach(oven -> oven.take(product)); //The product object (with its object id) should be found in just one oven and erased.
                    //Put it in a sorted cache for finished products. (another thread will pick it up)
                    this.cacheFinishedProducts.add(product);

                    System.out.println("FINISHED cooking product #: " + ((Food)product).getOrderNumber() + " from lane #: " + ((Food)product).getAssemblyLineId() + " - size: " + product.size() + " cooking time: " + product.cookTime().getSeconds());

                    this.timers.remove(timer); // we take care of freeing the memory as well.

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            timers.add(timer); // we do this to avoid garbage collection of the thread!

        }

        return productPlacedInOven;
    }

    /**
     * returns the first available finished product, sorted.
     */
    public Product getNextFinishedProducts() {
        return cacheFinishedProducts.poll();
    }
}
