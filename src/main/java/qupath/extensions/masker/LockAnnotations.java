package qupath.extensions.masker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.scripting.QPEx;

public class LockAnnotations implements PathCommand {

    protected static final Logger logger = LogManager.getRootLogger();

    @Override
    public void run() {
        try {
            PathObjectHierarchy hierarchy = QPEx.getCurrentImageData().getHierarchy();
            hierarchy.getFlattenedObjectList(null).stream().filter(PathObject::isAnnotation).forEach(annotation -> ((PathAnnotationObject) annotation).setLocked(true));
        } catch (Exception e) {
            logger.error("Error while locking annotations! Please select an image!\t" + e.getMessage());
            throw new IllegalArgumentException("Error while locking annotations! Please select an image!");
        }
    }
}
