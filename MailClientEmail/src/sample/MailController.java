package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.RED;

public class MailController {
    @FXML private HBox AllEmail;
    @FXML private HBox SentEmail;
    @FXML private HBox ReceivedEmail;
    @FXML private HBox NewMail;
    @FXML private HBox DeleteMail;
    @FXML private Text MailReceivedCheck;
    @FXML private Circle MailDot;
    @FXML private TableView<String> MailTable;

    private LoginModel model;

    public void handleShowAllMail(MouseEvent mouseEvent) {
        System.out.println("Funziona!");
    }

    public void MouseGlowAllMail(MouseEvent mouseEvent) {
        AllEmail.setEffect(new Glow(0.8));
    }

    public void MouseOutAllMail(MouseEvent mouseEvent) {
        AllEmail.setEffect(new Glow(0));
    }

    public void HandleGlowReceivedMail(MouseEvent mouseEvent) { ReceivedEmail.setEffect(new Glow(0.8)); }

    public void MouseOutReceivedEmail(MouseEvent mouseEvent) {
        ReceivedEmail.setEffect(new Glow(0));
    }

    public void HandleGlowNewMail(MouseEvent mouseEvent) { NewMail.setEffect(new Glow(0.8)); }

    public void MouseOutNewMail(MouseEvent mouseEvent) {
        NewMail.setEffect(new Glow(0));
    }

    public void HandleGlowDeleteMail(MouseEvent mouseEvent) { AllEmail.setEffect(new Glow(0.8)); }

    public void MouseOutDeleteMail(MouseEvent mouseEvent) {
        DeleteMail.setEffect(new Glow(0));
    }

    public void HandleGlowSentMail(MouseEvent mouseEvent) { SentEmail.setEffect(new Glow(0.8)); }

    public void HandleOutSentMail(MouseEvent mouseEvent) {
        SentEmail.setEffect(new Glow(0));
    }

    public void handleShowSentMail(MouseEvent mouseEvent) {
        //MailTable.setitems(qualcosa);
    }

    public void handleShowReceivedMail(MouseEvent mouseEvent) {
        //Maillist.setitems(qualcosa);
    }

    public void handleNewMail(MouseEvent mouseEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MailCreator.fxml"));
            Parent root2 = fxmlLoader.load();
            NewMailController controller = fxmlLoader.getController();
            DataMail modelMail = new DataMail();
            controller.initDataMail(modelMail);
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

    public void HandleTrialUpdate(ActionEvent actionEvent) {
        MailReceivedCheck.setFill(RED);
        MailReceivedCheck.setText("Sono arrivate nuove mail!");
        MailDot.setFill(RED);
    }

    public void HandleTrialRestore(ActionEvent actionEvent) {
        MailReceivedCheck.setFill(BLACK);
        MailReceivedCheck.setText("Non ci sono nuove mail");
        MailDot.setFill(BLACK);
    }

    public void HandlerAddArray(ActionEvent actionEvent) {
        ArrayList<String> luca = new ArrayList<>();
        luca.add("giacomo");
        Email prova = new Email("marco", luca, "prova", "maildiprova", new Date());
        MailReceivedCheck.setFill(RED);
        MailReceivedCheck.setText("Sono arrivate nuove mail!");
        MailDot.setFill(RED);
        model.ritornaMail().add(prova);
        String mom = model.ritornaAnteprima(prova);
        model.ritornaProva().add(mom);
    }

    public void initModel(LoginModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }

        this.model = model;
        MailTable.setItems(model.ritornaProva());
        for (Email e : model.ritornaMail()) {
            String t = model.ritornaAnteprima(e);
            model.ritornaProva().add(t);
        }
    }

    public void HandleMailClicked(MouseEvent mouseEvent) {
        if(MailTable.getSelectionModel().getSelectedIndex() != -1 && mouseEvent.getClickCount() == 2) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MailShow.fxml"));
                Parent root3 = fxmlLoader.load();
                Stage stage = new Stage();
                stage.setTitle("Crea nuova mail");
                stage.setScene(new Scene(root3, 800, 600));
                stage.setResizable(false);
                stage.show();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void Deselection(MouseEvent mouseEvent) {
        MailTable.getSelectionModel().clearSelection();
    }
}
