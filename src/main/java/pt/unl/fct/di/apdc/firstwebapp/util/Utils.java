package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	
	// FIELD SIZES
	
	public static final int USERNAME_MIN_LENGTH = 3;
	public static final int USERNAME_MAX_LENGTH = 20;
	
	public static final int PASSWORD_MIN_LENGTH = 5;
	public static final int PASSWORD_MAX_LENGTH = 20;
	
	public static final int EMAIL_MAX_LENGTH_LABELS = 63;
	public static final int EMAIL_MAX_LENGTH_LOCAL = 64;
	public static final int EMAIL_MAX_LENGTH_DOMAIN = 255;
	
	public static final int NAME_MIN_LENGTH = 3;
	public static final int NAME_MAX_LENGTH = 64;
	
	public static final String SUCCESS = "SUCCESS";
	
	// ERROR MESSAGES GENERAL
	
	public static final String FIELDS_NULL = "At least one field is null.";
	public static final String FIELDS_EMPTY = "At least one field is empty.";
	public static final String PW_NO_MATCH = "Passwords don't match.";
	public static final String USER_DIDNT_LOG_IN = "This user has not logged in in the last 24 hours.";
	
	// ERROR MESSAGES USERNAME
	
	public static final String WRONG_LENGTH_USERNAME = "Username must be between " + USERNAME_MIN_LENGTH + " and " + USERNAME_MAX_LENGTH + " characters long.";
	public static final String USERNAME_IN_USE = "Username already in use.";
	public static final String USERNAME_NOT_EXISTS = "Username does not exist, please register.";
	public static final String BAD_USERNAME = "Please use only lower case letters, base ten Roman digits and the special characters (-_.) for the username";

	// ERROR MESSAGES PASSWORD
	
    public static final String WRONG_LENGTH_PASSWORD = "Password must be between " + PASSWORD_MIN_LENGTH + " and " + PASSWORD_MAX_LENGTH + " characters long.";
	public static final String PASSWORD_NO_SPECIAL = "Password must contain at least one special character (!@#$%^&*(),.?\\\":{}|<>).";
	public static final String PASSWORD_NO_CAPS = "Password must contain at least one Upper Case Letter.";
	public static final String PASSWORD_NO_LOWKEY = "Password must contain at least one lower case Letter.";
	public static final String PASSWORD_NO_NUMBER = "Password must contain at least one Base Ten Roman digit.";
	
	// ERROR MESSAGES EMAIL
	
    public static final String BAD_EMAIL = "Please provide a valid email.";
	public static final String EMAIL_LONG_LOCAL = "The local part of the email address is too long. Max lengh = " + EMAIL_MAX_LENGTH_LOCAL + ".";
    public static final String EMAIL_LONG_DOMAIN = "The domain part of the email address is too long. Max lengh = " + EMAIL_MAX_LENGTH_DOMAIN + ".";
    public static final String EMAIL_LONG_LABEL = "One or more labels in the domain part of the email address is too long. Max lengh = " + EMAIL_MAX_LENGTH_LABELS + " each.";
    
    // ERROR MESSAGES NAME
    
    public static final String WRONG_LENGTH_NAME = "Name must be between " + NAME_MIN_LENGTH + " and " + NAME_MAX_LENGTH + " characters long.";
	public static final String BAD_NAME = "Please use only letters and spaces for the name";
    
    public Utils() {}
    
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
		if (USERNAME_MIN_LENGTH > username.length() || USERNAME_MAX_LENGTH < username.length()) {
			return WRONG_LENGTH_USERNAME;
		}

		Pattern pattern = Pattern.compile("[a-z0-9[-_.]]+");
		Matcher matcher = pattern.matcher(username);
		if (!matcher.matches()) {
			return BAD_USERNAME;
		}
		return SUCCESS;
	}
	
	public static String isPasswordValid(String password) {
		
		if(password.length() < PASSWORD_MIN_LENGTH || password.length() > PASSWORD_MAX_LENGTH) {
			return WRONG_LENGTH_PASSWORD;
		}
		
		Pattern pattern1 = Pattern.compile("[A-Z]");
		Matcher matcher1 = pattern1.matcher(password);
		Pattern pattern2 = Pattern.compile("[a-z]");
		Matcher matcher2 = pattern2.matcher(password);
		Pattern pattern3 = Pattern.compile("[0-9]");
		Matcher matcher3 = pattern3.matcher(password);
		Pattern pattern4 = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
		Matcher matcher4 = pattern4.matcher(password);
		
		if (!matcher1.find()) {
			return PASSWORD_NO_CAPS;
		}
		if (!matcher2.find()) {
			return PASSWORD_NO_LOWKEY;
		}
		if (!matcher3.find()) {
			return PASSWORD_NO_NUMBER;
		}
		if (!matcher4.find()) {
			return PASSWORD_NO_SPECIAL;
		}
		return SUCCESS;
	}

	public static String isEmailValid(String mail) {
        Pattern pattern = Pattern.compile("^(?![.])[a-zA-Z0-9_.]*[a-zA-Z0-9][a-zA-Z0-9_.]*@[a-zA-Z0-9]+[.][a-zA-Z0-9]+",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(mail);
        String[] domains = mail.split("@");
        String[] labels = domains[1].split(".");
        boolean allLabelsUnder63 = true;
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].length() > EMAIL_MAX_LENGTH_LABELS) {
                allLabelsUnder63 = false;
            }
        }
        if (!matcher.matches()) {
            return BAD_EMAIL;
        }
        if (domains[0].length() > EMAIL_MAX_LENGTH_LOCAL) {
            return EMAIL_LONG_LOCAL;
        }
        if (domains[1].length() > EMAIL_MAX_LENGTH_DOMAIN) {
            return EMAIL_LONG_DOMAIN;
        }
        if (!allLabelsUnder63) {
            return EMAIL_LONG_LABEL;
        }

        return SUCCESS;
    }

	public static String isNameValid(String name) {
		if (NAME_MIN_LENGTH > name.length() || NAME_MAX_LENGTH < name.length()) {
			return WRONG_LENGTH_NAME;
		}

		Pattern pattern = Pattern.compile("[[a-z]+[ ]*]*", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(name);
		
		if (!matcher.matches()) {
			return BAD_NAME;
		}
		return SUCCESS;
	}


}
