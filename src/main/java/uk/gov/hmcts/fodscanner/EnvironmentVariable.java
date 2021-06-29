package uk.gov.hmcts.fodscanner;

import org.apache.commons.lang3.Validate;

public class EnvironmentVariable {

    public static String getRequiredVariable(String name) {
        return Validate.notNull(System.getenv(name), "Environment variable `%s` is required", name);
    }

    public static String getOptionalVariable(String name) {
        return System.getenv(name);
    }
}
