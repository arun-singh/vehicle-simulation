package Graph;

import GUI.*;
import Statistics.Statistics;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Arun on 12/03/2017.
 */
public class GH {
    static Random ran = new Random();
    static GraphHopper hopper = new GraphHopper();
    public static void request(){
        hopper.setOSMFile("/Users/Arun/Documents/MOJO-Simulation/Resources/Birmingham.osm");
        hopper.setEncodingManager(new EncodingManager("car"));
        hopper.setGraphHopperLocation("../Resources/gh");

        hopper.importOrLoad();


        GHRequest req = new GHRequest(52.4470355, -1.9352142, 52.4406603, -1.9253879).
                setWeighting("fastest").
                setVehicle("car").
                setLocale(Locale.UK);
        GHResponse rsp = hopper.route(req);

        if(rsp.hasErrors()) {
            // handle them!
            // rsp.getErrors()
            System.out.println("errors");
            return;
        }

        PathWrapper path = rsp.getBest();

        PointList pointList = path.getPoints();
        System.out.println("Point size: " + pointList.size());
        double distance = path.getDistance();
        long timeInMs = path.getTime();

        for(int i = 0; i <pointList.size(); i++){
            double lat = pointList.getLat(i);
            double lon = pointList.getLon(i);
        }

        InstructionList il = path.getInstructions();
        int ilcount = 0;
        for(Instruction instruction : il) {
            instruction.getDistance();
            System.out.println("---------------------");
            System.out.println(instruction.getSign());
            ilcount+=instruction.getPoints().size();
            for (int i = 0; i < instruction.getPoints().size(); i++) {
                System.out.println(instruction.getPoints().getLatitude(i) + ", " + instruction.getPoints().getLongitude(i));
            }
        }
        System.out.println("Instruction point total: " + ilcount);
    }

    public static List<List<Link>> generateRoutes(HashMap<Integer, Link> linkMap, int routeTotal){
        hopper.setOSMFile("/Users/Arun/Documents/MOJO-Simulation/Resources/Birmingham.osm");
        hopper.setEncodingManager(new EncodingManager("car"));
        hopper.setGraphHopperLocation("../Resources/gh");
        hopper.importOrLoad();

        List<List<Link>> routes = new ArrayList<>();
        List<Link> inputLinks = Statistics.getInputLinks(linkMap);

        for(int i =0; i <routeTotal; i++){
            int startingIndex = ran.nextInt((inputLinks.size()-1) + 1);
            Link startingLink = inputLinks.get(startingIndex);
            Node startingNode = startingLink.getTarget();
            Node endingNode = pickEndLocation(inputLinks, startingLink);
            InstructionList iList = getGHRoute(hopper, startingNode, endingNode);
            drawInstructions(iList);

            routes.add(ghToLink(startingLink, endingNode, iList));
        }
        return routes;
    }

    private static Node pickEndLocation(List<Link> inputLinks, Link starting){
        inputLinks.remove(starting);
         List<Link> minimumDistance = inputLinks.stream()
                                                .filter(l-> MapUtil.getNodesDistance(new Coordinate(l.getSource().getLatitude(), l.getSource().getLongitude()),
                                                        new Coordinate(starting.getSource().getLatitude(), starting.getSource().getLongitude())) > 500)
                                                .collect(Collectors.toList());

        int ranIndex = ran.nextInt((minimumDistance.size()-1) + 1);
        return minimumDistance.get(ranIndex).getSource();
    }

    private static InstructionList getGHRoute(GraphHopper hopper, Node start, Node end){
        GHRequest req = new GHRequest(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude()).
                setWeighting("fastest").
                setVehicle("car").
                setLocale(Locale.UK);
        GHResponse rsp = hopper.route(req);

        if(rsp.hasErrors()) {
            // handle them!
            // rsp.getErrors()
            System.out.println("errors");
        }

        PathWrapper path = rsp.getBest();
        return path.getInstructions();
    }


    private static double distanceFromTarget(Instruction instruction, Link target){
        double sum = 0;
        List<Coordinate> coords = target.getPolyline().getCoordinates();
        PointList points = instruction.getPoints();
        for (int i = 0; i < points.size(); i++) {
            for(Coordinate c : coords){
                sum += MapUtil.getNodesDistance(new Coordinate(points.getLat(i), points.getLon(i)), new Coordinate(c.getLat(), c.getLon()));
            }
        }
        return sum;
    }

    private static List<Link> ghToLink(Link start, Node end, InstructionList iList){
        List<Link> route = new ArrayList<>();
        List<Coordinate> coords = new ArrayList<>();


        HashMap<Integer, Link> linkMap = GUI.Map.getInstance().getGrid().getLinkMap();
        Node chosen = start.getTarget();
        for(int i = 0; i <iList.size(); i++){
            Instruction firstInstr = iList.get(i);
            List<Link> targets = MapUtil.getTargetLinks(linkMap, chosen, route);

            //Optional<Link> newLink = targets.stream().reduce((t1,t2)->distanceFromTarget(firstInstr, t1) < distanceFromTarget(firstInstr, t2) ? t1 : t2);
            double avgDis = 100000;
            Link newLink = null;
            for(Link t : targets){
                double dis = distanceFromTarget(firstInstr, t);
                if(dis<avgDis) {
                    avgDis = dis;
                    newLink = t;
                }
            }

//            if(!newLink.isPresent()){
//                continue;
//            }
            chosen = newLink.getTarget();

            //draw new link
            Coordinate c = new Coordinate(newLink.getSource().getLatitude(), newLink.getSource().getLongitude());
            Coordinate c1 = new Coordinate(newLink.getTarget().getLatitude(), newLink.getTarget().getLongitude());
            coords.add(c); coords.add(c1);
            route.add(newLink);
        }
        MapPolyLine line = new MapPolyLine(coords);
        line.setBackColor(Color.BLUE);
        line.setColor(Color.BLUE);
        GUI.Map.getInstance().getMap().addMapPolygon(line);
        return route;
    }

    public synchronized static void drawInstructions(InstructionList iList){
        List<Coordinate> coords = new ArrayList<>();
        List<MapMarker> markers = new ArrayList<>();
        for(Instruction instr : iList){
            PointList pl = instr .getPoints();
            for (int i = 0; i < pl.size(); i++) {
                MapMarkerDot md = new MapMarkerDot(pl.getLat(i), pl.getLon(i));
                markers.add(md);
            }
        }
        GUI.Map.getInstance().getMap().setMapMarkerList(markers);
    }

    public static void main(String[] args){
        GH.request();
    }

}
