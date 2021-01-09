package Progetto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ConnectException;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
                sanitizedInput = (E)input;
            }
        }catch(IOException | ClassNotFoundException e){
            if (e instanceof SocketException){
                throw new ConnectException();
            }
            e.printStackTrace();
        }

        return sanitizedInput;
    }
}

class Email{
    private final String sender;
    private final ArrayList<String> receivers;
    private final String subject;
    private final String body;
    private final Date sendingDate;

    public static final String FIELDS_DELIMITER = ";#;";

    public Email(String sender, ArrayList<String> receivers, String subject, String body, Date sendingDate) {
        this.sender = sender;
        this.receivers = receivers;
        this.subject = subject;
        this.body = body;
        this.sendingDate = sendingDate;
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
            ;*;argomento della mail
            ;*;corpo della mail
            ;*;dd/MM/yyyy HH:mm:ss(Data https://docs.oracle.com/javase/8/docs/api/java/util/Date.html)
         */
    }
}