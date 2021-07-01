package uk.gov.hmcts.fortifyclient;

public class FortifyClientEnvironment {

    public final static String FORTIFY_USER_NAME = "FORTIFY_USER_NAME";
    public final static String FORTIFY_PASSWORD = "FORTIFY_PASSWORD";
    public final static String FORTIFY_RELEASE_ID = "FORTIFY_RELEASE_ID";
    public final static String FORTIFY_UNACCEPTABLE_SEVERITY = "FORTIFY_UNACCEPTABLE_SEVERITY";
    public final static String FORTIFY_EXCLUDE_PATTERN = "FORTIFY_EXCLUDE_PATTERN";
    public final static String GIT_URL = "GIT_URL";

    public static String getOptionalVariable(String name) {
        return System.getenv(name);
    }

}
