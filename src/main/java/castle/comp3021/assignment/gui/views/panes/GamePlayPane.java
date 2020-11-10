package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.DurationTimer;
import castle.comp3021.assignment.gui.FXJesonMor;
import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.gui.controllers.AudioManager;
import castle.comp3021.assignment.gui.controllers.ResourceLoader;
import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;
import castle.comp3021.assignment.gui.views.GameplayInfoPane;
import castle.comp3021.assignment.gui.views.SideMenuVBox;
import castle.comp3021.assignment.piece.*;
import castle.comp3021.assignment.protocol.*;
import castle.comp3021.assignment.gui.controllers.Renderer;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.Optional;

/**
 * This class implements the main playing function of Jeson Mor
 * The necessary components have been already defined (e.g., topBar, title, buttons).
 * Basic functions:
 *      - Start game and play, update scores
 *      - Restart the game
 *      - Return to main menu
 *      - Elapsed Timer (ticking from 00:00 -> 00:01 -> 00:02 -> ...)
 *          - The format is defined in {@link GameplayInfoPane#formatTime(int)}
 * Requirement:
 *      - The game should be initialized by configuration passed from {@link GamePane}, instead of the default configuration
 *      - The information of the game (including scores, current player name, ect.) is implemented in {@link GameplayInfoPane}
 *      - The center canvas (defined as gamePlayCanvas) should be disabled when current player is computer
 * Bonus:
 *      - A countdown timer (if this is implemented, then elapsed timer can be either kept or removed)
 *      - The format of countdown timer is defined in {@link GameplayInfoPane#countdownFormat(int)}
 *      - If one player runs out of time of each round {@link DurationTimer#getDefaultEachRound()}, then the player loses the game.
 * Hint:
 *      - You may find it useful to synchronize javafx UI-thread using {@link javafx.application.Platform#runLater}
 */

public class GamePlayPane extends BasePane {
    @NotNull
    private final HBox topBar = new HBox(20);
    @NotNull
    private final SideMenuVBox leftContainer = new SideMenuVBox();
    @NotNull
    private final Label title = new Label("Jeson Mor");
    @NotNull
    private final Text parameterText = new Text();
    @NotNull
    private final BigButton returnButton = new BigButton("Return");
    @NotNull
    private final BigButton startButton = new BigButton("Start");
    @NotNull
    private final BigButton restartButton = new BigButton("Restart");
    @NotNull
    private final BigVBox centerContainer = new BigVBox();
    @NotNull
    private final Label historyLabel = new Label("History");

    @NotNull
    private final TextArea historyFiled = new TextArea("");
    @NotNull
    private final ScrollPane scrollPane = new ScrollPane();


    /**
     * time passed in seconds
     * Hint:
     *      - Bind it to time passed in {@link GameplayInfoPane}
     */
    private final IntegerProperty ticksElapsed = new SimpleIntegerProperty();

    @NotNull
    private final Canvas gamePlayCanvas = new Canvas(300,200);

    private GameplayInfoPane infoPane = null;

    /**
     * You can add more necessary variable here.
     * Hint:
     *      - the passed in {@link FXJesonMor}
     *      - other global variable you want to note down.
     */
    FXJesonMor jesonMor = null;
    int srcX;
    int srcY;
    boolean confirmEnd;
    boolean winnerExist;
    Player playerCurrent = null;
    Move lastValidMove = null;


    public GamePlayPane() {
        connectComponents();
        styleComponents();
        setCallbacks();
    }

    /**
     * Components are added, adjust it by your own choice
     */
    @Override
    void connectComponents() {
        topBar.getChildren().add(title);
        historyFiled.setEditable(false);
        historyFiled.setPrefSize(100,200);
        historyFiled.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        leftContainer.getChildren().addAll(parameterText,historyLabel, historyFiled,startButton,restartButton,returnButton);
        setTop(topBar);
        setLeft(leftContainer);
        setCenter(centerContainer);
    }

    /**
     * style of title and scrollPane have been set up, no need to add more
     */
    @Override
    void styleComponents() {
        title.getStyleClass().add("head-size");
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(ViewConfig.WIDTH / 4.0, ViewConfig.HEIGHT / 3.0 );
        scrollPane.setContent(historyFiled);
    }

    /**
     * The listeners are added here.
     */
    @Override
    void setCallbacks() {
        startButton.setOnAction(e -> startGame());
        restartButton.setOnAction(e -> onRestartButtonClick());
        gamePlayCanvas.setOnMousePressed(this::onCanvasPressed);
        gamePlayCanvas.setOnMouseDragged(this::onCanvasDragged);
        gamePlayCanvas.setOnMouseReleased(this::onCanvasReleased);
        returnButton.setOnAction(e -> doQuitToMenuAction());
        //TODO
    }

    /**
     * Set up necessary initialization.
     * Hint:
     *      - Set buttons enable/disable
     *          - Start button: enable
     *          - restart button: disable
     *      - This function can be invoked before {@link GamePlayPane#startGame()} for setting up
     *
     * @param fxJesonMor pass in an instance of {@link FXJesonMor}
     */
    void initializeGame(@NotNull FXJesonMor fxJesonMor) {
        jesonMor = fxJesonMor;
        jesonMor.getConfiguration().setAllInitialPieces();

        startButton.setDisable(false);
        restartButton.setDisable(true);
        parameterText.setText(getParamters(jesonMor.getConfiguration()));

        double edgeSize = ResourceLoader.getImage('l').getHeight()*jesonMor.getConfiguration().getSize();
        gamePlayCanvas.setHeight(edgeSize);
        gamePlayCanvas.setWidth(edgeSize);
        jesonMor.renderBoard(gamePlayCanvas);

        centerContainer.getChildren().clear();
        infoPane = new GameplayInfoPane(jesonMor.getPlayer1Score(),jesonMor.getPlayer2Score(),jesonMor.getCurPlayerName(),ticksElapsed);

        centerContainer.getChildren().add(gamePlayCanvas);
        centerContainer.getChildren().add(infoPane);

        disnableCanvas();

        fxJesonMor.addOnTickHandler(new Runnable() {
            private IntegerProperty ticksProperty;
            {
                this.ticksProperty = ticksElapsed;
            }
            @Override public void run() {
                ticksElapsed.setValue(ticksElapsed.getValue()+1);
            }});
    }

    private String getParamters(Configuration configuration) {
        return "Paramters:\n\n" +
                "Size of board: " + configuration.getSize() + "\n" +
                "Num of protection moves: " + configuration.getNumMovesProtection()  + "\n" +
                "Player " + configuration.getPlayers()[0].getName() + (configuration.isFirstPlayerHuman()?"(human)":"(computer)") + "\n" +
                "Player " + configuration.getPlayers()[1].getName() + (configuration.isSecondPlayerHuman()?"(human)":"(computer)");
    }

    /**
     * enable canvas clickable
     */
    private void enableCanvas(){
        gamePlayCanvas.setDisable(false);
    }

    /**
     * disable canvas clickable
     */
    private void disnableCanvas(){
        gamePlayCanvas.setDisable(true);
    }

    /**
     * After click "start" button, everything will start from here
     * No explicit skeleton is given here.
     * Hint:
     *      - Give a carefully thought to how to activate next round of play
     *      - When a new {@link Move} is acquired, it needs to be check whether this move is valid.
     *          - If is valid, make the move, render the {@link GamePlayPane#gamePlayCanvas}
     *          - If is invalid, abort the move
     *          - Update score, add the move to {@link GamePlayPane#historyFiled}, also record the move
     *          - Move forward to next player
     *      - The player can be either computer or human, when the computer is playing, disable {@link GamePlayPane#gamePlayCanvas}
     *      - You can add a button to enable next move once current move finishes.
     *          - or you can add handler when mouse is released
     *          - or you can take advantage of timer to automatically change player. (Bonus)
     */
    public void startGame() {
        jesonMor.startCountdown();
        startButton.setDisable(true);
        enableCanvas();
        restartButton.setDisable(false);
        playerCurrent = jesonMor.getCurrentPlayer();
    }

    /**
     * Restart the game
     * Hint: end the current game and start a new game
     */
    private void onRestartButtonClick(){
        disnableCanvas();
        startButton.setDisable(false);
        restartButton.setDisable(true);
        jesonMor.getConfiguration().setAllInitialPieces();
        historyFiled.clear();
        initializeGame(new FXJesonMor(jesonMor.getConfiguration()));
    }

    /**
     * Add mouse pressed handler here.
     * Play click.mp3
     * draw a rectangle at clicked board tile to show which tile is selected
     * Hint:
     *      - Highlight the selected board cell using {@link Renderer#drawRectangle(GraphicsContext, double, double)}
     *      - Refer to {@link GamePlayPane#toBoardCoordinate(double)} for help
     * @param event mouse click
     */
    private void onCanvasPressed(MouseEvent event){
        gamePlayCanvas.getGraphicsContext2D().save();
        Renderer.drawRectangle(gamePlayCanvas.getGraphicsContext2D(), toBoardCoordinate(event.getX()),toBoardCoordinate(event.getY()));
        srcX = toBoardCoordinate(event.getX());
        srcY = toBoardCoordinate(event.getY());
    }

    /**
     * When mouse dragging, draw a path
     * Hint:
     *      - When mouse dragging, you can use {@link Renderer#drawOval(GraphicsContext, double, double)} to show the path
     *      - Refer to {@link GamePlayPane#toBoardCoordinate(double)} for help
     * @param event mouse position
     */
    private void onCanvasDragged(MouseEvent event){
        Renderer.drawOval(gamePlayCanvas.getGraphicsContext2D(), event.getX(),event.getY());
    }

    /**
     * Mouse release handler
     * Hint:
     *      - When mouse released, a {@link Move} is completed, you can either validate and make the move here, or somewhere else.
     *      - Refer to {@link GamePlayPane#toBoardCoordinate(double)} for help
     *      - If the piece has been successfully moved, play place.mp3 here (or somewhere else)
     * @param event mouse release
     */
    private void onCanvasReleased(MouseEvent event){
        gamePlayCanvas.getGraphicsContext2D().clearRect(0,0,gamePlayCanvas.getWidth(),gamePlayCanvas.getHeight());
        jesonMor.renderBoard(gamePlayCanvas);

        Player currPlayer = jesonMor.getCurrentPlayer();
        Move[] availableMoves = jesonMor.getAvailableMoves(jesonMor.getCurrentPlayer());
        Move movePending = new Move(srcX,srcY,toBoardCoordinate(event.getX()),toBoardCoordinate(event.getY()));
        Move lastValidMove = Arrays.stream(availableMoves).filter((move)->move.equals(movePending)).findFirst().orElse(null);
        String errorMsg = validateMove(jesonMor,movePending);
        if (errorMsg == null && lastValidMove != null){
            jesonMor.movePiece(lastValidMove);
            jesonMor.incMove();
            jesonMor.updateScore(currPlayer,jesonMor.getPiece(lastValidMove.getDestination()), lastValidMove);
            updateHistoryField(lastValidMove);
            checkWinner();
        } else {
            showInvalidMoveMsg(errorMsg);
        }

        jesonMor.renderBoard(gamePlayCanvas);

        currPlayer = jesonMor.getCurrentPlayer();
        jesonMor.getCurPlayerName().setValue(currPlayer.getName());

        if (currPlayer.getName().equals("White")){
            if (globalConfiguration.isFirstPlayerHuman()) {
                enableCanvas();
            }else{
                disnableCanvas();
                availableMoves = jesonMor.getAvailableMoves(currPlayer);
                lastValidMove = currPlayer.nextMove(jesonMor, availableMoves);
                if (lastValidMove != null){
                    jesonMor.movePiece(lastValidMove);
                    jesonMor.incMove();
                    jesonMor.updateScore(currPlayer,jesonMor.getPiece(lastValidMove.getDestination()), lastValidMove);
                    updateHistoryField(lastValidMove);
                    checkWinner();
                    // check winner
                } else {
                    // computer lose
                }
                jesonMor.renderBoard(gamePlayCanvas);
                enableCanvas();
                jesonMor.getCurPlayerName().setValue(currPlayer.getName());
            }
        }else{
            if (globalConfiguration.isSecondPlayerHuman()) {
                enableCanvas();
            }else{
                disnableCanvas();
                availableMoves = jesonMor.getAvailableMoves(currPlayer);
                lastValidMove = currPlayer.nextMove(jesonMor, availableMoves);
                if (lastValidMove != null){
                    jesonMor.movePiece(lastValidMove);
                    jesonMor.incMove();
                    jesonMor.updateScore(currPlayer,jesonMor.getPiece(lastValidMove.getDestination()), lastValidMove);
                    updateHistoryField(lastValidMove);
                    checkWinner();
                } else {
                    //TODO
                }
                jesonMor.renderBoard(gamePlayCanvas);
                enableCanvas();
                jesonMor.getCurPlayerName().setValue(currPlayer.getName());
            }
        }
    }

    private String validateMove(Game game, Move move) {
        Rule rules[];
        if (game.getPiece(move.getSource()).getLabel() == 'K'){
            rules = new Rule[]{
                    new OutOfBoundaryRule(),
                    new OccupiedRule(),
                    new VacantRule(),
                    new NilMoveRule(),
                    new FirstNMovesProtectionRule(game.getConfiguration().getNumMovesProtection()),
                    new KnightMoveRule(),
                    new KnightBlockRule(),
            };
        }else{
            rules = new Rule[]{
                    new OutOfBoundaryRule(),
                    new OccupiedRule(),
                    new VacantRule(),
                    new NilMoveRule(),
                    new FirstNMovesProtectionRule(game.getConfiguration().getNumMovesProtection()),
                    new ArcherMoveRule(),
            };
        }
        for (var rule :
                rules) {
            if (!rule.validate(game, move)) {
                return rule.getDescription();
            }
        }
        boolean isHumanPlayer = (game.getCurrentPlayer() == game.getConfiguration().getPlayers()[0] ? game.getConfiguration().isFirstPlayerHuman()  : game.getConfiguration().isSecondPlayerHuman());
        if (game.getPiece(move.getSource()).getPlayer() != game.getCurrentPlayer() || !isHumanPlayer){
            return "The piece you moved does not belong to you!";
        }
        return null;
    }

    /**
     * Creates a popup which tells the winner
     */
    private void createWinPopup(String winnerName){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Congratulations!");
        alert.setHeaderText("Confirm");
        alert.setContentText(winnerName+" wins!");

        alert.show();

        ButtonType btnType1 = new ButtonType("Start New Game");
        ButtonType btnType2 = new ButtonType("Export Move Records");
        ButtonType btnType3 = new ButtonType("Return to Main Menu");

        alert.getButtonTypes().setAll(btnType1, btnType2, btnType3);

        Optional result = alert.showAndWait();
        if (result.get() == btnType1) {
            startGame();
        } else if (result.get() == btnType2) {
            //TODO
        } else {
            endGame();
            doQuitToMenu();
        }
    }


    /**
     * check winner, if winner comes out, then play the win.mp3 and popup window.
     * The window has three options:
     *      - Start New Game: the same function as clicking "restart" button
     *      - Export Move Records: Using {@link castle.comp3021.assignment.protocol.io.Serializer} to write game's configuration to file
     *      - Return to Main menu, using {@link GamePlayPane#doQuitToMenuAction()}
     */
    private void checkWinner(){
        //TODO
    }

    /**
     * Popup a window showing invalid move information
     * @param errorMsg error string stating why this move is invalid
     */
    private void showInvalidMoveMsg(String errorMsg){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Move");
        alert.setHeaderText("Your movement is invalid due to follow reason(s):");
        alert.setContentText(errorMsg);
        alert.showAndWait();
    }

    /**
     * Before actually quit to main menu, popup a alert window to double check
     * Hint:
     *      - title: Confirm
     *      - HeaderText: Return to menu?
     *      - ContentText: Game progress will be lost.
     *      - Buttons: CANCEL and OK
     *  If click OK, then refer to {@link GamePlayPane#doQuitToMenu()}
     *  If click Cancle, than do nothing.
     */
    private void doQuitToMenuAction() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm");
        alert.setHeaderText("Return to menu?");
        alert.setContentText("Game progress will be lost");

        Optional result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            doQuitToMenu();
        }
    }

    /**
     * Update the move to the historyFiled
     * @param move the last move that has been made
     */
    private void updateHistoryField(Move move){
        historyFiled.appendText(move.getSource()+"->"+move.getDestination()+"\n");
    }

    /**
     * Go back to main menu
     * Hint: before quit, you need to end the game
     */
    private void doQuitToMenu() {
        endGame();
        SceneManager.getInstance().showPane(MainMenuPane.class);
    }

    /**
     * Converting a vertical or horizontal coordinate x to the coordinate in board
     * Hint:
     *      The pixel size of every piece is defined in {@link ViewConfig#PIECE_SIZE}
     * @param x coordinate of mouse click
     * @return the coordinate on board
     */
    private int toBoardCoordinate(double x){
        return (int)(x / ViewConfig.PIECE_SIZE);
    }

    /**
     * Handler of ending a game
     * Hint:
     *      - Clear the board, history text field
     *      - Reset buttons
     *      - Reset timer
     *
     */
    private void endGame() {
        globalConfiguration.boardReset();
        winnerExist = false;
        confirmEnd = true;
        globalConfiguration.getPlayers()[0].setScore(0);
        globalConfiguration.getPlayers()[1].setScore(0);
        jesonMor.resetGame();
        jesonMor.renderBoard(gamePlayCanvas);
        historyFiled.setText("");
        ticksElapsed.setValue(0);
        disnableCanvas();
        startButton.setDisable(false);
        restartButton.setDisable(true);
    }
}
