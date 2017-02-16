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

/**
 * Created by Arun on 17/01/2017.
 */
public class Main extends Application{

    static final int WIDTH = 1000;
    static final int HEIGHT = 1000;
    static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.primaryStage = primaryStage;
        Main.primaryStage.setWidth(WIDTH);
        Main.primaryStage.setHeight(HEIGHT);

        Map map = Map.getInstance();
        map.getMap().setDisplayPosition(new Coordinate(51.29766, -0.84528), 16);

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
                Grid grid = new Grid();
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