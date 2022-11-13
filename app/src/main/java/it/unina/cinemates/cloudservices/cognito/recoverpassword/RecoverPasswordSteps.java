package it.unina.cinemates.cloudservices.cognito.recoverpassword;

public enum RecoverPasswordSteps {
    IDLE,
    TOO_MANY_ATTEMPTS,
    EMAIL_SUCCESS,
    CONFIRMATION_CODE_SUCCESS,
    CONFIRMATION_CODE_FAILED,
    CHANGE_PASSWORD_SUCCESS
}
