package uk.gov.hmcts.fortifyclient;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScanReport {

    private final Map<Severity, Integer> counts;

    public ScanReport(Map<Severity, Integer> counts) {
        this.counts = counts;
    }

    public static ScanReport fromConsoleReport(final String consoleReport) {
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

        return new ScanReport(counts);
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
}
