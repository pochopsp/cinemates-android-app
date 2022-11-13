package it.unina.cinemates.cloudservices.cognito.login;

public enum LoginStatus {
    LOGGED_IN,
    LOGGED_OUT,
    WRONG_CREDENTIALS,
    ACCOUNT_NOT_CONFIRMED,
    SERVER_UNREACHABLE
}
