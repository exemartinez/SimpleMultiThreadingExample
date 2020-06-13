package com.foodfactory.model;

/**
 * Looks like a Store, but it might be WAY different.
 * It cooks the food and we might want to add some other functionality later.
 */
public class Oven implements Holder{
    private final Integer size; // we will not provide a getter & setter for this

    public Oven(Integer size) {
        this.size = size;
    }

    @Override
    public Integer getSize() {
        return size;
    }
}
