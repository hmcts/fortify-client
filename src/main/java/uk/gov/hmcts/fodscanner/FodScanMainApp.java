package uk.gov.hmcts.fodscanner;

import java.io.IOException;

public class FodScanMainApp {

    public static void main(String[] args) throws IOException, InterruptedException {
        String fortifyUserName = EnvironmentVariable.getRequiredVariable("FORTIFY_USER_NAME");
        String fortifyUserPassword = EnvironmentVariable.getRequiredVariable("FORTIFY_PASSWORD");
        String releaseId = EnvironmentVariable.getRequiredVariable("RELEASE_ID");

        FodScanHandler fodScanHandler = new FodScanHandler();
        boolean result = fodScanHandler.processScan(fortifyUserName, fortifyUserPassword, releaseId);
        if (!result) {
            System.exit(1);
        }

        System.out.println("Scan has been completed successfully!");
    }
}
