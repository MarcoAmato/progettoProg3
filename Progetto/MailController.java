package Progetto;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
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

    public void handleShowAllMail(MouseEvent mouseEvent) {
        System.out.println("Funziona!");
    }



    public void MouseOutShowMail(MouseEvent mouseEvent) {
        AllEmail.setEffect(new Glow(0));
    }

    public void HandleGlowMail(MouseEvent mouseEvent) {
        if (AllEmail.getId()==mouseEvent.getPickResult().getIntersectedNode().getId()) {
            AllEmail.setEffect(new Glow(0.8));
        } else if (SentEmail.getId()==mouseEvent.getPickResult().getIntersectedNode().getId()) {
            SentEmail.setEffect(new Glow(0.8));
        } else if (ReceivedEmail.getId()==mouseEvent.getPickResult().getIntersectedNode().getId()) {
            ReceivedEmail.setEffect(new Glow(0.8));
        } else if (NewMail.getId()==mouseEvent.getPickResult().getIntersectedNode().getId()) {
            NewMail.setEffect(new Glow(0.8));
        } else if (DeleteMail.getId()==mouseEvent.getPickResult().getIntersectedNode().getId()) {
            DeleteMail.setEffect(new Glow(0.8));
        }
    }

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

    public void handleShowSentMail(MouseEvent mouseEvent) {

    }

    public void handleShowReceivedMail(MouseEvent mouseEvent) {

    }

    //Dobbiamo gestire questa funzione importando il file giusto
    /*public void handleNewMail(MouseEvent mouseEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MailCreator.fxml"));
            Parent root2 = fxmlLoader.load();
            NewMailController controller = fxmlLoader.getController();
            DataMail model = new DataMail();
            controller.initDataMail(model);
            Stage stage = new Stage();
            stage.setTitle("Crea nuova mail");
            stage.setScene(new Scene(root2, 800, 600));
            stage.setResizable(false);
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public void handleDeleteMail(MouseEvent mouseEvent) {
        DeleteHandler.setText("Seleziona la mail da eliminare");
    }

}
