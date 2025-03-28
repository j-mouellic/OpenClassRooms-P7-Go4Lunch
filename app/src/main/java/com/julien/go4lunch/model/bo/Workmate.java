package com.julien.go4lunch.model.bo;

import java.util.Objects;
import java.util.Random;

/**
 * This class represents a workmate with attributes like ID, first name, last name, email, and avatar.
 */
public class Workmate {
    private String uid;
    private String name;
    private String email;
    private String avatar;
    private boolean isNotificationEnabled;
    public static final Random random = new Random();

    /**
     * Default constructor for Firestore deserialization.
     * Keeps an empty constructor for Firestore to create instances of Workmate.
     */
    public Workmate(){
        // KEEP EMPTY CONSTRUCTOR FOR FIRESTORE
    }

    /**
     * Constructor for the Workmate class.
     * Initializes a Workmate object with the given attributes.
     *
     * @param uid the unique identifier of the workmate
     * @param name the name of the workmate
     * @param email the email address of the workmate
     * @param avatar the avatar (profile picture) URL or path of the workmate
     * @param isNotificationEnabled indicates if the workmate has notifications enabled
     */
    public Workmate(String uid, String name, String email, String avatar, boolean isNotificationEnabled) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
        this.isNotificationEnabled = isNotificationEnabled;
    }

    /**
     * Gets the unique identifier of the workmate.
     *
     * @return the UID of the workmate
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the unique identifier of the workmate.
     *
     * @param uid the unique identifier to be set
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Gets the name of the workmate.
     *
     * @return the name of the workmate
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the workmate.
     *
     * @param name the name to be set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the email address of the workmate.
     *
     * @return the email of the workmate
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the workmate.
     *
     * @param email the email to be set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the avatar (profile picture) URL or path of the workmate.
     *
     * @return the avatar of the workmate
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     * Sets the avatar (profile picture) URL or path of the workmate.
     *
     * @param avatar the avatar to be set
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     * Gets whether notifications are enabled for the workmate.
     *
     * @return true if notifications are enabled, false otherwise
     */
    public boolean isNotificationEnabled() {
        return isNotificationEnabled;
    }

    /**
     * Sets whether notifications are enabled for the workmate.
     *
     * @param notificationEnabled the status to be set for notifications
     */
    public void setNotificationEnabled(boolean notificationEnabled) {
        isNotificationEnabled = notificationEnabled;
    }

    /**
     * Provides a string representation of the Workmate object.
     *
     * @return a string representation of the Workmate instance
     */
    @Override
    public String toString() {
        return "Workmate{" +
                "id=" + uid +
                ", firstName='" + name + '\'' +
                ", email='" + email + '\'' +
                ", avatar='" + avatar + '\'' +
                ", isNotificationEnabled=" + isNotificationEnabled +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Workmate workmate = (Workmate) o;
        return isNotificationEnabled == workmate.isNotificationEnabled && Objects.equals(uid, workmate.uid) && Objects.equals(name, workmate.name) && Objects.equals(email, workmate.email) && Objects.equals(avatar, workmate.avatar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, name, email, avatar, isNotificationEnabled);
    }

    public static String getRandomHexColorFromPalette(){
        String[] colorPalette = {
                "#4e91b6", "#16adc5", "#00c7bf", "#6d77b1", "#8267a4", "#94568e",
                "#007a68", "#ec9929", "#459085", "#d16c6b", "#374955", "#9baebc",
                "#5ab3a6", "#ff9e7b", "#ffc968", "#725894", "#005b45"
        };

        int randomInt = random.nextInt(colorPalette.length);
        return colorPalette[randomInt];
    }
}
