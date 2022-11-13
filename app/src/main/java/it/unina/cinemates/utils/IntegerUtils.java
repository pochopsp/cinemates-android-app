package it.unina.cinemates.utils;

import java.util.Random;

public class IntegerUtils {

    public static Integer randomInteger(){
        return Integer.parseInt(String.valueOf(new Random().nextInt()));
    }
}
