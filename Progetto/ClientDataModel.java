package Progetto;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientDataModel {
	private final BooleanProperty connectionOkay = new SimpleBooleanProperty();

	private StringProperty emailAddress;
	private ObservableList<Email> emailsReceived;
	private ObservableList<Email> emailsSent;
	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	private final Lock serverRequestLock = new ReentrantLock();
	private ObjectOutputStream serverOutputWriterStream;
	private ObjectInputStream serverOutputReaderStream;

	public ClientDataModel(){
		restartConnection();
		try {
			PipedOutputStream serverOutputOutputPipe = new PipedOutputStream();
			PipedInputStream serverOutputInputPipe = new PipedInputStream(serverOutputOutputPipe);
			serverOutputWriterStream = new ObjectOutputStream(serverOutputOutputPipe);
			serverOutputReaderStream = new ObjectInputStream(serverOutputInputPipe);
		} catch (IOException e) {
			System.out.println("Could not initialize serverOutputStreams");
			e.printStackTrace();
		}
	}

	/**
	 * @return emailAddress value
	 */
	public String getEmailAddress(){
		return emailAddress.get();
	}

	/**
	 * @return emailAddress property
	 */
	public StringProperty emailAddressProperty(){
		return emailAddress;
	}

	/**
	 * @return emailsReceived property
	 */
	public ObservableList<Email> emailsReceivedProperty(){
		return emailsReceived;
	}

	/**
	 * @return emailsSent property
	 */
	public ObservableList<Email> emailsSentProperty(){
		return emailsSent;
	}

	/**
	 * @return connectionOkay value
	 */
	public boolean getConnectionOkay(){
		return connectionOkay.get();
	}

	/**
	 * @return connectionOkay property
	 */
	public BooleanProperty connectionOkayProperty(){
		return connectionOkay;
	}

	/**
	 * Send the email the user inserted and tries to authenticate. On authentication
	 * a ServerInputReader thread is started to keep variables updated
	 * @param emailInserted Email inserted by user
	 * @return true on access success, false on wrong email or error
	 */
	public boolean getAccessFromServer(String emailInserted){
		try {
			serverRequestLock.lock();
			outStream.writeObject(emailInserted);

			boolean emailIsOkay = Common.getInputOfClass(inStream, Boolean.class);
			if(!emailIsOkay){
				return false;
			}

			//Converts synchronized arraylist to observable list
			ObservableList<Email> emailsSentInput = FXCollections.observableArrayList(getSynchronizedListOfEmailsFromServer());
			ObservableList<Email> emailsReceivedInput = FXCollections.observableArrayList(getSynchronizedListOfEmailsFromServer());

			emailAddress.set(emailInserted);
			emailsReceived = emailsReceivedInput;
			emailsSent = emailsSentInput;

			new ServerInputReader().start();

			return true;
		}catch(IOException e){
			e.printStackTrace();
			restartConnection();
			return false;
		}finally {
			serverRequestLock.unlock();
		}
	}

	/**
	 * Controller calls this method to send an email
	 * @param receivers List of email addresses of receivers
	 * @param subject Subject of email
	 * @param body Body of email
	 * @return true on email sent correctly, false on error
	 */
	public boolean sendEmail(ArrayList<String> receivers, String subject, String body){
		if(emailAddress.get() == null || receivers.contains(this.emailAddress.get())) return false;
		Email emailToSend = new Email(emailAddress.get(), receivers, subject, body, new Date());
		try{
			serverRequestLock.lock();

			outStream.writeObject(CSMex.NEW_EMAIL_TO_SEND);
			outStream.writeObject(emailToSend);

			boolean emailSentCorrectly = getBooleanFromInputReader();

			if(emailSentCorrectly){
				emailsSent.add(emailToSend);
				return true;
			}else{
				return false;
			}
		}catch (IOException e){
			e.printStackTrace();
			restartConnection();
			return false;
		}finally {
			serverRequestLock.unlock();
		}
	}

	/**
	 * Controller calls this method to delete an email
	 * @param emailToDelete Email to be deleted
	 * @return true on email deleted correctly, false on error
	 */
	public boolean deleteEmail(Email emailToDelete){
		try{
			serverRequestLock.lock();

			outStream.writeObject(CSMex.DELETE_EMAIL);
			outStream.writeObject(emailToDelete);

			boolean emailDeletedCorrectly = getBooleanFromInputReader();

			if(emailDeletedCorrectly){
				emailsSent.remove(emailToDelete);
				emailsReceived.remove(emailToDelete);
				return true;
			}else{
				return false;
			}
		}catch (IOException e){
			e.printStackTrace();
			restartConnection();
			return false;
		}finally {
			serverRequestLock.unlock();
		}
	}

	/**
	 * Replies to the emailToReply Email using body as reply content
	 * @param emailToReply Email to be replied
	 * @param body Body of reply
	 * @return true on replied correctly, false otherwise
	 */
	public boolean replyEmail(Email emailToReply, String body){
		String receiverOfReply = emailToReply.getSender();
		ArrayList<String> receiverToArrayList = new ArrayList<>();
		receiverToArrayList.add(receiverOfReply);
		String subjectOfReply = emailToReply.getSubject();
		return sendEmail(receiverToArrayList, subjectOfReply, body);
	}

	/**
	 * Replies to all emailToReply receivers using body as reply content
	 * @param emailToReply Email whose receivers are to be replied
	 * @param body Body of reply
	 * @return true on replied all correctly, false otherwise
	 */
	public boolean replyAllEmail(Email emailToReply, String body){
		ArrayList<String> receiversOfReply = emailToReply.getReceivers();
		receiversOfReply.remove(emailAddress.get());
		receiversOfReply.add(emailToReply.getSender());
		String subjectOfReply = emailToReply.getSubject();
		return sendEmail(receiversOfReply, subjectOfReply, body);
	}

	/*public boolean replyEmail(Email emailToReply, String replyMessage){
		ArrayList<String> senderArrayList = new ArrayList<>();
		senderArrayList.add(emailToReply.getSender());
		Email emailReply = new Email(this.emailAddress, senderArrayList, emailToReply.getSubject(), replyMessage, new Date());
		sendEmail(emailReply);
	}*/

	/**
	 * Asks server if emailAddress is registered and get response back
	 * @param emailAddress the email we want to verify is in database
	 * @return true if emailAddress is contained in database, false otherwise
	 */
	public boolean emailAddressExists(String emailAddress){
		try{
			serverRequestLock.lock();

			outStream.writeObject(CSMex.CHECK_EMAIL_ADDRESS_EXISTS);
			outStream.writeObject(emailAddress);
			return getBooleanFromInputReader();
		}catch (IOException e){
			System.out.println("Email checking failed");
			e.printStackTrace();
			restartConnection();
			return false;
		}finally {
			serverRequestLock.unlock();
		}
	}

	/**
	 * Creates a Connector thread that connects to database
	 */
	private void startConnection(){
		new Connector().start();
	}

	private void restartConnection(){
		connectionOkay.set(false);
		emailAddress = new SimpleStringProperty();
		//emailsReceived and emailsSent get initialized in getAccessFromServer()
			emailsReceived = null;
			emailsSent = null;
		//inStream and outStream get initialized in Connector run()
			inStream = null;
			outStream=null;
		startConnection();
	}

	/*public static void main(String[] args) {

		boolean connectionSuccess = false;

		try{
			InetAddress localhost = InetAddress.getLocalHost();
			Socket serverSocket = new Socket(localhost, 5000);

			outStream = new ObjectOutputStream(serverSocket.getOutputStream());
			inStream = new ObjectInputStream(serverSocket.getInputStream());

			connectionSuccess = true;
		}catch (IOException e){
			System.out.println("Server unreachable, try again later");
			e.printStackTrace();
			//dai messaggio di connessione non possibile alla view
		}

		if(connectionSuccess){
			boolean terminated = false;
			while(!terminated){
				try {
					getAccessFromServer();
					try{
						sleep(5000);
					}catch (InterruptedException e){
						e.printStackTrace();
					}
					//test///////////////////
					String sender = emailAddress;
					ArrayList<String> receivers = new ArrayList<>();
					receivers.add("ciao@nigga.it");
					receivers.add("bubu@bubu.bubu");
					String subject = "subject";
					String body = "body";
					Date date = new Date();

					boolean correctAddresses = true;

					for(String receiver: receivers){
						correctAddresses = emailAddressExists(receiver);
						if(!correctAddresses) break;
					}

					if(correctAddresses){
						sendEmail(new Email(sender, receivers, subject, body, date));
						System.out.println("Yes");
					}else{
						System.out.println("No");
					}

					//test///////////////////

					getInputFromServerLoop();
					terminated = true;
				}catch (IOException e){
					System.out.println("Connection interrupted. Trying to connect again...");
					try {
						sleep(5000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}*/

	/**
	 * @return a List<Email> object that contains syncArraylist sent from server
	 * @throws ConnectException when arraylist is not correctly received
	 */
	private List<Email> getSynchronizedListOfEmailsFromServer() throws ConnectException {
		final List<Email> syncArrayList =  Collections.synchronizedList(new ArrayList<>());
		return Common.ConvertToSyncArrayList(Common.getInputOfClass(inStream, syncArrayList.getClass()), Email.class);
	}

	/**
	 * @return an Email sent from serverInputReader
	 * @throws ConnectException when Email is not correctly received
	 */
	private Email getEmailFromServerInputReader() throws ConnectException{
		return Common.getInputOfClass(serverOutputReaderStream, Email.class);
	}

	/**
	 * @return an Email sent from server
	 * @throws ConnectException when Email is not correctly received
	 */
	private Email getEmailFromServer() throws ConnectException{
		return Common.getInputOfClass(inStream, Email.class);
	}

	/**
	 * @return an boolean sent from serverInputReader
	 * @throws ConnectException when boolean is not correctly received
	 */
	private boolean getBooleanFromInputReader() throws ConnectException{
		return Common.getInputOfClass(serverOutputReaderStream, Boolean.class);
	}


	/**
	 * This Thread connects to server and handles input from server
	 */
	private class Connector extends Thread{
		public Connector(){
			setDaemon(true);
		}

		@Override
		public void run() {
			while(!connectionOkay.get()){
				try{
					InetAddress localhost = InetAddress.getLocalHost();
					Socket serverSocket = new Socket(localhost, 5000);

					outStream = new ObjectOutputStream(serverSocket.getOutputStream());
					inStream = new ObjectInputStream(serverSocket.getInputStream());

					connectionOkay.set(true);
				}catch (IOException e){
					System.out.println("Connection failed, trying to connect again...");
					try {
						//noinspection BusyWait
						sleep(3000);
					}catch (InterruptedException interruptedException){
						interruptedException.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * This Thread reads input from server and updates client variables according to such input
	 */
	private class ServerInputReader extends Thread{
		public ServerInputReader(){
			setDaemon(true);
		}

		public void run(){
			while(connectionOkay.get()) {
				boolean inputIsCommand = false;
				int command = CSMex.FORCE_DISCONNECTION;
				try { //here client waits for server input
					while(!inputIsCommand){
						Object input = inStream.readObject();
						if(input.getClass() != Integer.class){
							serverOutputWriterStream.writeObject(input);
						}else{
							serverRequestLock.lock();
							inputIsCommand = true;
							command = (Integer) input;
						}
					}
					new CommandExecutor(command).start();
				} catch (IOException | ClassNotFoundException e) {
					System.out.println("Exception during getInputFromServerLoop");
					e.printStackTrace();
					restartConnection();
				}
			}
		}
	}

	private class CommandExecutor extends Thread{
		private final int command;

		public CommandExecutor(int command){
			setDaemon(true);
			this.command=command;
		}

		@Override
		public void run() {
			try{
				switch (command) {
					case CSMex.NEW_EMAIL_RECEIVED -> {
						Email newEmail = getEmailFromServer();
						emailsReceived.add(newEmail);
					}
					case CSMex.EMAIL_DELETED -> {
						Email emailToDelete = getEmailFromServer();
						emailsReceived.removeIf(email -> email.toString().equals(emailToDelete.toString()));
						emailsSent.removeIf(email -> email.toString().equals(emailToDelete.toString()));
					}
					case CSMex.FORCE_DISCONNECTION -> System.out.println("Disconnecting from server...");
					default -> System.out.println("Error, unexpected server command: " + command);
				}
			}catch (IOException e) {
				System.out.println("Exception in Command #"+command);
				e.printStackTrace();
				restartConnection();
			}finally {
				serverRequestLock.unlock();
			}
		}
	}
}