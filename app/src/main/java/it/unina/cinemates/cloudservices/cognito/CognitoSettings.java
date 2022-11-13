package it.unina.cinemates.cloudservices.cognito;
import android.content.Context;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Regions;

import it.unina.cinemates.BuildConfig;

public class CognitoSettings {
    private final String userPoolId;
    private final String clientId;
    private final String clientSecret;
    private final Regions cognitoRegion;

    private final Context context;

    // Costruttore
    public CognitoSettings(Context context){
        this.context = context;
        userPoolId = BuildConfig.USERPOOL_ID;
        clientId = BuildConfig.CLIENT_ID;
        clientSecret = BuildConfig.CLIENT_SECRET;
        cognitoRegion = Regions.EU_CENTRAL_1;
    }

    // Restituisce una istanza di UserPool Cognito
    public CognitoUserPool getUserPool() {
        //TODO forse andrebbe istanziata una sola volta a livello di activity ?
        return new CognitoUserPool(context, userPoolId, clientId, clientSecret, cognitoRegion);
    }
}
