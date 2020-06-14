package com.foodfactory.builders;

import com.foodfactory.model.StoreImpl;

/**
 * We dettach the way we build the stores from the store itself. I do not know
 * if I may change the very nature of what a StoreImpl is later.
 */
public class StoreBuilder {

    private static StoreBuilder storeBuilder;

    private StoreBuilder(){

    }

    public static StoreBuilder getInstance() {
        if (storeBuilder == null) storeBuilder = new StoreBuilder();
        return storeBuilder;
    }

    public StoreImpl build(Integer size) {
        StoreImpl store = new StoreImpl(size);
        return store;
    }
}
