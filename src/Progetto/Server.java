package Progetto;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class Server extends Application {
    private static final ArrayList<String> emailAddressesArray = new ArrayList<>(); //Contiene i nomi delle caselle di posta memorizzate nel database
    private static final ArrayList<Email> emailsArray = new ArrayList<>();

    @Override
    public void start(Stage primaryStage){
        loadDatabase();
        startServer();
    }

    public static void printDatabase(){
        for(String s: emailAddressesArray){
            System.out.println(s);
        }
        for(Email e: emailsArray){
            System.out.println(e);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }

    public static void loadDatabase(){
        Scanner scanner;

        try{
            scanner = new Scanner(new File("src/database"));

            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                switch (line.charAt(0)) {
                    case '+' -> {
                        //La riga è una mail
                        Scanner innerScanner = new Scanner(line.substring(1));
                        innerScanner.useDelimiter(Email.FIELDS_DELIMITER);
                        String sender = innerScanner.next();
                        String receiversString = innerScanner.next();
                        ArrayList<String> receivers = new ArrayList<>();
                        Scanner receiversScanner = new Scanner(receiversString);
                        while (receiversScanner.hasNext()) {
                            receivers.add(receiversScanner.next());
                        }
                        String subject = innerScanner.next();
                        String body = innerScanner.next();
                        Date sendingDate = null;
                        try {
                            sendingDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(innerScanner.next());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        emailsArray.add(new Email(sender, receivers, subject, body, sendingDate));
                    }
                    case '-' -> //La riga è un indirizzo di posta
                        emailAddressesArray.add(line.substring(1));
                    default -> System.out.println("Error in parsing database. The read line starts with unexpected character: " + line.charAt(0));
                }
            }
        }catch (IOException e){
            e.printStackTrace();
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
        private final Socket socket;

        public ClientHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            ObjectInputStream inStream;
            ObjectOutputStream outStream;
            try{
                inStream = new ObjectInputStream(socket.getInputStream());
                outStream = new ObjectOutputStream(socket.getOutputStream());
                while(true){
                    startConnection(inStream, outStream);
                }
            }catch (ConnectException e){
                System.out.println("Client disconnected, bye bye");
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        private void startConnection(ObjectInputStream inStream, ObjectOutputStream outStream) throws IOException {
            String userEmail = Common.getInputOfClass(inStream,String.class);
            boolean emailIsOkay = emailIsRegistered(userEmail);
            outStream.writeObject(emailIsOkay);
        }

        public boolean emailIsRegistered(String accountEmail){
            return emailAddressesArray.contains(accountEmail);
        }
    }

}
