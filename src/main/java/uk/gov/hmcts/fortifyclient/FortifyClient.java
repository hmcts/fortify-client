package uk.gov.hmcts.fortifyclient;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FortifyClient {

    private final FortifyClientConfig configuration;

    public FortifyClient(FortifyClientConfig configuration) {
        this.configuration = configuration;
    }

    public ScanReport requestScanAndGetResults() throws Exception {

        exportFortifyJar();
        String zipFileName = buildZipFile();
        String[] fortifyScanArgs = buildScanArgs(zipFileName);

        System.out.println("Fortify scan is starting...");
        Process proc = Runtime.getRuntime().exec(fortifyScanArgs);
        System.out.println("Fortify scan has been started...");
        proc.waitFor();
        InputStream in = proc.getInputStream();
        InputStream err = proc.getErrorStream();

        String consoleReport = IOUtils.toString(in, Charset.defaultCharset());

        System.out.println("Fortify scan call has been completed with exit code:" + proc.exitValue());
        System.out.println("Console report: " + consoleReport);

        if (proc.exitValue() != 0) {
            String errorReport = IOUtils.toString(err, Charset.defaultCharset());
            throw new FortifyClientException(errorReport);
        }

        return ScanReport.fromConsoleReport(consoleReport);
    }

    private String buildZipFile() throws Exception {
        String osName = System.getProperty("os.name").toLowerCase();

        System.out.println(CommandRunner.run(osName.startsWith("windows") ? "cmd.exe /c dir" : "ls -lt"));
        System.out.println("================");
        String rootDirectory = CommandRunner.run(osName.startsWith("windows") ? "cmd.exe /c echo %cd%" : "pwd").trim();
        String zipFileName = new FolderZipper().zip(rootDirectory, configuration.getExcludePatterns());
        System.out.println("Folder zipped into file : " + zipFileName);

        System.out.println(CommandRunner.run(osName.startsWith("windows") ? "cmd.exe /c dir" : "ls -lt"));
        System.out.println("================");

        return zipFileName;

    }

    private String[] buildScanArgs(String zipFileName) {
        return new String[] { "java", "-jar", configuration.getRequired("fortify.jar.path"),
                "-portalurl", configuration.getRequired("fortify.portal.url"),
                "-apiurl",
                configuration.getRequired("fortify.api.url"), "-userCredentials",
                configuration
                        .getUsername(),
                configuration.getPassword(), "-tenantCode", configuration.getRequired(
                        "fortify.tenant.code"),
                "-zipLocation", "./" + zipFileName, "-releaseId", configuration
                        .getReleaseId(),
                "-entitlementPreferenceType", configuration.getRequired("fortify.entitlementpreferencetype"),
                "-inProgressScanActionType", configuration.getRequired("fortify.inprogressscanactiontype"),
                "-pollingInterval", configuration.getRequired("fortify.pollinginterval") };
    }

    private synchronized void exportFortifyJar() throws Exception {
        String fortifyScanJar = configuration.getRequired("fortify.jar.path");
        if (!Files.exists(Paths.get(fortifyScanJar))) {
            exportResource(fortifyScanJar);
        }
    }

    private void exportResource(String resourceName) throws Exception {
        InputStream stream = FortifyClient.class.getResourceAsStream("/" + resourceName);
        if (stream == null) {
            throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
        }

        File targetFile = new File(resourceName);
        FileUtils.copyInputStreamToFile(stream, targetFile);
    }

}
