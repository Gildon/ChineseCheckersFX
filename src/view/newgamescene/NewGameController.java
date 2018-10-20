
package view.newgamescene;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

public class NewGameController implements Initializable {
    @FXML private ToggleGroup numPlayers;
    @FXML private ToggleGroup numColors;
    @FXML private ToggleGroup numHumans;
    @FXML private Group playerLabels;
    @FXML private Group playerTextFields;
    @FXML private Button startGameButton;
    private SimpleBooleanProperty isAllDataEntered;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        isAllDataEntered = new SimpleBooleanProperty(false);
        setupPlayerNamesListener();
        
    }
    
    @FXML protected void isPressedStartGame(){
        isAllDataEntered.set(true);
    }
    
    @FXML protected void adjustAvailablePlayerColorsAndHumanPlayers() {
        adjustAvailablePlayerColors();
        adjustAvailableHumanPlayers();
    }
    
    private void adjustAvailablePlayerColors() {
        ObservableList<Toggle> numColorsRadioButtons = numColors.getToggles();
        RadioButton selectedNumPlayers = (RadioButton)numPlayers.getSelectedToggle();
        switch (selectedNumPlayers.getText()) {
            case "2":
                for (Toggle t : numColorsRadioButtons) {
                    ((RadioButton)t).setDisable(false);
                }
                break;
            case "3":
                for (Toggle t : numColorsRadioButtons) {
                    if (((RadioButton)t).getText().equals("3")) {
                        ((RadioButton)t).setDisable(true);
                        ((RadioButton)t).setSelected(false);
                    }
                    else ((RadioButton)t).setDisable(false);
                }
                if(selectedNumPlayers.getText().equals("3"))
                    numColorsRadioButtons.get(1).setSelected(true);
                        
                break;
            default:
                for (Toggle t : numColorsRadioButtons) {
                    if (((RadioButton)t).getText().equals("1"))
                        ((RadioButton)t).setDisable(false);
                    else {
                        ((RadioButton)t).setDisable(true);
                        ((RadioButton)t).setSelected(false);
                    }
                  
                }
                ((RadioButton)numColorsRadioButtons.get(0)).setSelected(true);
        }
    }
    
    private void adjustAvailableHumanPlayers() {
        ObservableList<Toggle> numHumansRadioButtons = numHumans.getToggles();
        RadioButton selectedNumPlayers = (RadioButton)numPlayers.getSelectedToggle();
        for (Toggle numHumansRadioButton : numHumansRadioButtons) {
            ((RadioButton) numHumansRadioButton).setDisable(true);
            if (((RadioButton) numHumansRadioButton).isSelected() && 
                    Integer.parseInt(((RadioButton) numHumansRadioButton).getText()) > Integer.parseInt(selectedNumPlayers.getText())) {
                ((RadioButton)numHumansRadioButton).setSelected(false);
            }
        }
        int playersButton;
        for (playersButton = 0; playersButton < Integer.parseInt(selectedNumPlayers.getText()); playersButton++)
            ((RadioButton)numHumansRadioButtons.get(playersButton)).setDisable(false);
        
        ((RadioButton)numHumansRadioButtons.get(playersButton - 1)).setSelected(true);
        adjustVisibleNameTextFields();
    }
    
    @FXML protected void adjustVisibleNameTextFields() {
        int selectedNumHumans = Integer.parseInt(((RadioButton)numHumans.getSelectedToggle()).getText());
        ObservableList<Node> playerNameLabels = playerLabels.getChildren();
        ObservableList<Node> playerNameTextFields = playerTextFields.getChildren();
        
        for (int i = 1; i < 6; i++) {
            playerNameLabels.get(i).setVisible(false);
            playerNameLabels.get(i).setDisable(true);
            playerNameTextFields.get(i).setVisible(false);
            playerNameTextFields.get(i).setDisable(true);
        }
        for(int i = selectedNumHumans ; i<6 ; i++)
              ((TextField)playerNameTextFields.get(i)).clear();
        
        for (int i = 0; i < selectedNumHumans; i++) {
            playerNameLabels.get(i).setVisible(true);
            playerNameLabels.get(i).setDisable(false);
            playerNameTextFields.get(i).setVisible(true);
            playerNameTextFields.get(i).setDisable(false);
        }
        
        for (int i = 0; i < selectedNumHumans; i++) {
            if (((TextField)playerTextFields.getChildren().get(i)).getText().isEmpty())
                startGameButton.setDisable(true);
        }
        doCheckFieldsAndUpdateStartButton();
    }

    private void setupPlayerNamesListener() {
        ObservableList<Node> playerNameTextFields = playerTextFields.getChildren();
        for (Node textField : playerNameTextFields) {
            ((TextField)textField).textProperty().addListener((source, oldValue, newValue) -> {
                startGameButton.setDisable(true);
                doCheckFieldsAndUpdateStartButton();
            });
        }
    }
    
    private boolean isNameUsedByComputer(String name) {
        return name.equals("Anita") || name.equals("Odi")
            || name.equals("Silas") || name.equals("Niska")
            || name.equals("Jonas") || name.equals("Mimi");
    }
    
    public SimpleBooleanProperty isAllDataEntered() {
        return isAllDataEntered;
    }
    
    public int getNumPlayers() {
        return Integer.parseInt(((RadioButton)numPlayers.getSelectedToggle()).getText());
    }
    
    public int getNumOfColors(){
        return Integer.parseInt(((RadioButton)numColors.getSelectedToggle()).getText());
    }
    
    public List<String> getHumanNames(){
        List<String> humanNames = new ArrayList<>();
        
        ObservableList<Node> playerNameTextFields = playerTextFields.getChildren();
        for (Node textField : playerNameTextFields) {
            
            if(!((TextField)textField).isDisabled())
                humanNames.add(((TextField)textField).getText());
        }
        return humanNames;
    }
    
    private void doCheckFieldsAndUpdateStartButton() {
        boolean canStartGame = true;
        ObservableList<Node> playerNameTextFields = playerTextFields.getChildren();
        for (Node fieldOfText : playerNameTextFields) {
            boolean isFieldOkay = true;
            for (Node field : playerNameTextFields) {
                if (!field.isDisabled()) {
                    if (((TextField)field).getText().isEmpty()) {
                        canStartGame = false;   isFieldOkay = false;
                    }
                    if ((((TextField)field) != fieldOfText)
                            && ((TextField)field).getText().equals(((TextField)fieldOfText).getText())
                            && !((TextField)fieldOfText).getText().isEmpty()) {
                        canStartGame = false;   isFieldOkay = false;
                        ((TextField)field).setStyle("-fx-background-color: orange;");
                        fieldOfText.setStyle("-fx-background-color: orange;");
                    }
                    if (isNameUsedByComputer(((TextField)field).getText())) {
                        canStartGame = false;   isFieldOkay = false;
                    }
                }
            }
            if (isFieldOkay)
                ((TextField)fieldOfText).setStyle("-fx-background-color: white;");
        }
        if (canStartGame)
            startGameButton.setDisable(false);
    }
}