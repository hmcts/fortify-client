package uk.gov.hmcts.fortifyscanner;

import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesSingleton {
    private final static String PROPERTY_FILE_NAME = "fortify-application.properties";
    private Properties properties;

    private PropertiesSingleton() {
        try {
            properties = getPropertiesFromResource();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static class Initializer {
        static final PropertiesSingleton INSTANCE = new PropertiesSingleton();
    }

    public static PropertiesSingleton getInstance() {
        return Initializer.INSTANCE;
    }

    public String getValue(String key) {
        return Validate.notNull(properties.getProperty(key), "Property key `%s` is required", key);
    }

    private Properties getPropertiesFromResource() throws Exception {
        if (properties == null || properties.isEmpty()) {
            properties = new Properties();

            try (InputStream input = PropertiesSingleton.class.getClassLoader().getResourceAsStream(PROPERTY_FILE_NAME)) {
                if (input == null) {
                    throw new Exception("Unable to find " + PROPERTY_FILE_NAME);
                }

                properties.load(input);
            } catch (IOException e) {
                throw new Exception(e.getMessage(), e);
            }
        }

        return properties;
    }
}