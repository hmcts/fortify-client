package uk.gov.hmcts.fortifyclient;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FortifyClientTest {

    @Test
    void should_find_root_folder() throws Exception {
        FortifyClientConfig config = FortifyClientConfig.getNewDefaultInstance();
        FortifyClient fortifyClient = new FortifyClient(config);
        File curDir = new File(".").getAbsoluteFile().getCanonicalFile();
        File root = fortifyClient.findRootDirectory(curDir, 0);
        assertNotNull(root);
        assertNotNull(root.getParent());
    }

    @Test
    void createFortifyExportDirectory() throws Exception {
        FortifyClientConfig config = FortifyClientConfig.getNewDefaultInstance();
        FortifyClient fortifyClient = new FortifyClient(config);
        File file = fortifyClient.getFortifyExportDirectory();
        assertTrue(file.exists());
        assertTrue(file.isDirectory());
    }
}
