package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.FXJesonMor;
import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.gui.controllers.AudioManager;
import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.protocol.Configuration;
import castle.comp3021.assignment.protocol.MoveRecord;
import castle.comp3021.assignment.protocol.Place;
import castle.comp3021.assignment.protocol.exception.InvalidConfigurationError;
import castle.comp3021.assignment.protocol.exception.InvalidGameException;
import castle.comp3021.assignment.protocol.io.Deserializer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


public class ValidationPane extends BasePane{
    @NotNull
    private final VBox leftContainer = new BigVBox();
    @NotNull
    private final BigVBox centerContainer = new BigVBox();
    @NotNull
    private final Label title = new Label("Jeson Mor");
    @NotNull
    private final Label explanation = new Label("Upload and validation the game history.");
    @NotNull
    private final Button loadButton = new BigButton("Load file");
    @NotNull
    private final Button validationButton = new BigButton("Validate");
    @NotNull
    private final Button replayButton = new BigButton("Replay");
    @NotNull
    private final Button returnButton = new BigButton("Return");

    private Canvas gamePlayCanvas = new Canvas();

    /**
     * store the loaded information
     */
    private Configuration loadedConfiguration;
    private Integer[] storedScores;
    private FXJesonMor loadedGame;
    private Place loadedcentralPlace;
    private ArrayList<MoveRecord> loadedMoveRecords = new ArrayList<>();

    private BooleanProperty isValid = new SimpleBooleanProperty(false);


    public ValidationPane() {
        connectComponents();
        styleComponents();
        setCallbacks();
    }

    @Override
    void connectComponents() {
        validationButton.setDisable(true);
        replayButton.setDisable(true);
        leftContainer.getChildren().addAll(title,explanation,loadButton,validationButton,replayButton,returnButton);
        centerContainer.getChildren().addAll(gamePlayCanvas);
        setLeft(leftContainer);
        setCenter(centerContainer);
    }

    @Override
    void styleComponents() {
        title.getStyleClass().add("head-size");
    }

    /**
     * Add callbacks to each buttons.
     * Initially, replay button is disabled, gamePlayCanvas is empty
     * When validation passed, replay button is enabled.
     */
    @Override
    void setCallbacks() {
        returnButton.setOnAction(e -> returnToMainMenu());
        replayButton.setOnAction(e -> onClickReplayButton());
        validationButton.setOnAction(e -> onClickValidationButton());
        loadButton.setOnAction(e -> loadFromFile());
    }

    /**
     * load From File and deserializer the game by two steps:
     *      - {@link ValidationPane#getTargetLoadFile}
     *      - {@link Deserializer}
     * Hint:
     *      - Get file from {@link ValidationPane#getTargetLoadFile}
     *      - Instantiate an instance of {@link Deserializer} using the file's path
     *      - Using {@link Deserializer#parseGame()}
     *      - Initialize {@link ValidationPane#loadedConfiguration}, {@link ValidationPane#loadedcentralPlace},
     *                   {@link ValidationPane#loadedGame}, {@link ValidationPane#loadedMoveRecords}
     *                   {@link ValidationPane#storedScores}
     * @return whether the file and information have been loaded successfully.
     */
    private boolean loadFromFile() {
        File selectedFile = getTargetLoadFile();
        if (selectedFile != null) {
            try {
                Deserializer deserializer = new Deserializer(selectedFile.toPath());
                deserializer.parseGame();
                loadedConfiguration = deserializer.getLoadedConfiguration();
                loadedcentralPlace = deserializer.getLoadedConfiguration().getCentralPlace();
                loadedMoveRecords = deserializer.getMoveRecords();
                storedScores = deserializer.getStoredScores();
                isValid.setValue(true);
                validationButton.setDisable(false);
                replayButton.setDisable(true);
            }catch (FileNotFoundException e){
                e.printStackTrace();
                isValid.setValue(false);
                return  false;
            } catch (InvalidConfigurationError | InvalidGameException err) {
                showErrorConfiguration(err.getMessage());
                isValid.setValue(false);
            }
        }
        return true;

    }

    /**
     * When click validation button, validate the loaded game configuration and move history
     * Hint:
     *      - if nothing loaded, call {@link ValidationPane#showErrorMsg}
     *      - if loaded, check loaded content by calling {@link ValidationPane#validateHistory}
     *      - When the loaded file has passed validation, the "replay" button is enabled.
     */
    private void onClickValidationButton(){
        if (isValid.getValue()){
            try {
                if (validateHistory()) {
                    validationButton.setDisable(true);
                    replayButton.setDisable(false);
                    passValidationWindow();
                } else {
                    showErrorConfiguration("Config error");
                }
            }catch (Exception e) {
                showErrorConfiguration(e.getMessage());
            }
        } else{
            showErrorMsg();
        }
    }

    /**
     * Display the history of recorded move.
     * Hint:
     *      - You can add a "next" button to render each move, or
     *      - Or you can refer to {@link Task} for implementation.
     */
    private void onClickReplayButton(){

        loadedConfiguration.setAllInitialPieces();
        loadedGame = new FXJesonMor(loadedConfiguration);

        gamePlayCanvas.setWidth(loadedGame.getConfiguration().getSize()*ViewConfig.PIECE_SIZE);
        gamePlayCanvas.setHeight(loadedGame.getConfiguration().getSize()*ViewConfig.PIECE_SIZE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (MoveRecord moveRecord : loadedMoveRecords)
                {
                    if(!isValid.getValue())
                        return;

                    loadedGame.movePiece(moveRecord.getMove());
                    loadedGame.renderBoard(gamePlayCanvas);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    /**
     * Validate the {@link ValidationPane#loadedConfiguration}, {@link ValidationPane#loadedcentralPlace},
     *              {@link ValidationPane#loadedGame}, {@link ValidationPane#loadedMoveRecords}
     *              {@link ValidationPane#storedScores}
     * Hint:
     *      - validate configuration of game
     *      - whether each move is valid
     *      - whether scores are correct
     */
    private boolean validateHistory(){
        // TODO
        return true;
    }

    /**
     * Popup window show error message
     * Hint:
     *      - title: Invalid configuration or game process!
     *      - HeaderText: Due to following reason(s):
     *      - ContentText: errorMsg
     * @param errorMsg error message
     */
    private void showErrorConfiguration(String errorMsg){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid configuration or game process!");
        alert.setHeaderText("Due to following reason(s):");
        alert.setContentText(errorMsg);
        alert.showAndWait();
    }

    /**
     * Pop up window to warn no record has been uploaded.
     * Hint:
     *      - title: Error!
     *      - ContentText: You haven't loaded a record, Please load first.
     */
    private void showErrorMsg(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error!");
        alert.setContentText("You haven't loaded a record, Please load first.");
        alert.showAndWait();
    }

    /**
     * Pop up window to show pass the validation
     * Hint:
     *     - title: Confirm
     *     - HeaderText: Pass validation!
     */
    private void passValidationWindow(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm");
        alert.setHeaderText("Pass validation!");
        alert.getButtonTypes().remove(1);
        alert.showAndWait();
    }

    /**
     * Return to Main menu
     * Hint:
     *  - Before return, clear the rendered canvas, and clear stored information
     */
    private void returnToMainMenu(){
        validationButton.setDisable(true);
        replayButton.setDisable(true);
        loadButton.setDisable(false);
        gamePlayCanvas.getGraphicsContext2D().clearRect(0,0,gamePlayCanvas.getWidth(),gamePlayCanvas.getHeight());
        SceneManager.getInstance().showPane(MainMenuPane.class);
    }


    /**
     * Prompts the user for the file to load.
     * <p>
     * Hint:
     * Use {@link FileChooser} and {@link FileChooser#setSelectedExtensionFilter(FileChooser.ExtensionFilter)}.
     *
     * @return {@link File} to load, or {@code null} if the operation is canceled.
     */
    @Nullable
    private File getTargetLoadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Plain Text", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(null);
        return selectedFile;
    }

}
