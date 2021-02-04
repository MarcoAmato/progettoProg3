package Progetto;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
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
    @FXML private Text deleteHandler;
    @FXML private TableView<EmailPreview> mailList;
    @FXML private BorderPane mailView;
    @FXML private Button doDelete;
    @FXML private Button doNotDelete;
    @FXML private TableColumn<EmailPreview, String> Mittente;
    @FXML private TableColumn<EmailPreview, String> Oggetto;
    @FXML private TableColumn<EmailPreview, String> Data;

    private ClientDataModel clientDataModel;

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
            controller.initClientDataModel(this.clientDataModel);
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

    public void handleDeleteSelectedMail(MouseEvent mouseEvent) {
        if(mailList.getSelectionModel().getSelectedIndex() != -1) {
            deleteHandler.setText("Sei sicuro di voler eliminare questa mail?");
            doDelete.setDisable(false);
            doNotDelete.setDisable(false);
            doDelete.setVisible(true);
            doNotDelete.setVisible(true);
        } else {
            deleteHandler.setText("Nessuna mail selezionata");
        }
    }


    public void initClientDataModel(ClientDataModel model) {
        // assicura che il modello viene impostato una volta sola
        if (this.clientDataModel != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        this.clientDataModel = model;
        Mittente.setCellValueFactory(cellData -> cellData.getValue().senderProperty());
        Oggetto.setCellValueFactory(cellData -> cellData.getValue().bodyProperty());
        Data.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        mailList.setItems(model.ritornaMailList());
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

    public void handleRestoreDelete(ActionEvent actionEvent) {
        deleteHandler.setText("");
        doDelete.setDisable(true);
        doNotDelete.setDisable(true);
        doDelete.setVisible(false);
        doNotDelete.setVisible(false);
    }

    public void Deselection(MouseEvent mouseEvent) { mailList.getSelectionModel().clearSelection(); }
}
