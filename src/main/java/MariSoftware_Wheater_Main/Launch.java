package MariSoftware_Wheater_Main;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Screen;
import javafx.stage.StageStyle;
import javafx.geometry.Rectangle2D;
import javafx.application.Platform;

import java.io.IOException;

public class Launch extends Application{

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setAlwaysOnTop(true);
        stage.show();

        Platform.runLater(() -> {
            stage.sizeToScene();
            positionStage(stage);
        });

        stage.widthProperty().addListener((obs, oldVal, newVal) -> positionStage(stage));
        stage.heightProperty().addListener((obs, oldVal, newVal) -> positionStage(stage));
    }

    private void positionStage(Stage stage) {
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double margin = 8;
        double x = visualBounds.getMaxX() - stage.getWidth() - margin;
        double y = visualBounds.getMaxY() - stage.getHeight() - margin;
        stage.setX(x);
        stage.setY(y);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
