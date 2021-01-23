package sample;


import javafx.fxml.FXML;
import javafx.scene.control.ListView;


public class Controller {
    private Datamodel model;

    @FXML ListView<String> loglist;

    public void initModel(Datamodel model) {
        // assicura che il modello viene impostato una volta sola
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }

        this.model = model;
    }

    public void createlist(){
        loglist.setItems(model.ritornaCandidates());
    }

    public void loop() throws InterruptedException {
        String[] cars = {"Volvo", "BMW", "Ford", "Mazda",};
        for(int i = 0; i < cars.length; i++) {
            (model.ritornaCandidates()).add(cars[i]);

        }
    }

}
