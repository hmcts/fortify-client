package uk.gov.hmcts.fortifyscanner;

import org.apache.commons.lang3.Validate;

public class EnvironmentVariable {

    public final static String fortifyUserName = "FORTIFY_USER_NAME";
    public final static String fortifyPassword = "FORTIFY_PASSWORD";
    public final static String fortifyReleaseId = "FORTIFY_RELEASE_ID";
    public final static String fortifySeverity = "FORTIFY_SEVERITY";
    public final static String fortifyExcludePattern = "FORTIFY_EXCLUDE_PATTERN";

    public static String getRequiredVariable(String name) {
        return Validate.notNull(System.getenv(name), "Environment variable `%s` is required", name);
    }

    public static String getOptionalVariable(String name) {
        return System.getenv(name);
    }
}
