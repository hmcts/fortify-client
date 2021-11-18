package uk.gov.hmcts.fortifyclient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FortifyClientTest {
    FortifyClientConfig config;
    FortifyClient fortifyClient;

    @BeforeEach
    void setup() throws Exception {
        config = FortifyClientConfig.getNewDefaultInstance();
        fortifyClient = new FortifyClient(config);
    }

    @Test
    void should_fail_scan_with_wrong_credentials() throws Exception {
        Exception e = Assertions.assertThrows(FortifyClientException.class,
                fortifyClient::requestScanAndGetResults);
        assertTrue(e.getMessage().contains("code=401, message=Unauthorized"));
    }

    @Test
    void should_find_root_folder() throws Exception {
        File curDir = new File(".").getAbsoluteFile().getCanonicalFile();
        File root = fortifyClient.findRootDirectory(curDir, 0);
        assertNotNull(root);
        assertNotNull(root.getParent());
    }

    @Test
    void createFortifyExportDirectory() throws Exception {
        File file = fortifyClient.getFortifyExportDirectory();
        assertTrue(file.exists());
        assertTrue(file.isDirectory());
    }

    @Test
    void checkFortifyPath() {
        config.getPortalUrl();
        String g = config.getValueFor(FortifyClientEnvironment.FORTIFY_PORTAL_URL, "fortify.portal.url");
        assertTrue(g.contains("fortify.com"));
    }
}
