package exception;


/**
 * Created by pengan on 16-9-28.
 */
public class ConfigException extends Exception {
    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(Throwable e) {
        super(e.getLocalizedMessage());
    }

    public ConfigException(String s, Exception e) {
        super(e.getLocalizedMessage() + s);
    }
}
