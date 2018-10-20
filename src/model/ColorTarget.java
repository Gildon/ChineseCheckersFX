package model;

public class ColorTarget {
    private Color color;
    private int targetRow;
    private int targetCol;
    
    public ColorTarget(Color color, int tRow, int tCol) {
        this.color = color;
        targetRow = tRow;
        targetCol = tCol;
    }
    
    public Color getColor() { return color; }
    
    public int getTargetRow() { return targetRow; }
    public int getTargetCol() { return targetCol; }

    public void setColorAndTarget(Color c, int row, int col) { color = c; targetRow = row; targetCol = col; }
}
