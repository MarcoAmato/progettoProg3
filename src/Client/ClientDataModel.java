package Client;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientDataModel {
	private final BooleanProperty connectionOkay = new SimpleBooleanProperty();

	private String emailAddress;
	private List<Email> emailsReceived;
	private List<Email> emailsSent;
	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	private final Lock streamLock = new ReentrantLock();

	public ClientDataModel(){
		this.connectionOkay.set(false);
	}

	/**
	 *
	 * @return connectionOkay value
	 */
	public boolean getConnectionOkay(){
		return connectionOkay.get();
	}

	/**
	 *
	 * @return connectionOkay property
	 */
	public BooleanProperty connectionStatusProperty(){
		return connectionOkay;
	}

	/**
	 * Creates a Connector thread that connects to database
	 */
	public void startConnection(){
		new Connector().run();
	}

	/**
	 * This class is a Thread that connects to server and handles input from server
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
						sleep(5000);
					}catch (InterruptedException interruptedException){
						interruptedException.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * This thread reads input from server and updates client variables according to such input
	 */
	private class ServerInputReader extends Thread{
		public void run(){
			setDaemon(true);
			while(connectionOkay.get()) {
				try { //here client waits for server input which for the moment will be only a new email that the client has received
					int command = Common.getInputOfClass(inStream, Integer.class);
					streamLock.lock();
					switch (command) {
						case CSMex.NEW_EMAIL_RECEIVED -> {
							Email newEmail = getEmailFromServer();
							emailsReceived.add(newEmail);
						}
						default -> {
							System.out.println("Error, unexpected server command: " + command);
						}
					}
				} catch (IOException e) {
					System.out.println("Exception during getInputFromServerLoop");
					e.printStackTrace();
					connectionOkay.set(false);
				}finally {
					streamLock.unlock();
				}
			}
		}
	}

	private void restartConnection(){
		connectionOkay.set(false);
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
	 * Send the email the user inserted and tries to authenticate. On authentication
	 * a ServerInputReader thread is started to keep variables updated
	 * @param emailInserted Email inserted by user
	 * @return true on access success, false on wrong email or error
	 */
	public boolean getAccessFromServer(String emailInserted){
		try {
			streamLock.lock();

			outStream.writeObject(emailInserted);
			boolean emailIsOkay = getBooleanFromServer();
			if(!emailIsOkay){
				return false;
			}

			List<Email> emailsReceivedInput = getSynchronizedListOfEmailsFromServer();
			List<Email> emailsSentInput = getSynchronizedListOfEmailsFromServer();

			emailAddress = emailInserted;
			emailsReceived = emailsReceivedInput;
			emailsSent = emailsSentInput;

			new ServerInputReader().start();

			return true;
		}catch (ConnectException e){
			restartConnection();
			return false;
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}finally {
			streamLock.unlock();
		}
	}

	/**
	 * Controller calls this method to send an email
	 * @param emailToSend email to be sent
	 * @return true on email sent correctly, false on error
	 */
	public boolean sendEmail(Email emailToSend){
		try{
			streamLock.lock();

			outStream.writeObject(CSMex.NEW_EMAIL_TO_SEND);
			outStream.writeObject(emailToSend);

			boolean emailSentCorrectly = getBooleanFromServer();

			if(emailSentCorrectly){
				emailsSent.add(emailToSend);
				return true;
			}else{
				return false;
			}
		}catch (IOException e){
			connectionOkay.set(false);
			e.printStackTrace();
			return false;
		}finally {
			streamLock.unlock();
		}
	}

	/*public boolean replyEmail(Email emailToReply, String replyMessage){
		ArrayList<String> senderArrayList = new ArrayList<>();
		senderArrayList.add(emailToReply.getSender());
		Email emailReply = new Email(this.emailAddress, senderArrayList, emailToReply.getSubject(), replyMessage, new Date());
		sendEmail(emailReply);
	}*/

	/**
	 * @param emailAddress the email we want to verify is in database
	 * @return true if it is contained in database, false otherwise
	 */
	/*public boolean emailAddressExists(String emailAddress){
		try{
			inputLock.lock();

			outStream.writeObject(CSMex.CHECK_EMAIL_ADDRESS_EXISTS);
			outStream.writeObject(emailAddress);
			return getBooleanFromServer();
		}catch (IOException e){
			System.out.println("Email checking failed");
			e.printStackTrace();
			return false;
			//continua lato server e integra lato client con quello che c è già
		}finally {
			inputLock.unlock();
		}
	}*/

	/**
	 * @return a List<Email> object that contains syncArraylist sent from server
	 * @throws ConnectException when arraylist is not correctly received
	 */
	public List<Email> getSynchronizedListOfEmailsFromServer() throws ConnectException {
		final List<Email> syncArrayList =  Collections.synchronizedList(new ArrayList<>());
		return Common.ConvertToSyncArrayList(Common.getInputOfClass(inStream, syncArrayList.getClass()), Email.class);
	}

	/**
	 * @return an Email sent from server
	 * @throws ConnectException when Email is not correctly received
	 */
	public Email getEmailFromServer() throws ConnectException{
		return Common.getInputOfClass(inStream, Email.class);
	}

	/**
	 * @return an boolean sent from server
	 * @throws ConnectException when boolean is not correctly received
	 */
	public boolean getBooleanFromServer() throws ConnectException{
		return Common.getInputOfClass(inStream, Boolean.class);
	}


}