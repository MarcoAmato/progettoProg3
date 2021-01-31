package Client;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

public class ClientDataModel {
	public BooleanProperty connectionOkay = new SimpleBooleanProperty();

	private String emailAddress;
	private List<Email> emailsReceived;
	private List<Email> emailsSent;
	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	private final Lock inputLock = new ReentrantLock();
	private final Lock outputLock = new ReentrantLock();

	public ClientDataModel(){
		this.connectionOkay.set(false);
	}

	public void startConnection(){
		new Thread(() -> {
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
		}).start();
	}

	private void restartConnection(){
		connectionOkay.set(false);
		startConnection();
	}

	/*private static void closeConnection(){
		connectionOkay.set(false);
	}*/

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

	public boolean getAccessFromServer(String emailInserted){
		try {
			inputLock.lock();
			outputLock.lock();

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

			return true;

		}catch (ConnectException e){
			restartConnection();
			return false;
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}finally {
			inputLock.unlock();
			outputLock.unlock();
		}
	}

	/*public static void getInputFromServerLoop(){
		boolean inputFromServerOkay = true;
		while(inputFromServerOkay) {
			try { //here client waits for server input which for the moment will be only a new email that the client has received
				int command = Common.getInputOfClass(inStream, Integer.class);
				inputLock.lock();
				outputLock.lock();
				switch (command) {
					case CSMex.NEW_EMAIL_RECEIVED -> {
						Email newEmail = ClientUtil.getEmailFromServer();
						emailsReceived.add(newEmail);
					}
					default -> {
						System.out.println("Error, unexpected server command: " + command);
						inputFromServerOkay = false;
					}
				}
			} catch (IOException e) {
				System.out.println("Exception during getInputFromServerLoop");
				e.printStackTrace();
				inputFromServerOkay = false;
				connectionOkay.set(false);
			}finally {
				inputLock.unlock();
				outputLock.unlock();
			}
		}
	}*/

	/*public void sendEmail(Email emailToSend){ //la view chiama questo metodo quando vuole inviare la mail
		try{
			inputLock.lock();
			outputLock.lock();

			outStream.writeObject(CSMex.NEW_EMAIL_TO_SEND);
			outStream.writeObject(emailToSend);

			boolean emailSentCorrectly = ClientUtil.getBooleanFromServer();

			if(emailSentCorrectly){
				//tutto corretto, si da feedback alla view
				System.out.println("Email sent correctly");
				emailsSent.add(emailToSend);
			}else{
				System.out.println("Error, server didn't send the email");
			}
		}catch (IOException e){
			//dare feedback di impossibilità di inviare mail alla view
			System.out.println("Could not send email");
			e.printStackTrace();
		}finally {
			inputLock.unlock();
			outputLock.unlock();
		}
	}*/

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
			outputLock.lock();

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
			outputLock.unlock();
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
	/*public Email getEmailFromServer() throws ConnectException{
		return Common.getInputOfClass(inStream, Email.class);
	}*/

	/**
	 * @return an boolean sent from server
	 * @throws ConnectException when boolean is not correctly received
	 */
	public boolean getBooleanFromServer() throws ConnectException{
		return Common.getInputOfClass(inStream, Boolean.class);
	}
}