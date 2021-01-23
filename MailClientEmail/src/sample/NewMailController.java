package sample;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.util.Duration;


public class NewMailController {
    @FXML private TextArea MailText;
    @FXML private Text Controllo;
    @FXML private TextField Receivers;
    @FXML private ListView<String> ReceiversList;
    @FXML private Text DeleteText;

    private DataMail model;
    boolean Selected = false;

    public void HandleSentMail(ActionEvent actionEvent) {
        String luca = MailText.getText();
        if(luca.contains("s")) {
            Controllo.setText("Luca contiene s");
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished( event -> Controllo.setText("") );
            delay.play();
        } else {
            Controllo.setText("Luca non contiene s");
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished( event -> Controllo.setText("") );
            delay.play();
        }
    }

    public void handleAddReceiver(ActionEvent actionEvent) {
        model.ritornaCandidates().add(Receivers.getText());
        Receivers.setText("");
    }

    public void initDataMail(DataMail model) {
        // assicura che il modello viene impostato una volta sola
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }

        this.model = model;
        ReceiversList.setItems(model.ritornaCandidates());
    }

    public void DeleteReceiversHandler(ActionEvent actionEvent) {
        if (Selected) {
            final int val = ReceiversList.getSelectionModel().getSelectedIndex();
            ReceiversList.getItems().remove(val);
        }
    }

    public void ReceiverSelected(MouseEvent mouseEvent) {
        Selected = true;
    }
}
