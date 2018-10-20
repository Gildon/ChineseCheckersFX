
package view.gamescene;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class BoardAndPlayersSceneHolder {
    HBox holder;
    Marble selectedMarble;
    Label playerMessage;
    AnchorPane boardHolder;
    
    
    public BoardAndPlayersSceneHolder() {
        holder = new HBox();
        boardHolder = new AnchorPane();
        boardHolder.setPrefWidth(565);
        AnchorPane playersHolder = new AnchorPane();
        playersHolder.setPrefWidth(332);
        Separator line = new Separator();
        line.setId("playersseparator");
        line.setOrientation(Orientation.VERTICAL);
        holder.getChildren().addAll(boardHolder, line, playersHolder);
    }
    
    public void setBoard(AnchorPane pane) {
        AnchorPane board = (AnchorPane)holder.getChildren().get(0);
        board.getChildren().clear();
        board.getChildren().addAll(pane);
        boardHolder = pane;
    }
    
    public void setPlayers(AnchorPane pane) {
        AnchorPane players = (AnchorPane)holder.getChildren().get(2);
        players.getChildren().clear();
        players.getChildren().addAll(pane);
    }
    
    public HBox getScene() { return holder; }
    
    public Marble getSelectedMarble() { return selectedMarble; }
    
    public void setSelectedMarble(Marble m) { selectedMarble = m; }
    
    public AnchorPane getBoardPane(){ return boardHolder; }
}