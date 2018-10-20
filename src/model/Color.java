package model;

public enum Color {
    
    BLACK,
    WHITE,
    RED,
    GREEN,
    BLUE,
    YELLOW,
    NONE;
    
    private Cell target;
    
    public Cell getTarget() { return target; };
    
    public void setTarget(Cell t) { target = t; };
    
    @Override
    public String toString() {
        switch (this) {
            case BLACK:     return "BLACK";
            case WHITE:     return "WHITE";
            case RED:       return "RED";
            case GREEN:     return "GREEN";
            case BLUE:      return "BLUE";
            case YELLOW:    return "YELLOW";
            default:        return "b8b8b8";
        }
    }
}