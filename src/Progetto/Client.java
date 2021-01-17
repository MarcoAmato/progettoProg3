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

import static java.lang.Thread.sleep;

public class Client extends Application {
	private static String emailAddress;
	private static ArrayList<Email> emailsReceived;
	private static ArrayList<Email> emailsSent;
	private static ObjectInputStream inStream;
	private static ObjectOutputStream outStream;

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
					String sender = emailAddress;
					ArrayList<String> receivers = new ArrayList<>();
						receivers.add("ciao@nigga.it");
						receivers.add("bubu@bubu.bubua");
					String subject = "subject test";
					String body = "body test bla bla bla";
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

			ArrayList<Email> emailsReceivedInput = Common.getInputOfClass(inStream, ArrayList.class);
			ArrayList<Email> emailsSentInput = Common.getInputOfClass(inStream, ArrayList.class);

			if(     emailsReceivedInput == null
					|| emailsSentInput == null
					|| !Email.isArrayListOfEmails(emailsReceivedInput)
					|| !Email.isArrayListOfEmails(emailsSentInput)
			){
				throw new RuntimeException("Error, the emails got from the server have something wrong");
			}else{
				emailAddress = emailFromInput;
				emailsReceived = emailsReceivedInput;
				emailsSent = emailsSentInput;
			}

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
		}
	}

	public static void getInputFromServerLoop(){
		boolean connectionOkay = true;
		while(connectionOkay)
			try{ //here client waits for server input which for the moment will be only a new email that the client has received
				int command = Common.getInputOfClass(inStream, Integer.class);
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
			}catch (ConnectException e){
				connectionOkay = false;
			}
	}

	public static void sendEmail(Email emailToSend){ //la view chiama questo metodo quando vuole inviare la mail
		try{
			outStream.writeObject(CSMex.NEW_EMAIL_TO_SEND);
			outStream.writeObject(emailToSend);

			ArrayList<String> misspelledAccounts = Common.getInputOfClass(inStream, ArrayList.class);
			ArrayList<String> correctAccounts = Common.getInputOfClass(inStream, ArrayList.class);

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
		}
	}
}