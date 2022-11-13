package it.unina.cinemates.views.backend.enums;

import androidx.annotation.NonNull;

public enum ReportType {

    Offensive("Offensive"),
    Racist("Racist"),
    HateSpeech("Hate speech"),
    Sexist("Sexist"),
    Spam("Spam");

    private final String displayName;

    ReportType(String s){
        displayName = s;
    }

    @NonNull
    @Override
    public String toString() {
        return displayName;
    }
}
