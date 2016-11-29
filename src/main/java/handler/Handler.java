package handler;

import java.io.IOException;

/**
 * Created by pengan on 16-9-26.
 */
public interface Handler {
    void handle(byte[] data) throws IOException;
}
