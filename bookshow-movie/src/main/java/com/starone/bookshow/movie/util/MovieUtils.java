package com.starone.bookshow.movie.util;

public class MovieUtils {
    
    public static boolean isNullOrEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static boolean isNull(Object obj) {
		return obj == null;
	}

	public static boolean isInvalid(Long id) {
		return id == null || id <= 0;
	}
}
