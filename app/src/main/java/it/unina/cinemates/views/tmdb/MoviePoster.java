package it.unina.cinemates.views.tmdb;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MoviePoster {

    @SerializedName("id")
    Integer id;

    @SerializedName("poster_path")
    String posterPath;
}
