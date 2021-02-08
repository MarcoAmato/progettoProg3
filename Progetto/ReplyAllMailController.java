package Progetto;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;

public class ReplyAllMailController {

    @FXML private AnchorPane anchorPane;
    @FXML private ListView<String> receiversList;
    @FXML private Text mailSenderCheck;
    @FXML private TextField sender;
    @FXML private TextField subject;
    @FXML private TextArea mailText;

    private ClientDataModel clientDataModel;
    private Email emailToReply;


    public void initClientDataModel(ClientDataModel clientDataModel, Email emailToReply) {
        if (this.clientDataModel != null || this.emailToReply != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }

        this.clientDataModel = clientDataModel;
        this.emailToReply = emailToReply;
        this.clientDataModel.connectionOkayProperty().addListener(
                new CloseOnLostConnection(this.anchorPane));
        sender.setText(clientDataModel.getEmailAddress());
        subject.setText(emailToReply.getSubject());
        final ObservableList<String> receivers = FXCollections.observableArrayList(emailToReply.getReceivers());
        receiversList.setItems(receivers);
        receiversList.getItems().add(emailToReply.getSender());
        receivers.remove(clientDataModel.getEmailAddress());
    }

    public void handleReplyAll() {
        ArrayList<String> listOfReceivers = new ArrayList<>(receiversList.getItems());
        if (listOfReceivers.isEmpty()) {
            mailSenderCheck.setText("Nessun destinatario inserito");
        } else if (clientDataModel.replyAllEmail(emailToReply, mailText.getText())) {
            mailSenderCheck.setFill(Color.GREEN);
            mailSenderCheck.setText("Mail inviata!");
            mailText.setText("");
            subject.setText("");
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished( event -> mailSenderCheck.setText("") );
            delay.play();
        } else {
            mailSenderCheck.setFill(Color.RED);
            mailSenderCheck.setText("Errore nell'invio della mail");
        }
    }
}
