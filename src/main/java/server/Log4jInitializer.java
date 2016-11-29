package server;

import org.apache.log4j.LogManager;
import org.apache.log4j.helpers.FileWatchdog;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.xml.DOMConfigurator;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pengan on 16-9-8.
 */
public final class Log4jInitializer {

    private static final String format = "yyyy-MM-dd HH:mm:ss";

    public static void configureAndWatch(String filename, long delay) {
        XMLWatchdog xdog = new XMLWatchdog(filename);
        xdog.setName("Log4jWatchdog");
        xdog.setDelay(delay);
        xdog.start();
    }

    private static final class XMLWatchdog extends FileWatchdog {

        public XMLWatchdog(String filename) {
            super(filename);
        }

        @Override
        public void doOnChange() {
            new DOMConfigurator().doConfigure(filename, LogManager.getLoggerRepository());
            LogLog.warn(new SimpleDateFormat(format).format(new Date()) + " [" + filename + "] load completed.");
        }
    }

}
