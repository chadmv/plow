package com.breakersoft.plow.util;

import java.util.UUID;

public class PlowUtils {

	public static boolean isUuid(String s) {
		try {
			UUID.fromString(s);
			return true;
		}
		catch (IllegalArgumentException e) {

		}
		return false;
	}
}
