package com.foodfactory.model;

/**
 * The store where to put the products if the oven is not avialable. This class is thread safe.
 */
public interface Store {

    /**
     * Put a product in this store, if there is no space left in the store, it will block
     * until enough space frees up. This operation will put the products in FIFO order
     * @param product The Product to put in this StoreImpl
     */
    void put(Product product);

    /**
     * Take the next element that has to be processed respecting FIFO
     * @return
     */
    Product take();

    /**
     * Take the specified Product from the StoreImpl
     * @param product
     */
    void take(Product product);
}
