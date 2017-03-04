package Statistics;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Created by Arun on 02/03/2017.
 */
public class Display extends Application {
    int STAGE_WIDTH = 1000;
    int STAGE_HEIGHT = 1000;
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setWidth(STAGE_WIDTH);
        primaryStage.setHeight(STAGE_HEIGHT);

        GridPane gridPane = new GridPane();
        Scene scene = new Scene(gridPane);

        List<List<Statistics>> stats = Statistics.increaseCars(10000, 50000, 10000, 1);
        VBox time = Statistics.drawCarStats(stats, "Total number of cars", "Simulation time");
        VBox shockwaves  = Statistics.drawShockwaves(stats, "Total number of cars", "Shockwaves");
        VBox vehiclesRemaining = Statistics.vehiclesRemaining(stats, "Time", "Vehicles remaining");

        gridPane.add(time, 0, 0);
        gridPane.add(shockwaves, 0, 1);
        gridPane.add(vehiclesRemaining, 1, 0);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args){
        launch(args);
    }
}
