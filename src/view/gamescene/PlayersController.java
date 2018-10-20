
package view.gamescene;

import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class PlayersController {
    AnchorPane sceneContentHolder;
    GridPane playersView;
    SimpleBooleanProperty savedGame, savedGameAs, abandonedGame;
    Label playerMessage;
    
    public PlayersController() {
        sceneContentHolder = new AnchorPane();
        playersView = new GridPane();
        playersView.setHgap(20);
        playersView.setLayoutX(6);
        playersView.setLayoutY(70);
        
        savedGame = new SimpleBooleanProperty(false);
        savedGameAs = new SimpleBooleanProperty(false);
        abandonedGame = new SimpleBooleanProperty(false);
        Label playersLabel = new Label();
        playersLabel.setId("playerslabel");
        playersLabel.setText("         Players         ");
        playersLabel.setPrefWidth(337);
        Button saveGameButton = new Button();
        Button saveAsGameButton = new Button();
        Button leaveGameButton = new Button();
        saveGameButton.getStyleClass().add("playerbutton");
        saveAsGameButton.getStyleClass().add("playerbutton");
        leaveGameButton.getStyleClass().add("playerbutton");
        saveGameButton.setLayoutX(30);
        saveGameButton.setLayoutY(450);
        saveAsGameButton.setLayoutX(175);
        saveAsGameButton.setLayoutY(450);
        leaveGameButton.setPrefWidth(230);
        leaveGameButton.setLayoutX(54);
        leaveGameButton.setLayoutY(510);
        saveGameButton.setText("Save game");
        saveAsGameButton.setText("Save game as...");
        leaveGameButton.setText("Abandon your friends");
        leaveGameButton.setId("abandongamebutton");
        setSaveGameButtonListener(saveGameButton);
        setSaveAsGameButtonListener(saveAsGameButton);
        setAbandonGameButtonListener(leaveGameButton);
        playerMessage = new Label();
        playerMessage.setLayoutX(30);
        playerMessage.setLayoutY(350);
        playerMessage.setId("playermessage");
        sceneContentHolder.getChildren().addAll(playersLabel, playersView, saveGameButton, saveAsGameButton, leaveGameButton, playerMessage);
    }
    
    public void setCurrPlayerIndicator(int playerIndex) {
        Group playerIndicators = (Group)getNodeByRowColumnIndex(playerIndex, 1, playersView);
        for (Node node : playersView.getChildren())
            if (node instanceof Group) {
                node.setVisible(false);
            }
        if (playerIndicators != null) {
            playerIndicators.setVisible(true);
        }
    }
    
    public Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        Node result = null;
        ObservableList<Node> children = gridPane.getChildren();
        for(Node node : children) {
            if(GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                result = node;
                break;
            }
        }
        return result;
    }
    
    public void buildPlayersListGridPane(List<model.Player> players) {
        for (int row = 1; row <= players.size(); row++) {
            playersView.add(createPlayerColorIcons(players.get(row - 1).getColor()), 1, row);
            playersView.add(createPlayerNameLabel(players.get(row - 1).getName()), 2, row);
        }
    }
    
    private Group createPlayerColorIcons(List<model.Color> colors) {
        Group circles = new Group();
        int colorIconX = 0;
        for (model.Color color : colors) {
            circles.getChildren().add(createPlayerColorIcon(color, colorIconX));
            colorIconX += 18;
        }
        
        return circles;
    }
    
    private Circle createPlayerColorIcon(model.Color color, int xLoc) {
        Circle c = new Circle();
        c.setRadius(8);
        c.setFill(Paint.valueOf(color.toString()));
        c.setLayoutX(xLoc);
        c.getStyleClass().add("playerindicator");
        return c;
    }
    
    private Label createPlayerNameLabel(String playerName) {
        Label l = new Label();
        l.setText(playerName);
        l.getStyleClass().add("playerlabel");
        return l;
    }
    
    public SimpleBooleanProperty isAbandonedGame() {
        return abandonedGame;
    }
    
    private void abandonGame() {
        abandonedGame.set(true);
    }
    
    public SimpleBooleanProperty isSavedGame() {
        return savedGame;
    }
    
    private void saveGame() {
        savedGame.set(true);
    }
    
    private void saveGameAs() {
        savedGameAs.set(true);
    }
    
    public SimpleBooleanProperty isSavedGameAs() {
        return savedGameAs;
    }
    
    public void resetSaveGameListener() {
        savedGame.set(false);
    }
    
    public void resetSaveGameAsListener() {
        savedGameAs.set(false);
    }

    public AnchorPane getScene() { return sceneContentHolder; };

    public void resetAbandonGameListener() {
        abandonedGame.set(false);
    }
    
    private void setAbandonGameButtonListener(Button leaveGameButton) {
        leaveGameButton.setOnAction((ActionEvent e) -> {
            abandonGame();
        });
    }
    
    private void setSaveGameButtonListener(Button saveButton) {
        saveButton.setOnAction((ActionEvent e) -> {
            saveGame();
        });
    }
    
    private void setSaveAsGameButtonListener(Button saveAsButton) {
        saveAsButton.setOnAction((ActionEvent e) -> {
            saveGameAs();
        });
    }
    
    public void setPlayerMessage(String message) {
        playerMessage.setText(message);
        playerMessage.setVisible(true);
    }
    
    public void hidePlayerMessage() {
        playerMessage.setVisible(false);
    }
    
    public void removePlayerFromList(int playerIndex){
        Group playerIndicators = (Group)getNodeByRowColumnIndex(playerIndex, 1, playersView);
        Label playerLabel = (Label)getNodeByRowColumnIndex(playerIndex, 2, playersView);
        playersView.getChildren().removeAll(playerIndicators, playerLabel);
        reorderPlayersViewAfterDeletion(playerIndex);
    }
    
    private void reorderPlayersViewAfterDeletion(int toDeleteRow){
        int size = playersView.getChildren().size() / 2 + 1; // adding 1 to accomodate for already deleted player
        for(int row =toDeleteRow +1; row <= size ; row++){
            Group playerIndicators = (Group)getNodeByRowColumnIndex(row, 1, playersView);
            Label playerLabel = (Label)getNodeByRowColumnIndex(row, 2, playersView);
            playersView.getChildren().remove(getNodeByRowColumnIndex(row, 1, playersView));
            playersView.getChildren().remove(getNodeByRowColumnIndex(row, 2, playersView));
            playersView.add(playerIndicators, 1, row -1);
            playersView.add(playerLabel, 2, row -1);
        }
    }
}