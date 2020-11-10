package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.views.NumberTextField;
import castle.comp3021.assignment.protocol.Configuration;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;

/**
 * Abstraction for a {@link BorderPane} which can act as the root of a {@link javafx.scene.Scene}.
 */
public abstract class BasePane extends BorderPane {
    protected final String player1HumanStr = "Player 1: Human";
    protected final String player2HumanStr = "Player 2: Human";
    protected final String player1Computer = "Player 1: Computer";
    protected final String player2Computer = "Player 2: Computer";
    protected final String soundEnable = "Sound FX: Enabled";
    protected final String soundDisable = "Sound FX: Disabled";

    protected int getValue(NumberTextField sizeFiled, int defaultVale) {
        int size = defaultVale;
        try {
            size = sizeFiled.getValue();
        } catch (NumberFormatException e) {
            size = defaultVale;
        }
        return size;
    }

    protected void popUp(String title,String header,String content){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Shared configuration by all panes
     */
    protected static Configuration globalConfiguration = new Configuration();
    /**
     * Connects all components into the {@link BorderPane}.
     */
    abstract void connectComponents();

    /**
     * Styles all components as required.
     */
    abstract void styleComponents();

    /**
     * Set callbacks for all interactive components.
     */
    abstract void setCallbacks();
}
