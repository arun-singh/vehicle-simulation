package GUI;

import javafx.embed.swing.SwingNode;
import javafx.scene.layout.GridPane;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Created by Arun on 27/01/2017.
 */
public class Map extends GridPane {

    private JMapViewer map = new JMapViewer();
    private final int ZOOM_LEVEL = 50;
    private static Map ourInstance = new Map();

    public Map() {
        super();
        map.setPreferredSize(new Dimension(Main.WIDTH, Main.HEIGHT - 50));
        map.setSize(Main.WIDTH, Main.HEIGHT);
        map.setMapPolygonsVisible(true);
        map.setVisible(true);
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
        //swingNode.setContent(map);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                swingNode.setContent(map);
            }
        });
    }

    public void drawMapMarkers(java.util.List<Double[]> coords){
        for(int i = 0; i < coords.size(); i++){
            Coordinate c = new Coordinate(coords.get(i)[0], coords.get(i)[1]);
            map.addMapMarker(new MapMarkerDot(c));
        }
        map.repaint();
    }

    public JMapViewer getMap() {
        return map;
    }

    public static Map getInstance() {
        return ourInstance;
    }
}
