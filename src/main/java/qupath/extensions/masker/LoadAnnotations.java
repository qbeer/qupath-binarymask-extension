package qupath.extensions.masker;

import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import org.slf4j.Logger;
import qupath.imagej.objects.ROIConverterIJ;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.classes.PathClassFactory;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.roi.interfaces.ROI;
import qupath.lib.scripting.QPEx;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LoadAnnotations implements PathCommand {

    private final File f = new File(BinaryMaskCreator.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    private Logger logger = LoggerUtils.getLOGGER(f.getAbsolutePath().replace(f.getName(), "") + "debug.log");

    // Highgrade dysplasia must be before dyspalsia!
    private static List<String> classNames = Arrays.asList("highgrade_dysplasia", "inflammation", "lowgrade_dysplasia",
            "resection_edge", "adenocarcinoma", "suspicious_for_invasion",
            "tumor_necrosis", "lymphovascular_invasion", "artifact", "annotated");

    @Override
    public void run() {
        try {
            loadAnnotationsFromMaskFilesInLatestDir();
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            throw new IllegalArgumentException("Illegal argument exception : " + e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new IllegalArgumentException("Please select and image that has save masks!" + e.getMessage());
        }
    }

    private void loadAnnotationsFromMaskFilesInLatestDir() {
        ImageData<?> currentImageData = QPEx.getCurrentImageData();
        PathObjectHierarchy hierarchy = currentImageData.getHierarchy();
        ImageServer<?> server = currentImageData.getServer();

        String correspondingMaskName = server.getShortServerName();
        String masksPath = QPEx.buildFilePath(QPEx.PROJECT_BASE_DIR, "masks/latest");
        File directoryOfMasks = new File(masksPath);

        if (Objects.isNull(directoryOfMasks.listFiles()) || directoryOfMasks.listFiles().length == 0) {
            throw new IllegalArgumentException("No masks in latest directory for the image." + correspondingMaskName);
        }

        List<File> filteredFiles = new ArrayList<>();
        File[] files = Objects.nonNull(directoryOfMasks.listFiles()) ? directoryOfMasks.listFiles() : new File[]{};
        for (File file : files) {
            if (file.isFile() && (file.getName().split("__", -1)[0].equals(correspondingMaskName)) && file
                    .getName()
                    .endsWith("-mask.png")) {
                filteredFiles.add(file);
            }
        }

        List<PathObject> annotations = new ArrayList<>();
        for (File annotationMask : filteredFiles) {
            annotations.add(parseAnnotation(annotationMask, correspondingMaskName));
        }

        hierarchy.addPathObjects(annotations, false);

    }

    private PathObject parseAnnotation(File file, String imageName) {
        BufferedImage img;
        try {
            img = ImageIO.read(file);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read file : " + file.getName());
        }

        // Remove a "_" with the image name to not split
        // an empty string for the first part.
        String classificationNameAndLocation = file.getName()
                .replace(imageName + "__", "")
                .replace("-mask.png", "");

        // Classification name
        String classificationString = null;
        String location = null;

        for (String className : classNames) {
            if (classificationNameAndLocation.contains(className)) {
                classificationString = className;
                location = classificationNameAndLocation.replace(className + "__", "");
                break;
            }
        }

        // The format is (downSample, x, y, boundingWidth, boundingHeight)
        String regionString = location.replace("(", "").replace(")", "");
        String[] regionParts = regionString.split(",");

        double downSample = Double.parseDouble(regionParts[0]);
        int x = Integer.parseInt(regionParts[1]);
        int y = Integer.parseInt(regionParts[2]);

        // ROI with ImageJ
        ByteProcessor bp = new ByteProcessor(img);
        bp.setThreshold(127.5, Double.MAX_VALUE, ImageProcessor.NO_LUT_UPDATE);
        Roi roiIJ = new ThresholdToSelection().convert(bp);

        Calibration cal = new Calibration();
        cal.xOrigin = -x / downSample;
        cal.yOrigin = -y / downSample;
        ROI roi = ROIConverterIJ.convertToPathROI(roiIJ, cal, downSample, -1, 0, 0);

        // Set the class name properly
        PathClass pathClass = PathClassFactory.getPathClass(classificationString);

        return new PathAnnotationObject(roi, pathClass);
    }

}