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

    final private static String name = "Create binary masks";
    private QuPathGUI quPathGUI;
    private Stage dialog;

    public BinaryMaskCreator(final QuPathGUI quPathGUI) {
        this.quPathGUI = quPathGUI;
    }

    @Override
    public void run() {
        try {
            if (dialog == null) {
                dialog = new Stage();
                if (quPathGUI != null)
                    dialog.initOwner(quPathGUI.getStage());
                dialog.setTitle(name);
            }
        } catch (NoClassDefFoundError e) {
            dialog = null;
        }
    }

    private void createBinaryMask() {
        ImageData<?> currentImageData = QPEx.getCurrentImageData();
        PathObjectHierarchy hierarchy = currentImageData.getHierarchy();
        ImageServer<?> server = currentImageData.getServer();

        List<PathObject> flattenedObjectList = hierarchy.getFlattenedObjectList(null);
        List<PathObject> annotations = flattenedObjectList.stream().filter(PathObject::isAnnotation).collect(Collectors.toList());

        double downSample = 4.0;
        String pathOutput = QPEx.buildFilePath(QPEx.PROJECT_BASE_DIR, "masks");
        QPEx.mkdirs(pathOutput);


        String imageExportType = "PNG";

        annotations.forEach(annotation ->
                saveImageAndMask(pathOutput, server, annotation, downSample, imageExportType)
        );

        System.out.println("Done!");

    }

    private void saveImageAndMask(String pathOutput, ImageServer server, PathObject pathObject,
                                  double downsample, String
                                          imageExportType) {
        ROI roi = pathObject.getROI();
        PathClass pathClass = pathObject.getPathClass();
        String classificationName = pathClass == null ? "None" : pathClass.toString();
        if (roi == null) {
            System.out.println("Warning! No ROI for object " + pathObject + " - cannot export corresponding region & mask");
            return;
        }

        RegionRequest region = RegionRequest.createInstance(server.getPath(), downsample, roi);

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
        g2d.scale(1.0 / downsample, 1.0 / downsample);
        g2d.translate(-region.getX(), -region.getY());
        g2d.fill(shape);
        g2d.dispose();

        if (imageExportType != null) {
            File fileImage = new File(pathOutput, name + '.' + imageExportType.toLowerCase());
            try {
                ImageIO.write(img, imageExportType, fileImage);
            } catch (Exception e) {
                System.out.println("Couldn't write fileImage.");
            }
        }

        File fileMask = new File(pathOutput, name + "-mask.png");
        try {
            ImageIO.write(imgMask, "PNG", fileMask);
        } catch (Exception e) {
            System.out.println("Couldn't write fileMask.");

        }
    }
}
