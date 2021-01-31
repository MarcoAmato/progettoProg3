package Progetto;

import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;


public class LoginController {
    @FXML private GridPane loginInterface;
    @FXML private Text feedbackLabel;
    @FXML private TextField emailInput;
    @FXML private Button sendLoginMail;

    private ClientDataModel model;

    public void initModel(ClientDataModel model) {
        // ensure model is only set once:
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }

        this.model = model;
        if(model.getConnectionOkay()){
            showLogin();
        }
        model.connectionStatusProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if(!newValue){
                    hideLogin();
                }else if(newValue){
                    showLogin();
                }
            }
        });
    }

    @FXML
    public void handleLogin() {
        String insertedMail = emailInput.getText();
        boolean authorization = model.getAccessFromServer(insertedMail);
        if (authorization) {
            feedbackLabel.setText("Login Riuscito!");
            feedbackLabel.setFill(Color.GREEN);
            PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
            Stage stage1 = (Stage) loginInterface.getScene().getWindow();
            delay.setOnFinished( event -> stage1.close() );
            delay.play();
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MailClient.fxml"));
                Parent root1 = fxmlLoader.load();
                Stage stage = new Stage();
                stage.setTitle("Client E-mail");
                stage.setScene(new Scene(root1, 1280, 800));
                stage.show();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if(model.getConnectionOkay()){
                feedbackLabel.setText("L’email inserita non è valida");
            }
        }
    }

    private void hideLogin(){
        feedbackLabel.setText("Connessione persa, attendere per la riconnessione");
        sendLoginMail.setDisable(true);
        emailInput.setDisable(true);
    }

    private void showLogin(){
        feedbackLabel.setText("");
        sendLoginMail.setDisable(false);
        emailInput.setDisable(false);
    }
}

