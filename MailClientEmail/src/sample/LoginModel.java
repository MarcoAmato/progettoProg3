package sample;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LoginModel {

    private BooleanProperty giacomo = new SimpleBooleanProperty();

    public final boolean getGiacomo() {
        return giacomo.get();
    }

    public final void setGiacomo(Boolean value) {
        giacomo.set(value);
    }

    public BooleanProperty ritornaGiacomo() {
        return giacomo;
    }

    final ObservableList<Email> mail = FXCollections.observableArrayList();

    public ListProperty<Email> MailList = new SimpleListProperty<Email>(mail);

    public Email getMailList(int index) {
        return mail.get(index);
    }

    public Email setMailList(int index, Email element) {
        return mail.set(index, element);
    }

    public ObservableList<Email> ritornaMail() {
        return mail;
    }


    final ObservableList<String> prova = FXCollections.observableArrayList();

    public ObservableList<String> ritornaProva() {
        return prova;
    }

    public String ritornaAnteprima(Email h) {
            String mailElement = (h.getSender() + " " + h.getBody() + " " + h.getSendingDate());
        return mailElement;
    }
}
