package uk.gov.hmcts.fodscanner;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class ZipFolderTest {

    @Test
    void zip() throws IOException, InterruptedException {
        String osName = System.getProperty("os.name").toLowerCase();
        String rootDirectory = CommandRunner.run(osName.startsWith("windows")?"cmd.exe /c echo %cd%":"pwd").trim();

        ZipFolder zipFolder = new ZipFolder();
        zipFolder.zip(rootDirectory);
    }
}