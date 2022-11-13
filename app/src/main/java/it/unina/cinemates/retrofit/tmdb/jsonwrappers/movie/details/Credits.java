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
public class Credits {
    @SerializedName("cast")
    List<CastMember> cast;
    @SerializedName("crew")
    List<CrewMember> crew;
}
