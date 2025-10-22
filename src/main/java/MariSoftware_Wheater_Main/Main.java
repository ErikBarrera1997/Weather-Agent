package MariSoftware_Wheater_Main;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Main extends Application{

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI.fxml"));
        StackPane root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setAlwaysOnTop(true);
        stage.setX(1000);
        stage.setY(700);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


}
