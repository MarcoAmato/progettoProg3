package Progetto;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class MailController {
    @FXML private HBox AllEmail;
    @FXML private HBox SentEmail;
    @FXML private HBox ReceivedEmail;
    @FXML private HBox NewMail;
    @FXML private HBox DeleteMail;
    @FXML private Text DeleteHandler;
    @FXML private TableView<String> mailList;
    @FXML private BorderPane mailView;

    private ClientDataModel model;

    public void MouseOutShowMail(MouseEvent mouseEvent) {
        AllEmail.setEffect(new Glow(0));
    }

    public void HandleGlowAllMail() { AllEmail.setEffect(new Glow(0.8)); }

    public void HandleGlowSentMail() { SentEmail.setEffect(new Glow(0.8)); }

    public void HandleGlowReceivedMail() { ReceivedEmail.setEffect(new Glow(0.8)); }

    public void HandleGlowNewMail() { NewMail.setEffect(new Glow(0.8)); }

    public void HandleGlowDeleteMail() { DeleteMail.setEffect(new Glow(0.8)); }

    public void HandleOutGlowEmail(MouseEvent mouseEvent) {
        SentEmail.setEffect(new Glow(0));
    }

    public void MouseOutReceivedEmail(MouseEvent mouseEvent) {
        ReceivedEmail.setEffect(new Glow(0));
    }

    public void MouseOutNewMail(MouseEvent mouseEvent) {
        NewMail.setEffect(new Glow(0));
    }

    public void MouseOutDeleteMail(MouseEvent mouseEvent) {
        DeleteMail.setEffect(new Glow(0));
    }

    public void handleShowSentMail(MouseEvent mouseEvent) { }

    public void handleShowReceivedMail(MouseEvent mouseEvent) { }

    //Dobbiamo gestire questa funzione importando il file giusto
    public void handleNewMail(MouseEvent mouseEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MailCreator.fxml"));
            Parent root2 = fxmlLoader.load();
            NewMailController controller = fxmlLoader.getController();
            ClientDataModel model = new ClientDataModel();
            controller.initClientDataModel(model);
            Stage stage = new Stage();
            stage.setTitle("Crea nuova mail");
            stage.setScene(new Scene(root2, 800, 600));
            stage.setResizable(false);
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleDeleteMail(MouseEvent mouseEvent) {
        DeleteHandler.setText("Seleziona la mail da eliminare");
    }


    public void initClientDataModel(ClientDataModel model) {
        // assicura che il modello viene impostato una volta sola
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        this.model = model;
        mailList.getItems().add(mailList.getId());
    }

    public void HandleShowMail(MouseEvent mouseEvent) {
        if(mailList.getSelectionModel().getSelectedIndex() != -1 && mouseEvent.getClickCount() == 2) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MailShow.fxml"));
                Parent root3 = fxmlLoader.load();
                Stage stage = new Stage();
                stage.setTitle("Oggetto");
                stage.setScene(new Scene(root3, 800, 600));
                stage.setResizable(false);
                stage.show();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
