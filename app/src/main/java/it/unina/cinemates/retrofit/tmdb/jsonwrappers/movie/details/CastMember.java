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
public class CastMember {

    @SerializedName("name")
    String name;
    @SerializedName("character")
    String character;
    @SerializedName("known_for_department")
    String job;
    @SerializedName("profile_path")
    String imagePath;

}
