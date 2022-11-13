package it.unina.cinemates.cloudservices.cognito.signup;

public enum SignUpStatus {
    IDLE,
    EMAIL_TAKEN,
    USERNAME_TAKEN,
    SUCCESS,
    SERVER_UNREACHABLE,
    GENERIC_ERROR;
}
