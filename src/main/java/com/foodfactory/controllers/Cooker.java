package com.foodfactory.controllers;

import com.foodfactory.exceptions.CapacityExceededException;
import com.foodfactory.model.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * Handles the thread of execution of the Kitchen.
 * What needs to be cooked, in which order.
 * It workd, more or less, like a "business controller" for the backend.
 */
public class Cooker {

    private final List<Oven> ovens;
    private final List<Store> stores;
    private List<AssemblyLine> assemblyLines = null;
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
        timers.forEach(t -> ((ExecutorService)t).shutdownNow()); // Kills every cooking timer.
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

        Product nullableProduct  = product; // we copy the reference, because the Store interface doesn't handles a return type. It halts.
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

        Boolean productPlacedInOven = true;

        // Trying to get the product into the Oven //TODO add functionality to test if the Oven is turned on or not.
        for (Oven oven: ovens) {
            try {
                oven.put(product);
                productPlacedInOven = true;
                break;
            } catch (CapacityExceededException e) {
                productPlacedInOven = false;
            }
        }

        // If the product was placed in an oven we start "cooking it", and place it in the cooker's "cache".
        if (productPlacedInOven){
            startCookingTimer(product);
        }

        return productPlacedInOven;
    }

    /**
     * Start a thread with a timer to hold on it; when it finishes, we take the product out of the oven.
     * @param product
     */
    private void startCookingTimer(Product product) {

        Executor timer = Executors.newSingleThreadExecutor();

        timer.execute(()->{
            try {

                System.out.println("COOKING product #: " + ((Food)product).getOrderNumber() + " from lane #: " + ((Food)product).getAssemblyLineId() + " - size: " + product.size() + " cooking time: " + product.cookTime().getSeconds());
                TimeUnit.SECONDS.sleep(product.cookTime().getSeconds());

                //Take the product from the oven.
                ovens.forEach(oven -> oven.take(product)); //The product object (with its object id) should be found in just one oven and erased.
                //Put it in an Assembly Line for finished products (thread safe sorted cache).
                addNextFinishedProductToAssemblyLine(((Food)product).getAssemblyLineId(), product); // TODO: if we kill the main thread before this happens we might lose one product! fix this.

                System.out.println("FINISHED cooking product #: " + ((Food)product).getOrderNumber() + " from lane #: " + ((Food)product).getAssemblyLineId() + " - size: " + product.size() + " cooking time: " + product.cookTime().getSeconds());
                this.timers.remove(timer); // we take care of freeing the memory as well.

            } catch (InterruptedException e) {
                System.out.println("INTERRUPTED Cooking TIMER for product: "+ ((Food) product).getOrderNumber() + " of Assembly line: " + ((Food) product).getAssemblyLineId());
            }
        });

        timers.add(timer); // we do this to avoid garbage collection of the thread!
    }

    /**
     * Adds one more product to the cache, for its given AssemblyLine
     */
    private void addNextFinishedProductToAssemblyLine(Integer idAssemblyLine, Product product) {
        this.assemblyLines.get(idAssemblyLine).putAfter(product);
    }

    /**
     * We assign the assemblies lines to have access to the finishedProducts sorted queue.
     * @param assemblyLines
     */
    public void setAssemblyLines(List<AssemblyLine> assemblyLines) {
        this.assemblyLines = assemblyLines;
    }
}
