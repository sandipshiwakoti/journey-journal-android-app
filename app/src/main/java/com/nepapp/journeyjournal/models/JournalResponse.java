package com.nepapp.journeyjournal.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JournalResponse {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private List<JournalEntry> data = null;

    public JournalResponse(Boolean success, List<JournalEntry> data) {
        super();
        this.success = success;
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<JournalEntry> getData() {
        return data;
    }

    public void setData(List<JournalEntry> data) {
        this.data = data;
    }

}