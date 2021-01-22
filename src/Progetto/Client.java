package Progetto;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

public class Client extends Application {
	private static String emailAddress;
	private static ArrayList<Email> emailsReceived;
	private static ArrayList<Email> emailsSent;
	private static ObjectInputStream inStream;
	private static ObjectOutputStream outStream;
	private static final Lock inputLock = new ReentrantLock();
	private static final Lock outputLock = new ReentrantLock();

	@Override
	public void start(Stage primaryStage) {
		boolean connectionSuccess = false;

		try{
			InetAddress localhost = InetAddress.getLocalHost();
			Socket serverSocket = new Socket(localhost, 5000);

			outStream = new ObjectOutputStream(serverSocket.getOutputStream());
			inStream = new ObjectInputStream(serverSocket.getInputStream());

			connectionSuccess = true;
		}catch (IOException e){
			e.printStackTrace();
			//dai messaggio di connessione non possibile alla view
		}

		if(connectionSuccess){
			boolean terminated = false;
			while(!terminated){
				try {
					getAccessFromServer();
					//test///////////////////
					String sender = "prova@prova.vincy";
					ArrayList<String> receivers = new ArrayList<>();
						receivers.add("ciao@nigga.it");
						receivers.add("bubu@bubu.bubu");
					String subject = "vedi che uno non lo riconoscerà";
					String body = "ano è una cosa giusta provare a capire se ti riconosce le email scritte male no Marco scrivi un altro body I love furry potevi scrivere";
					Date date = new Date();

					sendEmail(new Email(sender, receivers, subject, body, date));
					//test///////////////////
					getInputFromServerLoop();
					terminated = true;
				}catch (ConnectException e){
					System.out.println("Connection interrupted. Trying to connect again...");
					try {
						sleep(5000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	public static void getAccessFromServer() throws ConnectException {
		try {
			inputLock.lock();
			outputLock.lock();
			boolean emailIsOkay = false;
			String emailFromInput = null;

			while(!emailIsOkay){ //loops until server recognises the email the user wrote
				emailFromInput = "prova@prova.vincy"; //Qui deve essere preso l'input dell'utente tramite la view

				outStream.writeObject(emailFromInput);
				emailIsOkay = Common.getInputOfClass(inStream, Boolean.class);
				if(!emailIsOkay){
					//dai feedback negativo alla view del client
					System.out.println("Wrong Email, try again!");
				}
			}

			ArrayList<Email> emailsReceivedInput = Common.ConvertArrayList(Common.getInputOfClass(inStream, ArrayList.class), Email.class);
			ArrayList<Email> emailsSentInput = Common.ConvertArrayList(Common.getInputOfClass(inStream, ArrayList.class), Email.class);

			emailAddress = emailFromInput;
			emailsReceived = emailsReceivedInput;
			emailsSent = emailsSentInput;

            /*System.out.println(emailAddress);
            for(Email e: emailsSent){
                System.out.println(e);
            }
            for(Email e: emailsReceived){
                System.out.println(e);
            }*/

		}catch (SocketException e){
			throw new ConnectException();
		}catch(IOException e){
			e.printStackTrace();
		}finally {
			inputLock.unlock();
			outputLock.unlock();
		}
	}

	public static void getInputFromServerLoop(){
		boolean connectionOkay = true;
		while(connectionOkay)
			try{ //here client waits for server input which for the moment will be only a new email that the client has received
				int command = Common.getInputOfClass(inStream, Integer.class);
				inputLock.lock();
				outputLock.lock();
				switch (command) {
					case CSMex.NEW_EMAIL_RECEIVED -> {
						Email newEmail = Common.getInputOfClass(inStream, Email.class);
						emailsReceived.add(newEmail);
					}
					default -> {
						System.out.println("Error, unexpected server command: " + command);
						connectionOkay = false;
					}
				}
				inputLock.unlock();
				outputLock.unlock();
			}catch (ConnectException e){
				connectionOkay = false;
			}
	}

	public static void sendEmail(Email emailToSend){ //la view chiama questo metodo quando vuole inviare la mail
		try{
			inputLock.lock();
			outputLock.lock();

			outStream.writeObject(CSMex.NEW_EMAIL_TO_SEND);
			outStream.writeObject(emailToSend);

			ArrayList<String> misspelledAccounts = Common.ConvertArrayList(Common.getInputOfClass(inStream, ArrayList.class), String.class);
			ArrayList<String> correctAccounts = Common.ConvertArrayList(Common.getInputOfClass(inStream, ArrayList.class), String.class);

			if(misspelledAccounts.size()>0){
				//Hai inserito male gli account
				//qui andrà dato feedback opportuno alla view
				System.out.println("You misspelled some email address.");
				System.out.println("Correct addresses: ");
				for(String email: correctAccounts)
					System.out.println("\t"+email);
				System.out.println("Misspelled addresses: ");
				for(String email: misspelledAccounts)
					System.out.println("\t"+email);
			}else{
				boolean emailSentCorrectly = Common.getInputOfClass(inStream, Boolean.class);
				if(emailSentCorrectly){
					//tutto corretto, si da feedback alla view
					System.out.println("Email sent correctly");
					emailsSent.add(emailToSend);
				}else{
					System.out.println("Error, server didn't send the email");
				}
			}
		}catch (IOException e){
			//dare feedback di impossibilità di inviare mail alla view
			System.out.println("Could not send email");
			e.printStackTrace();
		}finally {
			inputLock.unlock();
			outputLock.unlock();
		}
	}

	public static boolean emailAddressExists(String emailAddress){
		try{
			inputLock.lock();
			outputLock.lock();

			outStream.writeObject(CSMex.CHECK_EMAIL_ADDRESS_EXISTS);
			outStream.writeObject(emailAddress);
			return Common.getInputOfClass(inStream, Boolean.class);
		}catch (IOException e){
			System.out.println("Email checking failed");
			e.printStackTrace();
			return false;
			//continua lato server e integra lato client con quello che c è già
		}finally {
			inputLock.unlock();
			outputLock.unlock();
		}
	}
}