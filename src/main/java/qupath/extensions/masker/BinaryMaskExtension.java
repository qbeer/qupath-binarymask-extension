package qupath.extensions.masker;

import javafx.scene.control.Menu;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.QuPathExtension;

public class BinaryMaskExtension implements QuPathExtension {

    @Override
    public void installExtension(QuPathGUI quPath) {
        Menu binaryMaskMenu = quPath.getMenu("CsabaiBio tools", true);

        QuPathGUI.addMenuItems(binaryMaskMenu,
                QuPathGUI.createCommandAction(
                        new BinaryMaskCreator(),
                        "Export annotations from current image"));

        QuPathGUI.addMenuItems(binaryMaskMenu,
                QuPathGUI.createCommandAction(
                        new DuplicateAnnotation(),
                        "Duplicate selected annotation", null,
                        new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN)));
    }

    @Override
    public String getName() {
        return "CsabaiBio tools for SOTE-ELTE collaboration";
    }

    @Override
    public String getDescription() {
        return "Creates a binary mask from the labeled annotations into the mask" +
                " directory of the project and also able to duplicate selected annotations.";
    }
}
