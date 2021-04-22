package com.example.demo;

import org.apache.commons.csv.CSVRecord;

public class Product {
    private String name;
    private String sku;
    private String description;

    public Product(CSVRecord record) {
        name = record.get(0);
        sku = record.get(1);
        description = record.get(2);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name=" + name +
                ", sku=" + sku +
                ", description=" + description +
                '}';
    }
}
