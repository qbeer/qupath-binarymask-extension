package qupath.extensions.masker;

import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathObject;
import qupath.lib.scripting.QPEx;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RemoveSmallNoneTypeAnnotations implements PathCommand {
    @Override
    public void run() {
        try {
            removeSmallNoneTypeAnnotations();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while removing small annotations! Please select an image if none is selected!");
        }
    }

    private void removeSmallNoneTypeAnnotations() {
        Predicate<PathObject> removeSmall = annotation -> {
            PathAnnotationObject annotationObject = (PathAnnotationObject) annotation;
            double approximateArea = annotationObject.getROI().getBoundsHeight() * annotation.getROI().getBoundsWidth();
            return approximateArea < 10000 && Objects.isNull(annotation.getPathClass());
        };
        List<PathObject> annotations = QPEx
                .getAnnotationObjects()
                .stream()
                .filter(PathObject::isAnnotation)
                .filter(removeSmall).collect(Collectors.toList());

        QPEx.removeObjects(annotations, true);

    }
}
