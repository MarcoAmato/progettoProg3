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
        boolean terminated = false;
        while(!terminated){
            try {
                startClient();
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

    public static void main(String[] args) {
        launch(args);
    }

    public static void startClient() throws ConnectException {
        try {
            InetAddress localhost = InetAddress.getLocalHost();

            Socket serverSocket = new Socket(localhost, 5000);

            String emailFromInput = "prova@prova.vincy"; //Qui deve essere preso l'input dell'utente tramite la view

            ObjectOutputStream out = new ObjectOutputStream(serverSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(serverSocket.getInputStream());

            out.writeObject(emailFromInput);

            ArrayList<Email> emailsReceivedInput = Common.getInputOfClass(in, ArrayList.class);
            ArrayList<Email> emailsSentInput = Common.getInputOfClass(in, ArrayList.class);

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