package it.unina.cinemates.models;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("idPhoto")
    private Integer idPhoto;

    @SerializedName("tokenFirebase")
    private String tokenFirebase;

    @SerializedName("followersNumber")
    private Integer followersNumber;

    @SerializedName("followingNumber")
    private Integer followingNumber;

}
