package Progetto;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class Client extends Application {
    private static String emailAddress;
    private static ArrayList<Email> emailsReceived;
    private static ArrayList<Email> emailsSent;

    @Override
    public void start(Stage primaryStage) {
        ObjectInputStream inStream = null;
        ObjectOutputStream outStream = null;
        boolean connectionSuccess = false;

        try{
            InetAddress localhost = InetAddress.getLocalHost();
            Socket serverSocket = new Socket(localhost, 5000);

            outStream = new ObjectOutputStream(serverSocket.getOutputStream());
            inStream = new ObjectInputStream(serverSocket.getInputStream());

            connectionSuccess = true;
        }catch (IOException e){
            e.printStackTrace();
            //dai messaggio di connessione non possibile alla view
        }

        if(connectionSuccess){
            boolean terminated = false;
            while(!terminated){
                try {
                    getAccessFromServer(inStream, outStream);

                    terminated = true;
                }catch (ConnectException e){
                    System.out.println("Connection interrupted. Trying to connect again...");
                    try {
                        sleep(5000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void getAccessFromServer(ObjectInputStream inStream, ObjectOutputStream outputStream) throws ConnectException {
        try {
            boolean emailIsOkay = false;
            String emailFromInput = null;

            while(!emailIsOkay){ //loops until server recognises the email the user wrote
                emailFromInput = "prova@prova.vincy"; //Qui deve essere preso l'input dell'utente tramite la view

                outputStream.writeObject(emailFromInput);
                emailIsOkay = Common.getInputOfClass(inStream, Boolean.class);
                if(!emailIsOkay){
                    //dai feedback negativo alla view del client
                }
            }

            ArrayList<Email> emailsReceivedInput = Common.getInputOfClass(inStream, ArrayList.class);
            ArrayList<Email> emailsSentInput = Common.getInputOfClass(inStream, ArrayList.class);

            if(     emailsReceivedInput == null
                    || emailsSentInput == null
                    || !Email.isArrayListOfEmails(emailsReceivedInput)
                    || !Email.isArrayListOfEmails(emailsSentInput)
            ){
                throw new RuntimeException("Error, the emails got from the server have something wrong");
            }else{
                emailAddress = emailFromInput;
                emailsReceived = emailsReceivedInput;
                emailsSent = emailsSentInput;
            }

            System.out.println(emailAddress);
            for(Email e: emailsSent){
                System.out.println(e);
            }
            for(Email e: emailsReceived){
                System.out.println(e);
            }

        }catch (SocketException e){
            throw new ConnectException();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}