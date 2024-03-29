package Progetto;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainClient extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        ClientDataModel model = new ClientDataModel();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = fxmlLoader.load();
        LoginController loginController = fxmlLoader.getController();
        primaryStage.setTitle("Login");
        loginController.initClientDataModel(model);
        primaryStage.setScene(new Scene(root, 500, 275));
        primaryStage.setResizable(false);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
