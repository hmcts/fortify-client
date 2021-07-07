package uk.gov.hmcts.fortifyclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class ScanReportTest {

    static FortifyClientConfig clientConfig = null;
    static {
        try {
            clientConfig = FortifyClientConfig.getNewDefaultInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void should_parseReport_return_successful_result() {
        String report = "Beginning upload\n" +
                "Upload Status - Bytes sent:89316\n" +
                "Scan 702323 uploaded successfully. Total bytes sent: 89316\n" +
                "Poll Status: In Progress\n" +
                "Poll Status: Completed\n" +
                "Number of criticals: 1\n" +
                "Number of highs: 1\n" +
                "Number of mediums: 1\n" +
                "Number of lows: 1\n" +
                "For application status details see the customer portal: \n" +
                "https://emea.fortify.com/Redirect/Releases/65430\n" +
                "Pass/Fail status: Passed\n" +
                "Retiring Token : Token Retired Successfully";
        ScanReport resultOfScanReport = ScanReport.fromConsoleReport(clientConfig, report);
        for (Severity severity : Severity.values()) {
            assertEquals(1, resultOfScanReport.getCountOf(severity));
        }
    }

    @Test
    void should_hasAnyIssuesAtOrAbove_return_checks() {
        Map<Severity, Integer> counts = new HashMap<>();
        ScanReport scanReport = new ScanReport(clientConfig, counts);
        counts.put(Severity.CRITICAL, 1);
        counts.put(Severity.HIGH, 0);
        counts.put(Severity.MEDIUM, 0);
        counts.put(Severity.LOW, 0);
        assertTrue(scanReport.hasAnyIssuesAtOrAbove(Severity.CRITICAL));
        assertTrue(scanReport.hasAnyIssuesAtOrAbove(Severity.HIGH));
        assertTrue(scanReport.hasAnyIssuesAtOrAbove(Severity.MEDIUM));
        assertTrue(scanReport.hasAnyIssuesAtOrAbove(Severity.LOW));
    }

    @Test
    void should_isSuccessful_return_check() {
        Map<Severity, Integer> counts = new HashMap<>();
        ScanReport scanReport = new ScanReport(clientConfig, counts);
        counts.put(Severity.CRITICAL, 0);
        counts.put(Severity.HIGH, 0);
        counts.put(Severity.MEDIUM, 1);
        counts.put(Severity.LOW, 0);

        assertTrue(scanReport.isSuccessful(Severity.CRITICAL));
        assertTrue(scanReport.isSuccessful(Severity.HIGH));
        assertFalse(scanReport.isSuccessful(Severity.MEDIUM));
        assertFalse(scanReport.isSuccessful(Severity.LOW));
    }

    @Test
    void should_generate_html() throws Exception {
        FortifyClientConfig config = FortifyClientConfig.getNewDefaultInstance();
        FortifyClient fortifyClient = new FortifyClient(config);

        Map<Severity, Integer> counts = new HashMap<>();
        ScanReport scanReport = new ScanReport(clientConfig, counts);
        counts.put(Severity.CRITICAL, 3);
        counts.put(Severity.HIGH, 5);
        counts.put(Severity.MEDIUM, 1);
        counts.put(Severity.LOW, 6);
        scanReport.printToDefaultHtml(fortifyClient.getFortifyExportDirectory());

    }
}