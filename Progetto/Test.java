package Progetto;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class Test {
	public static void main(String[] args) {
		ClientDataModel cdmBubu = new ClientDataModel();
		ClientDataModel cdmW = new ClientDataModel();
		ServerDataModel sdm = new ServerDataModel("src/database");
		while(!cdmBubu.connectionOkayProperty().get()){
			try{
				sleep(2000);
			}catch (InterruptedException e){
				e.printStackTrace();
			}
		}
		cdmBubu.getAccessFromServer("bubu@bubu.bubu");
		cdmW.getAccessFromServer("w");

		ArrayList<String> receivers = new ArrayList<>();
		receivers.add("w");
		cdmBubu.sendEmail(receivers, "prova", "body");
	}
}
