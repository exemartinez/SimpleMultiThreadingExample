package com.foodfactory.controllers;

import com.foodfactory.controllers.Cooker;
import com.foodfactory.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is the kitchen, it is composed of many ovens and stores.
 * Assumption: the stores aren't indexed as one per OvenImpl; and the ovens doesn't has stores associated.
 * Number of Ovens: N and Stores: M
 */
public class Kitchen {

    private final List<Oven> ovens;
    private final List<Store> stores;
    private List<AssemblyLine> assemblyLines;
    private final ExecutorService executorCooker = Executors.newSingleThreadExecutor();
    private final ExecutorService executorDispatcher = Executors.newSingleThreadExecutor();
    private boolean endKitchen = false;
    private Cooker cooker;
    private AtomicBoolean lineContinues;

    public Kitchen(){
        ovens = new ArrayList<Oven>(); // I could have used an interface; I prefer to constraint it to JUST OVENS
        stores = new ArrayList<Store>(); // same thing here.
        cooker = new Cooker(ovens, stores); // The cooker will run in the master thread of the Kitchen and the retriever.
    }

    public void addOven(Oven oven) {
        ovens.add(oven);
    }

    public void addStore(Store store) {
        stores.add(store);
    }

    public void setAssemblyLines(List<AssemblyLine> assemblyLines) {
        this.assemblyLines = assemblyLines;
        this.cooker.setAssemblyLines(assemblyLines);
    }

    /**
     * Starts the kitchen's "Cooker".
     * A thread that monitors the assembly lines to take products to cook and retrieve them.
     */
    public void start() {

        runCookingActivities();
        // runPickingProductActivities();

    }

    /**
     * Inside this method, this thread takes the finished products and puts them in the finished lane.
     */
    private void runPickingProductActivities() {
        executorDispatcher.execute(()->{
            while(!endKitchen){

                for (AssemblyLine assemblyLine:assemblyLines) {

                    Product product = cooker.getNextFinishedProducts(assemblyLine.getId());

                    if (product != null) { // if there is any products in the sorted cache...
                        assemblyLine.putAfter(product);
                        System.out.println("DELIVERED product #: " + ((Food) product).getOrderNumber() + " from lane #: " + ((Food) product).getAssemblyLineId() + " - size: " + product.size() + " cooking time: " + product.cookTime().getSeconds());
                    }

                }
            }
        });
    }

    /**
     * The thread inside this method handles the cooking of the products.
     */
    private void runCookingActivities() {

        executorCooker.execute(()-> {

            while(!endKitchen){

                // Turn on all the ovens
                cooker.turnOnAllOvens();

                try {

                    // checks for the AssemblyLines to provide products
                    for (AssemblyLineStage assemblyLine : this.assemblyLines) {

                        //FIRST we try it over onto the stores, otherwise the lines will halt!
                        Product product = tryGettingProductFromStoresFirst();

                        String fromWhereTheProductWasTaken = "";

                        if (product == null){
                            product = assemblyLine.take();
                            fromWhereTheProductWasTaken = "Product taken from the assembly line...";
                        } else {
                            fromWhereTheProductWasTaken = "Product taken from one STORE...";
                        }

                        // THEN we go and put every product to cook.
                        if (product != null){

                            System.out.println(fromWhereTheProductWasTaken);
                            System.out.println("Trying to PUT in the OVEN product #: " + ((Food)product).getOrderNumber() + " from lane #: " + ((Food)product).getAssemblyLineId() + " - size: " + product.size() + " cooking time: " + product.cookTime().getSeconds());

                            lineContinues = new AtomicBoolean(cooker.cook(product));

                            if (!lineContinues.get()){
                                ((AssemblyLine)assemblyLine).haltProduction();
                                System.out.println("Production HALTED in Assembly Line #" + ((AssemblyLine)assemblyLine).getId());
                            } else if (((AssemblyLine)assemblyLine).isHalted()){
                                ((AssemblyLine)assemblyLine).continueProduction();
                                System.out.println("Production CONTINUES in Assembly Line #" + ((AssemblyLine)assemblyLine).getId());
                            }
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * Before we go for the AssemblyLine, we go into the Stores and
     * checkout for any remaining item to cook!
     * @return
     */
    private Product tryGettingProductFromStoresFirst() {
        Product product=null;

        for(Store store : stores){
            product = store.take();  // Store must be implemented as a Queue: it will return null if the Queue is empty.
        };

        return product;
    }

    /**
     * Procedurally STOPS the whole set of processes as is.
     *
     */
    public void stop() {
        // Turn Off all the ovens
        cooker.turnOffAllOvens();
        endKitchen = true;
    }

    /**
     * Brutal! but necessary option...
     */
    public void kill(){
        executorCooker.shutdownNow();
        executorDispatcher.shutdownNow();
    }


    /**
     * Notifies the cooker that a new assembly line is online and it will need
     * a new cache to sort some of the finiched elements.
     * @param id
     */
    public void addCacheToCooker(Integer id) {
        cooker.addOneMoreCache(id);
    }
}
