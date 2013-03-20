package com.breakersoft.plow.util;

import java.util.Collection;
import java.util.UUID;

public final class PlowUtils {

	public static boolean isUuid(String s) {
		try {
			UUID.fromString(s);
			return true;
		}
		catch (IllegalArgumentException e) {

		}
		return false;
	}

	public static boolean isValid(Collection<?> c) {
		if (c == null) {
			return false;
		}
		return !c.isEmpty();
	}
}
