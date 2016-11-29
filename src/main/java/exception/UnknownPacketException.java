package exception;

/**
 * Created by pengan on 16-10-9.
 */
public class UnknownPacketException extends RuntimeException {
    private static final long serialVersionUID = 3152986441780514147L;

    public UnknownPacketException() {
        super();
    }

    public UnknownPacketException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownPacketException(String message) {
        super(message);
    }

    public UnknownPacketException(Throwable cause) {
        super(cause);
    }

}
