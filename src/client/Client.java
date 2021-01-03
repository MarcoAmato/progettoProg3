package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Client extends Application {

    @Override
    public void start(Stage primaryStage) {
        startClient();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void startClient(){
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            Socket serverSocket = new Socket(localhost, 5000);
            String email = "matteo.trabacchino@libero.it"; //Qui deve essere preso l'input dell'utente tramite la view

            ObjectOutputStream out = new ObjectOutputStream(serverSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(serverSocket.getInputStream());

            out.writeObject(email);

            Boolean response = Common.getInputOfClass(in, Boolean.class);

            System.out.println(response);
        }catch(UnknownHostException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
