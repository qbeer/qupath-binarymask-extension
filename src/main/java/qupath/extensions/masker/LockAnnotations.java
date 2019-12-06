package qupath.extensions.masker;

import org.slf4j.Logger;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.scripting.QPEx;

import java.io.File;

public class LockAnnotations implements PathCommand {

    final File f = new File(BinaryMaskCreator.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    private Logger logger = LoggerUtils.getLOGGER("binaryMaskLogger",
            f.getAbsolutePath().replace(f.getName(), "") + "debug.log");

    @Override
    public void run() {
        try {
            PathObjectHierarchy hierarchy = QPEx.getCurrentImageData().getHierarchy();
            hierarchy.getFlattenedObjectList(null).stream().filter(PathObject::isAnnotation).forEach(annotation -> ((PathAnnotationObject) annotation).setLocked(true));
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while locking annotations! Please select an image!");
        }
    }
}
