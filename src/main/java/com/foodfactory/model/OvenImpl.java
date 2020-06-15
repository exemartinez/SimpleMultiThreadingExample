package com.foodfactory.model;

import com.foodfactory.exceptions.CapacityExceededException;

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * It cooks the food and handles its own storage of products
 */
public class OvenImpl implements Oven{
    private final Integer size; // TODO size was Double in the specification, refactor this.
    private final CopyOnWriteArrayList<Product> cookingProducts = new CopyOnWriteArrayList<>();
    private AtomicBoolean on = new AtomicBoolean(false);

    public OvenImpl(Integer size) {
        this.size = size;
    }

    @Override
    public double size() {
        return size;
    }

    @Override
    public void put(Product product) throws CapacityExceededException {
        Double currentOccupiedSize = cookingProducts.stream().mapToDouble(Product::size).sum(); // do not use a property to store the free space: multithreading could mess up with the current real value.

        if ((currentOccupiedSize + product.size()) > size){
            throw new CapacityExceededException();
        }

        cookingProducts.add(product);

    }

    @Override
    public void take(Product product) {
        cookingProducts.remove(product);
    }

    @Override
    public void turnOn() {
        this.on.set(true);
    }

    @Override
    public void turnOn(Duration duration) {
        // Not used.
    }

    @Override
    public void turnOff() {
        this.on.set(false);
    }

}
