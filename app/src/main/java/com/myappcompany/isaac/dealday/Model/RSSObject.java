package com.myappcompany.isaac.dealday.Model;

import java.util.List;

/**
 * Created by isaac on 19/03/18.
 */


public class RSSObject
{
    private List<Item> items;

    public RSSObject(List<Item> items) {
        this.items = items;
    }

    //Getters and Setters
    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}