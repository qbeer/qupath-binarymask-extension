package qupath.extensions.masker;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.scripting.QPEx;

public class LockAnnotations implements PathCommand {

    private LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

    LockAnnotations() {
        FileAppender fileAppender = new FileAppender();
        fileAppender.setContext(loggerContext);
        fileAppender.setName("debug-appender");
        // set the file name
        fileAppender.setFile(QPEx.PROJECT_BASE_DIR + "debug.log");

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%r %thread %level - %msg%n");
        encoder.start();

        fileAppender.setEncoder(encoder);
        fileAppender.start();

        // attach the rolling file appender to the logger of your choice
        Logger logbackLogger = loggerContext.getLogger("binaryMaskLogger");
        logbackLogger.addAppender(fileAppender);
    }

    @Override
    public void run() {
        try {
            PathObjectHierarchy hierarchy = QPEx.getCurrentImageData().getHierarchy();
            hierarchy.getFlattenedObjectList(null).stream().filter(PathObject::isAnnotation).forEach(annotation -> ((PathAnnotationObject) annotation).setLocked(true));
        } catch (Exception e) {
            loggerContext.getLogger("binaryMaskLogger").error("Error while locking annotations! Please select an image!\t" + e.getMessage());
            throw new IllegalArgumentException("Error while locking annotations! Please select an image!");
        }
    }
}
