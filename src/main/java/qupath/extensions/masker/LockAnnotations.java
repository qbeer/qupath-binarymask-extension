package qupath.extensions.masker;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.scripting.QPEx;

import java.io.File;

public class LockAnnotations implements PathCommand {

    private static Logger logger = Loggerutils.getLOGGER("", ""); // logger is already defined here

    @Override
    public void run() {
        try {
            PathObjectHierarchy hierarchy = QPEx.getCurrentImageData().getHierarchy();
            hierarchy.getFlattenedObjectList(null).stream().filter(PathObject::isAnnotation).forEach(annotation -> ((PathAnnotationObject) annotation).setLocked(true));
        } catch (Exception e) {
            logger.error("Error while locking annotations! Please select an image!");
            throw new IllegalArgumentException("Error while locking annotations! Please select an image!");
        }
    }
}
