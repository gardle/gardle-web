package com.gardle.config;

/**
 * Application constants.
 */
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^[_.@A-Za-z0-9-]*$";
    public static final String BIRTHDATE_REGEX = "^\\d\\d\\d\\d-\\d\\d-\\d\\d$";
    public static final String TELEPHONE_REGEX = "^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s\\./0-9]*$";

    public static final String SYSTEM_ACCOUNT = "system";
    public static final String DEFAULT_LANGUAGE = "de";
    public static final String ANONYMOUS_USER = "anonymoususer";

    private Constants() {
    }
}
