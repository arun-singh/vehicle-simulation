package GUI;

import Graph.*;
import javafx.embed.swing.SwingNode;
import javafx.scene.layout.GridPane;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Arun on 27/01/2017.
 */
public class Map extends GridPane {

    private Grid grid;
    private JMapViewer map = new JMapViewer();
    private List<MapMarker[]> markerPairs = new ArrayList<>();
    private final int ZOOM_LEVEL = 50;
    private static Map ourInstance = new Map();

    public Map() {
        super();
        map.setPreferredSize(new Dimension(Display.WIDTH, Display.HEIGHT));
        map.setSize(Display.WIDTH, Display.HEIGHT);
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
        MapController cont = new MapController(map);
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

    public void update(){
        map.repaint();
    }

    public void drawMapMarkers(java.util.List<Node[]> coords){
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

    public void drawMapMarkersFromLinks(HashMap<Integer, Link> linkMap){
        List<MapMarker> mapMarkerDots = new ArrayList<>();
        synchronized (map) {
            for (java.util.Map.Entry<Integer, Link> entry : linkMap.entrySet()) {
                Coordinate upstream = new Coordinate(entry.getValue().getSource().getLatitude(), entry.getValue().getSource().getLongitude());
                Coordinate downstream = new Coordinate(entry.getValue().getTarget().getLatitude(), entry.getValue().getTarget().getLongitude());

                MapMarkerDot up = new MapMarkerDot(upstream);
                MapMarkerDot down = new MapMarkerDot(downstream);

                mapMarkerDots.add(up);
                mapMarkerDots.add(down);

                //markerPairs.add(new MapMarker[]{up, down});
            }
            map.setMapMarkerList(mapMarkerDots);
        }
    }

    public void drawEdges(List<Node[]> coords){
        List<MapPolygon> lines = new ArrayList<>();
        for(int i = 0; i < coords.size(); i++){
            Coordinate upstream = new Coordinate(coords.get(i)[0].getLatitude(), coords.get(i)[0].getLongitude());
            Coordinate downstream = new Coordinate(coords.get(i)[1].getLatitude(), coords.get(i)[1].getLongitude());
            MapPolyLine poly = new MapPolyLine(new ArrayList<Coordinate>(){{
                add(upstream);
                add(downstream);
            }});
            poly.setColor(Color.red);
            poly.setStroke(new BasicStroke(4));
            poly.setVisible(true);
            lines.add(poly);
        }
        map.setMapPolygonList(lines);
        map.setMapPolygonsVisible(true);
        //map.repaint();
    }

    public void drawQueueServers(HashMap<Integer, Link> linkMap){
        System.out.println("Total nodes is " + (linkMap.entrySet().size() * 2));
        HashSet<Node> serverNodes = new HashSet<>();
        List<Link> links = linkMap.entrySet().stream()
                                                .filter(l->l.getValue().getServers().size()==0)
                                                .map(l->l.getValue())
                                                .collect(Collectors.toList());

        for(Link link : links){
            //for(Server server : link.getServers()){
                serverNodes.add(link.getSource());
            //}
        }
//
        List<MapMarker> mapMarkerDots = new ArrayList<>();
        for(Node node : serverNodes){
            Coordinate coordinate = new Coordinate(node.getLatitude(), node.getLongitude());
            mapMarkerDots.add(new MapMarkerDot(coordinate));
        }
        System.out.println("Total servers " + links.size());
        map.setMapMarkerList(mapMarkerDots);
    }

    public void drawUnconnected(List<Node[]> pairs){
        List<MapMarker> mapMarkerDots = new ArrayList<>();
        for(int i = 0; i < pairs.size(); i++){
            Coordinate upstream = new Coordinate(pairs.get(i)[0].getLatitude(), pairs.get(i)[0].getLongitude());
            Coordinate downstream = new Coordinate(pairs.get(i)[1].getLatitude(), pairs.get(i)[1].getLongitude());

            MapMarkerDot up = new MapMarkerDot(upstream);
            up.setBackColor(Color.CYAN);
            MapMarkerDot down = new MapMarkerDot(downstream);
            down.setBackColor(Color.cyan);

            mapMarkerDots.add(up);
            mapMarkerDots.add(down);
        }
        map.setMapMarkerList(mapMarkerDots);
    }


    public void drawRoutes(Vehicle[] vehicles){
        List<MapPolygon> polylines = new ArrayList<>();
        for (Vehicle veh :
                vehicles) {
            List<Coordinate> coords = new ArrayList<>();
            List<Link> route = veh.getRoute();
            for (int i = 0; i < route.size(); i++) {
//                List<Coordinate> line = route.get(i).getPolyline().getCoordinates();
//                for(Coordinate c : line){
//                    coords.add(c);
//                }
                Coordinate s = new Coordinate(route.get(i).getSource().getLatitude(), route.get(i).getSource().getLongitude());
                Coordinate t = new Coordinate(route.get(i).getTarget().getLatitude(), route.get(i).getTarget().getLongitude());
                coords.add(s); coords.add(t);
            }
            polylines.add(new MapPolyLine(coords));
        }
        map.setMapPolygonList(polylines);
    }

    public void drawGrid(HashMap<Integer, Link> linkMap){
        List<MapPolygon> poly = new ArrayList<>();
        List<Link> links = linkMap.entrySet().stream().map(l->l.getValue()).collect(Collectors.toList());
        for(java.util.Map.Entry<Integer, Link> entry : linkMap.entrySet()){
            List<Coordinate> coords = entry.getValue().getPolyline().getCoordinates();
            poly.add(new MapPolyLine(coords));
        }
        map.setMapPolygonList(poly);
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


    public JMapViewer getMap() {
        return map;
    }

    public static Map getInstance() {
        return ourInstance;
    }
    public List<MapMarker[]> getMarkerPairs(){return markerPairs;}

    public void setGrid(Grid grid) {
        this.grid = grid;
    }
    public Grid getGrid(){return grid;}
}
