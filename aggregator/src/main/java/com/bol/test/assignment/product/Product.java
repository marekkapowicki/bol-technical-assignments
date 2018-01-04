package com.bol.test.assignment.product;

public class Product {
    private final int id;
    private final String title;

    public Product(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
