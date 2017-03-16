package GUI;

import Graph.Grid;
import Graph.Node;
import Simulation.Simulate;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.openstreetmap.gui.jmapviewer.Coordinate;

import java.awt.*;
import java.util.*;

/**
 * Created by Arun on 17/01/2017.
 */
public class Main extends Application{

    static final int WIDTH = 1000;
    static final int HEIGHT = 1000;
    static Stage primaryStage;

    double maxLat, minLat, maxLon, minLon;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.primaryStage = primaryStage;
        Main.primaryStage.setWidth(WIDTH);
        Main.primaryStage.setHeight(HEIGHT);

        Map map = Map.getInstance();
        //TODO: Get box coords
        java.util.List<double[]> latlon = new ArrayList<>();
//        latlon.add(new double[]{52.4432354, -1.9366254});
//        latlon.add(new double[]{52.437009, -1.9293171});
//        latlon.add(new double[]{52.4469844, -1.9277054});
//        latlon.add(new double[]{52.4407134, -1.9255364});

        //52.422039, -1.813235
        //52.421541, -1.776757
        //52.411569, -1.774139
        //52.404448, -1.813321

        latlon.add(new double[]{52.422039, -1.813235});
        latlon.add(new double[]{52.421541, -1.776757});
        latlon.add(new double[]{52.411569, -1.774139});
        latlon.add(new double[]{52.404448, -1.813321});
        maxLat = Grid.maxLat(latlon);
        minLat = Grid.minLat(latlon);
        minLon = Grid.minLon(latlon);
        maxLon = Grid.maxLon(latlon);

        map.getMap().setDisplayPosition(new Coordinate(52.422039, -1.813235), 16);

        Scene scene = new Scene(map);
        Main.primaryStage.setScene(scene);
        Main.primaryStage.show();

        // Needed to display GUI before simulation begins
        Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
                return null;
            }
        };
        sleeper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Grid grid = new Grid(maxLat, minLat, maxLon, minLon);
                //Map.getInstance().getMap().setGrid(grid);
                //Map.getInstance().drawMapMarkers(grid.getPairs());
                //Map.getInstance().drawBounds();
                //Map.getInstance().drawQueueServers(grid.getLinkMap());
              Simulate simulate = new Simulate(grid);
                simulate.run();
            }
        });
        new Thread(sleeper).start();
    }

    public static void main(String[] args){
        launch(args);
    }
}