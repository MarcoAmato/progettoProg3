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

import static java.lang.Thread.sleep;

public class Client extends Application {

    @Override
    public void start(Stage primaryStage) {
        boolean terminated = false;
        while(!terminated){
            try {
                startClient();
                terminated = true;
            }catch (ConnectException e){}
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void startClient() throws ConnectException {
        try {
            InetAddress localhost = InetAddress.getLocalHost();

            Socket serverSocket = new Socket(localhost, 5000);


            String email = "bubu@bubu.bubuz"; //Qui deve essere preso l'input dell'utente tramite la view

            ObjectOutputStream out = new ObjectOutputStream(serverSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(serverSocket.getInputStream());

            while(true){
                out.writeObject(email);

                boolean emailIsOkay = Common.getInputOfClass(in, Boolean.class);
                System.out.println(emailIsOkay);
            }

        }catch (SocketException e){
            System.out.println("Connection interrupted. Trying to connect again...");
            try {
                sleep(5000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            throw new ConnectException();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}


/*while(true) {
        try {
        serverSocket = new Socket(localhost, 5000);
        }catch (ConnectException e){
        System.out.println("Connection interrupted. Trying to connect again...");
        }
        try {
        sleep(5000);
        } catch (InterruptedException e) {
        e.printStackTrace();
        }
        }*/
