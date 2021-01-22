package sample;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public class DataMail {
    final ObservableList<String> candidates = FXCollections.observableArrayList();
    public ListProperty<String> listalog = new SimpleListProperty<String>(candidates);

    public String getCandidate(int index) {
        return candidates.get(index);
    }

    public void setCandidate(int index, String element){
        candidates.set(index, element);
    }

    public ObservableList<String> ritornaCandidates() {
        return candidates;
    }

}
