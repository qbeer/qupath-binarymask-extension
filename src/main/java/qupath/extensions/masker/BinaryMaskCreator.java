package qupath.extensions.masker;

import javafx.stage.Stage;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.regions.RegionRequest;
import qupath.lib.roi.PathROIToolsAwt;
import qupath.lib.roi.interfaces.ROI;
import qupath.lib.scripting.QPEx;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class BinaryMaskCreator implements PathCommand {

    @Override
    public void run() {
        createBinaryMask();
    }

    private void createBinaryMask() {
        ImageData<?> currentImageData = QPEx.getCurrentImageData();
        PathObjectHierarchy hierarchy = currentImageData.getHierarchy();
        ImageServer<?> server = currentImageData.getServer();

        List<PathObject> flattenedObjectList = hierarchy.getFlattenedObjectList(null);
        List<PathObject> annotations = flattenedObjectList.stream().filter(PathObject::isAnnotation).collect(Collectors.toList());

        double downSample = 8.0;
        String pathOutput = QPEx.buildFilePath(QPEx.PROJECT_BASE_DIR, "masks");
        QPEx.mkdirs(pathOutput);

        annotations.forEach(annotation ->
                saveMask(pathOutput, server, annotation, downSample)
        );

    }

    private void saveMask(String pathOutput, ImageServer server, PathObject pathObject,
                          double downSample) {
        ROI roi = pathObject.getROI();
        PathClass pathClass = pathObject.getPathClass();
        String classificationName = pathClass == null ? "None" : pathClass.toString();
        if (roi == null) {
            System.out.println("Warning! No ROI for object " + pathObject + " - cannot export corresponding region & mask");
            return;
        }

        RegionRequest region = RegionRequest.createInstance(server.getPath(), downSample, roi);

        String name = String.format("%s_%s_(%.2f,%d,%d,%d,%d)",
                server.getShortServerName(),
                classificationName,
                region.getDownsample(),
                region.getX(),
                region.getY(),
                region.getWidth(),
                region.getHeight()
        );

        BufferedImage img = (BufferedImage) server.readBufferedImage(region);

        Shape shape = PathROIToolsAwt.getShape(roi);
        BufferedImage imgMask = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = imgMask.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.scale(1.0 / downSample, 1.0 / downSample);
        g2d.translate(-region.getX(), -region.getY());
        g2d.fill(shape);
        g2d.dispose();

        File fileMask = new File(pathOutput, name + "-mask.png");
        try {
            ImageIO.write(imgMask, "PNG", fileMask);
        } catch (Exception e) {
            System.out.println("Couldn't write fileMask.");

        }
    }
}
