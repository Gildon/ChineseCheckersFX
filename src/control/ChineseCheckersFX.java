package control;

import control.game.GameController;
import control.game.SceneViewerController;
import control.menu.MainMenuController;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import view.gamescene.*;
import view.newgamescene.NewGameController;

public class ChineseCheckersFX extends Application {
    
    private static final String SCENE_VIEWER_FXML_PATH = "/control/game/SceneViewerFXML.fxml";
    private static final String MAIN_MENU_SCENE_FXML_PATH = "/control/menu/MainMenu.fxml";
    private static final String NEW_GAME_SCENE_FXML_PATH = "/view/newgamescene/NewGame.fxml";
    
    private Stage primaryStage;
    private GameController game;
    private AnchorPane vistaHolder;
    private BoardAndPlayersSceneHolder gameScene;
    private PlayersController playerViewController;
    private boolean succeedingJumpMove = false;
    private int originRow, originCol;

    @Override
    public void start(Stage primaryStage) {
        
        this.primaryStage = primaryStage;
        Parent sceneViewer = loadSceneViewer();
        insertMainGameMenuToSceneViewer();
        
        Scene scene = new Scene(sceneViewer, 900, 600);
        game = new GameController();
        gameScene = new BoardAndPlayersSceneHolder();
        addCssToScene(scene);
        
        primaryStage.setTitle("Chinese Checkers 2.0");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addCssToScene(Scene scene) {
        String menuCss = ChineseCheckersFX.class.getResource("/resources/styles/menu.css").toExternalForm();
        String css = ChineseCheckersFX.class.getResource("/resources/styles/styles.css").toExternalForm();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(menuCss);
        scene.getStylesheets().add(css);
    }
    
    private SceneViewerController getSceneController(FXMLLoader fxmlLoader) {
        SceneViewerController svc = (SceneViewerController) fxmlLoader.getController();
        return svc;
    }
    
    private void setupMenuListeners(FXMLLoader fxmlLoader) {
        MainMenuController menuController = (MainMenuController) fxmlLoader.getController();
        addNewGameInitiatedListener(menuController);
        addLoadGameInitiatedListener(menuController);
    }
    
    private Node getPane(FXMLLoader fxmlLoader) throws IOException {
        return (Node) fxmlLoader.load(fxmlLoader.getLocation().openStream());
    }
    
    private void setVista(Pane pane) {
        vistaHolder.getChildren().setAll(pane);
    }
    
    // unused
    public static void main(String[] args) {
        launch(args);
    }
    
    private FXMLLoader getFXMLLoader(String fxml) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource(fxml));
        return fxmlLoader;
    }

    public static ArrayList<Circle> getAllNodes(AnchorPane boardPane) {
        ArrayList<Circle> nodes = new ArrayList<>();
        addAllDescendents(boardPane, nodes);
        return nodes;
    }

    private static void addAllDescendents(AnchorPane boardPane, ArrayList<Circle> nodes) {
        for (Node node : boardPane.getChildrenUnmodifiable())
            nodes.add((Circle)node);
    }

    private Parent loadSceneViewer() {
        FXMLLoader fxmlLoader = getFXMLLoader(SCENE_VIEWER_FXML_PATH);
        Parent root;
        try {
            root = (Parent) getPane(fxmlLoader);
            vistaHolder = getSceneController(fxmlLoader).getVistaHolder();
            setupReturnToMenuListener(getSceneController(fxmlLoader));
            return root;
        } catch (IOException e) { return null; }
    }

    private void insertMainGameMenuToSceneViewer() {
        try { 
            FXMLLoader menuLoader = getFXMLLoader(MAIN_MENU_SCENE_FXML_PATH);
            AnchorPane mainMenu = (AnchorPane) getPane(menuLoader);
            setVista(mainMenu);
            setupMenuListeners(menuLoader);
        } catch (Exception e) { }
    }
    
    private void setupNewGame(NewGameController ngc) {
        ngc.isAllDataEntered().addListener((src, oldVal, newVal)-> {
            if(newVal){
                game.startNewGame(ngc.getNumPlayers(), ngc.getNumOfColors(), ngc.getHumanNames());
                initiateGame();
                if(game.getPlayers().get(game.getCurrPlayer()).getType() == model.PlayerType.COMPUTER)
                    doComputerTurn();
            }  
        });   
    }

    private void addNewGameInitiatedListener(MainMenuController menuController) {
        menuController.isStartedNewGame().addListener((source, oldValue, newValue) -> {
            if (newValue) {
                try {
                    FXMLLoader newGameLoader = getFXMLLoader(NEW_GAME_SCENE_FXML_PATH);
                    AnchorPane newGamePane = (AnchorPane) getPane(newGameLoader);
                    setVista(newGamePane);
                    setupNewGame(newGameLoader.getController());
                    primaryStage.getScene().getStylesheets().remove(0);
                } catch (Exception e) 
                { System.out.println(e.getMessage()); }
            }
        });
    }

    private void addLoadGameInitiatedListener(MainMenuController menuController) {
        menuController.isLoadedGame().addListener((source, oldValue, newValue) -> {
            if (newValue) {
                try {
                    final FileChooser fileChooser = new FileChooser();
                    configureFileChooser(fileChooser, "Select a save file");
                    File file = fileChooser.showOpenDialog(primaryStage);
                    if (file == null) {
                        menuController.resetLoadGameListener();
                        return;
                    }
                    doLoadGame(file.getAbsolutePath(), menuController);
                    menuController.resetLoadGameListener();
                } catch (Exception e) 
                { System.out.println(e.getMessage()); }
            }
        });
    }
    
    private void doLoadGame(String path, MainMenuController menuController) {
        Task<Boolean> gameLoader = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
//                    Thread.sleep(1000);
                    return game.loadGame(path);
                } catch (Exception e) {
                    return false;
                }
            }
        };
        gameLoader.setOnSucceeded((WorkerStateEvent event) -> {
            vistaHolder.setDisable(false);
            if (gameLoader.getValue()) {
                primaryStage.getScene().getStylesheets().remove(0);
                initiateGame();
                if(game.getPlayers().get(game.getCurrPlayer()).getType() == model.PlayerType.COMPUTER)
                    doComputerTurn();
            }
            else
                addLoadFailedMessage(menuController);
        });
        vistaHolder.setDisable(true);
        Thread loader = new Thread(gameLoader);
        loader.start();
    }
    
    private void addLoadFailedMessage(MainMenuController menuController) {
        Pane menuPane = menuController.getPane();
        Label failMessage = new Label("Failed to load game");
        failMessage.setTextFill(Paint.valueOf("red"));
        failMessage.setLayoutX(385);
        failMessage.setLayoutY(420);
        menuPane.getChildren().add(failMessage);
    }
    
    private void initiateGame() {
        AnchorPane boardPane = new AnchorPane();
        setupMarbleClickListeners((ArrayList<Marble>)Board.renderBoard(boardPane, game.board));
        gameScene.setBoard(boardPane);
        playerViewController = new PlayersController();
        playerViewController.buildPlayersListGridPane(game.getPlayers());
        gameScene.setPlayers(playerViewController.getScene());
        setupSaveGameListener();
        setupSaveGameAsListener();
        setupAbandonGameListener();
        playerViewController.setCurrPlayerIndicator(1);
        setVista(gameScene.getScene());
    }
    
    private void setupMarbleClickListeners(ArrayList<Marble> marbles) {
        for (Marble marble : marbles) {
            marble.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent) -> {
                playerViewController.hidePlayerMessage();
                Marble clickedMarble = (Marble)MouseEvent.getSource();
                Marble selectedMarble = gameScene.getSelectedMarble();
                doMarbleAction(selectedMarble, clickedMarble);
            });
        }
    }
    
    private void doGameMove(Marble from, Marble to) {
        int fromRow = from.getRow(), fromCol = from.getCol();
        int toRow = to.getRow(), toCol = to.getCol();
        game.movePiece(fromRow, fromCol, toRow, toCol);
        view.gamescene.Board.updateMarbleAfterMove(gameScene.getBoardPane(), from, to);
        doEndGameActions();
    }
    
    private void doEndGameActions(){
        model.Player winningPlayer = game.playerHasWon();
        if (winningPlayer != null){
            displayVictoryMessage(winningPlayer);
        }
    }
    
    private void displayVictoryMessage(model.Player champion) {
        String name = champion.getName();
        Label victoryMessage = new Label(name + " has won!");
        victoryMessage.setStyle("-fx-font-size: 150;");
        victoryMessage.setLayoutX(130);
        victoryMessage.setLayoutY(200);
        victoryMessage.setRotate(35);
        vistaHolder.getChildren().add(victoryMessage);
        
        vistaHolder.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
            // deal with this differently?
            Parent sceneViewer = loadSceneViewer();
            insertMainGameMenuToSceneViewer();
            Scene scene = new Scene(sceneViewer, 900, 600);
            addCssToScene(scene);
            primaryStage.setScene(scene);
        });
    }
    
    private void endPlayerTurn() {
        game.advancePlayer();
        playerViewController.setCurrPlayerIndicator(game.getCurrPlayer() + 1);
        if(game.getPlayers().get(game.getCurrPlayer()).getType() == model.PlayerType.COMPUTER) {
            doDelayedComputerTurn();
        }
    }
    
    private void doDelayedComputerTurn() {
        Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                return null;
            }
        };
        sleeper.setOnSucceeded((WorkerStateEvent event) -> {
            doComputerTurn();
            gameScene.getBoardPane().setDisable(false);
        });
        gameScene.getBoardPane().setDisable(true);
        new Thread(sleeper).start();
    }
    
    private void endSucceedingMovesTurn(Marble selectedMarble, Marble clickedMarble) {
        succeedingJumpMove = false;
        selectedMarble.setStrokeWidth(1);
        gameScene.setSelectedMarble(null);
        clickedMarble.setStrokeWidth(1);
        endPlayerTurn();
    }
    
    private void doSucceedingJumpMove(Marble selectedMarble, Marble clickedMarble) {
        doGameMove(selectedMarble, clickedMarble);
        originRow = selectedMarble.getRow();
        originCol = selectedMarble.getCol();
        selectedMarble.setStrokeWidth(1);
        gameScene.setSelectedMarble(clickedMarble);
        clickedMarble.setStrokeWidth(5);
    }
    
    private void checkInitiallySelectedMarble(Marble clickedMarble) {
        if (game.isCurrPlayerPiece(clickedMarble.getRow(), clickedMarble.getCol())) {
            gameScene.setSelectedMarble(clickedMarble);
            clickedMarble.setStrokeWidth(5);
        }
        else
            playerViewController.setPlayerMessage("This is not your piece");
    }
    
    private void doMarbleAction(Marble selectedMarble, Marble clickedMarble) {
        if (selectedMarble != null)
            doMarbleMove(selectedMarble, clickedMarble);
        else
            checkInitiallySelectedMarble(clickedMarble);
    }
    
    private void doMarbleMove(Marble selectedMarble, Marble clickedMarble) {
        int fromRow = selectedMarble.getRow(), fromCol = selectedMarble.getCol();
        int toRow = clickedMarble.getRow(), toCol = clickedMarble.getCol();
        if (succeedingJumpMove) {
            if (game.trySucceedingJumpMoves(originRow, originCol, fromRow, fromCol, toRow, toCol)) {
                doSucceedingJumpMove(selectedMarble, clickedMarble);
                if (!game.isHoppingOverMove(fromRow, fromCol, toRow, toCol)) {
                    endSucceedingMovesTurn(selectedMarble, clickedMarble);
                }
            }
            else {
                endSucceedingMovesTurn(selectedMarble, clickedMarble);
                playerViewController.setPlayerMessage("Not a valid jump move");
            }
            return;
        }
        if (game.isMoveValid(fromRow, fromCol, toRow, toCol)) {
            if (game.isHoppingOverMove(fromRow, fromCol, toRow, toCol)) {
                succeedingJumpMove = true;
                doSucceedingJumpMove(selectedMarble, clickedMarble);
                return;
            }
            else {
                doGameMove(selectedMarble, clickedMarble);
                endPlayerTurn();
                succeedingJumpMove = false;
            }
        }
        else {
            playerViewController.setPlayerMessage("Invalid move");
        }
        selectedMarble.setStrokeWidth(1);
        gameScene.setSelectedMarble(null);
    }
    
    private static void configureFileChooser(final FileChooser fileChooser, String title){                           
        fileChooser.setTitle(title);
        fileChooser.setInitialDirectory(
            new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML", "*.xml")
        );
    }

    private void setupReturnToMenuListener(SceneViewerController svc) {
        svc.isReturnedToMenu().addListener((source, oldValue, newValue) -> {
            if (newValue) {
                svc.resetReturnedToMenuListener();
                addCssToScene(primaryStage.getScene());
                insertMainGameMenuToSceneViewer();
            }
        });
    }
    
    private void doSaveAndPrintResult(String path, boolean isOverwrite) {
        Task<Boolean> gameSaver = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    Thread.sleep(1000);
                    return game.saveGame(path, isOverwrite);
                    
                } catch (Exception e) {
                    return false;
                }
            }
        };
        gameSaver.setOnSucceeded((WorkerStateEvent event) -> {
            vistaHolder.setDisable(false);
            if (gameSaver.getValue())
                playerViewController.setPlayerMessage("Game saved successfully");
            else
                playerViewController.setPlayerMessage("Failed to save game");
        });
        playerViewController.setPlayerMessage("Saving...");
        vistaHolder.setDisable(true);
        Thread saver = new Thread(gameSaver);
        saver.start();
    }
    
    private void setupSaveGameAsListener() {
        playerViewController.isSavedGameAs().addListener((source, oldValue, newValue) -> {
            if (newValue) {
                doSaveGameAs();
            }
        });
    }
    
    private void doSaveGameAs() {
        FileChooser fileChooser = new FileChooser();
        configureFileChooser(fileChooser, "Save game");
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file == null) {
            playerViewController.resetSaveGameListener();
            playerViewController.resetSaveGameAsListener();
            return;
        }
        doSaveAndPrintResult(file.getAbsolutePath(), false);
    }

    private void setupSaveGameListener() {
        playerViewController.isSavedGame().addListener((source, oldValue, newValue) -> {
            if (newValue) {
                if (game.isLoadedGame())
                    doSaveAndPrintResult(game.getLoadedGamePath(), true);
                else doSaveGameAs();
                playerViewController.resetSaveGameListener();
            }
        });
    }
    
    private void setupAbandonGameListener() {
        playerViewController.isAbandonedGame().addListener((source, oldValue, newValue) -> {
            if (newValue) {
                playerViewController.removePlayerFromList(game.getCurrPlayer() +1);
                game.removeCurrPlayer();
                view.gamescene.Board.updateEntireBoard(gameScene.getBoardPane(), game.board);
                playerViewController.resetAbandonGameListener();
                if(game.isNoHumanLeft()){
                    doEndOfWorld();
                }
                else {
                    doEndGameActions();
                    endPlayerTurn();
                }
            }
        });
    }
    
    private void doEndOfWorld() {
        Label endOfWorldMessage = new Label("   NO HUMANS LEFT!");
        endOfWorldMessage.setStyle("-fx-background-color: red;");
        endOfWorldMessage.setTextFill(Paint.valueOf("yellow"));
        endOfWorldMessage.setFont(Font.font("Tahoma", FontWeight.BOLD, 100));
        endOfWorldMessage.setLayoutX(-100);
        endOfWorldMessage.setLayoutY(200);
        endOfWorldMessage.setPrefWidth(1150);
        endOfWorldMessage.setRotate(30);
        vistaHolder.getChildren().add(endOfWorldMessage);

        vistaHolder.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
            // deal with this differently?
            Parent sceneViewer = loadSceneViewer();
            insertMainGameMenuToSceneViewer();
            Scene scene = new Scene(sceneViewer, 900, 600);
            addCssToScene(scene);
            primaryStage.setScene(scene);
        });
    }
    
    private void doComputerTurn(){
        Marble fromMarble;
        boolean moveFound = false;
        ObservableList<Node> marbles = gameScene.getBoardPane().getChildren();
        while (!moveFound) {
            fromMarble = (Marble) marbles.get(randInt(0, 120));
            if (game.getPlayers().get(game.getCurrPlayer()).getColor().contains(game.board.getCells()[fromMarble.getRow()][fromMarble.getCol()].getColor())) {
                for (Node node : marbles) {
                    Marble toMarble = (Marble)node;
                    moveFound = game.isMoveValid(fromMarble.getRow(), fromMarble.getCol(), toMarble.getRow(), toMarble.getCol());
                    if (moveFound) {
                        doMarbleAction(fromMarble, toMarble);
                        if (succeedingJumpMove)
                            endSucceedingMovesTurn(fromMarble, toMarble); // prevent AI from making succeeding moves
                        return;
                    }
                }
            }
        }
    }
    
    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
}