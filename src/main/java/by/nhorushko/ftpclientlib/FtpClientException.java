package by.nhorushko.ftpclientlib;

public class FtpClientException extends RuntimeException {
    public FtpClientException(String message) {
        super(message);
    }

    public FtpClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public FtpClientException(Throwable cause) {
        super(cause);
    }
}
