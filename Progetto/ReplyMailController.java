package Progetto;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;

public class ReplyMailController {

    @FXML private VBox vBox;
    @FXML private TextArea mailText;
    @FXML private TextField subject;
    @FXML private TextField receiver;
    @FXML private Text controllo;

    private ClientDataModel model;

    public void initClientDataModel(ClientDataModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }

        this.model = model;
        this.model.connectionOkayProperty().addListener(
                new CloseOnLostConnection(this.vBox, this.model));
    }

    public void handleMailRespond(ActionEvent actionEvent) {
        ArrayList<String> listOfReceivers = new ArrayList<>(Arrays.asList(receiver.getText()));
        if (listOfReceivers.isEmpty()) {
            controllo.setText("Nessun destinatario inserito");
        } else if (model.sendEmail(listOfReceivers, subject.getText(), mailText.getText())) {
            controllo.setFill(Color.GREEN);
            controllo.setText("Mail inviata!");
            mailText.setText("");
            receiver.setText("");
            subject.setText("");
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished( event -> controllo.setText("") );
            delay.play();
        } else {
            controllo.setFill(Color.RED);
            controllo.setText("Errore nell'invio della mail");
        }
    }
}
