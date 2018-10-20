package model;

public class Cell {
    private Color color;
    
    public Cell(Color color) {
        this.color = color;
    }
  
    public Cell() { color = Color.NONE; }
    
    public Color getColor() { return color; }
    
    public void setColor(Color newColor) { color = newColor; }
    
    public boolean isEmpty() { return color == Color.NONE; }
}