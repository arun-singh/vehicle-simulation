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
        java.util.List<double[]> latlon = new ArrayList<>();
        latlon.add(new double[]{52.4432354, -1.9366254});
        latlon.add(new double[]{52.437009, -1.9293171});
        latlon.add(new double[]{52.4469844, -1.9277054});
        latlon.add(new double[]{52.4407134, -1.9255364});

        //52.422039, -1.813235
        //52.421541, -1.776757
        //52.411569, -1.774139
        //52.404448, -1.813321

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

//        latlon.add(new double[]{52.422039, -1.813235});
//        latlon.add(new double[]{52.421541, -1.776757});
//        latlon.add(new double[]{52.411569, -1.774139});
//        latlon.add(new double[]{52.404448, -1.813321});
        maxLat = Grid.maxLat(latlon);
        minLat = Grid.minLat(latlon);
        minLon = Grid.minLon(latlon);
        maxLon = Grid.maxLon(latlon);


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

        map.getMap().setDisplayPosition(new Coordinate(midLat, midLon), 15);

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
                    List<List<Statistics>> stats = Statistics.increaseCars(100, 1500, 100, 10,
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