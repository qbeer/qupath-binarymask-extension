package qupath.extensions.masker;

import javafx.scene.control.Menu;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.QuPathExtension;

public class BinaryMaskExtension implements QuPathExtension {

    @Override
    public void installExtension(QuPathGUI quPath) {
        Menu binaryMaskMenu = quPath.getMenu("Binary mask", true);

        QuPathGUI.addMenuItems(binaryMaskMenu,
                QuPathGUI.createCommandAction(
                        new BinaryMaskCreator(quPath),
                        "Run for current image"));

    }

    @Override
    public String getName() {
        return "Binary Mask Creator Script";
    }

    @Override
    public String getDescription() {
        return "Creates a binary mask from the labeled annotations into the mask directory of the project";
    }
}
