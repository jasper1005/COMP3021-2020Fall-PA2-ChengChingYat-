package castle.comp3021.assignment.gui.views.panes;
import castle.comp3021.assignment.gui.DurationTimer;
import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.gui.controllers.AudioManager;
import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;
import castle.comp3021.assignment.gui.views.NumberTextField;
import castle.comp3021.assignment.gui.views.SideMenuVBox;
import castle.comp3021.assignment.protocol.Configuration;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SettingPane extends BasePane {
    @NotNull
    private final Label title = new Label("Jeson Mor <Game Setting>");
    @NotNull
    private final Button saveButton = new BigButton("Save");
    @NotNull
    private final Button returnButton = new BigButton("Return");
    @NotNull
    private final Button isHumanPlayer1Button = new BigButton("Player 1: ");
    @NotNull
    private final Button isHumanPlayer2Button = new BigButton("Player 2: ");
    @NotNull
    private final Button toggleSoundButton = new BigButton("Sound FX: Enabled");

    @NotNull
    private final VBox leftContainer = new SideMenuVBox();

    @NotNull
    private final NumberTextField sizeFiled = new NumberTextField(String.valueOf(globalConfiguration.getSize()));

    @NotNull
    private final BorderPane sizeBox = new BorderPane(null, null, sizeFiled, null, new Label("Board size"));

    @NotNull
    private final NumberTextField durationField = new NumberTextField(String.valueOf(DurationTimer.getDefaultEachRound()));
    @NotNull
    private final BorderPane durationBox = new BorderPane(null, null, durationField, null,
            new Label("Max Duration (s)"));

    @NotNull
    private final NumberTextField numMovesProtectionField =
            new NumberTextField(String.valueOf(globalConfiguration.getNumMovesProtection()));
    @NotNull
    private final BorderPane numMovesProtectionBox = new BorderPane(null, null,
            numMovesProtectionField, null, new Label("Steps of protection"));

    @NotNull
    private final VBox centerContainer = new BigVBox();
    @NotNull
    private final TextArea infoText = new TextArea(ViewConfig.getAboutText());


    public SettingPane() {
        fillValues();
        connectComponents();
        styleComponents();
        setCallbacks();
    }

    /**
     * Add components to corresponding containers
     */
    @Override
    void connectComponents() {
        leftContainer.getChildren().addAll(title,sizeBox,numMovesProtectionBox,durationBox,
                isHumanPlayer1Button,isHumanPlayer2Button,toggleSoundButton,saveButton,returnButton);
        centerContainer.getChildren().add(infoText);
        setLeft(leftContainer);
        setCenter(centerContainer);
    }

    @Override
    void styleComponents() {
        infoText.getStyleClass().add("text-area");
        infoText.setEditable(false);
        infoText.setWrapText(true);
        infoText.setPrefHeight(ViewConfig.HEIGHT);
    }

    /**
     * Add handlers to buttons, textFields.
     * Hint:
     *  - Text of {@link SettingPane#isHumanPlayer1Button}, {@link SettingPane#isHumanPlayer2Button},
     *            {@link SettingPane#toggleSoundButton} should be changed accordingly
     *  - You may use:
     *      - {@link Configuration#isFirstPlayerHuman()},
     *      - {@link Configuration#isSecondPlayerHuman()},
     *      - {@link Configuration#setFirstPlayerHuman(boolean)}
     *      - {@link Configuration#isSecondPlayerHuman()},
     *      - {@link AudioManager#setEnabled(boolean)},
     *      - {@link AudioManager#isEnabled()},
     */
    @Override
    void setCallbacks() {
        isHumanPlayer1Button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(globalConfiguration.isFirstPlayerHuman()) {
                    globalConfiguration.setFirstPlayerHuman(false);
                    isHumanPlayer1Button.setText(player1Computer);
                } else {
                    globalConfiguration.setFirstPlayerHuman(true);
                    isHumanPlayer1Button.setText(player1HumanStr);
                }
            }
        });
        isHumanPlayer2Button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(globalConfiguration.isSecondPlayerHuman()) {
                    globalConfiguration.setSecondPlayerHuman(false);
                    isHumanPlayer2Button.setText(player2Computer);
                } else {
                    globalConfiguration.setSecondPlayerHuman(true);
                    isHumanPlayer2Button.setText(player2HumanStr);
                }
            }
        });
        toggleSoundButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(AudioManager.getInstance().isEnabled()){
                    AudioManager.getInstance().setEnabled(false);
                    toggleSoundButton.setText(soundDisable);
                } else {
                    AudioManager.getInstance().setEnabled(true);
                    toggleSoundButton.setText(soundEnable);
                }
            }
        });
        returnButton.setOnAction(e -> returnToMainMenu(false));
        saveButton.setOnAction(e -> returnToMainMenu(true));
    }

    /**
     * Fill in the default values for all editable fields.
     */
    private void fillValues() {
        sizeFiled.setText(String.valueOf(globalConfiguration.getSize()));
        numMovesProtectionField.setText(String.valueOf(globalConfiguration.getNumMovesProtection()));
        durationField.setText(String.valueOf(DurationTimer.getDefaultEachRound()));
        isHumanPlayer1Button.setText(globalConfiguration.isFirstPlayerHuman() ? player1HumanStr : player1Computer);
        isHumanPlayer2Button.setText(globalConfiguration.isSecondPlayerHuman() ? player2HumanStr : player2Computer);
        toggleSoundButton.setText(AudioManager.getInstance().isEnabled() ? soundEnable:soundDisable);
    }

    /**
     * Switches back to the {@link MainMenuPane}.
     *
     * @param writeBack Whether to save the values present in the text fields to their respective classes.
     */
    private void returnToMainMenu(final boolean writeBack) {
        if(writeBack){
            var size = getValue(sizeFiled,0);
            var numProtected = getValue(numMovesProtectionField,-1);
            var duration = getValue(durationField, 0);

            var option = validate(size,numProtected,duration);
            if(!option.isEmpty()) {
                popUp("Error!","Validation Failed",option.get());
                return;
            }

            globalConfiguration.setSize(sizeFiled.getValue());
            globalConfiguration.setNumMovesProtection(numMovesProtectionField.getValue());
            DurationTimer.setDefaultEachRound(durationField.getValue());
        }
        fillValues();
        SceneManager.getInstance().showPane(MainMenuPane.class);
    }

    /**
     * Validate the text fields
     * The useful msgs are predefined in {@link ViewConfig#MSG_BAD_SIZE_NUM}, etc.
     * @param size number in {@link SettingPane#sizeFiled}
     * @param numProtection number in {@link SettingPane#numMovesProtectionField}
     * @param duration number in {@link SettingPane#durationField}
     * @return If validation failed, {@link Optional} containing the reason message; An empty {@link Optional}
     *      * otherwise.
     */
    public static Optional<String> validate(int size, int numProtection, int duration) {
        var option = GamePane.validate(size,numProtection);
        if(!option.isEmpty())
            return option;

        if (duration <= 0) {
            return Optional.of(ViewConfig.MSG_NEG_DURATION);
        }

        return Optional.empty();
    }
}
