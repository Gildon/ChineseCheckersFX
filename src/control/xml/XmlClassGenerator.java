package control.xml;

import control.game.GameController;
import model.ColorTarget;
import model.Player;
import generated.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.XMLConstants;
import javax.xml.bind.Marshaller;
import org.xml.sax.SAXException;


public abstract class XmlClassGenerator {
    public static ChineseCheckers getGeneratedObjects(String fileName) throws JAXBException, SAXException, FileNotFoundException {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(XmlClassGenerator.class.getClassLoader().getResource("resources/chinese_checkers.xsd"));

        JAXBContext jaxbContext = JAXBContext.newInstance(ChineseCheckers.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        jaxbUnmarshaller.setSchema(schema);

        jaxbUnmarshaller.setEventHandler(new MyValidationEventHandler());

        ChineseCheckers chineseCheckers = (ChineseCheckers) jaxbUnmarshaller.unmarshal(new File(fileName));
        if (!ClassConvertor.validateXmlIntegrity(chineseCheckers))
            throw new SAXException();
        return chineseCheckers;
    }

    public static void generateXml(GameController game, String fileName) throws Exception {
        ChineseCheckers chineseCheckers = new ChineseCheckers();

        chineseCheckers.setBoard(ClassConvertor.convertGameBoardToGenerated(game));
        chineseCheckers.setCurrentPlayer(game.getPlayers().get(game.getCurrPlayer()).getName());
        chineseCheckers.setPlayers(ClassConvertor.convertGamePlayersToGenerated(game));

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(XmlClassGenerator.class.getClassLoader().getResource("resources/chinese_checkers.xsd"));
        
        JAXBContext jc = JAXBContext.newInstance(ChineseCheckers.class);

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setSchema(schema);
        marshaller.setEventHandler(new MyValidationEventHandler());
        try (OutputStream os = new FileOutputStream(fileName)) {
            marshaller.marshal(chineseCheckers, os);
        }
    }

    public abstract static class ClassConvertor {
        public static ArrayList<Player> generatePlayers(generated.Players gPlayers) {
            ArrayList<Player> generatedPlayers = new ArrayList<>();
            for (int i = 0; i < gPlayers.getPlayer().size(); i++)
                generatedPlayers.add(convertPlayer(gPlayers.getPlayer().get(i)));
            return generatedPlayers;
        }

        public static Player convertPlayer(generated.Players.Player gPlayer) {
            generated.PlayerType gPlayerType = gPlayer.getType();
            model.PlayerType playerType;
            if (gPlayerType.name().equals("HUMAN"))
                playerType = model.PlayerType.HUMAN;
            else playerType = model.PlayerType.COMPUTER;

            List<generated.ColorType> gColors = gPlayer.getColorDef();
            ArrayList<model.Color> pColors = new ArrayList<>();
            for (generated.ColorType cType : gColors)
                pColors.add(convertColor(cType.getColor()));

            return new Player(gPlayer.getName(), playerType, pColors);
        }

        public static model.Color convertColor(generated.Color c) {
            switch (c.name()) {
                case "BLACK":   return model.Color.BLACK;
                case "WHITE":   return model.Color.WHITE;
                case "RED":     return model.Color.RED;
                case "GREEN":   return model.Color.GREEN;
                case "BLUE":    return model.Color.BLUE;
                case "YELLOW":  return model.Color.YELLOW;
                default:        return model.Color.NONE;
            }
        }

        public static model.Board generateBoard(generated.Board gBoard) {
            model.Board loadedBoard = new model.Board();
            List<generated.Cell> gCells = gBoard.getCell();
            for (generated.Cell cell : gCells) {
                int row = cell.getRow() - 1;
                int col = cell.getCol() - 1;
                model.Color c = convertColor(cell.getColor());
                loadedBoard.getCells()[row][col].setColor(c);
            }
            return loadedBoard;
        }

        public static List<ColorTarget> generateTargets(List<generated.Players.Player> players) {
            List<ColorTarget> cTargets = new ArrayList<>();
            players.stream().forEach((player) -> {
                player.getColorDef().stream().forEach((cType) -> {
                    model.Color c = convertColor(cType.getColor());
                    cTargets.add(new ColorTarget(c, cType.getTarget().getRow() - 1, cType.getTarget().getCol() - 1));
                });
            });
            return cTargets;
        }

        private static boolean validateXmlIntegrity(ChineseCheckers cc) {
            return validateNumOfPiecesPerColor(cc) &&
                   validateCellLocations(cc) &&
                   validateAtLeastOneHumanPlayer(cc); // && validateTargetLocations(cc);
        }
        
        private static boolean validateAtLeastOneHumanPlayer(ChineseCheckers cc) {
            boolean humansExist = false;
            for (generated.Players.Player player : cc.getPlayers().getPlayer())
                if (player.getType() == PlayerType.HUMAN)
                    humansExist = true;
            return humansExist;
        }

        private static boolean validateTargetLocations(ChineseCheckers cc) {
            List<generated.Players.Player> players = cc.getPlayers().getPlayer();
            List<generated.ColorType.Target> validTargets = new ArrayList<>();
            for (int i = 0; i < 6; i++)
                validTargets.add(new generated.ColorType.Target());
            validTargets.get(0).setRow(1);      validTargets.get(0).setCol(1);
            validTargets.get(1).setRow(17);     validTargets.get(1).setCol(1);
            validTargets.get(2).setRow(5);      validTargets.get(2).setCol(1);
            validTargets.get(3).setRow(5);      validTargets.get(3).setCol(13);
            validTargets.get(4).setRow(13);     validTargets.get(4).setCol(13);
            validTargets.get(5).setRow(13);     validTargets.get(5).setCol(1);

            for (generated.Players.Player p : players) {
                for (generated.ColorType color : p.getColorDef()) {
                    // doesn't work before not the same reference
                    if (!validTargets.contains(color.getTarget()))
                            return false;
                }
            }
            return true;
        }

        private static boolean validateNumOfPiecesPerColor(ChineseCheckers cc) {
            List<generated.Cell> cells = cc.getBoard().getCell();
            int colors[] = new int[6];
            boolean rightNumOfColors = true;
            for (generated.Cell cell : cells) {
                switch (cell.getColor().name()) {
                    case "BLUE":    colors[0]++;    break;
                    case "YELLOW":  colors[1]++;  break;
                    case "GREEN":   colors[2]++;   break;
                    case "RED":     colors[3]++;     break;
                    case "BLACK":   colors[4]++;   break;
                    case "WHITE":   colors[5]++;   break;
                }
            }
            for (int i = 0; i < 6; i++)
                if ((colors[i]> 0 && colors[i]!= 10) || (colors[i] < 10 && colors[i] != 0))
                    rightNumOfColors = false;
            return rightNumOfColors;
        }

        private static boolean validateCellLocations(ChineseCheckers cc) {
            List<generated.Cell> cells = cc.getBoard().getCell();
            for (generated.Cell cell : cells) {
                int row = cell.getRow();
                int col = cell.getCol();
                switch (row) {
                    case 1:     if (col != 1)   return false;   break;
                    case 2:     if (col > 2)    return false;   break;
                    case 3:     if (col > 3)    return false;   break;
                    case 4:     if (col > 4)    return false;   break;
                    case 5:     if (col > 13)   return false;   break;
                    case 6:     if (col > 12)   return false;   break;
                    case 7:     if (col > 11)   return false;   break;
                    case 8:     if (col > 10)   return false;   break;
                    case 9:     if (col > 9)    return false;   break;
                    case 10:    if (col > 10)   return false;   break;
                    case 11:    if (col > 11)   return false;   break;
                    case 12:    if (col > 12)   return false;   break;
                    case 13:    if (col > 13)   return false;   break;
                    case 14:    if (col > 4)    return false;   break;
                    case 15:    if (col > 3)    return false;   break;
                    case 16:    if (col > 2)    return false;   break;
                    case 17:    if (col != 1)   return false;   break;
                    default:                    return true;
                }
            }
            return true;
        }

        public static generated.Board convertGameBoardToGenerated(GameController game){
            generated.Board gBoard = new generated.Board();
            model.Cell gameCell[][] = game.board.getCells();

            for(int i = 0 ; i< gameCell.length ; i++){
                for(int j = 0 ; j< gameCell[i].length ; j++){
                    if(gameCell[i][j].getColor() != model.Color.NONE){
                        generated.Cell cell = new generated.Cell();
                        cell.setColor(convertGameColorToGeneratedColor(gameCell[i][j].getColor()));
                        cell.setCol(j+1);
                        cell.setRow(i+1);
                        gBoard.getCell().add(cell);
                    }
                }
            }
            return gBoard;
        }

        public static Players convertGamePlayersToGenerated(GameController game) {
            Players generatedPlayers = new Players();

            for (int i = 0; i < game.getPlayers().size() ; i++)
                generatedPlayers.getPlayer().add((convertPlayer(game.getPlayers().get(i) , game)));
            return generatedPlayers;
        }

        public static Players.Player convertPlayer(model.Player gPlayer , GameController game) {
            Players.Player generatedPlayer = new Players.Player();

            if(gPlayer.getType() == model.PlayerType.HUMAN)
                  generatedPlayer.setType(PlayerType.HUMAN);
            else  generatedPlayer.setType(PlayerType.COMPUTER);

            generatedPlayer.setName(gPlayer.getName());

            for (model.Color color : gPlayer.getColor()) {
                ColorType colorType = new ColorType();
                colorType.setColor(convertGameColorToGeneratedColor(color));
                ColorType.Target target = new ColorType.Target();
                for (ColorTarget colorTarget : game.getColorTarget()) {
                    if(colorTarget.getColor() == color){
                        target.setCol(colorTarget.getTargetCol() + 1);
                        target.setRow(colorTarget.getTargetRow() + 1);
                        colorType.setTarget(target);
                        break;
                    }
                }
                generatedPlayer.getColorDef().add(colorType);
            }

        return generatedPlayer;
        }

        public static Color convertGameColorToGeneratedColor(model.Color c) {
            switch (c.name()) {
                case "BLACK":   return Color.BLACK;
                case "WHITE":   return Color.WHITE;
                case "RED":     return Color.RED;
                case "GREEN":   return Color.GREEN;
                case "BLUE":    return Color.BLUE;
                case "YELLOW":  return Color.YELLOW;

                default:        return Color.WHITE; // never get here
            }
        }
    }
}
