package uk.gov.hmcts.fortifyclient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FortifyClientTest {

    @Test
    void should_fail_scan_with_wrong_credentials() throws Exception {
        FortifyClientConfig config = FortifyClientConfig.getNewDefaultInstance();
        FortifyClient fortifyClient = new FortifyClient(config);
        Exception e = Assertions.assertThrows(FortifyClientException.class,
                fortifyClient::requestScanAndGetResults);
        Assertions.assertTrue(e.getMessage().contains("code=401, message=Unauthorized"));
    }

}
