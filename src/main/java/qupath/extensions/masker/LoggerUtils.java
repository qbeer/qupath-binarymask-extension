package qupath.extensions.masker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;

class LoggerUtils {

    private static Logger LOGGER = null;

    static Logger getLOGGER(String string, String file) {

        if (isNull(LoggerUtils.LOGGER)) {

            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            PatternLayoutEncoder ple = new PatternLayoutEncoder();

            ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
            ple.setContext(lc);
            ple.start();
            FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
            fileAppender.setFile(file);
            fileAppender.setEncoder(ple);
            fileAppender.setContext(lc);
            fileAppender.start();

            Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            logger.detachAndStopAllAppenders();
            logger.addAppender(fileAppender);
            logger.setLevel(Level.INFO);
            logger.setAdditive(true); /* set to true if root should log too */

            LoggerUtils.LOGGER = logger;

        }

        return LoggerUtils.LOGGER;
    }

}