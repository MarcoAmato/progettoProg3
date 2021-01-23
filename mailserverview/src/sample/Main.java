package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("prova.fxml"));
        Parent root = fxmlLoader.load();
        Controller controller = fxmlLoader.getController();
        Datamodel model = new Datamodel();
        controller.initModel(model);
        controller.createlist();
        primaryStage.setTitle("Mail Server");
        primaryStage.setScene(new Scene(root, 640, 400));
        primaryStage.show();
        controller.loop();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
