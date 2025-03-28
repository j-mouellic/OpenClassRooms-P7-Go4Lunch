package com.julien.go4lunch.model.bo.place;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.processing.Generated;

@Generated("jsonschema2pojo")
public class PlusCode {

    @SerializedName("compound_code")
    @Expose
    private String compoundCode;
    @SerializedName("global_code")
    @Expose
    private String globalCode;

    public String getCompoundCode() {
        return compoundCode;
    }

    public void setCompoundCode(String compoundCode) {
        this.compoundCode = compoundCode;
    }

    public String getGlobalCode() {
        return globalCode;
    }

    public void setGlobalCode(String globalCode) {
        this.globalCode = globalCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(PlusCode.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("compoundCode");
        sb.append('=');
        sb.append(((this.compoundCode == null)?"<null>":this.compoundCode));
        sb.append(',');
        sb.append("globalCode");
        sb.append('=');
        sb.append(((this.globalCode == null)?"<null>":this.globalCode));
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
        result = ((result* 31)+((this.compoundCode == null)? 0 :this.compoundCode.hashCode()));
        result = ((result* 31)+((this.globalCode == null)? 0 :this.globalCode.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PlusCode) == false) {
            return false;
        }
        PlusCode rhs = ((PlusCode) other);
        return (((this.compoundCode == rhs.compoundCode)||((this.compoundCode!= null)&&this.compoundCode.equals(rhs.compoundCode)))&&((this.globalCode == rhs.globalCode)||((this.globalCode!= null)&&this.globalCode.equals(rhs.globalCode))));
    }

}
