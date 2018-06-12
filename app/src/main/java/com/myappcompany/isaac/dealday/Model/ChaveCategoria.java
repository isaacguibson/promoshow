package com.myappcompany.isaac.dealday.Model;

public class ChaveCategoria {

    private String key;
    private String category;

    public ChaveCategoria(String key, String category) {
        this.key = key;
        this.category = category;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
