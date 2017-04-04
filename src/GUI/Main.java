package GUI;

import Graph.Grid;
import Graph.Node;
import Simulation.Simulate;
import Statistics.Statistics;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.openstreetmap.gui.jmapviewer.Coordinate;

import javafx.scene.control.Button;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

import java.util.*;
import java.util.List;

/**
 * Created by Arun on 17/01/2017.
 */
public class Main extends Application{

    static final int WIDTH = 1000;
    static final int HEIGHT = 1000;
    static Stage primaryStage;

    double maxLat, minLat, maxLon, minLon;
    boolean statsMode = false;
    static CheckBox recordBox;
    CoordPane coordPane = new CoordPane();
    Map map = Map.getInstance();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.primaryStage = primaryStage;
        Main.primaryStage.setWidth(WIDTH);
        Main.primaryStage.setHeight(HEIGHT);

        Main.recordBox = new CheckBox();

        //TODO: Get box coords
        GridPane gp = new GridPane();

        Button control = new Button("Start");
        control.setOnAction(this::ex);
        VBox m = new VBox(map);
        map.getMap().setDisplayPosition(new Coordinate((52.3800027+52.6299987)/2, (-2.1999973 + -1.7000001)/2), 10);
        map.getMap().addMapMarker(new MapMarkerDot(new Coordinate(52.6299987, -2.1999973)));
        map.getMap().addMapMarker(new MapMarkerDot(new Coordinate(52.3800027, -1.7000001)));


        gp.add(m, 0, 0);
        gp.add(control, 0, 1);
        gp.add(recordBox, 1, 1);
        gp.add(coordPane, 0, 2);

        Scene scene = new Scene(gp);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.show();
    }

    public void ex(javafx.event.ActionEvent event){

        maxLat = Double.parseDouble(coordPane._maxLatText.getText());
        minLat = Double.parseDouble(coordPane._minLatText.getText());
        maxLon = Double.parseDouble(coordPane._maxLonText.getText());
        minLon = Double.parseDouble(coordPane._minLonText.getText());

        double midLat = (minLat + maxLat) / 2;
        double midLon = (minLon + maxLon) / 2;

        map.getMap().setDisplayPosition(new Coordinate(midLat, midLon), 14);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Map.getInstance().update();
            }
        };

        Timer timer = new Timer();
        long delay = 0;
        long intevalPeriod = 100;

        timer.scheduleAtFixedRate(task, delay,
                intevalPeriod);

        Thread thread = new Thread(){
            public void run(){
                if(!statsMode) {
                    Grid grid = new Grid(maxLat, minLat, maxLon, minLon);
                    Simulate simulate = new Simulate(grid);
                    simulate.run();
                }else {
                    List<List<Statistics>> stats = Statistics.increaseCars(100, 1600, 100, 5,
                            new double[]{maxLat, minLat, maxLon, minLon});

                    Platform.runLater(() -> {
                        drawGraphs(stats);
                    });
                }
            }
        };
        thread.start();
    }

    public void drawGraphs(List<List<Statistics>> stats){
        Stage graphs = new Stage();
        graphs.setWidth(WIDTH);
        graphs.setHeight(HEIGHT);

        VBox time = Statistics.drawCarStats(stats, "Total number of cars", "Simulation time");
        VBox shockwaves  = Statistics.drawShockwaves(stats, "Total number of cars", "Shockwaves");
        VBox vehiclesRemaining = Statistics.vehiclesRemaining(stats, "Time", "Vehicles remaining");
        VBox shockwaveSingle = Statistics.drawShockwaveSingleJourney(stats, "Time", "Shockwaves generated");

        GridPane gridPane = new GridPane();
        gridPane.add(time, 0, 0);
        gridPane.add(shockwaves, 0, 1);
        gridPane.add(vehiclesRemaining, 1, 0);
        gridPane.add(shockwaveSingle, 1, 1);

        Scene scene = new Scene(gridPane);
        graphs.setScene(scene);
        graphs.show();
    }

    public static void main(String[] args){
        launch(args);
    }
}