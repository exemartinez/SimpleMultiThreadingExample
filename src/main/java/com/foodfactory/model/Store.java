package com.foodfactory.model;

/**
 * This holds the food, but doesn't cooks it.
 */
public class Store implements Holder{

    private final Integer size; // we will not provide a getter & setter for this

    public Store(Integer size) {
        this.size = size;
    }

    @Override
    public Integer getSize() {
        return size;
    }
}
