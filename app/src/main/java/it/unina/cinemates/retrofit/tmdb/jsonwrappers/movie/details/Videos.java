package it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.details;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Videos {
    @SerializedName("results")
    private List<TrailerKey> results;
}

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
class TrailerKey {
    @SerializedName("key")
    private String key;

    @Override
    public String toString() {
        return key;
    }
}