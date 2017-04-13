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
import com.graphhopper.util.shapes.GHPoint3D;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerCircle;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.Style;
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
        List<Link> inputLinks = MapUtil.getInputLinks(linkMap, 0);

        for(int i =0; i <routeTotal; i++){
            int startingIndex = ran.nextInt((inputLinks.size()-1) + 1);
            Link startingLink = inputLinks.get(startingIndex);
            Node startingNode = startingLink.getTarget();
            Node endingNode = pickEndLocation(inputLinks, startingLink);
            InstructionList iList = getGHRoute(hopper, startingNode, endingNode);
            List<Link> route = ghToLink(startingLink, endingNode, iList);
            //drawInstructions(iList);
            drawRoute(route);
            routes.add(route);
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
        return sum/coords.size();
    }

    private static double distanceFromTarget2(Instruction instruction, Link target){
        double sum = 0;
        GUI.Map m = GUI.Map.getInstance();
        PointList points = instruction.getPoints();
        List<Coordinate> coords = target.getPolyline().getCoordinates();

        List<Point> targetPoints = new ArrayList<>();
        List<Point> instructionPoints = new ArrayList<>();
        for(Coordinate c : coords){
            targetPoints.add(m.getMap().getMapPosition(c));
        }
        for(int i = 0; i < points.size(); i++){
            Coordinate c = new Coordinate(points.getLat(i), points.getLon(i));
            instructionPoints.add(m.getMap().getMapPosition(c));
        }

        for (Point instr : instructionPoints) {
            for(Point p : targetPoints){
                sum += Point.distanceSq(instr.getX(), instr.getY(), p.getX(), p.getY());
            }
        }
        return sum;
    }

    private static double distanceFromTarget3(InstructionList iList, Link target, int pos){
        double sum = 0;
        double minimumTotal = 0;
        GUI.Map m = GUI.Map.getInstance();
        List<Coordinate> coords = target.getPolyline().getCoordinates();

        List<Point> targetPoints = new ArrayList<>();
        List<List<Point>> instructionPoints = new ArrayList<>();
        for(Coordinate c : coords){
            targetPoints.add(m.getMap().getMapPosition(c));
        }

        for(int i = pos; i < iList.size(); i++){
            List<Point> instrPoints = new ArrayList<>();
            Instruction currInstruction = iList.get(i);
            for(GHPoint3D p : currInstruction.getPoints()){
                Coordinate c = new Coordinate(p.getLat(), p.getLon());
                instrPoints.add(m.getMap().getMapPosition(c));
            }
            instructionPoints.add(instrPoints);
        }

        for(List<Point> hp : instructionPoints) {
            double min = Integer.MAX_VALUE;
            for (Point instr : hp) {
                for (Point p : targetPoints) {
                    double dis = Point.distanceSq(instr.getX(), instr.getY(), p.getX(), p.getY());
                    if(dis<min) min = dis;
                }
            }
            minimumTotal+=min;
        }
        return minimumTotal;
    }

    private static double distanceFromTarget4(Instruction instruction, Link target){
        double sum = 0;
        GUI.Map m = GUI.Map.getInstance();
        PointList points = instruction.getPoints();
        List<Coordinate> coords = target.getPolyline().getCoordinates();

        List<Point> targetPoints = new ArrayList<>();
        List<Point> instructionPoints = new ArrayList<>();
        for(Coordinate c : coords){
            targetPoints.add(m.getMap().getMapPosition(c));
        }
        for(int i = 0; i < points.size(); i++){
            Coordinate c = new Coordinate(points.getLat(i), points.getLon(i));
            instructionPoints.add(m.getMap().getMapPosition(c));
        }

        for (Point instr : instructionPoints) {
            double dis = Integer.MAX_VALUE;
            for(Point p : targetPoints){
                double dc = Point.distanceSq(instr.getX(), instr.getY(), p.getX(), p.getY());
                if(dc<dis) dis=dc;
            }
            sum+=dis;
        }
        return sum;
    }

    private static List<Link> ghToLink(Link start, Node end, InstructionList iList){
        List<Link> route = new ArrayList<>();
        List<Coordinate> coords = new ArrayList<>();

        HashMap<Integer, Link> linkMap = GUI.Map.getInstance().getGrid().getLinkMap();
        Link chosen = start;
        for(int i = 1; i <iList.size(); i++){
            Instruction instr = iList.get(i);
//            List<Link> targets = MapUtil.getTargetLinks(linkMap, chosen, route);
//
//            //Optional<Link> newLink = targets.stream().reduce((t1,t2)->distanceFromTarget(firstInstr, t1) < distanceFromTarget(firstInstr, t2) ? t1 : t2);
//            double avgDis = Integer.MAX_VALUE;
//            Link newLink = null;
//            for(Link t : targets){
//                double dis = distanceFromTarget4(instr, t);
//                if(dis<avgDis) {
//                    avgDis = dis;
//                    newLink = t;
//                }
//            }
//
//            if(newLink==null){
//                continue;
//            }
//            chosen = newLink;
//            route.add(newLink);
            List<Link> updatedRoute = recursiveSnap(iList, linkMap, chosen, new ArrayList<>(), i);
            if(updatedRoute.size()>0) {
                chosen = updatedRoute.get(updatedRoute.size() - 1);
                route.addAll(route.size(), updatedRoute);
            }
        }
        return route;
    }

    public static List<Link> recursiveSnap(InstructionList instr, HashMap<Integer, Link> linkMap, Link chosen, List<Link> route, int pos){
        List<Server> servers = chosen.getServers();
        if (servers.size()==0)
            return route;
        boolean switchInstr = pos!=instr.size()-1 ? switchInsruction(chosen, instr.get(pos), instr.get(pos+1)) : false;
        if(switchInstr)
            return route;

        List<Link> targets = new ArrayList<>();
        for(Server s: servers){
            targets.add(s.getOutgoing());
        }

        double avgDis = Integer.MAX_VALUE;
        Link newLink = null;
        for(Link t : targets){
            double dis = distanceFromTarget4(instr.get(pos), t);
            if(dis<avgDis) {
                avgDis = dis;
                newLink = t;
            }
        }
        chosen = newLink;

        route.add(newLink);
        return recursiveSnap(instr, linkMap, chosen, route, pos);
    }

    public static boolean switchInsruction(Link chosen, Instruction current, Instruction next){
        GUI.Map m = GUI.Map.getInstance();

        PointList currentPoints = current.getPoints();
        GHPoint3D lastCurrent = currentPoints.toGHPoint(currentPoints.size()-1);

        PointList nextPoints = next.getPoints();
        GHPoint3D firstNext = nextPoints.toGHPoint(0);

        Point lC = m.getMap().getMapPosition(lastCurrent.getLat(), lastCurrent.getLon());
        Point fN = m.getMap().getMapPosition(firstNext.getLat(), firstNext.getLon());

        Point tar = m.getMap().getMapPosition(chosen.getTarget().getLatitude(), chosen.getTarget().getLongitude());

        double disCurrent = Point.distanceSq(tar.getX(), tar.getY(), lC.getX(), lC.getY());
        double disNext = Point.distanceSq(tar.getX(), tar.getY(), fN.getX(), fN.getY());

        if(disNext < disCurrent)
            return true;

        return false;
    }


    public synchronized static void drawInstructions(InstructionList iList){
        List<Coordinate> coords = new ArrayList<>();
        List<MapMarker> markers = new ArrayList<>();
        for(Instruction instr : iList){
            PointList pl = instr .getPoints();
            for (int i = 0; i < pl.size(); i++) {
                MapMarkerDot md = new MapMarkerDot(pl.getLat(i), pl.getLon(i));
                md.setBackColor(Color.cyan);
                markers.add(md);
            }
        }
        GUI.Map.getInstance().getMap().setMapMarkerList(markers);
    }


    public synchronized static void drawRoute(List<Link> route){
        List<MapMarker> markers = new ArrayList<>();
        for(int i = 0; i < route.size(); i++){
            if(i==route.size()-1){
                MapMarkerDot tar = new MapMarkerDot(route.get(i).getTarget().getLatitude(),route.get(i).getTarget().getLongitude());
                tar.setBackColor(Color.red);
                markers.add(tar);
                continue;
            }
            MapMarkerDot src = new MapMarkerDot(route.get(i).getSource().getLatitude(), route.get(i).getSource().getLongitude());
            src.setBackColor(Color.red);
            markers.add(src);
        }
        //markers.addAll(route.size(), GUI.Map.getInstance().getMap().getMapMarkerList());
        GUI.Map.getInstance().getMap().setMapMarkerList(markers);
    }

    public static void main(String[] args){
        GH.request();
    }

}
