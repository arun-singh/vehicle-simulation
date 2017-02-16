package GUI;

import Graph.Link;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;

/**
 * Created by Arun on 27/01/2017.
 */
public class LinkController {

    Map map = Map.getInstance();
    private static LinkController ourInstance = new LinkController();

    private LinkController(){}

    public void onChange(Link link, double runningProportion){
        //link.getPolyline().updatePath(runningProportion);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                map.getMap().revalidate();
            }
        });
    }

    public static LinkController getInstance(){
        return ourInstance;
    }
}
