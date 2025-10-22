package MariSoftware_Weather_Functions;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

public class WeatherAnimations {

    /**
     * Devuelve una animación especificada.
     * @param weatherType El tipo de clima actual. Se determina qué animación debe de ser devuelta.
     * @return Una animación del clima especificado.
     */
    public static Node getWeatherAnimation(String weatherType) {
        return switch (weatherType.toLowerCase()) {
            case "clear" -> createSunAnimation();
            case "clouds" -> createCloudAnimation();
            case "rain" -> createRainAnimation();
            case "thunderstorm" -> createStormAnimation();
            default -> new Label("?");
        };

    }

    /**
     * Crea una representación visual de un sol (clima despejado, soleado).
     * @return La animación del sol.
     */
    public static Node createSunAnimation() {
        Circle sun = new Circle(20, Color.YELLOW);
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1), sun);
        pulse.setFromX(1);
        pulse.setToX(1.2);
        pulse.setFromY(1);
        pulse.setToY(1.2);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
        return sun;
    }

    private static Node createCloudAnimation() {
        Circle cloud = new Circle(20, Color.LIGHTGRAY);
        TranslateTransition floaty = new TranslateTransition(Duration.seconds(2), cloud);
        floaty.setFromY(0);
        floaty.setToY(-5);
        floaty.setCycleCount(Animation.INDEFINITE);
        floaty.setAutoReverse(true);
        floaty.play();
        return cloud;
    }

    private static Node createRainAnimation() {
        VBox drops = new VBox(5);
        for (int i = 0; i < 3; i++) {
            Circle drop = new Circle(3, Color.DEEPSKYBLUE);
            TranslateTransition fall = new TranslateTransition(Duration.seconds(1 + i * 0.2), drop);
            fall.setFromY(0);
            fall.setToY(20);
            fall.setCycleCount(Animation.INDEFINITE);
            fall.setAutoReverse(true);
            fall.play();
            drops.getChildren().add(drop);
        }
        drops.setAlignment(Pos.CENTER);
        return drops;
    }

    private static Node createStormAnimation() {
        Polygon ray = new Polygon(0,0, 10,20, 5,20, 15,40, 5,30, 10,30, 0,10);
        ray.setFill(Color.YELLOW);
        FadeTransition flash = new FadeTransition(Duration.seconds(0.5), ray);
        flash.setFromValue(1.0);
        flash.setToValue(0.2);
        flash.setCycleCount(Animation.INDEFINITE);
        flash.setAutoReverse(true);
        flash.play();
        return ray;
    }
}
