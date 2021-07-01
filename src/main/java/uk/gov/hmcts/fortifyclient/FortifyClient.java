package uk.gov.hmcts.fortifyclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class FortifyClient {

    private final FortifyClientConfig configuration;

    public FortifyClient(FortifyClientConfig configuration) {
        this.configuration = configuration;
    }

    public ScanReport requestScanAndGetResults() throws Exception {

        exportFortifyJar();
        String zipFileName = buildZipFile();
        String[] fortifyScanArgs = buildScanArgs(zipFileName);

        log.info("About to request a Fortify scan for the zipped content of the repository...");
        Process proc = Runtime.getRuntime().exec(fortifyScanArgs);
        log.info("Fortify scan has been requested. Awaiting results...");
        proc.waitFor();
        InputStream in = proc.getInputStream();
        InputStream err = proc.getErrorStream();

        String consoleReport = IOUtils.toString(in, Charset.defaultCharset());

        log.info("Fortify scan call has been completed with exit code:" + proc.exitValue());
        log.info("Console report: " + consoleReport);

        if (proc.exitValue() != 0) {
            String errorReport = IOUtils.toString(err, Charset.defaultCharset());
            throw new FortifyClientException(errorReport);
        }

        return ScanReport.fromConsoleReport(consoleReport);
    }

    private String buildZipFile() throws Exception {
        String osName = System.getProperty("os.name").toLowerCase();

        log.debug(CommandRunner.run(osName.startsWith("windows") ? "cmd.exe /c dir" : "ls -lt"));
        String rootDirectory = findRootDirectory(CommandRunner.run(osName.startsWith("windows") ? "cmd.exe /c echo %cd%" : "pwd").trim(), circuitBreaker);
        log.debug("Root directory has been selected : " + rootDirectory);
        String zipFileName = new FolderZipper().zip(rootDirectory, configuration.getExcludePatterns());
        log.info("Folder zipped into file : " + zipFileName);
        log.debug(CommandRunner.run(osName.startsWith("windows") ? "cmd.exe /c dir" : "ls -lt"));

        return zipFileName;

    }

    int circuitBreaker = 250;

    private String findRootDirectory(final String rootDirectory, int circuitBreaker) {
        String tempRootDirectoryWithGithub = rootDirectory+File.separator+".github";
        String tempRootDirectoryWithGit = rootDirectory+File.separator+".git";

        if (Files.exists(Paths.get(tempRootDirectoryWithGit)) || Files.exists(Paths.get(tempRootDirectoryWithGithub))) {
            return rootDirectory;
        } else {
            int lastIndexOfSlash = rootDirectory.lastIndexOf(File.separator);
            if (lastIndexOfSlash == -1 || circuitBreaker <= 0) {
                throw new RuntimeException("Couldn't find the root directory!");
            }

            String reducedRootDirectory = rootDirectory.substring(0, lastIndexOfSlash);
            return findRootDirectory(reducedRootDirectory, --circuitBreaker);
        }
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
