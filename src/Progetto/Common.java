package Progetto;

import java.io.IOException;
import java.io.ObjectInputStream;
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

    private final String FIELDS_DELIMITER = ";*;";

    public Email(String sender, ArrayList<String> receivers, String subject, String body, Date sendingDate) {
        this.sender = sender;
        this.receivers = receivers;
        this.subject = subject;
        this.body = body;
        this.sendingDate = sendingDate;
    }

    public String toString(){
        String returnString = "+"+sender+FIELDS_DELIMITER;
        for(String s: receivers){
            returnString = returnString + " "+s;
        }
        returnString += FIELDS_DELIMITER;
        returnString += subject + FIELDS_DELIMITER;
        returnString += body + FIELDS_DELIMITER;
        returnString += sendingDate + FIELDS_DELIMITER;
        return returnString; //+nomeMittente@test.it;*;nomeDestinatario1@test.it nomeDestinatario2@test.it nomeDestinatarioN@test.it;*;argomento della mail;*; corpo della mail;*; dow mon dd hh:mm:ss zzz yyyy (Data in formato toString come specificato su oracle https://docs.oracle.com/javase/8/docs/api/java/util/Date.html);*;
    }
}