package GUI;

import Graph.Grid;
import Simulation.Simulate;
import Statistics.Statistics;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
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
public class Display extends Application{

    static final int WIDTH = 1000;
    static final int HEIGHT = 1000;
    static final double maxLatDatabase = 52.6299987;
    static final double minLatDatabase = 52.3800027;
    static final double maxLonDatabase = -1.7000001;
    static final double minLonDatabase = -2.1999973;

    static Stage primaryStage;
    static public ScheduledService<Boolean> service;

    double maxLat, minLat, maxLon, minLon;
    public static CheckBox recordBox;
    public static CheckBox stats;
    private Button control;

    CoordPane coordPane = new CoordPane();
    Map map = Map.getInstance();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Display.primaryStage = primaryStage;
        Display.primaryStage.setWidth(WIDTH);
        Display.primaryStage.setHeight(HEIGHT);

        recordBox = new CheckBox();
        stats = new CheckBox();

        GridPane gp = new GridPane();
        gp.setVgap(10);

        Label drawBox = new Label("Draw bounding box");
        Label statsOption = new Label("Enable statistics mode");

        control = new Button("Start");
        control.setOnAction(this::execute);
        VBox m = new VBox(map);
        setPosition();
        map.getMap().addMapMarker(new MapMarkerDot(new Coordinate(maxLatDatabase, minLonDatabase)));
        map.getMap().addMapMarker(new MapMarkerDot(new Coordinate(minLatDatabase, maxLonDatabase)));


        GridPane user = new GridPane();
        user.getColumnConstraints().add(new ColumnConstraints(70));
        user.getColumnConstraints().add(new ColumnConstraints(130));
        user.getColumnConstraints().add(new ColumnConstraints(50));
        user.getColumnConstraints().add(new ColumnConstraints(145));
        user.getColumnConstraints().add(new ColumnConstraints(50));
        //user.getColumnConstraints().add(new ColumnConstraints(650));
//        grid.getRowConstraints().add(new RowConstraints(getHeight() * 0.65));
//        grid.getRowConstraints().add(new RowConstraints(getHeight() * 0.35));

        user.add(control, 0, 0);
        user.add(drawBox, 1, 0);
        user.add(recordBox, 2, 0);
        user.add(statsOption, 3, 0);
        user.add(stats, 4, 0);

        gp.add(m, 0, 0);
        gp.add(user, 0, 1);
//        gp.add(recordBox, 1, 1);
        gp.add(coordPane, 0, 2);

        Scene scene = new Scene(gp);
        Display.primaryStage.setScene(scene);
        Display.primaryStage.show();
    }

    private void execute(javafx.event.ActionEvent event){

        maxLat = Double.parseDouble(coordPane._maxLatText.getText());
        minLat = Double.parseDouble(coordPane._minLatText.getText());
        maxLon = Double.parseDouble(coordPane._maxLonText.getText());
        minLon = Double.parseDouble(coordPane._minLonText.getText());

        double midLat = (minLat + maxLat) / 2;
        double midLon = (minLon + maxLon) / 2;

        map.getMap().setDisplayPosition(new Coordinate(midLat, midLon), 14);

//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                Map.getInstance().update();
//            }
//        };

//        Timer timer = new Timer();
//        long delay = 0;
//        long intevalPeriod = 100;
//
//        timer.scheduleAtFixedRate(task, delay,
//                intevalPeriod);

            service = new ScheduledService<Boolean>() {
            @Override
            protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        Map.getInstance().update();
                        return true;
                    }
                };
            }
        };
        service.setPeriod(javafx.util.Duration.millis(100));
        service.start();


        userInputLock(true);
        Thread thread = new Thread(){
            public void run(){
                if(!stats.isSelected()) {
                    Grid grid = new Grid(maxLat, minLat, maxLon, minLon);
                    Simulate simulate = new Simulate(grid);
                    simulate.run();

                    Platform.runLater(() -> {
                        userInputLock(false);
                        clearMap();
                    });
                    Platform.runLater(() -> {
                        clearRectangles();
                    });
                    Platform.runLater(() -> {
                        clearMarkers();
                    });
                    Platform.runLater(() -> {
                        setPosition();
                    });

                    }else {
                    List<List<Statistics>> stats = Statistics.increaseCars2(100, 1500, 100, 20,
                            new double[]{maxLat, minLat, maxLon, minLon});

                    Platform.runLater(() -> {
                        drawGraphs(stats);
                        userInputLock(false);
                        clearMap();
                        clearRectangles();
                    });
                }
            }
        };
        thread.start();
    }

    private void drawGraphs(List<List<Statistics>> stats){
        Stage graphs = new Stage();
        graphs.setWidth(WIDTH);
        graphs.setHeight(HEIGHT);

        VBox time = Statistics.drawCarStats(stats, "Total number of cars", "Simulation time");
        VBox shockwaves  = Statistics.drawShockwaves(stats, "Total number of cars", "Shockwaves");
        VBox vehiclesRemaining = Statistics.drawVehiclesRemaining(stats, "Time", "Vehicles remaining");
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

    public void userInputLock(boolean lock){
        stats.setDisable(lock);
        recordBox.setDisable(lock);
        control.setDisable(lock);
    }

    public synchronized void clearMap(){
        map.getMap().removeAllMapPolygons();
    }

    public synchronized void clearRectangles(){
        map.getMap().removeAllMapRectangles();
    }

    public synchronized void clearMarkers(){
        map.getMap().removeAllMapMarkers();

        map.getMap().addMapMarker(new MapMarkerDot(new Coordinate(maxLatDatabase, minLonDatabase)));
        map.getMap().addMapMarker(new MapMarkerDot(new Coordinate(minLatDatabase, maxLonDatabase)));
    }

    public synchronized void setPosition(){
        map.getMap().setDisplayPosition(new Coordinate((minLatDatabase+maxLatDatabase)/2, (minLonDatabase+maxLonDatabase)/2), 10);
    }

    public static void main(String[] args){
        launch(args);
    }
}