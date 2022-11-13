package it.unina.cinemates.views.backend;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.unina.cinemates.views.backend.enums.FollowRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class UserProfile {

    @SerializedName("username")
    private String username;

    @SerializedName("bio")
    private String bio;

    @SerializedName("idPhoto")
    private Integer idPhoto;

    @SerializedName("followingNumber")
    private int followingNumber;

    @SerializedName("followersNumber")
    private int followersNumber;

    @SerializedName("followRequestStatus")
    private FollowRequestStatus followRequestStatus;

    @SerializedName("movieLists")
    private List<MovieListPreview> movieLists;
}
