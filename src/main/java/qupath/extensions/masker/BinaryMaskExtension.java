package qupath.extensions.masker;

import javafx.scene.control.Menu;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.slf4j.Logger;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.QuPathExtension;

import java.io.File;

public class BinaryMaskExtension implements QuPathExtension {

    @Override
    public void installExtension(QuPathGUI quPath) {

        final File f = new File(BinaryMaskExtension.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        Logger logger = LoggerUtils.getLOGGER("binaryMaskLogger",
                f.getAbsolutePath().replace(f.getName(), "") + "debug.log");

        Menu binaryMaskMenu = quPath.getMenu("CsabaiBio tools", true);

        QuPathGUI.addMenuItems(binaryMaskMenu,
                QuPathGUI.createCommandAction(
                        new BinaryMaskCreator(),
                        "Export annotations from current image", null,
                        new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN)));

        QuPathGUI.addMenuItems(binaryMaskMenu,
                QuPathGUI.createCommandAction(
                        new LockAnnotations(),
                        "Lock annotations on current image", null,
                        new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN)));

        QuPathGUI.addMenuItems(binaryMaskMenu,
                QuPathGUI.createCommandAction(
                        new UnlockAnnotations(),
                        "Unlock annotations on current image", null,
                        new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN)));

        QuPathGUI.addMenuItems(binaryMaskMenu,
                QuPathGUI.createCommandAction(
                        new LoadAnnotations(),
                        "Load annotations", null,
                        new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN)));
    }

    @Override
    public String getName() {
        return "csabAIbio tools for SOTE-ELTE collaboration";
    }

    @Override
    public String getDescription() {
        return "Creates a binary mask from the labeled annotations into the mask" +
                " directory of the project and also able to duplicate selected annotations.";
    }
}
