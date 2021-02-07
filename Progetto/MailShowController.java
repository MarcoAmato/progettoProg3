package Progetto;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MailShowController {
    @FXML private AnchorPane anchorPane;
    @FXML private Button reply;
    @FXML private Button replyAll;
    @FXML private TextField sender;
    @FXML private TextField subject;
    @FXML private TextArea mailText;
    @FXML private ListView<String> receiversList;

    private ClientDataModel clientDataModel;
    private Email email;

    public void initClientDataModel(ClientDataModel clientDataModel, Email email) {
        if (this.clientDataModel != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }

        this.clientDataModel = clientDataModel;
        this.clientDataModel.connectionOkayProperty().addListener
                (new CloseOnLostConnection(anchorPane, clientDataModel));
        this.email = email;

        sender.setText(email.getSender());
        subject.setText(email.getSubject());
        mailText.setText(email.getBody());
        //receiversList.setItems(email.getReceivers());



        if(email.getSender().equals(clientDataModel.getEmailAddress())){
            reply.setDisable(true);
            reply.setVisible(false);
            replyAll.setDisable(true);
            replyAll.setVisible(false);
        }
    }

    public void handleReply() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ReplyMail.fxml"));
            Parent rootReply = fxmlLoader.load();
            ReplyMailController replyMailController = fxmlLoader.getController();
            replyMailController.initClientDataModel(this.clientDataModel, this.email);
            Stage stage = new Stage();
            stage.setTitle("Rispondi a "+this.email.getSender());
            stage.setScene(new Scene(rootReply, 550, 600));
            stage.setResizable(false);
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleForwardTo() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ForwardMail.fxml"));
            Parent rootFor = fxmlLoader.load();
            ForwardToController forwardToController = fxmlLoader.getController();
            forwardToController.initClientDataModel(this.clientDataModel, this.email);

            Stage stage = new Stage();
            stage.setTitle("Inoltra a...");
            stage.setScene(new Scene(rootFor, 600, 400));
            stage.setResizable(false);
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleReplyAll(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ReplyAllMail.fxml"));
            Parent rootRespAll = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Rispondi a tutti");
            stage.setScene(new Scene(rootRespAll, 800, 600));
            stage.setResizable(false);
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
