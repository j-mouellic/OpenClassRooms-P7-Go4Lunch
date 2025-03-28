package com.julien.go4lunch.model;

import com.julien.go4lunch.model.bo.place.Location;
import com.julien.go4lunch.model.bo.place.Photo;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a restaurant with attributes such as name, address, distance, type, opening hours,
 * number of reviews, rating, and picture.
 */
public class Restaurant implements Serializable {
    private String id;
    private String name;
    private String address;
    private Location location;
    private List<String> types;
    private Boolean isOpened;
    private Integer numberOfReviews;
    private Double rating;
    private String website;
    private String formattedPhoneNumber;
    private List<Photo> photos;
    private String icon;


    /**
     * Constructor for the Restaurant class.
     *
     * @param name                 the name of the restaurant
     * @param location             the location of the restaurant
     * @param address              the address of the restaurant
     * @param types                the type of cuisine offered by the restaurant
     * @param isOpened             the opening hours of the restaurant
     * @param numberOfReviews      the number of reviews the restaurant has received
     * @param rating               the rating of the restaurant
     * @param website              the website of the restaurant
     * @param formattedPhoneNumber the phone nubme of the restaurant
     * @param photos               the list of restaurant's photos
     * @param icon                 the icon of the restaurant
     */
    public Restaurant(String id, String name, String address, Location location, List<String> types, Boolean isOpened, Integer numberOfReviews, Double rating, String website, String formattedPhoneNumber, List<Photo> photos, String icon) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.location = location;
        this.types = types;
        this.isOpened = isOpened;
        this.numberOfReviews = numberOfReviews;
        this.rating = rating;
        this.website = website;
        this.formattedPhoneNumber = formattedPhoneNumber;
        this.photos = photos;
        this.icon = icon;
    }

    public Restaurant() {
        // KEEP EMPTY CONSTRUCTOR
    }

    /**
     * Gets and sets the id of the restaurant.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets and sets the name of the restaurant.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets and sets the address of the restaurant.
     */
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets and sets the address of the restaurant.
     */
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }


    /**
     * Gets and sets the type of cuisine offered by the restaurant.
     */
    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    /**
     * Gets and sets the opening hours of the restaurant.
     */
    public Boolean getOpened() {
        return isOpened;
    }

    public void setOpened(Boolean opened) {
        isOpened = opened;
    }

    /**
     * Gets and sets the number of reviews the restaurant has received.
     */
    public int getNumberOfReviews() {
        return numberOfReviews != null ? numberOfReviews : 0;
    }

    public void setNumberOfReviews(int numberOfReviews) {
        this.numberOfReviews = numberOfReviews;
    }

    /**
     * Gets and sets the rating of the restaurant.
     */
    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    /**
     * Gets and sets the website URL of the restaurant.
     */
    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    /**
     * Gets and sets the phone number of the restaurant.
     */
    public String getFormattedPhoneNumber() {
        return formattedPhoneNumber;
    }

    public void setformattedPhoneNumber(String formattedPhoneNumber) {
        this.formattedPhoneNumber = formattedPhoneNumber;
    }

    /**
     * Gets and sets the URL of the restaurant's picture.
     */
    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    /**
     * Gets and sets the restaurant's icon.
     */
    public String getIcon(){ return icon; }
    public void setIcon(String icon){
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", types='" + types + '\'' +
                ", isOpened=" + isOpened +
                ", numberOfReviews=" + numberOfReviews +
                ", rating=" + rating +
                ", photos='" + photos + '\'' +
                ", icon = " + icon +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Restaurant that = (Restaurant) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(address, that.address) && Objects.equals(location, that.location) && Objects.equals(types, that.types) && Objects.equals(isOpened, that.isOpened) && Objects.equals(numberOfReviews, that.numberOfReviews) && Objects.equals(rating, that.rating) && Objects.equals(website, that.website) && Objects.equals(formattedPhoneNumber, that.formattedPhoneNumber) && Objects.equals(photos, that.photos) && Objects.equals(icon, that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, address, location, types, isOpened, numberOfReviews, rating, website, formattedPhoneNumber, photos, icon);
    }
}
