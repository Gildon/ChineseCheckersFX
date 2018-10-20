package model;

import java.util.ArrayList;

public class Board {
    private final static int MIDROW = 8;
    public static final int HEIGHT = 17;
    public final static int WIDTH = 13;
    private final Cell[][] cells;
    public final static int TOP = 0, UPPER_RIGHT = 1, LOWER_RIGHT = 2,
                             BOTTOM = 3, LOWER_LEFT = 4, UPPER_LEFT = 5;
    private Cell[][] corners;
    private final static double rowOffset[] = new double[] { 6, 5.5, 5, 4.5, 0, 0.5, 1, 1.5, 2,
                                                             1.5, 1, 0.5, 0, 4.5, 5, 5.5, 6 };
    
    public Board() { 
        this.cells = new Cell[HEIGHT][];
        initializeOuterBoard();
        initializeInnerBoard();
        buildCornerReferences();
    }
    
    private void initializeOuterBoard() {
        for(int i = 0; i < 4; i++) {
            cells[i] = new Cell[i + 1];
            createRowCells(i);
            cells[HEIGHT - i - 1] = new Cell[i + 1];
            createRowCells(HEIGHT - i - 1);
        }

    }
    private void initializeInnerBoard() {
        for(int i = 4; i < 9; i++) {
            cells[i] = new Cell[WIDTH - i + 4];
            createRowCells(i);
            if (i != MIDROW) {
                cells[HEIGHT - i - 1] = new Cell[WIDTH - i + 4];
                createRowCells(HEIGHT - i - 1);
            }
        }
    }
    
    private void createRowCells(int row) {
        for (int i = 0; i < cells[row].length; i++)
                cells[row][i] = new Cell();
    }
    
    public Cell[][] getCells() { return cells; }
    
    private void buildCornerReferences() {
        corners = new Cell[6][];
        corners[TOP] = new Cell[] { cells[0][0], cells[1][0], cells[1][1], cells[2][0], cells[2][1], cells[2][2], cells[3][0], cells[3][1], cells[3][2], cells[3][3] };
        corners[UPPER_LEFT] = new Cell[] {  cells[4][0], cells[4][1], cells[4][2], cells[4][3], cells[5][0], cells[5][1], cells[5][2], cells[6][0], cells[6][1], cells[7][0] };
        corners[LOWER_LEFT] = new Cell[] { cells[12][0], cells[9][0], cells[10][0], cells[10][1], cells[11][0], cells[11][1], cells[11][2], cells[12][1], cells[12][2], cells[12][3] };
        corners[BOTTOM] = new Cell[] { cells[16][0], cells[13][0], cells[13][1], cells[13][2], cells[13][3], cells[14][0], cells[14][1], cells[14][2], cells[15][0], cells[15][1] };
        corners[LOWER_RIGHT] = new Cell[] { cells[12][12], cells[9][9], cells[10][9], cells[10][10], cells[11][9], cells[11][10], cells[11][11], cells[12][9], cells[12][10], cells[12][11] };
        corners[UPPER_RIGHT] = new Cell[] { cells[4][12], cells[4][9], cells[4][10], cells[4][11], cells[5][9], cells[5][10], cells[5][11], cells[6][9], cells[6][10], cells[7][9] } ;
    }
    
    public Cell[][] getCorners() { return corners; }
    
    public void updateCell(Cell cell, Color color) {
        cell.setColor(color);
    }
    
    public void removeColor(Color c) {
        for (Cell[] cell : cells)
            for (Cell piece : cell)
                if (piece.getColor() == c)
                    piece.setColor(Color.NONE);
    }
    
    public ArrayList getUsedCorners() {
        ArrayList usedCorners = new ArrayList<>();
        for (int corner = Board.TOP; corner <= Board.UPPER_LEFT; corner++) {
            if (getCorners()[corner][0].getColor() != Color.NONE)
                usedCorners.add(corner);
        }
        return usedCorners;
    }
    
    public void allocateCornerToColor(Color color, ArrayList usedCorners) {
        if (usedCorners.contains(Board.TOP)) {
            if (usedCorners.contains(Board.BOTTOM)) {
                if (usedCorners.contains(Board.UPPER_LEFT)) {
                    if (usedCorners.contains(Board.LOWER_RIGHT)) {
                        if (usedCorners.contains(Board.UPPER_RIGHT))
                            populateCorner(color, Board.LOWER_LEFT);
                        else populateCorner(color, Board.UPPER_RIGHT);
                    }
                    else populateCorner(color, Board.LOWER_RIGHT);
                }
                else populateCorner(color, Board.UPPER_LEFT);
            }
            else populateCorner(color, Board.BOTTOM);
        }
        else populateCorner(color, Board.TOP);    
    }
    
    private void populateCorner(Color c, int corner) {
        for (Cell cell : corners[corner])
            updateCell(cell, c);
    }
    
    public double colArrayLocationWithOffset(int row, int col) {
        return col + rowOffset[row];
    }
    
    public double getRowOffset(int row) {
        return rowOffset[row];
    }

    public int getOpposingCornerRow(Color color) {
        switch (color.name()) {
            case "RED":     return 16;
            case "BLUE":    return 0;
            case "GREEN":   return 12;
            case "YELLOW":  return 4;
            case "BLACK":   return 12;
            case "WHITE":   return 4;
            default:        return 0;
        }
    }

    public int getOpposingCornerCol(Color color) {
        switch (color.name()) {
            case "RED":     return 0;
            case "BLUE":    return 0;
            case "GREEN":   return 12;
            case "YELLOW":  return 0;
            case "BLACK":   return 0;
            case "WHITE":   return 12;
            default:        return 0;
        }
    }
}