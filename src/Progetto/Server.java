package Progetto;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server extends Application {
    private static final int NUM_THREAD = 100;

    private static final ExecutorService clientHandlerExecutor = Executors.newFixedThreadPool(NUM_THREAD);
    private static final Map<String, ClientHandler> currentClientsMap = new HashMap<>();
    private static Database database;

    @Override
    public void start(Stage primaryStage){
        database = new Database(new File("src/database"));
        startServer();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void addClientHandlerToHashMap(String email, ClientHandler clientHandler){
        currentClientsMap.put(email, clientHandler);
    }

    public static ClientHandler getClientHandlerFromEmail(String email){
        return currentClientsMap.get(email);
    }

    public static void saveEmail(Email email){
        database.saveEmail(email);
    }

    private static void startServer(){
        try{
            ServerSocket server = new ServerSocket(5000);
            while(true){
                Socket client = server.accept();
                Runnable handler = new ClientHandler(client);
                clientHandlerExecutor.execute(handler);
            }
        }catch(IOException e){e.printStackTrace();}
    }

    private static class Database{
        private final File databaseFile;
        private final ArrayList<String> emailAddressesArray; //Contiene i nomi delle caselle di posta memorizzate nel database
        private final ArrayList<Email> emailsArray;
        private final ReadWriteLock rwl = new ReentrantReadWriteLock();
        private final Lock readLock = rwl.readLock();
        private final Lock writeLock = rwl.writeLock();

        public Database(File databaseFile){
            this.databaseFile = databaseFile;
            this.emailAddressesArray = new ArrayList<>();
            this.emailsArray = new ArrayList<>();

            loadDatabase();
        }

        private void loadDatabase(){
            //scanner = new Scanner(new File("src/database"));
            writeLock.lock();
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
            }finally {
                writeLock.unlock();
            }
        }

        public boolean emailIsRegistered(String accountEmail){
            readLock.lock();
            boolean retValue = emailAddressesArray.contains(accountEmail);
            readLock.unlock();
            return retValue;
        }

        public ArrayList<Email> getEmailsSent(String sender){
            ArrayList<Email> sentEmails = new ArrayList<>();

            readLock.lock();
            for(Email e: emailsArray){
                if(e.getSender().equals(sender)) sentEmails.add(e);
            }
            readLock.unlock();

            return sentEmails;
        }

        public ArrayList<Email> getEmailsReceived(String receiver){
            ArrayList<Email> sentEmails = new ArrayList<>();

            readLock.lock();
            for(Email e: emailsArray){
                if(e.getReceivers().contains(receiver)) sentEmails.add(e);
            }
            readLock.unlock();

            return sentEmails;
        }

        public void saveEmail(Email email){
            writeLock.lock();
            emailsArray.add(email);
            try{
                BufferedWriter out = new BufferedWriter(new FileWriter(databaseFile, true));
                out.append(email.toString()+"\n");
                out.close();
            }catch (IOException e){
                System.out.println("Could not save new email to database");
                e.printStackTrace();
            }finally {
                writeLock.unlock();
            }
        }
        /*public void printDatabase(){
            for(String s: emailAddressesArray){
                System.out.println(s);
            }
            for(Email e: emailsArray){
                System.out.println(e);
            }
        }*/
    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
    private static class ClientHandler implements Runnable{
        private final Socket socket;

        private String emailAddress;
        private ArrayList<Email> emailsSent;
        private ArrayList<Email> emailsReceived;
        private ObjectInputStream inStream;
        private ObjectOutputStream outStream;


        public ClientHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            try{
                inStream = new ObjectInputStream(socket.getInputStream());
                outStream = new ObjectOutputStream(socket.getOutputStream());

                startConnection();

                if(emailAddress!=null){ //allora non è stata trovata una email associata
                    //vai in attesa di input utente fino a richiesta di chiusura connessione
                    boolean clientWantsToDisconnect = false;
                    while(!clientWantsToDisconnect){
                        int command = Common.getInputOfClass(inStream, Integer.class);
                        switch (command) {
                            case CSMex.NEW_EMAIL_TO_SEND:
                                synchronized (outStream){ //the outStream should be locked because in the meantime a new Email could be received and the client is waiting for the list of mispelled accounts
                                    Email newEmailToSend = Common.getInputOfClass(inStream, Email.class);
                                    ArrayList<String> receivers = newEmailToSend.getReceivers();

                                    ArrayList<String> misspelledAccounts = new ArrayList<>();
                                    ArrayList<String> correctAccounts = new ArrayList<>();
                                    for(String receiver: receivers){
                                        if(!database.emailIsRegistered(receiver)){
                                            misspelledAccounts.add(receiver);
                                        }else{
                                            correctAccounts.add(receiver);
                                        }
                                    }
                                    outStream.writeObject(misspelledAccounts);
                                    outStream.writeObject(correctAccounts);
                                    if(misspelledAccounts.size() == 0){
                                        for(String receiver: receivers){
                                            sendEmail(newEmailToSend, receiver);
                                        }
                                        Server.saveEmail(newEmailToSend);
                                        outStream.writeObject(true);
                                    }
                                }

                                break;
                            //tutti gli altri casi mi aspetto cose diverse Ex. scrivere mail, forzare refresh, ecc...
                            default:
                                clientWantsToDisconnect = true;
                                break;
                        }
                    }
                }
            }catch (ConnectException e){
                System.out.println("Client disconnected, bye bye");
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        private void startConnection() throws IOException {
            boolean emailIsOkay = false;
            String userEmail = null;
            while(!emailIsOkay){
                userEmail = Common.getInputOfClass(inStream,String.class);
                emailIsOkay = database.emailIsRegistered(userEmail);
                if(emailIsOkay){
                    outStream.writeObject(true);
                }else{
                    outStream.writeObject(false);
                }
            }
            //fills variables based on email
            emailAddress = userEmail;
            emailsSent = database.getEmailsSent(emailAddress);
            emailsReceived = database.getEmailsReceived(emailAddress);
            //adds the clientHandler to hash map
            Server.addClientHandlerToHashMap(emailAddress, this);

            //gives positive feedback to client
            outStream.writeObject(emailsSent);
            outStream.writeObject(emailsReceived);

            /*System.out.println(emailsSent);
            System.out.println(emailsReceived);*/
        }

        private void sendEmail(Email email, String receiver){
            emailsSent.add(email);
            ClientHandler activeClientHandler = getClientHandlerFromEmail(receiver);
            if(activeClientHandler != null){
                activeClientHandler.receiveEmail(email);
            }
        }

        private void receiveEmail(Email email){
            emailsReceived.add(email);
            try{
                synchronized (outStream){ //needs synchro because multiple emails could be received in a row and this could lead to errors
                    outStream.writeObject(CSMex.NEW_EMAIL_RECEIVED);
                    outStream.writeObject(email);
                }
            }catch (IOException e){
                System.out.println("Error, server can't write new mail to client");
                e.printStackTrace();
            }
        }
    }

}