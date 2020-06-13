package com.foodfactory.model;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AssemblyLine implements AssemblyLineStage {

    private static final long PRODUCTION_TIME = 3; // I like it every three seconds, a prime number takes the oddities out!

    // DISCLAIMER: These values do not represent REAL cooking times neither food product sizes (just intented for this simulation use).
    private static final int MIN_PRODUCT_SIZE = 10;
    private static final int MAX_PRODUCT_SIZE = 30;
    private static final int MIN_PRODUCT_COOK = 5;
    private static final int MAX_PRODUCT_COOK = 15;
    private static final int MIN_PRODUCTIVITY_DELAY = 0;
    private static final int MAX_PRODUCTIVITY_DELAY = 3;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ConcurrentLinkedQueue<Product> waitingProducts = new ConcurrentLinkedQueue<>(); // IN queue
    private final ConcurrentLinkedQueue<Product> finishedProducts = new ConcurrentLinkedQueue<>(); // OUT queue

    private final Integer id;
    private AtomicInteger production = new AtomicInteger(0);

    public AssemblyLine (Integer id){
        this.id = id;
    }

    /**
     * Initiates the generation of products and its due threads.
     */
    public void start() {
        executor.execute(()-> {

            while(true){

                try {

                    Integer productivityDelay = ThreadLocalRandom.current().nextInt(MIN_PRODUCTIVITY_DELAY, MAX_PRODUCTIVITY_DELAY);
                    TimeUnit.SECONDS.sleep(PRODUCTION_TIME + productivityDelay);
                    Product product = generateRandomProduct();

                    addProduct(product);

                    //TODO Replace all the 'sysout' for proper loggers.
                    System.out.println("Added product - size: " + product.size() + " cooking time: " + product.cookTime() + " to Assembly line: " + this.getId());

                } catch (InterruptedException e) {
                    //TODO: actionate in a proper way or use a flag to terminate the whole thread appropiately
                    e.printStackTrace();
                }

            }

        });

    }

    private synchronized void addProduct(Product product) {
        this.waitingProducts.add(product);
    }

    /**
     * I randomize the values that a new product might have, just before it enters the "input" line.
     * I tries with minimal and maximum values but, ideally, those must be out of some sort of "setup scheme".
     * @return
     */
    private Product generateRandomProduct() {
        Integer size = ThreadLocalRandom.current().nextInt(MIN_PRODUCT_SIZE, MAX_PRODUCT_SIZE);
        Integer cookTime = ThreadLocalRandom.current().nextInt(MIN_PRODUCT_COOK, MAX_PRODUCT_COOK);

        Food food = new Food(size, cookTime.longValue());

        // Adding tracking data
        food.setAssemblyLine(this.getId());
        food.setOrderNumber(production.getAndIncrement());

        return food;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public void putAfter(Product product) {
        finishedProducts.add(product);
    }

    @Override
    public synchronized Product take() {
        return this.waitingProducts.poll();
    }

    public void stop(){
        executor.shutdownNow(); //Brutal! we should use shoutdown and then ask the thread to take care of itself.
    }

    /**
     * Prints the number of elements in each queue of the AssemblyLine.
     * This should go into a file, a DB or a log; not to the standard output.
     * However, for the intention of this exercise it will suffice.
     *
     */
    public void printStatus() {

        //Print waiting Queue
        System.out.println("Assembly Line #" + this.getId() + ": still not cooked products - " + this.waitingProducts.size());

        //Print finished products Queue
        System.out.println("Assembly Line #" + this.getId() + ": finished products - " + this.finishedProducts.size());

    }

}
