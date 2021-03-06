package com.foodfactory.model;

import java.time.Duration;

/**
 * A generic food.
 * We can extend this class into hamburgers, fries or whatever.
 */
public class Food implements Product {

    private final Integer size;
    private final Duration cookTime;
    private Integer orderNumber;
    private Integer assemblyLineId;

    public Food(Integer size, Long cookTime){
        this.size = size;
        this.cookTime = Duration.ofSeconds(cookTime);
    }

    @Override
    public double size() {
        return size;
    }

    @Override
    public Duration cookTime() {
        return cookTime;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Integer getAssemblyLineId() {
        return assemblyLineId;
    }

    public void setAssemblyLineId(Integer assemblyLineId) {
        this.assemblyLineId = assemblyLineId;
    }
}
