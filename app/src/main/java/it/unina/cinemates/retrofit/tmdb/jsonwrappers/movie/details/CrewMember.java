package it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.details;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class CrewMember {
    @SerializedName("name")
    String name;
    @SerializedName("job")
    String job;
}
