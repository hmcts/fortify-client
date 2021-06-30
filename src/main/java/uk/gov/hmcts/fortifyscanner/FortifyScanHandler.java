package uk.gov.hmcts.fortifyscanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FortifyScanHandler {
    private final ZipFolder zipFolder = new ZipFolder();
    private final PropertiesSingleton properties = PropertiesSingleton.getInstance();

    public boolean processScan(final String fortifyUserName, final String fortifyUserPassword, final String releaseId) throws IOException, InterruptedException {
        String osName = System.getProperty("os.name").toLowerCase();

        System.out.println(CommandRunner.run(osName.startsWith("windows") ? "cmd.exe /c dir" : "ls -lt"));
        System.out.println("================");
        String rootDirectory = CommandRunner.run(osName.startsWith("windows") ? "cmd.exe /c echo %cd%" : "pwd").trim();
        String zipFolderName = zipFolder.zip(rootDirectory);
        System.out.println("Folder zipped : " + zipFolderName);

        System.out.println(CommandRunner.run(osName.startsWith("windows") ? "cmd.exe /c dir" : "ls -lt"));
        System.out.println("================");

        System.out.println("Fortify scan is starting...");

        String fortifyScanJar = properties.getValue("fortify.jar.path");

        try {
            if (!Files.exists(Paths.get(fortifyScanJar))) {
                exportResource(fortifyScanJar);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("Fortify scan has been started");

        String[] fortifyScanArgs = new String[]{"java", "-jar", properties.getValue("fortify.jar.path"),
                "-portalurl", properties.getValue("fortify.portal.url"),
                "-apiurl", properties.getValue("fortify.api.url"),
                "-userCredentials", fortifyUserName, fortifyUserPassword,
                "-tenantCode", properties.getValue("fortify.tenant.code"),
                "-zipLocation", "./" + zipFolderName,
                "-releaseId", releaseId,
                "-entitlementPreferenceType", properties.getValue("fortify.entitlementpreferencetype"),
                "-inProgressScanActionType", properties.getValue("fortify.inprogressscanactiontype"),
                "-pollingInterval", properties.getValue("fortify.pollinginterval")
        };

        Process proc = Runtime.getRuntime().exec(fortifyScanArgs);
        proc.waitFor();
        InputStream in = proc.getInputStream();
        InputStream err = proc.getErrorStream();

        String res = IOUtils.toString(in, Charset.defaultCharset());
        System.out.println(res);

        System.out.println("Fortify scan call has been completed with exit code:" + proc.exitValue());

        if (proc.exitValue() != 0) {
            String errRes = IOUtils.toString(err, Charset.defaultCharset());
            System.out.println(errRes);

            return false;
        }


        String severity = EnvironmentVariable.getOptionalVariable(EnvironmentVariable.fortifySeverity);
        if (severity == null) {
            severity = properties.getValue("fortify.severity");
        }

        System.out.printf("Scan result is checking out for any vulnerability at %s severity.\n", severity);
        if (isScanFailed(Severity.valueOf(severity), res)) {
            System.out.println("Scan has been failed with severity : " + severity);
            return false;
        }

        return true;
    }

    private void exportResource(String resourceName) throws Exception {
        InputStream stream = FortifyScanHandler.class.getResourceAsStream("/" + resourceName);
        if (stream == null) {
            throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
        }

        File targetFile = new File(resourceName);
        FileUtils.copyInputStreamToFile(stream, targetFile);
    }

    private boolean isScanFailed(final Severity severity, final String result) {
        String regexPattern = "(?<=%s)(.*)(?=\\n|\\r)";

        switch (severity) {
            case CRITICAL:
                regexPattern = String.format(regexPattern, "criticals: ");
                break;
            case HIGH:
                regexPattern = String.format(regexPattern, "criticals: |highs: ");
                break;
            case MEDIUM:
                regexPattern = String.format(regexPattern, "criticals: |highs: |mediums: ");
                break;
            case LOW:
                regexPattern = String.format(regexPattern, "criticals: |highs: |mediums: |lows: ");
                break;
        }

        Matcher matcher = Pattern.compile(regexPattern).matcher(result);
        int matchResult = 0;
        while (matcher.find()) {
            matchResult += Integer.parseInt(matcher.group(1).trim());
        }

        return matchResult > 0;
    }
}
