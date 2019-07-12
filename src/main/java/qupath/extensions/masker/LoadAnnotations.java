package qupath.extensions.masker;

import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import qupath.imagej.objects.ROIConverterIJ;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathObject;
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
import java.util.stream.Collectors;

public class LoadAnnotations implements PathCommand {
    @Override
    public void run() {
        try {
            loadAnnotationsFromMaskFilesInLatestDir();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Illegal argument exception : " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Please select and image that has save masks!");
        }
    }

    private void loadAnnotationsFromMaskFilesInLatestDir() {
        ImageData<?> currentImageData = QPEx.getCurrentImageData();
        PathObjectHierarchy hierarchy = currentImageData.getHierarchy();
        ImageServer<?> server = currentImageData.getServer();

        String correspondingMaskName = server.getShortServerName();
        String masksPath = QPEx.buildFilePath(QPEx.PROJECT_BASE_DIR, "masks/latest");
        File directoryOfMasks = new File(masksPath);

        if (Objects.isNull(directoryOfMasks.listFiles())) {
            throw new IllegalArgumentException("No masks in latest directory for the image.");
        }

        List<File> filteredFiles = new ArrayList<>();
        File[] files = Objects.nonNull(directoryOfMasks.listFiles()) ? directoryOfMasks.listFiles() : new File[]{};
        for (File file : files) {
            if (file.isFile() && file.getName().contains(correspondingMaskName) && file
                    .getName()
                    .endsWith("-mask.png")) {
                filteredFiles.add(file);
            }
        }

        List<PathObject> annotations = new ArrayList<>();
        for (File annotationMask : filteredFiles) {
            try {
                annotations.add(parseAnnotation(annotationMask, correspondingMaskName));
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot parse annotation from " +
                        annotationMask.getName() + " : " + e.getLocalizedMessage());
            }
        }

        hierarchy.addPathObjects(annotations, false);

    }

    private PathObject parseAnnotation(File file, String imageName) {
        // Read the image
        BufferedImage img;
        try {
            img = ImageIO.read(file);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read file : " + file.getName());
        }

        // Split the file name into parts: [Image name, Classification, Region]
        String[] parts = file.getName()
                .replace(imageName, "")
                .replace("-mask.png", "")
                .split("_");


        String classificationString = parts[0];

        String regionString = parts[1].replace("(", "").replace(")", "");

        String[] regionParts = regionString.split(",");
        double downsample = Double.parseDouble(regionParts[0]);
        int x = Integer.parseInt(regionParts[1]);
        int y = Integer.parseInt(regionParts[2]);

        // To create the ROI, travel into ImageJ
        ByteProcessor bp = new ByteProcessor(img);
        bp.setThreshold(127.5, Double.MAX_VALUE, ImageProcessor.NO_LUT_UPDATE);
        Roi roiIJ = new ThresholdToSelection().convert(bp);

        Calibration cal = new Calibration();
        cal.xOrigin = -x / downsample;
        cal.yOrigin = -y / downsample;
        ROI roi = ROIConverterIJ.convertToPathROI(roiIJ, cal, downsample, -1, 0, 0);

        // Create & return the object
        return new PathAnnotationObject(roi, PathClassFactory.getPathClass(classificationString));
    }

}