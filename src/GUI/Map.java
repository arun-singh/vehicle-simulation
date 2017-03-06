package GUI;

import Graph.Grid;
import Graph.Node;
import javafx.embed.swing.SwingNode;
import javafx.scene.layout.GridPane;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Created by Arun on 27/01/2017.
 */
public class Map extends GridPane {

    private Grid grid;
    private JMapViewerExtension map = new JMapViewerExtension();
    private List<MapMarker[]> markerPairs = new ArrayList<>();
    private final int ZOOM_LEVEL = 50;
    private static Map ourInstance = new Map();

    public Map() {
        super();
        map.setPreferredSize(new Dimension(Main.WIDTH, Main.HEIGHT - 50));
        map.setSize(Main.WIDTH, Main.HEIGHT);
        map.setMapPolygonsVisible(true);
        map.setVisible(true);
        map.setToolTipText("");
        createMap();
    }

    public void createMap(){
        SwingNode swingNode = new SwingNode();
        createSwingContent(swingNode);
        this.add(swingNode, 0, 0);
    }

    private void createSwingContent(final SwingNode swingNode) {
        map.setDisplayPosition(new Coordinate((52), (-1.6)), ZOOM_LEVEL);
        DefaultMapController cont = new DefaultMapController(map);
        cont.setMovementMouseButton(MouseEvent.BUTTON1);
        map.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1, true));
        swingNode.setContent(map);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                swingNode.setContent(map);
            }
        });
    }

    public void drawMapMarkers(java.util.List<Node[]> coords){
        System.out.println("Caleed");
        List<MapMarker> mapMarkerDots = new ArrayList<>();
        synchronized (map) {
            for (int i = 0; i < coords.size(); i++) {
                Coordinate upstream = new Coordinate(coords.get(i)[0].getLatitude(), coords.get(i)[0].getLongitude());
                Coordinate downstream = new Coordinate(coords.get(i)[1].getLatitude(), coords.get(i)[1].getLongitude());

                MapMarkerDot up = new MapMarkerDot(upstream);
                MapMarkerDot down = new MapMarkerDot(downstream);

                mapMarkerDots.add(up);
                mapMarkerDots.add(down);

                markerPairs.add(new MapMarker[]{up, down});
            }
            map.setMapMarkerList(mapMarkerDots);
        }
    }

    public void drawBounds(){
        java.util.List<double[]> latlon = new ArrayList<>();
        latlon.add(new double[]{52.4432354, -1.9366254});
        latlon.add(new double[]{52.437009, -1.9293171});
        latlon.add(new double[]{52.4469844, -1.9277054});
        latlon.add(new double[]{52.4407134, -1.9255364});

        for(int i = 0; i<latlon.size(); i++){
            double lat = latlon.get(i)[0];
            double lon = latlon.get(i)[1];
            Coordinate upstream = new Coordinate(lat, lon);
            map.addMapMarker(new MapMarkerDot(upstream));
        }
        map.repaint();
    }


    public JMapViewerExtension getMap() {
        return map;
    }

    public static Map getInstance() {
        return ourInstance;
    }
    public List<MapMarker[]> getMarkerPairs(){return markerPairs;}

    public void setGrid(Grid grid) {
        this.grid = grid;
    }
}
