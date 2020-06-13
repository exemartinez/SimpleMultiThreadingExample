package com.foodfactory.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the kitchen, it is composed of many ovens and stores.
 * Assumption: the stores aren't indexed as one per Oven; and the ovens doesn't has stores associated.
 * Number of Ovens: N and Stores: M
 */
public class Kitchen {

    private final List<Oven> ovens;
    private final List<Store> stores;

    public Kitchen(){
        ovens = new ArrayList<Oven>(); // I could have used an interface; I prefer to constraint it to JUST OVENS
        stores = new ArrayList<Store>(); // same thing here.
    }

    public void addOven(Oven oven) {
        ovens.add(oven);
    }

    public void addStore(Store store) {
        stores.add(store);
    }
}
