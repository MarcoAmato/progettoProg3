package Progetto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

class Common {
    public static <E> E getInputOfClass(ObjectInputStream inputStream, Class expectedInputClass){
        E sanitizedInput = null;

        try{
            Object input = inputStream.readObject();
            if(input == null){
                throw new Error("Expected "+ expectedInputClass.getName() +", received null");
            }else if (!(input.getClass() == expectedInputClass)){
                throw new Error("Expected "+ expectedInputClass.getName() +", received object of class: " + input.getClass().getName());
            }
            else{
                //input is not null and type arraylist
                sanitizedInput = (E)input;
            }
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }catch (Error e){
            System.out.println(e.getMessage());
        }

        return sanitizedInput;
    }
}

class Email{
    private String sender;
    private ArrayList<String> receivers;
    private String subject;
    private String body;
    private Date sendingDate;

    public static final String FIELDS_DELIMITER = ";#;";

    public Email(String sender, ArrayList<String> receivers, String subject, String body, Date sendingDate) {
        this.sender = sender;
        this.receivers = receivers;
        this.subject = subject;
        this.body = body;
        this.sendingDate = sendingDate;
    }

    public String toString(){
        String returnString = "+"+sender+FIELDS_DELIMITER;
        boolean firstReceiver = true;
        for(String s: receivers){
            if(firstReceiver){
                returnString += s;
                firstReceiver = false;
            }else{
                returnString += " " + s;
            }
        }
        returnString += FIELDS_DELIMITER;
        returnString += subject + FIELDS_DELIMITER;
        returnString += body + FIELDS_DELIMITER;

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dateString = formatter.format(sendingDate);
        returnString += dateString + FIELDS_DELIMITER;
        return returnString;
        /*  +nomeMittente@test.it
            ;*;nomeDestinatario1@test.it nomeDestinatario2@test.it nomeDestinatarioN@test.it
            ;*;argomento della mail
            ;*;corpo della mail
            ;*;dd/MM/yyyy HH:mm:ss(Data https://docs.oracle.com/javase/8/docs/api/java/util/Date.html)
         */
    }
}