package qupath.extensions.masker;

import javafx.scene.Node;
import javafx.scene.control.Menu;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.OpenWebpageCommand;
import qupath.lib.gui.extensions.QuPathExtension;


public class BinaryMaskExtension implements QuPathExtension {

    @Override
    public void installExtension(QuPathGUI qupath) {
        Menu menu = qupath.getMenu("Extensions>Weka", true);
        QuPathGUI.addMenuItems(
                menu,
                QuPathGUI.createCommandAction(new OpenWebpageCommand(qupath, "http://www.cs.waikato.ac.nz/ml/weka/downloading.html"), "Download Weka (web)")
        );

        Menu menu2 = qupath.getMenu("Extensions>Mask", true);

        QuPathGUI.addMenuItems(menu2,
                QuPathGUI.createCommandAction(
                        new BinaryMaskCreator(qupath),
                        "Create all binary masks"));

        Menu menuClassify = qupath.getMenu("WTF", true);
        QuPathGUI.addMenuItems(
                menuClassify,
                QuPathGUI.createCommandAction(new BinaryMaskCreator(qupath), "Create detection classifier (Weka)")
        );
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
