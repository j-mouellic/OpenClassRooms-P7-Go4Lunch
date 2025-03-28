package com.julien.go4lunch.model.bo.place;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.processing.Generated;

@Generated("jsonschema2pojo")
public class Viewport {

    @SerializedName("northeast")
    @Expose
    private Northeast northeast;
    @SerializedName("southwest")
    @Expose
    private Southwest southwest;

    public Northeast getNortheast() {
        return northeast;
    }

    public void setNortheast(Northeast northeast) {
        this.northeast = northeast;
    }

    public Southwest getSouthwest() {
        return southwest;
    }

    public void setSouthwest(Southwest southwest) {
        this.southwest = southwest;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Viewport.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("northeast");
        sb.append('=');
        sb.append(((this.northeast == null)?"<null>":this.northeast));
        sb.append(',');
        sb.append("southwest");
        sb.append('=');
        sb.append(((this.southwest == null)?"<null>":this.southwest));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.southwest == null)? 0 :this.southwest.hashCode()));
        result = ((result* 31)+((this.northeast == null)? 0 :this.northeast.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Viewport) == false) {
            return false;
        }
        Viewport rhs = ((Viewport) other);
        return (((this.southwest == rhs.southwest)||((this.southwest!= null)&&this.southwest.equals(rhs.southwest)))&&((this.northeast == rhs.northeast)||((this.northeast!= null)&&this.northeast.equals(rhs.northeast))));
    }

}
