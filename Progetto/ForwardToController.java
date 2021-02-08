package Progetto;

import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;

public class ForwardToController {

    @FXML private GridPane gridPane;
    @FXML private TextField receivers;
    @FXML private ListView<String> receiversList;
    @FXML private Text controllo;
    @FXML private Text checkForward;

    private ClientDataModel clientDataModel;
    private Email emailToReply;
    private final ObservableList<String> emailReceivers = new SimpleListProperty<>(FXCollections.observableArrayList());

    public void initClientDataModel(ClientDataModel clientDataModel, Email emailToReply) {
        if (this.clientDataModel != null || this.emailToReply != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }

        this.clientDataModel = clientDataModel;
        this.emailToReply = emailToReply;
        this.clientDataModel.connectionOkayProperty().addListener(
                new CloseOnLostConnection(this.gridPane));
        receiversList.setItems(emailReceivers);

    }

    public void handleAddReceivers() {
        if(!clientDataModel.emailAddressExists(receivers.getText())) {
            controllo.setText("Email non presente nel database.");
        } else if (receivers.getText().equals(clientDataModel.getEmailAddress())){
            controllo.setText("Non è possibile inserire la propria mail tra i destinatari!");
        } else if (receiversList.getItems().contains(receivers.getText())) {
            controllo.setText("L'Email richiesta è già stata inserita");
        } else {
            controllo.setText("");
            receiversList.getItems().add(receivers.getText());
        }

        receivers.setText("");
    }

    public void forwardMail() {

        ArrayList<String> listOfReceivers = new ArrayList<>(receiversList.getItems());
        if (listOfReceivers.isEmpty()) {
            checkForward.setText("Nessun destinatario inserito");
        } else if (clientDataModel.forwardEmail(this.emailToReply, listOfReceivers)) {
            checkForward.setFill(Color.GREEN);
            checkForward.setText("Mail inviata!");
            receivers.setText("");
            receiversList.getItems().clear();
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished( event -> checkForward.setText("") );
            delay.play();
        } else {
            checkForward.setFill(Color.RED);
            checkForward.setText("Errore nell'invio della mail");
        }
    }

    public void deleteReceiver() {

        final int val = receiversList.getSelectionModel().getSelectedIndex();
        if(val == -1) {
            controllo.setText("Nessun Destinatario selezionato");
        }else {
            receiversList.getItems().remove(val);
            controllo.setText("");
        }
    }
}
