package Progetto;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MailController {
    @FXML private BorderPane borderPane;
    @FXML private HBox sentEmail;
    @FXML private HBox receivedEmail;
    @FXML private HBox newMail;
    @FXML private HBox deleteMail;
    @FXML private Text deleteHandler;
    @FXML private TableView<EmailPreview> mailList;
    @FXML private Button doDelete;
    @FXML private Button doNotDelete;
    @FXML private TableColumn<EmailPreview, String> mittente;
    @FXML private TableColumn<EmailPreview, String> oggetto;
    @FXML private TableColumn<EmailPreview, String> data;

    private ClientDataModel clientDataModel;
    private final ObservableList<EmailPreview> mailSentPreviews = FXCollections.observableArrayList(new ArrayList<>());
    private final ObservableList<EmailPreview> mailReceivedPreviews = FXCollections.observableArrayList(new ArrayList<>());

    public void initClientDataModel(ClientDataModel clientDataModel) {
        // assicura che il modello viene impostato una volta sola
        if (this.clientDataModel != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        this.clientDataModel = clientDataModel;
        mittente.setCellValueFactory(cellData -> cellData.getValue().senderProperty());
        oggetto.setCellValueFactory(cellData -> cellData.getValue().bodyProperty());
        data.setCellValueFactory(cellData -> cellData.getValue().sendingDateProperty());

        //Binds mailSentPreviews to clientDataModel.emailsSent
        fillEmailPreviewsWithEmails(mailSentPreviews, this.clientDataModel.emailsSentProperty());
        this.clientDataModel.emailsSentProperty().addListener(new EmailPreviewUpdater(this.mailSentPreviews));

        //Binds mailReceivedPreviews to clientDataModel.emailsReceived
        fillEmailPreviewsWithEmails(mailReceivedPreviews, this.clientDataModel.emailsReceivedProperty());
        this.clientDataModel.emailsSentProperty().addListener(new EmailPreviewUpdater(this.mailReceivedPreviews));

        //Client returns to login when connectionOkay becomes false
        this.clientDataModel.connectionOkayProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if(!newValue){
                    goToLogin(clientDataModel);
                }
            }
        });
    }

    private void goToLogin(ClientDataModel clientDataModel){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Stage stage = (Stage) borderPane.getScene().getWindow();
                stage.close();
                try{
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
                    Parent rootLogin = fxmlLoader.load();
                    LoginController loginController = fxmlLoader.getController();
                    loginController.initClientDataModel(clientDataModel);
                    Stage stageLogin = new Stage();
                    stageLogin.setTitle("Login");
                    stageLogin.setScene(new Scene(rootLogin, 500, 275));
                    stage.setResizable(false);
                    stageLogin.show();
                    stageLogin.setOnCloseRequest(event -> Platform.exit());
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void handleShowSentMail() {
        mailList.setItems(mailSentPreviews);
        setEmailPreviewTable(mailList);
    }

    public void handleShowReceivedMail() {
        mailList.setItems(mailReceivedPreviews);
        setEmailPreviewTable(mailList);
    }

    public void setEmailPreviewTable(TableView<EmailPreview> emailPreviewTableView){
        emailPreviewTableView.setRowFactory(tv -> {
            TableRow<EmailPreview> row = new TableRow<>();
            row.setOnMouseClicked(event->{
                if(event.getClickCount() == 2 && (!row.isEmpty()) ){
                    EmailPreview rowEmailPreview = row.getItem();
                    goToMailShow(rowEmailPreview.getEmail());
                }
            });
            return row;
        });
    }

    private void goToMailShow(Email emailToShow){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MailShow.fxml"));
            Parent parentRoot = fxmlLoader.load();
            MailShowController mailShowController = fxmlLoader.getController();
            mailShowController.initClientDataModel(this.clientDataModel, emailToShow);
            Stage stage = new Stage();
            stage.setTitle(emailToShow.getSubject());
            stage.setScene(new Scene(parentRoot, 800, 600));
            stage.setResizable(false);
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Dobbiamo gestire questa funzione importando il file giusto
    public void handleNewMail(MouseEvent mouseEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MailCreator.fxml"));
            Parent root2 = fxmlLoader.load();
            NewMailController newMailController = fxmlLoader.getController();
            newMailController.initClientDataModel(this.clientDataModel);
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

    /*public void handleShowMail(MouseEvent mouseEvent) {
        if(mailList.getSelectionModel().getSelectedIndex() != -1 && mouseEvent.getClickCount() == 2) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MailShow.fxml"));
                Parent root3 = fxmlLoader.load();
                MailShowController mailShowController = fxmlLoader.getController();
                mailShowController.initClientDataModel(this.clientDataModel);
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
    }*/

    public void handleRestoreDelete(ActionEvent actionEvent) {
        deleteHandler.setText("");
        doDelete.setDisable(true);
        doNotDelete.setDisable(true);
        doDelete.setVisible(false);
        doNotDelete.setVisible(false);
    }

    public ObservableList<EmailPreview> mailSentListProperty() {
        return mailSentPreviews;
    }

    public EmailPreview getMailSentPreviews(int index) {
        return mailSentPreviews.get(index);
    }

    public EmailPreview setMailSentPreviews(int index, EmailPreview element) {
        return mailSentPreviews.set(index, element);
    }

    public ObservableList<EmailPreview> mailReceivedPreviewsProperty() {
        return mailReceivedPreviews;
    }

    public EmailPreview getMailPreviews(int index) {
        return mailReceivedPreviews.get(index);
    }

    public EmailPreview setReceivedMails(int index, EmailPreview element) {
        return mailReceivedPreviews.set(index, element);
    }

    public void deselection(MouseEvent mouseEvent) { mailList.getSelectionModel().clearSelection(); }

    private void fillEmailPreviewsWithEmails(ObservableList<EmailPreview> emailPreviews, ObservableList<Email> emails){
        for(Email email: emails){
            emailPreviews.add(new EmailPreview(email));
        }
    }

    public void HandleOutGlowEmail() { sentEmail.setEffect(new Glow(0)); }

    public void MouseOutReceivedEmail() { receivedEmail.setEffect(new Glow(0)); }

    public void MouseOutNewMail() { newMail.setEffect(new Glow(0)); }

    public void MouseOutDeleteMail() { deleteMail.setEffect(new Glow(0)); }

    public void HandleGlowSentMail() { sentEmail.setEffect(new Glow(0.8)); }

    public void HandleGlowReceivedMail() { receivedEmail.setEffect(new Glow(0.8)); }

    public void HandleGlowNewMail() { newMail.setEffect(new Glow(0.8)); }

    public void HandleGlowDeleteMail() { deleteMail.setEffect(new Glow(0.8)); }

    public void HandleOutGlowEmail(MouseEvent mouseEvent) { sentEmail.setEffect(new Glow(0)); }

    public void MouseOutReceivedEmail(MouseEvent mouseEvent) { receivedEmail.setEffect(new Glow(0)); }

    public void MouseOutNewMail(MouseEvent mouseEvent) { newMail.setEffect(new Glow(0)); }

    public void MouseOutDeleteMail(MouseEvent mouseEvent) { deleteMail.setEffect(new Glow(0)); }


    private static class EmailPreviewUpdater implements ListChangeListener<Email> {
        private final ObservableList<EmailPreview> emailsPreviewToUpdate;

        public EmailPreviewUpdater (ObservableList<EmailPreview> emailsPreviewToUpdate) {
            this.emailsPreviewToUpdate = emailsPreviewToUpdate;
        }

        @Override
        public void onChanged(Change<? extends Email> change) {
            while(change.next()){
                if(change.wasAdded()){
                    for(Email email: change.getAddedSubList()){
                        emailsPreviewToUpdate.add(new EmailPreview(email));
                    }
                }else if(change.wasRemoved()){
                    for(Email email: change.getRemoved()){
                        emailsPreviewToUpdate.removeIf(preview -> email == preview.getEmail());
                    }
                }
            }
        }
    }

    private static class EmailPreview implements Serializable {
        private SimpleStringProperty sender;
        private SimpleStringProperty body;
        private SimpleStringProperty sendingDate;
        private Email emailConnected;

        public EmailPreview(Email emailToCopy){
            this.sender = new SimpleStringProperty(emailToCopy.getSender());
            this.body = new SimpleStringProperty(emailToCopy.getBody());
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String emailToCopyDateToString = formatter.format(emailToCopy.getSendingDate());
            this.sendingDate = new SimpleStringProperty(emailToCopyDateToString);
            this.emailConnected = emailToCopy;
        }

        public String getSender() { return sender.get(); }

        public StringProperty senderProperty() { return sender; }

        public String getBody() { return body.get(); }

        public StringProperty bodyProperty() { return body; }

        public String getSendingDate() { return sendingDate.toString(); }

        public StringProperty sendingDateProperty() { return sendingDate; }

        public Email getEmail(){
            return emailConnected;
        }
    }
}
