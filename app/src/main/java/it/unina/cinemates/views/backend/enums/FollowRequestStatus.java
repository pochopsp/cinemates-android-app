package it.unina.cinemates.views.backend.enums;

import androidx.annotation.NonNull;

public enum FollowRequestStatus {
    NoRequest("NoRequest"),
    Pending("Pending"),
    Accepted("Accepted");

    private final String displayName;

    FollowRequestStatus(String displayName){
        this.displayName = displayName;
    }

    @NonNull
    @Override
    public String toString() {
        return displayName;
    }

}
