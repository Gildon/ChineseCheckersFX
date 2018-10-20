
package view.gamescene;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;

public class Marble extends Circle {
    private final int row;
    private final int col;
    
    public Marble(int r, int c) { 
        row = r; col = c; 
        this.setRadius(13);
        this.setStrokeWidth(1);
        this.setStrokeType(StrokeType.OUTSIDE);
        this.setStroke(Paint.valueOf("BLACK"));
        this.setFill(Paint.valueOf("b8b8b8"));
        this.getStyleClass().add("emptymarble");
    }
    
    public Marble(int r, int c, String fill) {
        this(r,c);
        this.setFill(Paint.valueOf(fill));
        if (!fill.equals("b8b8b8")) {
            this.getStyleClass().clear();
            this.getStyleClass().add("marble");
        }
    }
    
    public int getRow() { return row; }
    public int getCol() { return col; }
}