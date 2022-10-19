package uk.gov.hmcts.fortifyclient;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.io.File;
import java.util.Objects;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.org.lidalia.slf4jext.Level.DEBUG;

class FolderZipperTest {
    TestLogger logger = TestLoggerFactory.getTestLogger(FolderZipper.class);
    FortifyClientConfig config;
    FortifyClient fortifyClient;
    FolderZipper zipper;

    @BeforeEach
    void setup() throws Exception {
        config = FortifyClientConfig.getNewDefaultInstance();
        fortifyClient = new FortifyClient(config);
        zipper = new FolderZipper();
    }

    @Test
    void should_successfully_zip() throws Exception {
        zipper.zip(new File("."), fortifyClient.getFortifyExportDirectory(), config.getExcludePatterns());
    }

    @Test
    void should_log_target_file_as_debug() throws Exception {
        zipper.zip(new File("."), fortifyClient.getFortifyExportDirectory(), config.getExcludePatterns());
        for (LoggingEvent logLevel : logger.getLoggingEvents()) {
            if (Objects.equals(logLevel.getMessage(), "File targeted : {}"))
                MatcherAssert.assertThat(logLevel.getLevel(), is(DEBUG));
            if (Objects.equals(logLevel.getMessage(), "File zipped : {}"))
                MatcherAssert.assertThat(logLevel.getLevel(), is(DEBUG));
            if (Objects.equals(logLevel.getMessage(), "File excluded : {}"))
                MatcherAssert.assertThat(logLevel.getLevel(), is(DEBUG));
        }
    }

    @Test
    void invalid_source_folder() throws Exception {
        IllegalArgumentException err = Assertions.assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                zipper.zip
                        (new File("/unknown_path/"), fortifyClient.getFortifyExportDirectory(), config.getExcludePatterns());
            }
        });
        assertTrue(err.getMessage().contains("Please provide a folder. Source : "));
    }

    @AfterEach
    public void clearLoggers() {
        TestLoggerFactory.clear();
        logger.clear();
    }

}
