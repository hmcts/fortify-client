package uk.gov.hmcts.fortifyclient;

public class FortifyClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FortifyClientException(String message) {
        super(message);
    }

}
