package it.unina.cinemates.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringValidation {

    public static boolean isEmailValid(String email){
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPasswordValid(String password) {
        Pattern letter = Pattern.compile("[a-zA-z]");
        Pattern digit = Pattern.compile("[0-9]");
        Pattern special = Pattern.compile ("[!@#$%&*()_+=|<>?{}\\[\\]~-]");
        Pattern uppercase = Pattern.compile("[A-Z]");

        Matcher hasLetter = letter.matcher(password);
        Matcher hasDigit = digit.matcher(password);
        Matcher hasSpecial = special.matcher(password);
        Matcher hasAnUppercase = uppercase.matcher(password);

        return password.length() >= 8 && hasLetter.find() && hasDigit.find() && hasSpecial.find() && hasAnUppercase.find();
    }

    public static boolean isUsernameValid(String username) {
        return username.length() > 2 && username.trim().length() > 0 && username.length() <= 10 && username.matches("[a-zA-Z0-9]*");
    }

}
