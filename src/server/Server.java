package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        startServer();
    }


    public static void main(String[] args) {
        launch(args);
    }

    private static void startServer(){
        try{
            ServerSocket server = new ServerSocket(5000);
            while(true){ //Va aggiunto un sistema con Thread pool perchè attualmente vengono accettati tutti i client che richiedono la connessione generando nuovi Thread e questo non va bene per le prestazioni
                Socket client = server.accept();
                Runnable handler = new ClientHandler(client);
                Thread t = new Thread(handler);
                t.start();
            }
        }catch(IOException e){e.printStackTrace();}
    }

    private static class ClientHandler implements Runnable{
        private Socket socket;

        public ClientHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            ObjectInputStream inStream = null;
            ObjectOutputStream outStream = null;
            try{
                inStream = new ObjectInputStream(socket.getInputStream());
                outStream = new ObjectOutputStream(socket.getOutputStream());
                startConnection(inStream, outStream);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        private void startConnection(ObjectInputStream inStream, ObjectOutputStream outStream) throws IOException {
            String userEmail = Common.getInputOfClass(inStream,String.class);
            boolean stringIsValid = findAccount(userEmail);
            outStream.writeObject(stringIsValid);
        }

        public boolean findAccount(String accountEmail){
            //Questa funzione cerca l'indirizzo mail inserito nel database. Se lo trova restituisce true, altrimenti false
            return true;
        }
    }

}
