package com.julien.go4lunch.model.bo.place;

import com.google.gson.annotations.SerializedName;

public class ResultDetails {
    @SerializedName("result")
    private Result result;

    public ResultDetails(Result result) {
        this.result = result;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }


}
