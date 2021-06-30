package uk.gov.hmcts.fortifyclient;

public class FortifyClientMainApp {

    public static void main(String[] args) throws Exception {

        FortifyClientConfig config = FortifyClientConfig.getNewDefaultInstance();

        FortifyClient fortifyClient = new FortifyClient(config);

        ScanReport report = null;
        try {
            report = fortifyClient.requestScanAndGetResults();

            boolean failed = report.hasAnyIssuesAtOrAbove(config.getUnacceptableSeverity());

            if (failed) {
                System.out.println("Scan has been failed at severity : " + config.getUnacceptableSeverity());
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }

        System.out.println("Scan has been completed successfully. Scan Report: \n" + report);

    }
}
