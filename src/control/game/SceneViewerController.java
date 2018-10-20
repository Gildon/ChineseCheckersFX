
package control.game;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

public class SceneViewerController implements Initializable {

    private SimpleBooleanProperty isReturnedToMenu;
    @FXML AnchorPane vistaHolder;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        isReturnedToMenu = new SimpleBooleanProperty(false);
    }
    
    @FXML protected void returnToMenu() {
        isReturnedToMenu.set(true);
    }
    
    public AnchorPane getVistaHolder() {
        return vistaHolder;
    }
    
    public SimpleBooleanProperty isReturnedToMenu() {
        return isReturnedToMenu;
    }
    
    public void resetReturnedToMenuListener() {
        isReturnedToMenu.set(false);
    }
}
