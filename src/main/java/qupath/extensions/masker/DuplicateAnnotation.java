package qupath.extensions.masker;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.roi.interfaces.ROI;
import qupath.lib.scripting.QPEx;

public class DuplicateAnnotation implements PathCommand {

    @Override
    public void run() {
        duplicateSelectedAnnotation();
    }

    private void duplicateSelectedAnnotation() {
        PathObject selectedObject = QPEx.getSelectedObject();
        PathObjectHierarchy currentHierarchy = QPEx.getCurrentHierarchy();
        ROI roi = selectedObject.getROI();
        PathAnnotationObject annotation = new PathAnnotationObject(roi);
        currentHierarchy.addPathObject(annotation, false);
    }
}
