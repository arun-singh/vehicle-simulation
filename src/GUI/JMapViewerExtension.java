package GUI;

import Graph.Grid;
import Graph.Node;
import javafx.scene.paint.*;
import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import java.awt.*;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by Arun on 05/03/2017.
 */
public class JMapViewerExtension extends JMapViewer{
    private Grid grid;

    public JMapViewerExtension(){
        super();
        new DefaultMapController(this){
            @Override
            public void mouseClicked(MouseEvent e) {
                Coordinate c = map.getPosition(e.getPoint());
                MapMarkerDot source = nearest(c);
                MapMarkerDot target = (MapMarkerDot) Map.getInstance().getMarkerPairs()
                        .stream()
                        .filter(m->m[0].equals(source))
                        .collect(Collectors.toList())
                        .get(0)[1];

                if(source==null||target==null)
                    System.out.println("null");
                else{
                    Color sourceColour = source.getBackColor().equals(Color.RED) ? Color.YELLOW : Color.RED;
                    Color targetColour = source.getBackColor().equals(Color.RED) ? Color.YELLOW : Color.GREEN;
                    source.setBackColor(sourceColour);
                    target.setBackColor(targetColour);
                    repaint();
                }
            }
        };

    }

//    @Override
//    public String getToolTipText(MouseEvent event){
//        System.out.println("Click");
//        Coordinate c = getPosition(event.getX(), event.getY());
//        MapMarkerDot marker = nearest(c);
//        if(marker!=null) {
//            Node[] pair = findTargetMarker(grid.getPairs(), c);
//            MapMarkerDot target = nearest(new Coordinate(pair[1].getLatitude(), pair[1].getLongitude()));
//            if(target!=null){
//                synchronized(this){
//                    System.out.println("changed");
//                    target.setColor(Color.RED);
//                }
//                return "Changed";
//            }
//        }
//        System.out.println("Error");
//        return "Error";
//    }

    public MapMarkerDot nearest(Coordinate c){
        MapMarkerDot mmd = (MapMarkerDot)this.getMapMarkerList().stream()
                .reduce((m1, m2)-> getNodesDistance(m1.getCoordinate(), c) < getNodesDistance(m2.getCoordinate(), c) ? m1 : m2 )
                .get();
        return mmd;
    }

    public Node[] findTargetMarker(List<Node[]> pairs, Coordinate target){
        List<Node[]> pair =  pairs.stream()
                .filter(n->n[0].getLatitude()==target.getLat() && n[0].getLongitude()==target.getLon())
                .collect(Collectors.toList());
        return pair.size() != 0 ? pair.get(0) : null;
    }


    public static double getNodesDistance(Coordinate startCoord, Coordinate endCoord) {
        double crowFliesLength;
        double R = 6371000; // Earths radius
        // get lat and longs
        double latI = startCoord.getLat();
        double latI2 = endCoord.getLat();
        double lngI = startCoord.getLon();
        double lngI2 = endCoord.getLon();
        // calculate change in lat and longs in rads
        double dLat = Math.toRadians(latI2 - latI);
        double dLng = Math.toRadians(lngI2 - lngI);
        // Use 'Haversine' method to convert lat/long into distance is
        // metres
        double a = (Math.sin(dLat / 2) * Math.sin(dLat / 2))
                + (Math.cos(Math.toRadians(latI)) * Math.cos(Math
                .toRadians(latI2)))
                * (Math.sin(dLng / 2) * Math.sin(dLng / 2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        crowFliesLength = R * c;
        return crowFliesLength;
    }


    public void setGrid(Grid grid) {
        this.grid = grid;
    }
}
