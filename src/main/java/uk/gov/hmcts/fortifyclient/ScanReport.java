package uk.gov.hmcts.fortifyclient;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScanReport {

    private static final Logger log = LoggerFactory.getLogger(ScanReport.class);

    public static final File DEFAULT_HTML_FILE = new File(
            "." + File.separator + "Fortify Reports" + File.separator + "FortifyScanReport.html");

    private final Map<Severity, Integer> counts;
    private FortifyClientConfig clientConfig;

    public ScanReport(FortifyClientConfig clientConfig, Map<Severity, Integer> counts) {
        this.clientConfig = clientConfig;
        this.counts = counts;
    }

    public static ScanReport fromConsoleReport(
            final FortifyClientConfig clientConfig,
            final String consoleReport) {
        String regexPattern = "(?<=%ss: )(.*)(?=\\n|\\r)";
        Map<Severity, Integer> counts = new HashMap<>();

        for (Severity severity : Severity.values()) {
            Matcher matcher = Pattern.compile(String.format(regexPattern, severity.toString().toLowerCase())).matcher(consoleReport);
            while (matcher.find()) {
                int matchResult;
                matchResult = Integer.parseInt(matcher.group(1).trim());
                counts.put(severity, matchResult);
            }
        }

        return new ScanReport(clientConfig, counts);
    }

    public FortifyClientConfig getClientConfig() {
        return clientConfig;
    }

    public int getCountOf(Severity severity) {
        return counts.get(severity);
    }

    public boolean isSuccessful(Severity threshold) {
        return !hasAnyIssuesAtOrAbove(threshold);
    }

    public boolean hasAnyIssuesAtOrAbove(final Severity threshold) {
        switch (threshold) {
            case CRITICAL:
                return getCountOf(Severity.CRITICAL) > 0;
            case HIGH:
                return (getCountOf(Severity.CRITICAL) + getCountOf(Severity.HIGH)) > 0;
            case MEDIUM:
                return (getCountOf(Severity.CRITICAL) + getCountOf(Severity.HIGH) + getCountOf(Severity.MEDIUM)) > 0;
            case LOW:
                return (getCountOf(Severity.CRITICAL) + getCountOf(Severity.HIGH) + getCountOf(Severity.MEDIUM) + getCountOf(Severity.LOW)) > 0;
            default:
                throw new RuntimeException("Unsupported severity: " + threshold);
        }
    }

    @Override
    public String toString() {
        return "ScanReport{" +
                "counts=" + counts +
                '}';
    }

    public void printToDefaultHtml() throws Exception {
        printToHtml(DEFAULT_HTML_FILE);
    }

    public void printToHtml(File file) throws Exception {
        String fileContent = IOUtils.toString(
                this.getClass().getClassLoader()
                        .getResourceAsStream("ReportTemplate.html"),
                Charset.defaultCharset());
        fileContent = fileContent.replace("[Critical]", "" + getCountOf(Severity.CRITICAL));
        fileContent = fileContent.replace("[High]", "" + getCountOf(Severity.HIGH));
        fileContent = fileContent.replace("[Medium]", "" + getCountOf(Severity.MEDIUM));
        fileContent = fileContent.replace("[Low]", "" + getCountOf(Severity.LOW));
        fileContent = fileContent.replace("[PortalLink]",
                "" + clientConfig.getPortalUrl() + "/Releases/" + clientConfig.getReleaseId() + "/Overview");
        fileContent = fileContent.replace("[ReleaseId]", "" + clientConfig.getReleaseId());
        fileContent = fileContent.replace("[ExcludePatterns]", "" + Arrays.asList(clientConfig.getExcludePatterns()));
        fileContent = fileContent
                .replace("[UnacceptableSeverity]",
                "" + Arrays.asList(clientConfig.getUnacceptableSeverity()));
        fileContent = fileContent
                .replace("[AcceptableStatus]",
                        isSuccessful(clientConfig.getUnacceptableSeverity()) ? "Yes, successful." : "No, failed.");
        file = file.getAbsoluteFile().getCanonicalFile();
        if (file.exists())
            file.createNewFile();
        log.info("Generating report file {}...", file);
        FileUtils.write(file, fileContent, Charset.defaultCharset());
        log.info("Generated report file {}.", file);
    }
}
