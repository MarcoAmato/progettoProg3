package Progetto;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ServerDataModel{
	private static final int NUM_THREAD = 100;

	private static final ExecutorService clientHandlerExecutor = Executors.newFixedThreadPool(NUM_THREAD);
	private static final Map<String, ClientHandler> currentClientsMap = Collections.synchronizedMap(new HashMap<>());
	private static Database database;
	private static final ObservableList<String> logList = FXCollections.observableArrayList();

	public ServerDataModel(String pathToDatabase){
		database = new Database(new File(pathToDatabase));
		log("Database created");
		ClientAcceptor clientAcceptor = new ClientAcceptor();
		clientAcceptor.start();
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

	public static boolean deleteEmailFromDatabase(Email email){
		return database.deleteEmail(email);
	}

	private static void log(String logMessage){ // lo devono fare gli handler
		synchronized (logList){
			Platform.runLater(() -> logList.add(logMessage));
		}
	}

	public ObservableList<String> logList(){
		return logList;
	}

	private static class ClientAcceptor extends Thread{
		@SuppressWarnings("InfiniteLoopStatement")
		@Override
		public void run() {
			try{
				ServerSocket server = new ServerSocket(5000);
				log("Socket created");
				while(true){
					Socket client = server.accept();
					log("New client connected to server");
					Runnable handler = new ClientHandler(client);
					clientHandlerExecutor.execute(handler);
				}
			}catch(IOException e){
				log("Error: exception in accepting Client");
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

			try (Scanner scanner = new Scanner(databaseFile)) {

				while (scanner.hasNextLine()) {
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
								log("Error, incorrect date format detected in database");
								e.printStackTrace();
							}
							emailsArray.add(new Email(sender, receivers, subject, body, sendingDate));
						}
						//La riga è un indirizzo di posta
						case '-' -> emailAddressesArray.add(line.substring(1));
						default -> {
							log("Error in parsing database, unexpected line starting character: " + line.charAt(0));
							System.out.println("Error in parsing database. The read line starts with unexpected character: " + line.charAt(0));
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
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
				log("Could not save new email to database");
				e.printStackTrace();
			}finally {
				writeLock.unlock();
			}
			log("New email inserted in database");
		}

		/**
		 * Deletes email line from database. It does so copying every line except
		 * the one with emailToBeDeleted as string in another file. Finally this
		 * file replaces databaseFile
		 * @param emailToBeDeleted Email to be deleted
		 * @return true on success, false on failure
		 */
		public boolean deleteEmail(Email emailToBeDeleted){
			writeLock.lock();
			emailsArray.removeIf(email -> email.toString().equals(emailToBeDeleted.toString()));
			try{
				File tempFile = new File("temp.txt");

				BufferedReader reader = new BufferedReader(new FileReader(databaseFile));
				BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

				String lineToRemove = emailToBeDeleted.toString();
				String currentLine;

				while((currentLine = reader.readLine()) != null) {
					// trim newline when comparing with lineToRemove
					String trimmedLine = currentLine.trim();
					if(trimmedLine.equals(lineToRemove)) continue;
					writer.write(currentLine + System.getProperty("line.separator"));
				}
				writer.close();
				reader.close();
				Path tempSource = tempFile.toPath();
				Path databaseSource = databaseFile.toPath();
				synchronized (databaseFile){
					Files.move(tempSource, databaseSource, REPLACE_EXISTING);
				}
				log("Email deleted successfully from database");
				return true;
			}catch (IOException e) {
				log("Could not delete email from database");
				e.printStackTrace();
				return false;
			}finally {
				writeLock.unlock();
			}
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
					boolean clientDisconnected = false;
					while(!clientDisconnected){
						int command = Common.getInputOfClass(inStream, Integer.class);
						clientDisconnected = !answerMessage(command);
					}
					endConnection();
				}
			}catch (IOException e){
				e.printStackTrace();
			}
			log(emailAddress+" disconnected");
			System.out.println("Client disconnected, bye bye");
		}

		/**
		 * Handles the client request according to command.
		 * @param command CSMex command sent by client
		 * @return true when connection should be kept alive, false on close request or error
		 */
		public boolean answerMessage(int command){
			try{
				streamLock.lock();
				switch (command) {
					case CSMex.NEW_EMAIL_TO_SEND -> {
						Email newEmailToSend = Common.getInputOfClass(inStream, Email.class);
						boolean result = sendEmail(newEmailToSend);
						outStream.writeObject(result);
						return true;
					}
					case CSMex.DELETE_EMAIL -> {
						Email emailToDelete = Common.getInputOfClass(inStream, Email.class);
						boolean result = deleteEmail(emailToDelete);
						outStream.writeObject(result);
						return true;
					}
					case CSMex.DISCONNECTION -> {
						log("Client " + emailAddress + " disconnected");
						return false;
					}
					case CSMex.CHECK_EMAIL_ADDRESS_EXISTS -> {
						String emailAddress = Common.getInputOfClass(inStream, String.class);
						outStream.writeObject(database.emailIsRegistered(emailAddress));
						return true;
					}
					default -> {
						log("Error, unexpected command from client: #" + command);
						return false;
					}
				}
			} catch (IOException e) {
				log("Error in handling client message #"+command);
				e.printStackTrace();
				return false;
			}finally {
				streamLock.unlock();
			}
		}

		/**
		 * Handles the request to send emailToSend to the ClientHandlers of receivers, if present,
		 * and then saves the email to database
		 * @param emailToSend Email to be sent
		 * @return true on success, false on failure
		 * @throws IOException When client communication fails
		 */
		public boolean sendEmail(Email emailToSend) throws IOException{
			ArrayList<String> receivers = emailToSend.getReceivers();
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
						sendEmailToClientHandler(emailToSend, receiver);
					}
				}
				emailsSent.add(emailToSend);
				saveEmail(emailToSend);
				log("Email from " + emailAddress + " sent correctly");
				return true;
			} else {
				log("Error, email from " + emailAddress + " contains an incorrect receiver address");
				return false;
			}
		}

		/**
		 * If there is a ClientHandler active for receiver then it informs the clientDataModel that email
		 * was received, otherwise does nothing.
		 * @param email Email received from another user
		 * @param receiver email address of receiver
		 */
		private void sendEmailToClientHandler(Email email, String receiver){
			ClientHandler activeClientHandler = getClientHandlerFromEmail(receiver);
			if(activeClientHandler != null){
				log("Sending email to Client Handler of "+activeClientHandler.emailAddress);
				activeClientHandler.receiveEmail(email);
			}
		}

		/**
		 * ClientHandler saves email then informs clientDataModel giving it email
		 * @param email new Email received from another user
		 */
		private void receiveEmail(Email email){
			log(emailAddress+" received new email");
			emailsReceived.add(email);
			try{
				//needs synchro because multiple emails could be received in a row and this could lead to errors
				streamLock.lock();
				outStream.writeObject(CSMex.NEW_EMAIL_RECEIVED);
				outStream.writeObject(email);
			}catch (IOException e){
				log("Error, server can't write new mail to client");
				e.printStackTrace();
			}finally {
				streamLock.unlock();
			}
		}


		/**
		 * Deletes emailToBeDeleted, first on ClientHandles involved, then on database
		 * @param emailToBeDeleted Email to be deleted
		 * @return true on success, false on failure
		 * @throws IOException When client communication fails
		 */
		private boolean deleteEmail(Email emailToBeDeleted) throws IOException{
			synchronized (currentClientsMap){
				//The HashMap is locked so while we delete the emails no client logs. Otherwise he could read deleted emails
				for (String receiver : emailToBeDeleted.getReceivers()) {
					removeEmailFromClientHandler(emailToBeDeleted, receiver);
				}
			}

			emailsSent.removeIf(email -> email.toString().equals(emailToBeDeleted.toString()));

			emailsReceived.removeIf(email -> email.toString().equals(emailToBeDeleted.toString()));

			if(deleteEmailFromDatabase(emailToBeDeleted)){
				log("Email from " + emailToBeDeleted.getSender() + " deleted");
				return true;
			}else{
				log("Error: Email from " + emailToBeDeleted.getSender() + " failed to be deleted");
				return false;
			}
		}

		/**
		 * If clientEmail is active removes email from his handler, otherwise
		 * does nothing
		 * @param emailToDelete Email to delete from clientHandler
		 * @param clientEmail email address checked if active
		 */
		private void removeEmailFromClientHandler(Email emailToDelete, String clientEmail){
			ClientHandler activeClientHandler = getClientHandlerFromEmail(clientEmail);
			if(activeClientHandler != null){
				log("Removed email from ClientHandler of "+activeClientHandler.emailAddress);
				activeClientHandler.removeEmail(emailToDelete);
			}
		}

		/**
		 * Removes emailToRemove both on ClientHandler and on ClientDataModel
		 * side
		 * @param emailToRemove Email to be removed
		 */
		private void removeEmail(Email emailToRemove){
			log(emailAddress + " removing an email");

			emailsReceived.removeIf(email -> email.toString().equals(emailToRemove.toString()));
			emailsSent.removeIf(email -> email.toString().equals(emailToRemove.toString()));
			try{
				streamLock.lock();
				outStream.writeObject(CSMex.EMAIL_DELETED);
				outStream.writeObject(emailToRemove);
			}catch (IOException e){
				log("Error, server can't remove email from client" + emailAddress);
				e.printStackTrace();
			}finally {
				streamLock.unlock();
			}
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
						log("Failed login for email: " + userEmail);
						outStream.writeObject(false);
					}
				}
				log(userEmail+" logged in");
				//fills variables based on email
				emailAddress = userEmail;
				emailsSent = Collections.synchronizedList(database.getEmailsSent(emailAddress));
				emailsReceived = Collections.synchronizedList(database.getEmailsReceived(emailAddress));
				//adds the clientHandler to hash map
				addClientHandlerToHashMap(emailAddress, this);

				//gives positive feedback to client
				outStream.writeObject(emailsSent);
				outStream.writeObject(emailsReceived);

				log("Client " + userEmail + " connected");

				return true;

                /*System.out.println(emailsSent);
                System.out.println(emailsReceived);*/
			}catch (IOException e){
				log("Error in client login");
				e.printStackTrace();
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
	}
}
