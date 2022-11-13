package it.unina.cinemates.views.tmdb.enums;

import androidx.annotation.NonNull;

public enum MovieStatus {
    //TODO fare toString italiano
    RELEASED("Released"),
    CANCELED("Canceled"),
    POST_PRODUCTION("Post Production"),
    IN_PRODUCTION("In Production"),
    PLANNED("Planned"),
    RUMORED("Rumored");

    private final String displayName;

    MovieStatus(String displayName){
        this.displayName = displayName;
    }

    @NonNull
    @Override
    public String toString() {
        return displayName;
    }
}
