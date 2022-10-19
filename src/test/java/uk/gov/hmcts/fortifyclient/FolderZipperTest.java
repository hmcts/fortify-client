package uk.gov.hmcts.fortifyclient;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.LoggerFactory;

class FolderZipperTest {

    Logger logger = (Logger) LoggerFactory.getLogger(FolderZipper.class);
    ListAppender<ILoggingEvent> listAppender;
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
        setupLogging();
        zipper.zip(new File("."), fortifyClient.getFortifyExportDirectory(), config.getExcludePatterns());
        List<ILoggingEvent> logsList = listAppender.list;
        MatcherAssert.assertThat(logsList.get(0).getLevel(), is(Level.INFO));
        logsList.forEach(loggingEvent -> {
            if (loggingEvent.getMessage().startsWith("File targeted : {}")
                    || loggingEvent.getMessage().startsWith("File zipped : {}")
                    || loggingEvent.getMessage().startsWith("File excluded : {}")) {
                assertSame(Level.DEBUG, loggingEvent.getLevel());
            }
        });
    }

    @Test
    void invalid_source_folder() {
        IllegalArgumentException err = Assertions.assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                zipper.zip(new File("/unknown_path/"),
                            fortifyClient.getFortifyExportDirectory(), config.getExcludePatterns());
            }
        });
        assertTrue(err.getMessage().contains("Please provide a folder. Source : "));
    }

    private void setupLogging() {
        logger.detachAndStopAllAppenders();
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

}
