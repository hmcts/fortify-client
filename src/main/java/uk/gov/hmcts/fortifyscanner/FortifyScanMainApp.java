package uk.gov.hmcts.fortifyscanner;

import java.io.IOException;

public class FortifyScanMainApp {

    public static void main(String[] args) throws IOException, InterruptedException {
        String fortifyUserName = EnvironmentVariable.getRequiredVariable(EnvironmentVariable.fortifyUserName);
        String fortifyUserPassword = EnvironmentVariable.getRequiredVariable(EnvironmentVariable.fortifyPassword);
        String releaseId = EnvironmentVariable.getRequiredVariable(EnvironmentVariable.fortifyReleaseId);

        FortifyScanHandler fortifyScanHandler = new FortifyScanHandler();
        boolean result = fortifyScanHandler.processScan(fortifyUserName, fortifyUserPassword, releaseId);
        if (!result) {
            System.exit(1);
        }

        System.out.println("Scan has been completed successfully!");
    }
}
