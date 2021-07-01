package uk.gov.hmcts.fortifyclient;

import org.junit.jupiter.api.Test;

import java.io.File;

class FolderZipperTest {

    @Test
    void should_successfully_zip() throws Exception {

        FortifyClientConfig config = FortifyClientConfig.getNewDefaultInstance();

        FolderZipper zipper = new FolderZipper();
        zipper.zip(new File("."), config.getExcludePatterns());
    }

}