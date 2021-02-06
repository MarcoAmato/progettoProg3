package Progetto;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MailShowController {

    private ClientDataModel clientDataModel;
    private Email email;

    public void initClientDataModel(ClientDataModel clientDataModel, Email email) {
        if (this.clientDataModel != null || this.email != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }

        this.clientDataModel = clientDataModel;
        this.email = email;
    }

    public void handleReply(ActionEvent actionEvent) {
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

    public void handleForwardTo(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ForwardMail.fxml"));
            Parent rootFor = fxmlLoader.load();
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
