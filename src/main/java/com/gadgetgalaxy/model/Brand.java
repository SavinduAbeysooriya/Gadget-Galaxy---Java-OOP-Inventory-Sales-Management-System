package com.gadgetgalaxy.model;

/**
 * Model class for Brand.
 * Demonstrates Encapsulation.
 */
public class Brand {
    private int brandId;
    private String brandName;
    private String country;

    public Brand(int brandId, String brandName, String country) {
        this.brandId = brandId;
        this.brandName = brandName;
        this.country = country;
    }

    public Brand(String brandName, String country) {
        this(0, brandName, country);
    }

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return brandName;
    }
}
