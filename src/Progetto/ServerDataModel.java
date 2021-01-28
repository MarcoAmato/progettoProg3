package Progetto;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerDataModel extends Thread{
	private static final int NUM_THREAD = 100;

	private static final ExecutorService clientHandlerExecutor = Executors.newFixedThreadPool(NUM_THREAD);
	private static final Map<String, ClientHandler> currentClientsMap = Collections.synchronizedMap(new HashMap<>());
	private static Database database;
	private static final ObservableList<String> logList = FXCollections.observableArrayList();
	private static ObjectOutputStream logOutputStream;
	private static final Lock logOutPutStreamLock = new ReentrantLock();
	private static ObjectInputStream logInputStream;
	/*private static Logger logger = new Logger();*/

	public ServerDataModel(String pathToDatabase){
		boolean logStreamCreated = false;
		try{
			PipedOutputStream pipedOutputStream = new PipedOutputStream();
			PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
			logOutputStream = new ObjectOutputStream(pipedOutputStream);
			logInputStream = new ObjectInputStream(pipedInputStream);
			logStreamCreated = true;
		}catch (IOException e){
			System.out.println("It was not possible to create the logStream");
			e.printStackTrace();
		}
		if(logStreamCreated){
			database = new Database(new File(pathToDatabase));
			log("Database created");
			ClientAcceptor clientAcceptor = new ClientAcceptor();
			clientAcceptor.start();//start thread
		}
	}

	@SuppressWarnings("InfiniteLoopStatement")
	@Override
	public synchronized void run() {
		while (true) {
			receiveLog();
		}
	}

	public static void addClientHandlerToHashMap(String email, ClientHandler clientHandler){
		currentClientsMap.put(email, clientHandler);
	}

	public static void removeClientHandlerFromHashMap(String email){
		currentClientsMap.remove(email);
	}

	public static ClientHandler getClientHandlerFromEmail(String email){
		return currentClientsMap.get(email);
	}

	public static void saveEmail(Email email){
		database.saveEmail(email);
	}

	private static void log(String logMessage){ // lo devono fare gli handler
		synchronized (logList){
			logList.add(logMessage);
		}
	}

	public static void receiveLog(){
		try{
			Object logReceived = logInputStream.readObject();
			if(logReceived == null || logReceived.getClass()!=String.class){
				throw new ClassNotFoundException();
			}
			log((String) logReceived);
		}catch (ClassNotFoundException e){
			System.out.println("Unexpected class received in logStream");
			e.printStackTrace();
		}catch (IOException e){
			System.out.println("Log reading failed");
			e.printStackTrace();
		}
	}

	public ObservableList<String> logList(){
		return logList;
	}

	public static void writeLogToLogStream(String log){
		logOutPutStreamLock.lock();
		try {
			logOutputStream.writeObject(log);
		} catch (IOException e) {
			System.out.println("Error in writing to logPipe");
			e.printStackTrace();
		}finally {
			logOutPutStreamLock.unlock();
		}
	}

	/*private static class Logger extends Thread{
		private PipedOutputStream pipedOutputStream;

		public Logger(PipedOutputStream pipedOutputStream){
			this.pipedOutputStream = pipedOutputStream;
		}

		@Override
		public void run() {
			setDaemon(true);

		}

	}*/

	private static class ClientAcceptor extends Thread{
		@SuppressWarnings("InfiniteLoopStatement")
		@Override
		public void run() {
			try{
				ServerSocket server = new ServerSocket(5000);
				writeLogToLogStream("Socket created");
				while(true){
					Socket client = server.accept();
					Runnable handler = new ClientHandler(client);
					clientHandlerExecutor.execute(handler);
				}
			}catch(IOException e){
				writeLogToLogStream("Error: exception in accepting Client");
				e.printStackTrace();
			}
		}
	}

	private static class Database{
		private final File databaseFile;
		private final ArrayList<String> emailAddressesArray; //Contiene i nomi delle caselle di posta memorizzate nel database
		private final ArrayList<Email> emailsArray;
		private final ReadWriteLock rwl = new ReentrantReadWriteLock();
		public final Lock readLock = rwl.readLock();
		public final Lock writeLock = rwl.writeLock();

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
					//La riga è una mail
					switch (line.charAt(0)) {
						case '+' -> {
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
								writeLogToLogStream("Error, incorrect date format detected in database");
								e.printStackTrace();
							}
							emailsArray.add(new Email(sender, receivers, subject, body, sendingDate));
						}
						//La riga è un indirizzo di posta
						case '-' -> emailAddressesArray.add(line.substring(1));
						default -> {
							writeLogToLogStream("Error in parsing database, unexpected line starting character: " + line.charAt(0));
							System.out.println("Error in parsing database. The read line starts with unexpected character: " + line.charAt(0));
						}
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
				out.append(email.toString()).append("\n");
				out.close();
			}catch (IOException e){
				System.out.println("Could not save new email to database");
				e.printStackTrace();
			}finally {
				writeLock.unlock();
			}
			writeLogToLogStream("New email inserted in database");
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
		private List<Email> emailsSent;
		private List<Email> emailsReceived;
		private ObjectInputStream inStream;
		private ObjectOutputStream outStream;
		private final Lock streamLock = new ReentrantLock();

		public ClientHandler(Socket socket){
			this.socket = socket;
		}

		@Override
		public void run() {
			try{
				inStream = new ObjectInputStream(socket.getInputStream());
				outStream = new ObjectOutputStream(socket.getOutputStream());

				boolean connectionEstablished = startConnection();

				if(connectionEstablished){ //allora è stata trovata una email associata
					//vai in attesa di input utente fino a richiesta di chiusura connessione
					boolean clientWantsToDisconnect = false;
					while(!clientWantsToDisconnect){
						int command = Common.getInputOfClass(inStream, Integer.class);
						clientWantsToDisconnect = answerMessageWithFeedback(command);
					}
					endConnection();
				}
			}catch (IOException e){
				e.printStackTrace();
			}
			System.out.println("Client disconnected, bye bye");
		}

		/*
		Handles the client request based on the command that was received.
		It returns true if the client wants to disconnect or an error makes
		closing connection the best option, if the client is not reachable
		or if an unexpected message is received.
		Otherwise returns false
		 */
		public boolean answerMessageWithFeedback(int command){
			try{
				streamLock.lock();
				switch (command) {
					case CSMex.NEW_EMAIL_TO_SEND -> {
						Email newEmailToSend = Common.getInputOfClass(inStream, Email.class);
						ArrayList<String> receivers = newEmailToSend.getReceivers();
						boolean allReceiversExist = true;
						for (String receiver : receivers) {
							if (!allReceiversExist) break;
							if (!database.emailIsRegistered(receiver)) {

								allReceiversExist = false;
							}
						}
						if (allReceiversExist) {
							synchronized (currentClientsMap) {
								//The HashMap is locked so while we send the emails no client logs. Otherwise he could not receive the new emails
								for (String receiver : receivers) {
									sendEmail(newEmailToSend, receiver);
								}
							}
							emailsSent.add(newEmailToSend);
							saveEmail(newEmailToSend);
							outStream.writeObject(true);

							writeLogToLogStream("Email from " + emailAddress + " sent correctly");
						} else {
							outStream.writeObject(false);
							writeLogToLogStream("Error, email from " + emailAddress + " contains an incorrect receiver address");
						}
					}
					case CSMex.DISCONNECTION -> {
						writeLogToLogStream("Client " + emailAddress + " disconnected");
						return true;
					}
					case CSMex.CHECK_EMAIL_ADDRESS_EXISTS -> {
						String emailAddress = Common.getInputOfClass(inStream, String.class);
						outStream.writeObject(database.emailIsRegistered(emailAddress));
					}
					default -> writeLogToLogStream("Error, unexpected command from client: #" + command);
				}
			} catch (IOException e) {
				writeLogToLogStream("Error in handling client message #"+command);
				e.printStackTrace();
				return true;
			}finally {
				streamLock.unlock();
			}
			return false;
		}

		private boolean startConnection(){
			try{
				streamLock.lock();
				database.readLock.lock(); //the database is locked outside loop so during connection no client can send email while connection is not completed
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
				writeLogToLogStream(userEmail+" logged in");
				//fills variables based on email
				emailAddress = userEmail;
				emailsSent = Collections.synchronizedList(database.getEmailsSent(emailAddress));
				emailsReceived = Collections.synchronizedList(database.getEmailsReceived(emailAddress));
				//adds the clientHandler to hash map
				addClientHandlerToHashMap(emailAddress, this);

				//gives positive feedback to client
				outStream.writeObject(emailsSent);
				outStream.writeObject(emailsReceived);

				return true;

                /*System.out.println(emailsSent);
                System.out.println(emailsReceived);*/
			}catch (IOException e){
				writeLogToLogStream("Error in client login");
				System.out.println("It was not possible to establish connection with client");
				return false;
			}finally {
				streamLock.unlock();
				database.readLock.unlock();
			}
		}

		/**
		 * ClientHandler is terminating, everything necessary to end
		 * connection is done here
		 */
		private void endConnection(){
			removeClientHandlerFromHashMap(emailAddress);
		}

		private void sendEmail(Email email, String receiver){
			ClientHandler activeClientHandler = getClientHandlerFromEmail(receiver);
			if(activeClientHandler != null){
				writeLogToLogStream("Sending email to Client Handler of "+activeClientHandler.emailAddress);
				activeClientHandler.receiveEmail(email);
			}
		}

		private void receiveEmail(Email email){
			writeLogToLogStream(emailAddress+" received new email");
			emailsReceived.add(email);
			try{
				//needs synchro because multiple emails could be received in a row and this could lead to errors
				streamLock.lock();
				outStream.writeObject(CSMex.NEW_EMAIL_RECEIVED);
				outStream.writeObject(email);
			}catch (IOException e){
				System.out.println("Error, server can't write new mail to client");
				e.printStackTrace();
			}finally {
				streamLock.unlock();
			}
		}
	}
}
