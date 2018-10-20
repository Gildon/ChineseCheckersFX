
package view.gamescene;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

abstract public class Board {
    private final static int rowLengths[] = new int[] { 1, 2, 3, 4, 13, 12, 11, 10, 9,
                                                        10, 11, 12, 13, 4, 3, 2, 1 };
    private final static int rowStartingX[] = new int[] { 285, 265, 245, 225, 45, 65, 85, 105, 125,
                                                          105, 85, 65, 45, 225, 245, 265, 285 };
    
    private final static int HEIGHT = 17;
    private final static int TOP_MARBLE_Y = 55;
    private final static int MARBLE_X_DIFF = 40, MARBLE_Y_DIFF = 30;
    
    public static List renderBoard(Pane pane, model.Board board) {
        ArrayList<Marble> marbles = new ArrayList<>();
        for (int row = 0; row < HEIGHT; row++) {
            for (int col = 0; col < board.getCells()[row].length; col++) {
                marbles.add(createMarble(row, col,
                                         rowStartingX[row] + MARBLE_X_DIFF * col,
                                         TOP_MARBLE_Y + MARBLE_Y_DIFF * row,
                                         board.getCells()[row][col].getColor().toString()));
            }
        }
        pane.getChildren().addAll(marbles);
        return marbles;
    }

    private static void setMarbleXAndY(Marble marble, int x, int y) {
        marble.setLayoutX(x);
        marble.setLayoutY(y);
    }
    
    private static Marble createMarble(int row, int col, int marbleX, int marbleY, String fill) {
        Marble marble = new Marble(row, col, fill);
        setMarbleXAndY(marble, marbleX, marbleY);
        return marble;
    }
    
    public static void updateMarbleAfterMove(Pane pane, Marble from, Marble to) {
        doMarbleTransition(pane, from, to);
    }
    
    private static void doMarbleTransition(Pane pane, Marble from, Marble to) {
        Marble dummyMarble = new Marble(1,1);
        dummyMarble.setFill(from.getFill());
        dummyMarble.getStyleClass().clear();
        dummyMarble.getStyleClass().add(from.getStyleClass().get(0));
        from.setFill(Paint.valueOf("b8b8b8"));
        from.getStyleClass().clear();
        from.getStyleClass().add("emptymarble");
        pane.getChildren().add(dummyMarble);
        
        TranslateTransition translateTransition = 
                new TranslateTransition(Duration.seconds(0.2), dummyMarble);
        translateTransition.setOnFinished((ActionEvent t) -> {
                pane.getChildren().remove(dummyMarble);
                to.setFill(dummyMarble.getFill());
                to.getStyleClass().clear();
                to.getStyleClass().add("marble");
        });
       
        translateTransition.setFromX(from.getLayoutX());
        translateTransition.setFromY(from.getLayoutY());
        translateTransition.setToX(to.getLayoutX());
        translateTransition.setToY(to.getLayoutY());

        translateTransition.play();
    }
    
    public static void updateEntireBoard(AnchorPane boardPane, model.Board b) {
        ObservableList<Node> marbles = boardPane.getChildren();
        for (Node marble : marbles) {
            if(marble instanceof Marble){
                Marble m = (Marble) marble;
                String fill = b.getCells()[m.getRow()][m.getCol()].getColor().toString();
                if (fill.equals("b8b8b8")) {
                    m.getStyleClass().clear();
                    m.getStyleClass().add("emptymarble");
                }
                m.setFill(Paint.valueOf(b.getCells()[m.getRow()][m.getCol()].getColor().toString()));
            }  
        }
    }
}