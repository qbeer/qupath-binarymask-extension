package qupath.extensions.masker;

import org.slf4j.Logger;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.regions.RegionRequest;
import qupath.lib.roi.PathROIToolsAwt;
import qupath.lib.roi.interfaces.ROI;
import qupath.lib.scripting.QPEx;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BinaryMaskCreator implements PathCommand {

    final File f = new File(BinaryMaskCreator.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    private Logger logger = LoggerUtils.getLOGGER("binaryMaskLogger",
            f.getAbsolutePath().replace(f.getName(), "") + "debug.log");

    @Override
    public void run() {
        try {
            createBinaryMask();
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        } catch (UnsupportedOperationException e) {
            logger.error(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            logger.error("Error while creating a binary mask! Select an image if none is selected!");
            throw new IllegalArgumentException("Error while creating binary mask! Select an image if none is selected!");
        }
    }

    private void createBinaryMask() {
        ImageData<?> currentImageData = QPEx.getCurrentImageData();
        PathObjectHierarchy hierarchy = currentImageData.getHierarchy();
        ImageServer<?> server = currentImageData.getServer();

        List<PathObject> flattenedObjectList = hierarchy.getFlattenedObjectList(null);
        List<PathObject> annotations = flattenedObjectList.stream().filter(PathObject::isAnnotation).collect(Collectors.toList());

        annotations.forEach(this::checkForNoneTypeAnnotation);

        String pathOutput = QPEx.buildFilePath(QPEx.PROJECT_BASE_DIR, "masks");
        String latestPath = QPEx.buildFilePath(QPEx.PROJECT_BASE_DIR, "masks/latest");
        QPEx.mkdirs(pathOutput);
        QPEx.mkdirs(latestPath);

        Date date = new Date();
        Timestamp ts = new Timestamp(date.getTime());

        deleteCorrespondingFilesFromLatestDir(server.getShortServerName(), latestPath);

        annotations.forEach(annotation ->
                saveMask(pathOutput, latestPath, server, annotation, String.valueOf(ts.getNanos()))
        );

    }

    private void checkForNoneTypeAnnotation(PathObject annotation) {
        if (Objects.isNull(annotation.getPathClass())) {
            logger.error("There are annotations with no type!");
            throw new IllegalArgumentException("There are annotations with no type!");
        }
    }

    private void saveMask(String pathOutput, String latestPath, ImageServer server, PathObject pathObject,
                          String timestamp) {
        ROI roi = pathObject.getROI();
        String classificationName = pathObject.getPathClass().toString();
        if (roi == null) {
            return;
        }

        double downSample = adaptiveDownSampling(roi);

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

        File fileMask = new File(pathOutput, name + "-mask-" + timestamp + ".png");
        try {
            ImageIO.write(imgMask, "PNG", fileMask);
        } catch (Exception e) {
            logger.error("Could not write mask to file, image : " + server.getShortServerName() +
                    " class name : " + classificationName + " whole name : " + name);
            throw new UnsupportedOperationException("Couldn't write fileMask." + name + "\t");
        }

        File currentMask = new File(latestPath, name + "-mask.png");
        try {
            ImageIO.write(imgMask, "PNG", currentMask);
        } catch (Exception e) {
            logger.error("Could not write current mask to directory masks/latest! " + currentMask.getName());
            throw new UnsupportedOperationException(
                    "Couldn't write current mask to directory masks/latest.");
        }


    }

    private void deleteCorrespondingFilesFromLatestDir(String imageName, String latestPath) {
        File dir = new File(latestPath);
        File[] matches = dir.listFiles((dir1, name) -> name.startsWith(imageName) && name.endsWith(".png"));
        if (Objects.isNull(matches)) {
            return;
        }
        for (File match : matches) {
            if (!match.delete()) {
                logger.error("Could not delete corresponding files from masks/latest directory.");
                throw new UnsupportedOperationException("Couldn't delete corresponding files from masks/latest.");
            }
        }
    }

    private double adaptiveDownSampling(ROI roi) {
        if (roi.getBoundsWidth() > 180000 && roi.getBoundsHeight() > 180000) {
            return 64.0;
        } else if (roi.getBoundsWidth() > 120000 && roi.getBoundsWidth() > 120000) {
            return 32.0;
        } else if (roi.getBoundsWidth() > 60000 && roi.getBoundsHeight() > 60000) {
            return 16.0;
        } else if (roi.getBoundsWidth() > 20000 && roi.getBoundsHeight() > 20000) {
            return 8.0;
        }
        return 4.0;
    }
}
