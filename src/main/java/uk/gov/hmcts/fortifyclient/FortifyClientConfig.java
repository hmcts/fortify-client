package uk.gov.hmcts.fortifyclient;

import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

public class FortifyClientConfig {

    private final static String DEFAULT_CONFIG_RESOURCE = "fortify-client.properties";

    private final static String DEFAULT_CONFIG_FILE = "config/fortify-client.properties";

    private final Properties properties;

    private FortifyClientConfig(Properties properties) {
        this.properties = properties;
    }

    public static FortifyClientConfig getNewDefaultInstance() throws Exception {
        return FortifyClientConfig.fromDefaultResource().overwriteWithDefaultFile();
    }

    public String getRequired(String key) {
        return Validate.notNull(properties.getProperty(key), "Property key `%s` is required", key);
    }

    public static FortifyClientConfig fromDefaultResource() throws Exception {
        return fromResource(DEFAULT_CONFIG_RESOURCE);
    }

    public static FortifyClientConfig fromResource(String resourceName) throws Exception {

        Properties properties = new Properties();
        try (InputStream input = FortifyClientConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (input == null) {
                throw new Exception("Unable to find " + resourceName);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new Exception(e.getMessage(), e);
        }

        return new FortifyClientConfig(properties);
    }

    public FortifyClientConfig overwriteWithDefaultFile() throws Exception {
        return overwriteWithFileIfExists(DEFAULT_CONFIG_FILE);
    }

    public FortifyClientConfig overwriteWithFileIfExists(String fileName) throws Exception {
        File file = new File(fileName);
        if (file.exists()) {
            Properties propertiesInTheFile = new Properties();
            propertiesInTheFile.load(new FileInputStream(file));
            for (Object key : propertiesInTheFile.keySet()) {
                this.properties.setProperty((String) key, propertiesInTheFile.getProperty((String) key));
            }
        }
        return this;
    }

    public String getUsername() {
        return getValueFor(FortifyClientEnvironment.FORTIFY_USER_NAME, "fortify.client.username");
    }

    public String getValueFor(String envVar, String propertyKey) {
        String optionalVariable = FortifyClientEnvironment.getOptionalVariable(envVar);
        if (optionalVariable == null) {
            optionalVariable = properties.getProperty(propertyKey);
        }
        return optionalVariable;
    }

    public String getPassword() {
        return getValueFor(FortifyClientEnvironment.FORTIFY_PASSWORD, "fortify.client.password");
    }

    public String getReleaseId() {
        return getValueFor(FortifyClientEnvironment.FORTIFY_RELEASE_ID, "fortify.client.releaseId");
    }

    public Severity getUnacceptableSeverity() {
        return Severity.valueOf(getValueFor(FortifyClientEnvironment.FORTIFY_UNACCEPTABLE_SEVERITY,"fortify.client.unacceptableSeverity"));
    }

    public String[] getExcludePatterns() {
        String excludePattern = getValueFor(FortifyClientEnvironment.FORTIFY_EXCLUDE_PATTERN,
                "fortify.exclude.pattern");
        return excludePattern.split(Pattern.quote("|"));
    }

    public String getPortalUrl() {
        return getValueFor(FortifyClientEnvironment.FORTIFY_PORTAL_URL, "fortify.portal.url");
    }

}