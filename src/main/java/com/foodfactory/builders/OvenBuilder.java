package com.foodfactory.builders;

import com.foodfactory.model.Oven;

/**
 * It is in charge of returning an "Oven" kind of object.
 * I do believe these objects could "evolve" during this development,
 * so I try to encapsulate and dettach the way in which we build them.
 */
public class OvenBuilder {
    private static OvenBuilder ovenBuilder;

    private OvenBuilder(){

    }

    public static OvenBuilder getInstance() {
        if (ovenBuilder == null) ovenBuilder = new OvenBuilder();
        return ovenBuilder;
    }

    public Oven build(Integer size) {
        return new Oven(size);
    }
}
