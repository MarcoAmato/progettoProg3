package Progetto;

import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Date;


public class NewMailController {
    @FXML private TextArea mailText;
    @FXML private Text controllo;
    @FXML private TextField receivers;
    @FXML private ListView<String> receiversList;
    @FXML private Text deleteText;
    @FXML private TextField sender;
    @FXML private TextField subject;


    private ClientDataModel model;
    boolean Selected = false;
    private ObservableList<String> emailReceivers = new SimpleListProperty<>(FXCollections.observableArrayList());

    public void HandleSentMail(ActionEvent actionEvent) {
        ArrayList<String> listOfReceivers = new ArrayList<>(receiversList.getItems());
        Email insertedMail = new Email(sender.getText(), listOfReceivers, subject.getText(), mailText.getText(), new Date());
        if (listOfReceivers.isEmpty()) {
            controllo.setText("Nessun destinatario inserito");
        } else if (model.sendEmail(listOfReceivers, subject.getText(), mailText.getText())) {
            controllo.setText("Mail inviata!");
            mailText.setText("");
            receivers.setText("");
            subject.setText("");
            receiversList.getItems().clear();
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished( event -> controllo.setText("") );
            delay.play();
        } else {
            controllo.setText("Errore nell'invio della mail");
        }
    }

    public void handleAddReceiver(ActionEvent actionEvent) {
        if(!model.emailAddressExists(receivers.getText())) {
            deleteText.setText("Email non presente nel database.");
        } else if (receivers.getText().equals(model.getEmailAddress())){
            deleteText.setText("Non è possibile inserire la propria mail tra i destinatari!");
        } else if (receiversList.getItems().contains(receivers.getText())) {
            deleteText.setText("L'Email richiesta è già stata inserita");
        } else {
            deleteText.setText("");
            receiversList.getItems().add(receivers.getText());
        }

        //model.ritornaCandidates().add(receivers.getText());
        receivers.setText("");
    }

    public void initClientDataModel(ClientDataModel model) {
        // assicura che il modello viene impostato una volta sola
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }

        this.model = model;
        sender.setText(model.getEmailAddress());
    }

    public void DeleteReceiversHandler(ActionEvent actionEvent) {
        if (Selected) {
            final int val = receiversList.getSelectionModel().getSelectedIndex();
            receiversList.getItems().remove(val);
        }
    }

    public void ReceiverSelected(MouseEvent mouseEvent) {
        Selected = true;
    }

}
