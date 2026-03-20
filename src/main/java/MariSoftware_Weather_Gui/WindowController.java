package MariSoftware_Weather_Gui;

import MariSoftware_Weather_Functions.WeatherAnimations;
import MariSoftware_Wheater_Services.LocationService;
import MariSoftware_Wheater_Services.WeatherService;
import Marisoftware_Wheater_Data.Location;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WindowController {
    private static final long REFRESH_INTERVAL_MS = 5 * 60 * 1000;

    @FXML private Label cityLabel;
    @FXML private Label conditionLabel;
    @FXML private Label temperatureText;
    @FXML private Label temperatureLabel;
    @FXML private StackPane weatherIcon;
    @FXML private VBox weatherBox;
    @FXML private Label errorLabel;
    @FXML private Label loadingLabel;
    @FXML private Button closeButton;
    @FXML private Button refreshButton;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private String cachedCity;
    private String cachedCountry;

    @FXML
    private void handleClose() {
        scheduler.shutdownNow();
        Platform.exit();
    }

    @FXML
    private void handleRefresh() {
        scheduler.execute(() -> refreshWeather(true));
    }

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
        weatherIcon.getChildren().setAll(weatherElement);

        refreshWeather(true);
        scheduler.scheduleAtFixedRate(() -> refreshWeather(false), REFRESH_INTERVAL_MS, REFRESH_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void refreshWeather(boolean forceRefresh) {
        try {
            String[] location = LocationService.getCityAndCountryCached(REFRESH_INTERVAL_MS, forceRefresh);
            cachedCity = location[0];
            cachedCountry = location[1];

            Location temperature = WeatherService.getWeatherCached(cachedCity, cachedCountry, REFRESH_INTERVAL_MS, forceRefresh);

            Platform.runLater(() -> {
                cityLabel.setText("Clima en: " + cachedCity);
                conditionLabel.setText(temperature.getWeather());
                temperatureText.setText("Temperatura");
                temperatureLabel.setText(String.format("%.1f\u00B0C", temperature.getTemperature()));
                Node dynamicIcon = WeatherAnimations.getWeatherAnimation(temperature.getWeather().toLowerCase());
                weatherIcon.getChildren().setAll(dynamicIcon);
                errorLabel.setVisible(false);
                if (refreshButton != null) {
                    refreshButton.setVisible(false);
                }
                weatherBox.requestLayout();
                weatherBox.autosize();
                weatherBox.getScene().getWindow().sizeToScene();
            });

        } catch (Exception e) {
            Platform.runLater(() -> {
                weatherBox.setVisible(true);
                errorLabel.setText("No se pudo actualizar. Reintenta.");
                errorLabel.setVisible(true);
                if (refreshButton != null) {
                    refreshButton.setVisible(true);
                }
                weatherBox.requestLayout();
            });
        }
    }
}
