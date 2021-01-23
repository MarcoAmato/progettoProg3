package sample;

import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {
    @FXML private GridPane InterfacciaLogin;
    @FXML private Text labelTest;
    @FXML private TextField email;

    private LoginModel model;

    public void initModel(LoginModel model) {
        // ensure model is only set once:
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }

        this.model = model;
    }
    @FXML
    public void handleButtonAction() {
        String mailprova = "Trabacca.matteo@virgilio.it";
        if (mailprova.equals(email.getText())) {
            labelTest.setText("Login Riuscito!");
            labelTest.setFill(Color.GREEN);
            model.ritornaGiacomo().addListener(new ChangeListener() {
                public void changed(ObservableValue o, Object oldV, Object newV) {
                    if (model.getGiacomo()==true) {
                        PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
                        Stage stage1 = (Stage) InterfacciaLogin.getScene().getWindow();
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
                    }
                }
            });
            model.setGiacomo(true);
        } else {
            labelTest.setText("Login Fallito");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}

