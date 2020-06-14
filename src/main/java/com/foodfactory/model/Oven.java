package com.foodfactory.model;

import com.foodfactory.exceptions.CapacityExceededException;

import java.time.Duration;

/**
 * This interface represents the OvenImpl that cooks the products in the different assembly lines
 */
public interface Oven {
    /**
     * This returns the size of the oven in cm2. As a simplification of the problem, assume that the
     * sizes of the products can be summed, and that value should not exceed the size of the oven. Otherwise an
     * exception is thrown if adding a product.
     * @return
     */
    double size();

    /**
     * Puts a product in the oven to be cooked. The oven can be functioning at the time the product is put in.
     * @param product The product to put in the oven
     * @throws CapacityExceededException if the oven capacity is exceeded.
     */
    void put(Product product) throws CapacityExceededException;

    /**
     * Take the specified Product out of the oven. The oven can be functioning at the time the product is taken out.
     * @param product
     */
    void take(Product product);

    /**
     * Turns on the OvenImpl. If the oven was turned on with a duration, the duration is ignored.
     */
    void turnOn();

    /**
     * Turn on the OvenImpl for the specified duration. If the oven is turned on, it updates the duration.
     * @param duration the duration to mantain the oven before turning it off.
     */
    void turnOn(Duration duration);

    /**
     * Turn off the OvenImpl immediately, even if it was turned on with a duration which will be ignored.
     */
    void turnOff();

}
