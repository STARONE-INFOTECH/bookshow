package com.starone.bookshow.person.utils;

public final class PersonUtils {

    private PersonUtils(){
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static boolean isNullOrEmpty(String str){
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNull(Object obj){
        return obj == null;
    }

    public static boolean isInvalid(Long id){
        return id == null || id <= 0;
    }
}
