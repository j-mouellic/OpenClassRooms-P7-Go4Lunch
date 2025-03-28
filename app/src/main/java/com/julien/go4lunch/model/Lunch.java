package com.julien.go4lunch.model;

import java.util.Objects;

/**
 * This class represents a lunch event involving workmates and a restaurant at a specific date.
 */
public class Lunch {
    private Workmate workmate;
    private Restaurant restaurant;
    private String date;

    public Lunch(){
        // KEEP EMPTY CONSTRUCTOR FOR FIRESTORE
    }

    /**
     * Constructor for the Lunch class.
     *
     * @param workmate  the workmate participating in the lunch
     * @param restaurant the restaurant where the lunch takes place
     * @param date       the date and time of the lunch
     */
    public Lunch(Workmate workmate, Restaurant restaurant, String date) {
        this.workmate = workmate;
        this.restaurant = restaurant;
        this.date = date;
    }

    /**
     * Gets and sets the list of workmates participating in the lunch.
     **/
    public Workmate getWorkmate() {
        return workmate;
    }

    public void setWorkmate(Workmate workmate) {
        this.workmate = workmate;
    }

    /**
     * Gets and sets the restaurant where the lunch takes place.
     **/
    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    /**
     * Gets and sets the date and time of the lunch.
     **/
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Lunch{" +
                "workmate=" + workmate +
                ", restaurant=" + restaurant +
                ", date=" + date +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lunch lunch = (Lunch) o;
        return Objects.equals(workmate, lunch.workmate) && Objects.equals(restaurant, lunch.restaurant) && Objects.equals(date, lunch.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workmate, restaurant, date);
    }
}
