package uk.gov.hmcts.fortifyclient;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderZipper {

    private static final String FOLDER_REGEX = ".*(\\\\|/)%s(\\\\|/).*";
    private static final String FILE_REGEX = ".*\\.(%s)$";
    private static final String ZIP_FILE_SUFFIX = "_fortify_target.zip";

    private static final Logger log = LoggerFactory.getLogger(FolderZipper.class);

    public String zip(final File sourceFolder, final String[] excludePatterns) throws Exception {

        Path source = sourceFolder.toPath();

        if (!Files.isDirectory(source)) {
            throw new IllegalArgumentException("Please provide a folder. Source : " + sourceFolder);
        }

        String zipFileName = buildZipFileName(source);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName))) {

            log.info("The exclude pattern to be applied : {}", Arrays.toString(excludePatterns));

            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    if (attributes.isSymbolicLink()) {
                        return FileVisitResult.CONTINUE;
                    }

                    try (FileInputStream fis = new FileInputStream(file.toFile())) {
                        Path targetFile = source.relativize(file);

                        log.debug("File targeted : {}", file);
                        if (!excludeFile(file.toString(), excludePatterns)
                                && !targetFile.toString().contains(zipFileName)) {
                            zos.putNextEntry(new ZipEntry(targetFile.toString()));

                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, len);
                            }
                            zos.closeEntry();

                            log.debug("File zipped : {}", file);
                        } else {
                            log.debug("File excluded : {}", file);
                        }

                    } catch (IOException e) {
                        log.error("Error:", e);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    log.error("Unable to zip : {}", file, exc);
                    return FileVisitResult.CONTINUE;
                }
            });

            return zipFileName;
        }
    }

    private String buildZipFileName(Path sourcePath) {
        String zipFileName = FortifyClientEnvironment.getOptionalVariable(FortifyClientEnvironment.GIT_URL);
        if (StringUtils.isNotBlank(zipFileName)) {
            zipFileName = zipFileName.substring(zipFileName.lastIndexOf("/") + 1, zipFileName.indexOf(".git")) + ZIP_FILE_SUFFIX;
        } else {
            zipFileName = sourcePath.getFileName() + ZIP_FILE_SUFFIX;
        }

        return zipFileName;
    }

    private boolean excludeFile(final String fileName, final String[] excludePatterns) {
        for (String pattern : excludePatterns) {
            if (pattern.charAt(0) == '/') {
                if (fileName.matches(String.format(FOLDER_REGEX, pattern.replaceFirst("/", "")))) {
                    log.debug("File : {} matched with pattern : {}", fileName, pattern);
                    return true;
                }
            }

            if (pattern.startsWith("*.")) {
                if (fileName.matches(String.format(FILE_REGEX, pattern.replace("*.", "")))) {
                    log.debug("File : {} matched with pattern : {}", fileName, pattern);
                    return true;
                }
            }
        }

        return false;
    }
}
