package Progetto;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class Test {
	private static final String testEmailAddress = "bubu@bubu.bubu";

	public static void main(String[] args) {
		ClientDataModel clientDataModel = new ClientDataModel();
		ServerDataModel serverDataModel = new ServerDataModel("src/database");

		//testConnectionOkayProperty(clientDataModel, true);

		while (!clientDataModel.getConnectionOkay()){
			try{
				sleep(500);
			} catch (InterruptedException interruptedException) {
				interruptedException.printStackTrace();
			}
		}

		BooleanProperty connectionOkay = testConnectionOkayProperty(clientDataModel, false);
			/*System.out.println(connectionOkay.get());*/
		testGetAccessFromServer(clientDataModel, false);
		//String emailAddressValue = testGetEmailAddress(clientDataModel, true);
		StringProperty emailAddressProperty = testEmailAddressProperty(clientDataModel, false);
		ObservableList<Email> emailsReceived = testEmailsReceivedProperty(clientDataModel, false);
		ObservableList<Email> emailsSent = testEmailsSentProperty(clientDataModel, false);

		//testSendEmail(clientDataModel, true);

		//testReplyEmail(clientDataModel, emailsReceived.get(0),true);
		//testReplyAllEmail(clientDataModel, emailsReceived.get(0),true);
		//testEmailAddressExist(clientDataModel, "bubu@bubu.bubu", true);

		//testDeleteEmail(clientDataModel, emailsSent.get(0), true);
		//printObservableList(emailsSent);
	}

	//tests getEmailAddress
	public static String testGetEmailAddress(ClientDataModel clientDataModel, boolean printResult){
		String result = clientDataModel.getEmailAddress();
		if(printResult){
			System.out.println("testGetEmailAddress: " + result);
		}
		return result;
	}

	//tests emailAddressProperty
	public static StringProperty testEmailAddressProperty(ClientDataModel clientDataModel, boolean printResult){
		StringProperty emailAddress = clientDataModel.emailAddressProperty();
		if(printResult){
			if(emailAddress == null){
				System.out.println("testGetEmailAddress: null");
			}else{
				System.out.println("testGetEmailAddress: value = " + emailAddress.get());
			}
		}
		return emailAddress;
	}

	//test emailsReceivedProperty
	public static ObservableList<Email> testEmailsReceivedProperty(ClientDataModel clientDataModel, boolean printResult){
		ObservableList<Email> emailsReceivedProperty = clientDataModel.emailsReceivedProperty();
		if(printResult){
			if(emailsReceivedProperty == null){
				System.out.println("testEmailsReceivedProperty: null");
			}else{
				System.out.println("testEmailsReceivedProperty: value = ");
				printObservableList(emailsReceivedProperty);
			}
		}
		return emailsReceivedProperty;
	}

	//test emailsSentProperty
	public static ObservableList<Email> testEmailsSentProperty(ClientDataModel clientDataModel, boolean printResult){
		ObservableList<Email> emailsSentProperty = clientDataModel.emailsSentProperty();
		if(printResult){
			if(emailsSentProperty == null){
				System.out.println("testEmailsSentProperty: null");
			}else{
				System.out.println("testEmailsSentProperty: value = ");
				printObservableList(emailsSentProperty);
			}
		}
		return emailsSentProperty;
	}

	//tests connectionOkayProperty
	public static BooleanProperty testConnectionOkayProperty(ClientDataModel clientDataModel, boolean printResult){
		BooleanProperty connectionOkay = clientDataModel.connectionOkayProperty();
		if(printResult){
			if(connectionOkay == null){
				System.out.println("testConnectionOkayProperty: null");
			}else {
				System.out.println("testConnectionOkayProperty: value = " + connectionOkay.get());
			}
		}
		return connectionOkay;
	}

	//tests getAccessFromServer
	public static void testGetAccessFromServer(ClientDataModel clientDataModel, boolean printResult){
		boolean result = clientDataModel.getAccessFromServer(testEmailAddress);
		if(printResult){
			System.out.println("testGetAccessFromServer: "+ result);
		}
	}

	//tests sendEmail
	public static void testSendEmail(ClientDataModel clientDataModel, boolean printResult){
		ArrayList<String> receivers = new ArrayList<>();
			receivers.add("ciao@prova.vincy");
			receivers.add("prova@prova.vincy");
		String subject = "test";
		String body = "test test test";

		boolean result = clientDataModel.sendEmail(receivers, subject, body);
		if(printResult) {
			System.out.println("testSendEmail: "+ result);
		}
	}

	//tests deleteEmail
	public static void testDeleteEmail(ClientDataModel clientDataModel, Email emailToDelete, boolean printResult){
		boolean okay = clientDataModel.deleteEmail(emailToDelete);
		if(printResult){
			System.out.println("testDeleteEmail: "+okay);
		}
	}

	//test replyEmail
	public static void testReplyEmail(ClientDataModel clientDataModel, Email emailToReply, boolean printResult){
		boolean okay = clientDataModel.replyEmail(emailToReply, "test reply");
		if(printResult){
			System.out.println("testReplyEmail: "+okay);
		}
	}

	//test replyAllEmail
	public static void testReplyAllEmail(ClientDataModel clientDataModel, Email emailToReply, boolean printResult){
		boolean okay = clientDataModel.replyAllEmail(emailToReply, "test reply all");
		if(printResult){
			System.out.println("testReplyAllEmail: " + okay);
		}
	}

	public static void testEmailAddressExist(ClientDataModel clientDataModel, String emailAddressToCheck, boolean printResult){
		boolean okay = clientDataModel.emailAddressExists(emailAddressToCheck);
		if(printResult){
			System.out.println("testEmailAddressExist: " + okay);
		}
	}

	//prints an observableList
	private static <T> void printObservableList(ObservableList<T> observableList){
		observableList.forEach(System.out::println);
	}
}