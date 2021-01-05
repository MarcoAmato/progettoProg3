package Progetto;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class Server extends Application {
    private static ArrayList<String> mailBoxes; //Contiene i nomi delle caselle di posta memorizzate nel database
    private static ArrayList<Email> emails;

    @Override
    public void start(Stage primaryStage) throws Exception{
        loadDatabase();
        startServer();
    }


    public static void main(String[] args) {
        launch(args);
    }

    public static void loadDatabase(){
        Scanner scanner = null;

        try{
            scanner = new Scanner(new File("src/database.txt"));

            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                switch (line.charAt(0)){
                    case '+': //La riga è una mail
                        Scanner innerScanner = new Scanner(line.substring(1));
                        innerScanner.useDelimiter(";*;");
                        String sender = innerScanner.next();
                        String receiversString = innerScanner.next();
                        ArrayList<String> receivers = new ArrayList<>();
                        Scanner receiversScanner = new Scanner(receiversString);
                        while(receiversScanner.hasNext()){
                            receivers.add(receiversScanner.next());
                        }
                        String subject = innerScanner.next();
                        String body = innerScanner.next();
                        Date sendingDate = null;
                        try {
                            sendingDate = new SimpleDateFormat("dow mon dd hh:mm:ss zzz yyyy").parse(innerScanner.next());
                        }catch (ParseException e){
                            e.printStackTrace();
                        }
                        emails.add(new Email(sender, receivers, subject, body, sendingDate));
                        break;
                    case '-': //La riga è un indirizzo di posta
                        mailBoxes.add(line);
                        break;
                    default:
                        throw new Error("The read line starts with unexpected character: " + line.charAt(0));
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (Error e){
            e.getMessage();
        }
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
