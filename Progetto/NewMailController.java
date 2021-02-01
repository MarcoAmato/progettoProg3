package Progetto;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;


public class NewMailController {
    @FXML private TextArea mailText;
    @FXML private Text Controllo;
    @FXML private TextField receivers;
    @FXML private ListView<String> receiversList;
    @FXML private Text DeleteText;

    private ClientDataModel model;
    boolean Selected = false;
    private ObservableList<String> emailReceivers = new SimpleListProperty<>(FXCollections.observableArrayList());

    public void HandleSentMail(ActionEvent actionEvent) {
        String object = mailText.getText();


        /*String luca = MailText.getText();
        if(luca.contains("s")) {
            Controllo.setText("Luca contiene s");
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished( event -> Controllo.setText("") );
            delay.play();
        } else {
            Controllo.setText("Luca non contiene s");
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished( event -> Controllo.setText("") );
            delay.play();
        }*/
    }

    public void handleAddReceiver(ActionEvent actionEvent) {
        if(model.emailAddressExists(receivers.getText())){
            receiversList.add
        }

        model.ritornaCandidates().add(receivers.getText());
        receivers.setText("");
    }

    public void initDataMail(DataMail model) {
        // assicura che il modello viene impostato una volta sola
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }

        this.model = model;
        receiversList.setItems(model.ritornaCandidates());
    }

    public void DeleteReceiversHandler(ActionEvent actionEvent) {
        if (Selected) {
            final int val = receiversList.getSelectionModel().getSelectedIndex();
            receiversList.getItems().remove(val);
        }
    }

    public void ReceiverSelected(MouseEvent mouseEvent) {
        Selected = true;
    }
}
