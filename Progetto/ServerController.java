package Progetto;

import javafx.fxml.FXML;

import javafx.scene.control.ListView;

public class ServerController{
	private ServerDataModel serverDataModel;

	@FXML ListView<String> logList;

	public void initModel(ServerDataModel serverDataModel) {
		// serverDataModel initialized only once
		if (this.serverDataModel != null) {
			throw new IllegalStateException("ServerModel can only be initialized once");
		}

		this.serverDataModel = serverDataModel;
		logList.setItems(serverDataModel.logList());
	}

}