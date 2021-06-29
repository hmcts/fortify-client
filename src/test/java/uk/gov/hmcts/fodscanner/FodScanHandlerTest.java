package uk.gov.hmcts.fodscanner;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FodScanHandlerTest {

   //@Test
    void processScan() throws IOException, InterruptedException {
        FodScanHandler fodScanHandler = new FodScanHandler();
        boolean result = fodScanHandler.processScan("<user_name>", "<user_password>", "<release_id>");
        assertTrue(result);
    }
}