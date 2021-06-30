package uk.gov.hmcts.fortifyscanner;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FortifyScanHandlerTest {

   @Test
    void processScanWrongCredential() throws IOException, InterruptedException {
        FortifyScanHandler fortifyScanHandler = new FortifyScanHandler();
        boolean result = fortifyScanHandler.processScan("<user_name>", "<user_password>", "123");
        assertFalse(result);
    }
}