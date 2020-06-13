package com.foodfactory.builders;

import com.foodfactory.model.Store;

/**
 * We dettach the way we build the stores from the store itself. I do not know
 * if I may change the very nature of what a Store is later.
 */
public class StoreBuilder {

    private static StoreBuilder storeBuilder;

    private StoreBuilder(){

    }

    public static StoreBuilder getInstance() {
        if (storeBuilder == null) storeBuilder = new StoreBuilder();
        return storeBuilder;
    }

    public Store build(Integer size) {
        Store store = new Store(size);
        return store;
    }
}
