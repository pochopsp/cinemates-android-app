package it.unina.cinemates.views.backend;

import com.google.gson.annotations.SerializedName;

import it.unina.cinemates.views.backend.enums.FollowRequestStatus;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UserResult {

    @SerializedName("username")
    private String username;

    @SerializedName("idPhoto")
    private Integer idPhoto;

    @SerializedName("status")
    private FollowRequestStatus followRequestStatus;

}


