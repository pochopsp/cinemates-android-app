package it.unina.cinemates.views.backend;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Locale;

import it.unina.cinemates.models.Movie;
import it.unina.cinemates.views.backend.enums.ListType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class MovieListFull {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private ListType type;

    @SerializedName("ownerId")
    private String ownerId;

    @SerializedName("moviesInList")
    private List<Movie> moviesInList;

    @Getter(AccessLevel.NONE)
    @SerializedName("averageRating")
    private Float averageRating;
    public Float getAverageRating(){
        return averageRating == null ? 0f : Float.parseFloat(String.format(Locale.US, "%.2g%n", averageRating).trim());
    }

    @SerializedName("commentsNumber")
    private int commentsNumber;

    @SerializedName("reactionsNumber")
    private int reactionsNumber;

    @SerializedName("requesterReaction")
    private LoggedUserReaction requesterReaction;
}
