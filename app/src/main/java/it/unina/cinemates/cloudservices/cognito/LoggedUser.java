package it.unina.cinemates.cloudservices.cognito;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoggedUser {

    private String username = null;
    private String email = null;
    private Integer idPhoto = null;
    private String cognitoToken = null;
    private String firebaseToken = null;
    private Integer toWatchListId = null;
    private Integer favoritesListId = null;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private static final LoggedUser instance = new LoggedUser();

    public static LoggedUser getInstance(){
        return instance;
    }

}
