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
    private static Database database;

    @Override
    public void start(Stage primaryStage){
        database = new Database(new File("src/database"));
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

    private static class Database{
        private final File databaseFile;
        private final ArrayList<String> emailAddressesArray; //Contiene i nomi delle caselle di posta memorizzate nel database
        private final ArrayList<Email> emailsArray;

        public Database(File databaseFile){
            this.databaseFile = databaseFile;
            this.emailAddressesArray = new ArrayList<>();
            this.emailsArray = new ArrayList<>();

            loadDatabase();
        }

        private void loadDatabase(){
            //scanner = new Scanner(new File("src/database"));
            Scanner scanner;

            try{
                scanner = new Scanner(databaseFile);

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

        public boolean emailIsRegistered(String accountEmail){
            return emailAddressesArray.contains(accountEmail);
        }

        public ArrayList<Email> getEmailsSent(String sender){
            ArrayList<Email> sentEmails = new ArrayList<>();

            for(Email e: emailsArray){
                if(e.getSender().equals(sender)) sentEmails.add(e);
            }

            return sentEmails;
        }

        public ArrayList<Email> getEmailsReceived(String receiver){
            ArrayList<Email> sentEmails = new ArrayList<>();

            for(Email e: emailsArray){
                if(e.getReceivers().contains(receiver)) sentEmails.add(e);
            }

            return sentEmails;
        }

        public void printDatabase(){
            for(String s: emailAddressesArray){
                System.out.println(s);
            }
            for(Email e: emailsArray){
                System.out.println(e);
            }
        }
    }

    private static class ClientHandler implements Runnable{
        private final Socket socket;

        private String emailAddress;
        private ArrayList<Email> emailsSent;
        private ArrayList<Email> emailsReceived;

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

                startConnection(inStream, outStream);

            }catch (ConnectException e){
                System.out.println("Client disconnected, bye bye");
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        private void startConnection(ObjectInputStream inStream, ObjectOutputStream outStream) throws IOException {
            String userEmail = Common.getInputOfClass(inStream,String.class);
            boolean emailIsOkay = database.emailIsRegistered(userEmail);
            if (emailIsOkay) emailAddress = userEmail;
            emailsSent = database.getEmailsSent(emailAddress);
            emailsReceived = database.getEmailsReceived(emailAddress);

            outStream.writeObject(emailsSent);
            outStream.writeObject(emailsReceived);

            System.out.println(emailsSent);
            System.out.println(emailsReceived);
        }
    }

}
