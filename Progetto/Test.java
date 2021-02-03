package Progetto;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class Test {
	private static final String testEmailAddress = "bubu@bubu.bubu";

	public static void main(String[] args) {
		ClientDataModel clientDataModel = new ClientDataModel();
		ServerDataModel serverDataModel = new ServerDataModel("src/database");

		while (!clientDataModel.getConnectionOkay()){
			try{
				sleep(500);
			} catch (InterruptedException interruptedException) {
				interruptedException.printStackTrace();
			}
		}
		testGetAccessFromServer(clientDataModel, true);
		testSendEmail(clientDataModel, true);
	}

	public static void testSendEmail(ClientDataModel clientDataModel, boolean printResult){
		ArrayList<String> receivers = new ArrayList<>();
			receivers.add("ciao@prova.vincya");
			receivers.add("prova@prova.vincy");
		String subject = "test";
		String body = "test test test";

		boolean result = clientDataModel.sendEmail(receivers, subject, body);
		if(printResult) {
			System.out.println("testSendEmail: "+ result);
		}
	}

	public static void testGetAccessFromServer(ClientDataModel clientDataModel, boolean printResult){
		boolean result = clientDataModel.getAccessFromServer(testEmailAddress);
		if(printResult){
			System.out.println("testGetAccessFromServer: "+ result);
		}
	}
}
