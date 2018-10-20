
package control.game;

import control.xml.XmlClassGenerator;
import control.xml.XmlClassGenerator.ClassConvertor;
import generated.ChineseCheckers;
import java.io.FileNotFoundException;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import model.*;
import org.xml.sax.SAXException;

public class GameController {
    
    public Board board;
    private ArrayList<Player> players;
    private ArrayList<model.Color> availableColors;
    private ArrayList<String> computerNames;
    private List<ColorTarget> colorTargets;
    private String loadedGamePath;
    private int currPlayer;

    public int getCurrPlayer() { return currPlayer; }
    public List<Player> getPlayers(){ return players; }
    public List<ColorTarget> getColorTarget(){ return colorTargets; }
    
    public Player playerHasWon() {
        if (players.size() == 1) {
            return players.get(0);
        }
        for (Player player : players) {
            boolean allPlayerPiecesInPlace = false;
            for (Color playerColor : player.getColor()) {
                allPlayerPiecesInPlace = colorPiecesInTargetCorner(playerColor);
                if (!allPlayerPiecesInPlace)
                    break;
            }
            if (allPlayerPiecesInPlace) {
                return player;
            }
        }
        return null;
    }
    
    private boolean colorPiecesInTargetCorner(model.Color color){
        for (ColorTarget cTarget : colorTargets) {
            if (cTarget.getColor() == color) {
                int targetRow = cTarget.getTargetRow();
                int targetCol = cTarget.getTargetCol();
                for (int i = model.Board.TOP; i <= model.Board.UPPER_LEFT; i++) {
                    boolean allPiecesInRightCorner = true;
                    if (board.getCorners()[i][0] == board.getCells()[targetRow][targetCol]) {
                        for (model.Cell cornerCell : board.getCorners()[i]) {
                            if (cornerCell.getColor() != color) {
                                allPiecesInRightCorner = false;
                                break;
                            }
                        }
                        if (allPiecesInRightCorner) 
                            return true;
                    }
                }
            }
        }
        return false;
    }
    
    public void advancePlayer() {
        currPlayer++;
        if (currPlayer == players.size())
            currPlayer = 0;
    }
    
    public void removeCurrPlayer() {
        Player p = players.get(currPlayer);
        ArrayList<Color> playerColors = p.getColor();
        playerColors.stream().forEach((color) -> board.removeColor(color));
        players.remove(p);
        currPlayer--;
    }

    public boolean isCurrPlayerPiece(int row, int col) {
        return players.get(currPlayer).getColor().contains(board.getCells()[row][col].getColor());
    }
    
    public boolean trySucceedingJumpMoves(int fromRow, int fromCol, int toRow, int toCol, int newRow, int newCol) {
        if (newRow == fromRow && newCol == fromCol) {
            System.out.println("You are trying to move to your previous location!  You missed your chance..");
        }
        else {
            double toColDiff = board.colArrayLocationWithOffset(toRow, toCol);
            double newColDiff = board.colArrayLocationWithOffset(newRow, newCol);
            if (abs(newRow - toRow) == 2 || abs(toColDiff - newColDiff) == 2) {
                if(isMoveValid(toRow, toCol, newRow, newCol)) {
                    return true;
                }
            }
            else {
                System.out.println("Your move is illegal!");
            }
        }
        return false;
    }
    
    public boolean isHoppingOverMove(int fromRow, int fromCol, int toRow, int toCol) {
        double fromColDiff = board.colArrayLocationWithOffset(fromRow, fromCol);
        double toColDiff = board.colArrayLocationWithOffset(toRow, toCol);
        boolean hasAvailableJumpMove = hasAvailableJumpMove(fromRow, fromCol, toRow , toCol);
        if ((abs(fromRow - toRow) != 2 && abs(fromColDiff - toColDiff) != 2 ) || !hasAvailableJumpMove)
            return false;

        return true;
    }
    
    private boolean hasAvailableJumpMove(int prevRow, int prevCol, int currRow, int currCol) {

        final int RIGHT = -1 , LEFT = 1;
        final int DIAGONAL_RIGHT = 1 , DIAGONAL_LEFT = -1;
        final int DIAGONAL_UP = -2 , DIAGONAL_DOWN = 2;

        boolean canHopRight = canHopSideways(prevRow, prevCol, currRow, currCol + 2, RIGHT);
        boolean canHopLeft = canHopSideways(prevRow, prevCol, currRow, currCol - 2, LEFT);
        boolean canHopUpRight = canHopDiagonally(prevRow, prevCol, currRow, currCol, DIAGONAL_RIGHT, DIAGONAL_UP);
        boolean canHopUpLeft = canHopDiagonally(prevRow, prevCol, currRow, currCol, DIAGONAL_LEFT, DIAGONAL_UP);
        boolean canHopDownRight = canHopDiagonally(prevRow, prevCol, currRow, currCol, DIAGONAL_RIGHT, DIAGONAL_DOWN);
        boolean canHopDownLeft = canHopDiagonally(prevRow, prevCol, currRow, currCol, DIAGONAL_LEFT, DIAGONAL_DOWN);

        return canHopRight || canHopLeft || canHopUpRight || canHopDownRight || canHopUpLeft || canHopDownLeft;
    }
    
    private boolean canHopSideways(int prevRow, int prevCol, int currRow , int toCol , int direction){
        return  (toCol >= 0 && (board.getCells()[currRow].length > toCol))
                && (board.getCells()[currRow][toCol].isEmpty())
                && (!board.getCells()[currRow][toCol + direction].isEmpty())
                && (!(prevCol == toCol && prevRow == currRow));
    }

    private boolean canHopDiagonally(int prevRow , int prevCol , int currRow , int currCol, double xDirection , int yDirection ){
        if(currRow + yDirection >= 0 && currRow + yDirection < Board.HEIGHT){
            int targetCol = (int)(currCol + xDirection + board.getRowOffset(currRow) - board.getRowOffset(currRow + yDirection));
            int betweenCurrAndTargetCol = (int)(currCol +  xDirection/2 + board.getRowOffset(currRow) - board.getRowOffset(currRow + yDirection/2));
            if (targetCol < board.getCells()[currRow + yDirection].length && targetCol >= 0)
                return  (board.getCells()[currRow + yDirection][targetCol].isEmpty())
                        && ((currRow + yDirection != prevRow) || (currRow + yDirection == prevRow && targetCol != prevCol))
                        && (!board.getCells()[currRow + yDirection/2][betweenCurrAndTargetCol].isEmpty());
        }
        return false;
    }
    
    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        board.updateCell(board.getCells()[toRow][toCol], board.getCells()[fromRow][fromCol].getColor());
        board.updateCell(board.getCells()[fromRow][fromCol], Color.NONE);
    }
    
    public boolean isMoveValid(int fromRow, int fromCol, int toRow, int toCol) {
        if (!board.getCells()[toRow][toCol].isEmpty())
            return false;
        double fromColDiff = board.colArrayLocationWithOffset(fromRow, fromCol);
        double toColDiff = board.colArrayLocationWithOffset(toRow, toCol);
        if (distnceIsTooLong(fromColDiff, toColDiff, fromRow, toRow))
            return false;
        if (isInvalidSidewaysJump(fromColDiff, toColDiff, fromRow, toRow, fromCol, toCol))
            return false;
        if (abs(fromColDiff - toColDiff) == 1.5) // impossible col diff
            return false;
        if (isInvalidDiagonalJump(fromRow, toRow, fromColDiff, toColDiff))
            return false;
        return true;
    }
    
    private boolean distnceIsTooLong(double fromColDiff, double toColDiff, int fromRow, int toRow) {
        return abs(fromColDiff - toColDiff) > 2 || abs(fromRow - toRow) > 2;
    }    
    
    private boolean isInvalidSidewaysJump(double fromColDiff, double toColDiff, int fromRow, int toRow, int fromCol, int toCol) {
        if (abs(fromColDiff - toColDiff) == 2) {
            if (fromRow != toRow)
                return true;
            if (board.getCells()[toRow][(fromCol + toCol) / 2].getColor() == Color.NONE)
                return true;
        }
        return false;
    }

    private boolean isInvalidDiagonalJump(int fromRow, int toRow, double fromColDiff, double toColDiff) {
        if (abs(fromRow - toRow) == 2) {
            if (abs(fromColDiff - toColDiff) != 1)
                return true;
            int hoppedOverRow = (fromRow + toRow) / 2;
            int hoppedOverCol = (int)((fromColDiff + toColDiff) / 2 - board.getRowOffset(hoppedOverRow));
            if (board.getCells()[hoppedOverRow][hoppedOverCol].isEmpty())
                return true;
        }
        return false;
    }    
    
    public boolean saveGame(String fullPath, boolean isOverwrite) {
        boolean newSaveFile = true;
        if (!loadedGamePath.isEmpty()) {
            if (isOverwrite) {
                try {
                    XmlClassGenerator.generateXml(this, loadedGamePath);
                    newSaveFile = false;
                    return true;
                } catch (Exception ex) {
                    Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
        }
        if (newSaveFile) {
            try {
                XmlClassGenerator.generateXml(this, fullPath);
                loadedGamePath = fullPath;
                return true;
            } catch (Exception ex) {
                Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return false;
    }
    
    public boolean loadGame(String fullPath) {
        ChineseCheckers chineseCheckers;
        
        try {
            chineseCheckers = XmlClassGenerator.getGeneratedObjects(fullPath);
        } catch (JAXBException | SAXException | FileNotFoundException ex) {
            System.err.println("Failed to load file");
            return false;
        }
        loadedGamePath = fullPath;
        board = ClassConvertor.generateBoard(chineseCheckers.getBoard());
        players = ClassConvertor.generatePlayers(chineseCheckers.getPlayers());
        colorTargets = ClassConvertor.generateTargets(chineseCheckers.getPlayers().getPlayer());
        for (int player = 0; player < players.size(); player++) {
            if (players.get(player).getName().equals(chineseCheckers.getCurrentPlayer()))
                currPlayer = player;
        }
        return true;
    }
    
    public void startNewGame(int numPlayers, int numColors, List<String> humanNames) {
        addAvailableColorsAndCompNames();
        setupNewBoard(numPlayers, numColors, humanNames);
        loadedGamePath = "";
        currPlayer = 0;
    }
    
    private void addAvailableColorsAndCompNames() {
        availableColors = new ArrayList<model.Color>() {{ add(Color.RED); add(Color.BLUE); add(Color.GREEN);
                                                    add(Color.YELLOW); add(Color.BLACK); add(Color.WHITE); }};

        computerNames = new ArrayList<String>() {{ add("Anita"); add("Odi"); add("Silas");
                                                   add("Niska"); add("Jonas"); add("Mimi"); }};
    }
    
    private ArrayList<Color> assignColorsToPlayer(int numOfColors) {
        ArrayList<Color> playerColors = new ArrayList<>();
        for (int i = 0; i < numOfColors; i++) {
            playerColors.add(getNextUnusedColor());
        }
        return playerColors;
    }
    
    private Color getNextUnusedColor() {
        Color c = availableColors.get(0);
        availableColors.remove(0);
        return c;
    }

    private void setupNewBoard(int numOfPlayers, int colorsPerPlayer, List<String> humanNames) {
        board = new Board();
        players = new ArrayList<>();
        colorTargets = setColorTargets(numOfPlayers, colorsPerPlayer);
        for (int i = 0; i < humanNames.size(); i++)
            addPlayer(new Player(humanNames.get(i), PlayerType.HUMAN, assignColorsToPlayer(colorsPerPlayer)));
            
        int numOfComputerPlayers = numOfPlayers - humanNames.size();
        for(int i=0 ; i<numOfComputerPlayers ; i++)
            addPlayer(createComputerPlayer(colorsPerPlayer));
        
        long seed = System.nanoTime();
        Collections.shuffle(players, new Random(seed));
    }
    
    private void addPlayer(Player p) {
        players.add(p);
        p.getColor().stream().forEach((color) -> {
            ArrayList usedCorners = board.getUsedCorners();
            board.allocateCornerToColor(color, usedCorners);
        });
    }
    
    private Player createComputerPlayer(int numOfColors) {
        ArrayList<Color> compColors = assignColorsToPlayer(numOfColors);
        String name = getNextCompName();
        return new Player(name, PlayerType.COMPUTER, compColors);
    }
    
    private String getNextCompName() {
        String s = computerNames.get(0);
        computerNames.remove(0);
        return s;
    }
    
    private List<ColorTarget> setColorTargets(int numOfPlayers, int colorsPerPlayer) {
        List<ColorTarget> cTargets = new ArrayList<>();
        int colorsInUse = numOfPlayers * colorsPerPlayer;
        for (int currColor = 0; currColor < colorsInUse; currColor++) {
            int tRow = board.getOpposingCornerRow(availableColors.get(currColor));
            int tCol = board.getOpposingCornerCol(availableColors.get(currColor));
            ColorTarget cTarget = new ColorTarget(availableColors.get(currColor), tRow, tCol);
            cTargets.add(cTarget);
        }
        return cTargets;
    }
    
    public boolean isNoHumanLeft(){
        boolean isNoHumanLeft = true;
        for (Player player : players) {
            if(player.getType() == PlayerType.HUMAN)
                isNoHumanLeft = false;
        }
        return isNoHumanLeft;
    }
    
    public boolean isLoadedGame() {
        return !loadedGamePath.isEmpty();
    }
    
    public String getLoadedGamePath(){
        return loadedGamePath;
    }
}
