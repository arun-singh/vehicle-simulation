package GUI;

import Graph.Grid;
import Graph.Link;
import Graph.MapUtil;
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
                Coordinate c = (Coordinate)map.getPosition(e.getPoint());
                MapMarkerDot source = nearest(c);
                Node nsource = new Node(source.getLat(), source.getLon());
                System.out.println("-----------------------------------------------");
                System.out.println(source.getLat() + ", " + source.getLon());

                List<Link> targetList = Map.getInstance().getGrid()
                        .getLinkMap()
                        .entrySet()
                        .stream()
                        .filter(m->m.getValue().getSource().equals(nsource))
                        .map(l->l.getValue())
                        .collect(Collectors.toList());

                if(targetList.size()==0)
                    System.out.println("No Targets");
                else{
                    System.out.println("-----------------------------------------------");
                    System.out.println("Source has " + targetList.size() + " targets");
                    Color sourceColour = source.getBackColor().equals(Color.GREEN) ? Color.YELLOW : Color.GREEN;
                    source.setBackColor(sourceColour);
                    repaint();

                    for(int i = 0 ; i < targetList.size(); i++){
                        double targetLat = targetList.get(i).getTarget().getLatitude();
                        double targetLon = targetList.get(i).getTarget().getLongitude();
                        System.out.println(targetLat + ", " + targetLon);
                        for (int j = 0; j < getMapMarkerList().size(); j++) {
                            if(getMapMarkerList().get(j).getLat() == targetLat && getMapMarkerList().get(j).getLon()==targetLon){
                                //System.out.print("Found";
                                List<MapMarker> l = getMapMarkerList();
                                MapMarkerDot ret= (MapMarkerDot) getMapMarkerList().get(j);
                                Color targetColour = source.getBackColor().equals(Color.GREEN) ? Color.RED : Color.YELLOW;
                                //System.out.println(targetColour.toString());
                                ret.setBackColor(targetColour);
                                map.repaint();
                                map.revalidate();
                                break;
                            }
                        }
                    }
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
                .reduce((m1, m2)-> MapUtil.getNodesDistance(m1.getCoordinate(), c) < MapUtil.getNodesDistance(m2.getCoordinate(), c) ? m1 : m2 )
                .get();
        return mmd;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }
}
