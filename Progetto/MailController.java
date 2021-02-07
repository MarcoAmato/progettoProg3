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
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MailController {
    @FXML private BorderPane borderPane;
    @FXML private Label mailIdentifier;
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
    @FXML private Text mailUpdater;

    private EmailPreview emailPreviewSelected;
    private ClientDataModel clientDataModel;
    private final ObservableList<EmailPreview> mailSentPreviews = FXCollections.observableArrayList(new ArrayList<>());
    private final ObservableList<EmailPreview> mailReceivedPreviews = FXCollections.observableArrayList(new ArrayList<>());

    public void initClientDataModel(ClientDataModel clientDataModel) {
        if (this.clientDataModel != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        this.clientDataModel = clientDataModel;
        mailIdentifier.setText("Email di " + clientDataModel.getEmailAddress());
        mittente.setCellValueFactory(cellData -> cellData.getValue().senderProperty());
        oggetto.setCellValueFactory(cellData -> cellData.getValue().subjectProperty());
        data.setCellValueFactory(cellData -> cellData.getValue().sendingDateProperty());

        //Binds mailSentPreviews to clientDataModel.emailsSent
        fillEmailPreviewsWithEmails(mailSentPreviews, this.clientDataModel.emailsSentProperty());
        this.clientDataModel.emailsSentProperty().addListener(new EmailPreviewUpdater(this.mailSentPreviews));

        //Binds mailReceivedPreviews to clientDataModel.emailsReceived
        fillEmailPreviewsWithEmails(mailReceivedPreviews, this.clientDataModel.emailsReceivedProperty());
        this.clientDataModel.emailsReceivedProperty().addListener(new EmailPreviewUpdater(this.mailReceivedPreviews));

        //Client returns to login when connectionOkay becomes false
        this.clientDataModel.connectionOkayProperty().addListener
            (new ConnectionFailedHandler(this.borderPane, this.clientDataModel));

        mailReceivedPreviews.addListener((ListChangeListener<EmailPreview>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    System.out.println("Test");
                    mailUpdater.setText("Sono arrivate nuove mail!");
                    mailUpdater.setFill(Color.RED);
                }
            }
        });
    }

    public void handleShowSentMail() {
        deleteMail.setDisable(false);
        deleteMail.setVisible(true);
        mailList.setItems(mailSentPreviews);
        setEmailPreviewTable(mailList);
    }

    public void handleShowReceivedMail() {
        deleteMail.setDisable(true);
        deleteMail.setVisible(false);
        mailList.setItems(mailReceivedPreviews);
        setEmailPreviewTable(mailList);
        mailUpdater.setFill(Color.BLACK);
        mailUpdater.setText("Non ci sono nuove mail");
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NewMail.fxml"));
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
            this.emailPreviewSelected = mailList.getSelectionModel().getSelectedItem();
            deleteHandler.setText("Sei sicuro di voler eliminare questa mail?");
            doDelete.setDisable(false);
            doNotDelete.setDisable(false);
            doDelete.setVisible(true);
            doNotDelete.setVisible(true);
        } else {
            deleteHandler.setText("Nessuna mail selezionata");
        }
    }

    public void handleDelete(){
        if(this.emailPreviewSelected != null){
            Email emailToDelete = this.emailPreviewSelected.getEmail();
            boolean emailIsDeleted = clientDataModel.deleteEmail(emailToDelete);
            System.out.println("Email eliminata: " + emailIsDeleted);
            handleRestoreDelete();
        }
    }

    public void handleRestoreDelete() {
        this.emailPreviewSelected = null;
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

    public void HandleOutSentEmail() { sentEmail.setEffect(new Glow(0)); }

    public void MouseOutReceivedEmail() { receivedEmail.setEffect(new Glow(0)); }

    public void MouseOutNewMail() { newMail.setEffect(new Glow(0)); }

    public void MouseOutDeleteMail() { deleteMail.setEffect(new Glow(0)); }

    public void HandleGlowSentMail() { sentEmail.setEffect(new Glow(0.8)); }

    public void HandleGlowReceivedMail() { receivedEmail.setEffect(new Glow(0.8)); }

    public void HandleGlowNewMail() { newMail.setEffect(new Glow(0.8)); }

    public void HandleGlowDeleteMail() { deleteMail.setEffect(new Glow(0.8)); }

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
        private SimpleStringProperty subject;
        private SimpleStringProperty sendingDate;
        private Email emailConnected;

        public EmailPreview(Email emailToCopy){
            this.sender = new SimpleStringProperty(emailToCopy.getSender());
            this.subject = new SimpleStringProperty(emailToCopy.getSubject());
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String emailToCopyDateToString = formatter.format(emailToCopy.getSendingDate());
            this.sendingDate = new SimpleStringProperty(emailToCopyDateToString);
            this.emailConnected = emailToCopy;
        }

        public String getSender() { return sender.get(); }

        public StringProperty senderProperty() { return sender; }

        public String getSubject() { return subject.get(); }

        public StringProperty subjectProperty() { return subject; }

        public String getSendingDate() { return sendingDate.toString(); }

        public StringProperty sendingDateProperty() { return sendingDate; }

        public Email getEmail(){
            return emailConnected;
        }
    }

    private static class ConnectionFailedHandler implements ChangeListener<Boolean> {

        private final Pane paneToClose;
        private final ClientDataModel clientDataModel;

        public ConnectionFailedHandler(Pane paneToClose, ClientDataModel clientDataModel) {
            this.paneToClose = paneToClose;
            this.clientDataModel = clientDataModel;
        }
        @Override
        public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
            if(!newValue){
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Stage stage = (Stage) paneToClose.getScene().getWindow();
                        stage.close();
                        try{
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
                            Parent rootLogin = fxmlLoader.load();
                            LoginController loginController = fxmlLoader.getController();
                            loginController.initClientDataModel(clientDataModel);
                            Stage stageLogin = new Stage();
                            stageLogin.setTitle("Login");
                            stageLogin.setScene(new Scene(rootLogin, 500, 275));
                            stageLogin.setResizable(false);
                            stageLogin.show();
                            stageLogin.setOnCloseRequest(event -> Platform.exit());
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }
}
