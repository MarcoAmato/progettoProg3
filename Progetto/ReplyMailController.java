package Progetto;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class ReplyMailController {

    @FXML private VBox vBox;
    @FXML private TextField sender;
    @FXML private TextArea mailText;
    @FXML private TextField subject;
    @FXML private TextField receiver;
    @FXML private Text controllo;

    private ClientDataModel clientDataModel;
    private Email emailToReply;

    public void initClientDataModel(ClientDataModel clientDataModel, Email emailToReply) {
        if (this.clientDataModel != null || this.emailToReply != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }

        this.clientDataModel = clientDataModel;
        this.emailToReply = emailToReply;
        this.clientDataModel.connectionOkayProperty().addListener(
                new CloseOnLostConnection(this.vBox));

        sender.setText(clientDataModel.getEmailAddress());
        receiver.setText(emailToReply.getSender());
        subject.setText(emailToReply.getSubject());
    }

    public void handleMailReply() {
        if (clientDataModel.replyEmail(this.emailToReply, this.mailText.getText())) {
            controllo.setFill(Color.GREEN);
            controllo.setText("Mail inviata!");
            mailText.setText("");
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished( event -> controllo.setText("") );
            delay.play();
        } else {
            controllo.setFill(Color.RED);
            controllo.setText("Errore nell'invio della mail");
        }
    }
}
