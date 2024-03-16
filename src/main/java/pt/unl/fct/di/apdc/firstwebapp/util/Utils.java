package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	public static final String SUCCESS = "SUCCESS";
	public static final String NO_SPECIAL = "Password must contain at least one special character (!@#$%^&*(),.?\\\":{}|<>).";
	public static final String NO_CAPS = "Password must contain at least one Upper Case Letter.";
	public static final String NO_LOWKEY = "Password must contain at least one Lower Case Letter.";
	public static final String NO_NUMBER = "Password must contain at least one Base Ten Roman Digit.";
	public static final String SHORT_NAME = "Name must be at least 3 characters long";
	public static final String LONG_NAME = "Name must be at most 64 characters long";
	public static final String BAD_NAME = "Please use only letters and spaces for the name";
	public static final String SHORT_USERNAME = "Username must be at least 3 characters long";
	public static final String LONG_USERNAME = "Username must be at most 20 characters long";
	public static final String BAD_USERNAME = "Please use only lower case letters, digits and the special characters (-_.) for the username";
	public static final String BAD_EMAIL = "Please provide a valid email.";
	public static final String FIELDS_NULL = "At least one field is null.";
	public static final String FIELDS_EMPTY = "At least one field is empty.";
	public static final String PW_NO_MATCH = "Passwords don't match.";

	public static boolean isFieldNull(String field) {
		return field == null;
	}

	public static boolean isFieldEmpty(String field) {
		return field.isBlank();
	}

	public static boolean areFieldsEqual(String field1, String field2) {
		return field1.equals(field2);
	}
	
	public static String isUsernameValid(String username) {
		if (3 > username.length()) {
			return SHORT_USERNAME;
		}
		if (20 < username.length()) {
			return LONG_USERNAME;
		}
		Pattern pattern = Pattern.compile("[a-z0-9[-_.]]*[a-z0-9]+");
		Matcher matcher = pattern.matcher(username);
		if (!matcher.matches()) {
			return BAD_USERNAME;
		}
		return SUCCESS;
	}
	
	public static String isPasswordValid(String password) {
		Pattern pattern1 = Pattern.compile("[A-Z]");
		Matcher matcher1 = pattern1.matcher(password);
		Pattern pattern2 = Pattern.compile("[a-z]");
		Matcher matcher2 = pattern2.matcher(password);
		Pattern pattern3 = Pattern.compile("[0-9]");
		Matcher matcher3 = pattern3.matcher(password);
		Pattern pattern4 = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
		Matcher matcher4 = pattern4.matcher(password);
		if (!matcher1.find()) {
			return NO_CAPS;
		}
		if (!matcher2.find()) {
			return NO_LOWKEY;
		}
		if (!matcher3.find()) {
			return NO_NUMBER;
		}
		if (!matcher4.find()) {
			return NO_SPECIAL;
		}
		return SUCCESS;
	}

	public static String isEmailValid(String mail) {
		Pattern pattern = Pattern.compile("^(?![.])[a-zA-Z0-9_.]*[a-zA-Z0-9][a-zA-Z0-9_.]*@[a-zA-Z0-9]+[.][a-zA-Z0-9]+",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(mail);
		if (!matcher.matches()) {
			return BAD_EMAIL;
		}
		return SUCCESS;
	}

	public static String isNameValid(String name) {
		if (3 > name.length()) {
			return SHORT_NAME;
		}
		if (64 < name.length()) {
			return LONG_NAME;
		}

		Pattern pattern = Pattern.compile("[a-z[ ]]+", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(name);
		if (!matcher.matches()) {
			return BAD_NAME;
		}
		return SUCCESS;
	}


}
