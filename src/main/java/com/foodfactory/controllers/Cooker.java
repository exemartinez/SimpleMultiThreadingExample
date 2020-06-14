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

    private final List<Oven> ovens;
    private final List<Store> stores;
    private List<AssemblyLine> assemblyLines = null;
    private final List<PriorityBlockingQueue<Product>> cacheFinishedAssemblyLine = new CopyOnWriteArrayList<PriorityBlockingQueue<Product>>();
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
                //Put it in a sorted cache for finished products. (another thread will pick it up)
                addNextFinishedProductToAssemblyLine(((Food)product).getAssemblyLineId(), product);

                System.out.println("FINISHED cooking product #: " + ((Food)product).getOrderNumber() + " from lane #: " + ((Food)product).getAssemblyLineId() + " - size: " + product.size() + " cooking time: " + product.cookTime().getSeconds());

                this.timers.remove(timer); // we take care of freeing the memory as well.

            } catch (InterruptedException e) {
                System.out.println("Cooking TIMER for product: "+ ((Food) product).getOrderNumber() + " of Assembly line: " + ((Food) product).getAssemblyLineId());
            }
        });

        timers.add(timer); // we do this to avoid garbage collection of the thread!
    }

    /**
     * returns the first available finished product, sorted.
     */
    public Product getNextFinishedProducts(Integer idAssemblyLine) {

        if (cacheFinishedAssemblyLine.size() > idAssemblyLine) { // We are doing this just to avoid an "IndexOutOfBounds" kind of exception at the beginning.
            return cacheFinishedAssemblyLine.get(idAssemblyLine).poll();
        }

        return null;
    }

    /**
     * Adds one more product to the cache, for its given AssemblyLine
     */
    private void addNextFinishedProductToCache(Integer idAssemblyLine, Product product) {
        cacheFinishedAssemblyLine.get(idAssemblyLine).add(product);
    }

    /**
     * Adds one more product to the cache, for its given AssemblyLine
     */
    private void addNextFinishedProductToAssemblyLine(Integer idAssemblyLine, Product product) {
        this.assemblyLines.get(idAssemblyLine).putAfter(product);
    }

    /**
     * Creates one more cache for the finished products.
     * @param id
     */
    public void addOneMoreCache(Integer id) {
        // I want them sorted by their Food "order number"
        final Comparator<Product> orderNumberSorter = Comparator.comparing(product -> ((Food)product).getOrderNumber());
        final PriorityBlockingQueue<Product> cacheFinishedProduct = new PriorityBlockingQueue<>(CACHE_INITIAL_CAPACITY,orderNumberSorter);

        cacheFinishedAssemblyLine.add(cacheFinishedProduct);
    }

    public List<AssemblyLine> getAssemblyLines() {
        return assemblyLines;
    }

    /**
     * We assign the assemblies lines to have access to the finishedProducts sorted queue.
     * @param assemblyLines
     */
    public void setAssemblyLines(List<AssemblyLine> assemblyLines) {
        this.assemblyLines = assemblyLines;
    }
}
