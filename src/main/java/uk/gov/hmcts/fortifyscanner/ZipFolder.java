package uk.gov.hmcts.fortifyscanner;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFolder {
    private static final String folderRegex = ".*(\\\\|/)%s(\\\\|/).*";
    private static final String fileRegex = ".*\\.(%s)$";
    private final PropertiesSingleton properties = PropertiesSingleton.getInstance();

    public String zip(final String path) throws IOException {
        Path source = Paths.get(path.trim());

        if (!Files.isDirectory(source)) {
            throw new IOException("Please provide a folder. Source : " + path);
        }

        String zipFileName = EnvironmentVariable.getOptionalVariable(EnvironmentVariable.gitUrl);
        if (StringUtils.isNotBlank(zipFileName)) {
            zipFileName = zipFileName.substring(zipFileName.lastIndexOf("/") + 1, zipFileName.indexOf(".git"));
        } else {
            zipFileName = source.getFileName().toString() + ".zip";
        }

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            String[] excludePattern = properties.getValue("fortify.exclude.pattern").split(Pattern.quote("|"));

            String excludePatternEnvVar = EnvironmentVariable.getOptionalVariable(EnvironmentVariable.fortifyExcludePattern);
            if (excludePatternEnvVar != null) {
                excludePattern = excludePatternEnvVar.split(Pattern.quote("|"));
            }

            final String[] finalExcludePattern = excludePattern;

            System.out.println("The exclude pattern to be applied : " + Arrays.toString(finalExcludePattern));

            String finalZipFileName = zipFileName;
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    if (attributes.isSymbolicLink()) {
                        return FileVisitResult.CONTINUE;
                    }

                    try (FileInputStream fis = new FileInputStream(file.toFile())) {
                        Path targetFile = source.relativize(file);

                        System.out.printf("File targeted : %s%n", file);
                        if (!excludeFile(file.toString(), finalExcludePattern) && !targetFile.toString().contains(finalZipFileName)) {
                            zos.putNextEntry(new ZipEntry(targetFile.toString()));

                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, len);
                            }
                            zos.closeEntry();

                            System.out.printf("File zipped : %s%n", file);
                        } else {
                            System.out.printf("File excluded : %s%n", file);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    System.err.printf("Unable to zip : %s%n%s%n", file, exc);
                    return FileVisitResult.CONTINUE;
                }
            });

            return zipFileName;
        }
    }

    private boolean excludeFile(final String fileName, final String[] excludePattern) {
        for (String pattern : excludePattern) {
            if (pattern.charAt(0) == '/') {
                if (fileName.matches(String.format(folderRegex, pattern.replaceFirst("/", "")))) {
                    System.out.printf("File : %s matched with pattern : %s%n", fileName, pattern);
                    return true;
                }
            }

            if (pattern.startsWith("*.")) {
                if (fileName.matches(String.format(fileRegex, pattern.replace("*.", "")))) {
                    System.out.printf("File : %s matched with pattern : %s%n", fileName, pattern);
                    return true;
                }
            }
        }

        return false;
    }
}
