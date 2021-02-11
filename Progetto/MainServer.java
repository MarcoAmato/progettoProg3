package Progetto;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainServer extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("logList.fxml"));
		Parent root = fxmlLoader.load();
		ServerController serverController = fxmlLoader.getController(); //connects to ServerController
		ServerDataModel serverModel = new ServerDataModel("src/database"); //creates ServerDataModel
		serverController.initModel(serverModel);
		primaryStage.setTitle("Mail Server");
		primaryStage.setScene(new Scene(root, 640, 400));
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
