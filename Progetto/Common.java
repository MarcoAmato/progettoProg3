package Progetto;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

class Common {
    public static <E> E getInputOfClass(ObjectInputStream inputStream, Class<E> expectedInputClass) throws ConnectException{
        E sanitizedInput = null;

        try{
            Object input = inputStream.readObject();
            if(input == null){
                System.out.println("Expected "+ expectedInputClass.getName() +", received null");
            }else if (!(input.getClass() == expectedInputClass)){
                System.out.println("Expected "+ expectedInputClass.getName() +", received object of class: " + input.getClass().getName());
            }
            else{
                //input is not null and type arraylist
                sanitizedInput = expectedInputClass.cast(input);
            }
        }catch(IOException | ClassNotFoundException e){
            if (e instanceof SocketException){
                throw new ConnectException();
            }
            e.printStackTrace();
        }

        return sanitizedInput;
    }

   public static <T> List <T> ConvertToSyncArrayList(List<?> l, Class<T> newSyncArrayListClass){
        if(l == null){
            throw new RuntimeException("Error, could not convert null ArrayList");
        }

        List<T> newSyncArrayList = Collections.synchronizedList(new ArrayList<>());

        for (Object element: l) {
            if(element == null){
                throw new RuntimeException("Error, trying to insert null element in ArrayList");
            }else if(element.getClass() != newSyncArrayListClass){
                throw new RuntimeException("Error, trying to insert an object of class "+element.getClass()+" in Arraylist of type " + newSyncArrayListClass.getName());
            }
            else{
                newSyncArrayList.add(newSyncArrayListClass.cast(element));
            }
        }

        return newSyncArrayList;
    }
}

class CSMex {
    //Here are found all the constants that client and server use to know what the next objects will be

    //Server sent constants
    public static final int NEW_EMAIL_RECEIVED = 0;
    public static final int EMAIL_DELETED = 1;
    public static final int FORCE_DISCONNECTION = 2;

    //Client sent constants
    public static final int NEW_EMAIL_TO_SEND = 0;
    public static final int DISCONNECTION = 1;
    public static final int CHECK_EMAIL_ADDRESS_EXISTS = 2;
    public static final int DELETE_EMAIL = 3;
}

class Email implements Serializable, Comparable<Email> {
    private final String sender;
    private final ArrayList<String> receivers;
    private final String subject;
    private final String body;
    private final Date sendingDate;

    public static final String FIELDS_DELIMITER = ";#;";

    public Email(String sender, ArrayList<String> receivers, String subject, String body, Date sendingDate) {
        body = body.replace("\n", " ");
        this.sender = sender;
        this.receivers = receivers;
        this.subject = subject;
        this.body = body;
        this.sendingDate = sendingDate;
    }

    public String getSender() {
        return sender;
    }

    public ArrayList<String> getReceivers() {
        return receivers;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public Date getSendingDate() {
        return sendingDate;
    }

    public String toString(){
        StringBuilder returnString = new StringBuilder("+" + sender + FIELDS_DELIMITER);
        boolean firstReceiver = true;
        for(String s: receivers){
            if(firstReceiver){
                returnString.append(s);
                firstReceiver = false;
            }else{
                returnString.append(" ").append(s);
            }
        }
        returnString.append(FIELDS_DELIMITER);
        returnString.append(subject).append(FIELDS_DELIMITER);
        returnString.append(body).append(FIELDS_DELIMITER);

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dateString = formatter.format(sendingDate);
        returnString.append(dateString).append(FIELDS_DELIMITER);
        return returnString.toString();
        /*  +nomeMittente@test.it
            ;*;nomeDestinatario1@test.it nomeDestinatario2@test.it nomeDestinatarioN@test.it
            ;*;subject of mail
            ;*;body of mail
            ;*;dd/MM/yyyy HH:mm:ss(Date https://docs.oracle.com/javase/8/docs/api/java/util/Date.html)
         */
    }

    @Override
    public int compareTo(Email emailCompared) {
        Date thisSendingDate = this.sendingDate;
        Date emailComparedSendingDate = this.sendingDate;
        if(thisSendingDate.before(emailComparedSendingDate)){
            return 1;
        }else if(thisSendingDate.after(emailComparedSendingDate)){
            return -1;
        }else{
            return 0;
        }
    }
}

class CloseOnLostConnection implements ChangeListener<Boolean> {

    private final Pane paneToClose;

    public CloseOnLostConnection(Pane paneToClose) {
        this.paneToClose = paneToClose;
    }
    @Override
    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
        if(!newValue){
            Platform.runLater(() -> {
                Stage stage = (Stage) paneToClose.getScene().getWindow();
                stage.close();
            });
        }
    }
}




