package qupath.extensions.masker;

import org.slf4j.Logger;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.scripting.QPEx;

public class UnlockAnnotations implements PathCommand {

    private static Logger logger = LoggerUtils.getLOGGER("", ""); // logger is already defined here

    @Override
    public void run() {
        try {
            PathObjectHierarchy hierarchy = QPEx.getCurrentImageData().getHierarchy();
            hierarchy.getFlattenedObjectList(null).stream().filter(PathObject::isAnnotation).forEach(annotation -> ((PathAnnotationObject) annotation).setLocked(false));
        } catch (Exception e) {
            logger.error("Error while unlocking annotations! Please select an image!");
            throw new IllegalArgumentException("Error while unlocking annotations! Please select an image!");
        }
    }
}
