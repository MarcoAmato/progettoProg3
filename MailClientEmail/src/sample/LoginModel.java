package sample;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class LoginModel {

    private BooleanProperty giacomo = new SimpleBooleanProperty();

    public final boolean getGiacomo(){
        return giacomo.get();
    }

    public final void setGiacomo(Boolean value){
        giacomo.set(value);
    }

    public BooleanProperty ritornaGiacomo() {
        return giacomo;
    }

}
