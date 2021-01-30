package sample;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class NewMailController {
    @FXML private TextArea MailText;
    @FXML private Text Controllo;
    @FXML private TextField Receivers;
    @FXML private ListView<String> ReceiversList;
    @FXML private TextField sender;
    @FXML private TextField subject;

    private DataMail model;

    public void HandleSentMail(ActionEvent actionEvent) {
        Email luca = new Email(sender.getText(), new ArrayList<>(ReceiversList.getItems()), subject.getText(), MailText.getText(), new Date());
        Controllo.setText("Mail inviata!");
        System.out.println(luca);
        MailText.setText("");
        //Receivers.setText("");
        subject.setText("");
        ReceiversList.getItems().clear();
        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished( event -> Controllo.setText("") );
    }

    public void handleAddReceiver(ActionEvent actionEvent) {
        ReceiversList.setDisable(false);
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
        if (!ReceiversList.isDisable()) {
            if(ReceiversList.getSelectionModel().getSelectedIndex() != -1) {
                final int val = ReceiversList.getSelectionModel().getSelectedIndex();
                model.ritornaCandidates().remove(val);
                if (model.ritornaCandidates().size() == 0) {
                    ReceiversList.setDisable(true);
                }
            }
        }
    }

}
