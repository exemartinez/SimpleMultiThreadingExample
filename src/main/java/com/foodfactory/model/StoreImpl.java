package com.foodfactory.model;

import com.foodfactory.exceptions.CapacityExceededException;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This holds the food, but doesn't cooks it.
 */
public class StoreImpl implements Store{

    private final Integer size; // we will not provide a getter & setter for this
    private final ConcurrentLinkedQueue<Product> storedProducts = new ConcurrentLinkedQueue<>();

    public StoreImpl(Integer size){
        this.size = size;
    }

    public Integer getSize() {
        return size;
    }

    @Override
    public void put(Product product) {
        Double currentOccupiedSize = storedProducts.stream().mapToDouble(Product::size).sum(); // do not use a property to store the free space: multithreading could mess up with the current real value.

        if ((currentOccupiedSize + product.size()) > size){
            product = null; // THIS IS WRONG, but we are just representing the case when the current interface is forced into our design: "How we deal with it?"
        } else {
            storedProducts.add(product);
        }

    }

    @Override
    public Product take() {
        return storedProducts.poll();
    }

    @Override
    public void take(Product product) {

    }
}
