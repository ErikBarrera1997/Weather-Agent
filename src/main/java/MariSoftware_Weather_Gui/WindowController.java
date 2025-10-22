package MariSoftware_Weather_Gui;

import MariSoftware_Weather_Functions.WeatherAnimations;
import MariSoftware_Wheater_Services.LocationService;
import MariSoftware_Wheater_Services.WeatherService;
import Marisoftware_Wheater_Data.Location;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class WindowController {
    @FXML private Label cityLabel;
    @FXML private Label conditionLabel;
    @FXML private Label temperatureText;
    @FXML private Label temperatureLabel;
    @FXML private StackPane weatherIcon;
    @FXML private VBox weatherBox;
    @FXML private Label errorLabel;

    public void initialize() {

        Node weatherElement = WeatherAnimations.getWeatherAnimation("clear");
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1), weatherElement);
        pulse.setFromX(1);
        pulse.setToX(1.2);
        pulse.setFromY(1);
        pulse.setToY(1.2);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        new Thread(() -> {
            try {
                String[] location = LocationService.getCityAndCountry();
                Location temperaure = WeatherService.getWeather(location[0], location[1]);

                javafx.application.Platform.runLater(() -> {
                    cityLabel.setText("Clima en: " + location[0]);
                    conditionLabel.setText(temperaure.getWeather());
                    temperatureText.setText("Temperatura");
                    temperatureLabel.setText(String.format("%.1f°C", temperaure.getTemperature()));
                    Node dynamicIcon = WeatherAnimations.getWeatherAnimation(temperaure.getWeather().toLowerCase());
                    weatherIcon.getChildren().setAll(dynamicIcon);
                    weatherBox.requestLayout();
                    weatherBox.autosize();
                    weatherBox.getScene().getWindow().sizeToScene();
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    weatherBox.setVisible(true);
                    errorLabel.setText("No se pudo cargar los datos");
                    errorLabel.setVisible(true);
                    weatherBox.requestLayout();
                    weatherBox.autosize();
                    weatherBox.getScene().getWindow().sizeToScene();
                });
            }

        }).start();
    }


}
