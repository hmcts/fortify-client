package uk.gov.hmcts.fortifyclient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

class FortifyClientTest {

    @Test
    void should_fail_scan_with_wrong_credentials() throws Exception {
        FortifyClientConfig config = FortifyClientConfig.getNewDefaultInstance();
        FortifyClient fortifyClient = new FortifyClient(config);
        Exception e = Assertions.assertThrows(FortifyClientException.class,
                fortifyClient::requestScanAndGetResults);
        Assertions.assertTrue(e.getMessage().contains("code=401, message=Unauthorized"));
    }

    @Test
    void should_find_root_folder() throws Exception {
        FortifyClientConfig config = FortifyClientConfig.getNewDefaultInstance();
        FortifyClient fortifyClient = new FortifyClient(config);
        File curDir = new File(".").getAbsoluteFile().getCanonicalFile();
        File root = fortifyClient.findRootDirectory(curDir, 0);
        Assertions.assertNotNull(root);
        Assertions.assertNotNull(root.getParent());
    }

}
