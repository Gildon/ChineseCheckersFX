package model;

import java.util.ArrayList;

public class Player {
    
    private ArrayList<Color> colors;
    private String name;
    private PlayerType type;
    
    public Player(String name, PlayerType type, ArrayList<Color> colors) {
        this.name = name;
        this.type = type;
        this.colors = colors;
    }
    
    public void setName(String value) {
        this.name = value;
    }
    
    public String getName() {
        return name;
    }
            
    public void setType(PlayerType value) {
        this.type = value;
    }
    
    public PlayerType getType() {
        return type;
    }
    
    public ArrayList<Color> getColor() {
        if (colors == null) {
            colors = new ArrayList<>();
        }
        return this.colors;
    }
}