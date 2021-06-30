package uk.gov.hmcts.fortifyclient;

import org.junit.jupiter.api.Test;

class FolderZipperTest {

    @Test
    void should_successfully_zip() throws Exception {

        FortifyClientConfig config = FortifyClientConfig.getNewDefaultInstance();

        String osName = System.getProperty("os.name").toLowerCase();
        String rootDirectory = CommandRunner.run(osName.startsWith("windows")?"cmd.exe /c echo %cd%":"pwd").trim();

        FolderZipper zipper = new FolderZipper();
        zipper.zip(rootDirectory, config.getExcludePatterns());
    }

}