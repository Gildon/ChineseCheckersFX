
package control.menu;

import static java.lang.System.exit;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class MainMenuController implements Initializable {

    @FXML AnchorPane menuPane;
    private SimpleBooleanProperty startedNewGame , loadedGame;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        startedNewGame = new SimpleBooleanProperty(false);
        loadedGame = new SimpleBooleanProperty(false);
    }    
    
    @FXML protected void quitGame() {
        exit(0);
    }
    
    @FXML protected void startNewGame() {
        startedNewGame.set(true);
    }
    
    @FXML protected void loadGame() {
        loadedGame.set(true);
    }
    
    public SimpleBooleanProperty isStartedNewGame() {
        return startedNewGame;
    }
    
    public SimpleBooleanProperty isLoadedGame() {
        return loadedGame;
    }
    
    public void resetLoadGameListener() {
        loadedGame.set(false);
    }
    
    public Pane getPane() {
        return menuPane;
    }
}
