package uk.gov.hmcts.fortifyclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FortifyClientMainApp {
    private static final Logger log = LoggerFactory.getLogger(FortifyClientMainApp.class);

    public static void main(String[] args) throws Exception {

        FortifyClientConfig config = FortifyClientConfig.getNewDefaultInstance();

        FortifyClient fortifyClient = new FortifyClient(config);

        ScanReport report = null;
        try {
            report = fortifyClient.requestScanAndGetResults();

            boolean failed = report.hasAnyIssuesAtOrAbove(config.getUnacceptableSeverity());

            if (failed) {
                log.error("Scan has been failed at severity : {}", config.getUnacceptableSeverity());
                System.exit(1);
            }
        } catch (Exception e) {
            log.error("Error:", e);
            e.printStackTrace();
            System.exit(2);
        }

        log.info("Scan has been completed successfully. Scan Report: {}\n", report);
    }
}
