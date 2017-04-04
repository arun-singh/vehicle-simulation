package Graph;

import Database.Query_V3;
import GUI.Map;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * Created by Arun on 22/01/2017.
 */
public class Grid {

    private LinkedHashMap<Integer, Link> linkMap;
    static Random ran = new Random();
    private List<Node[]> pairs;
    private List<Double> linkLengths;
    private List<Boolean> oneway;
    private List<List<Coordinate>> linestring;
    private int averageConnectivity;

    int maxCarLength = 4;
    int minLanes = 1;
    int maxLane = 2;

    public Grid(double maxLat, double minLat, double maxLon, double minLon){
        linkMap = new LinkedHashMap<>();
        Map.getInstance().setGrid(this);
        System.out.println("Querying database");
        ResultSet rs = Query_V3.getLinkFromBox(maxLat, minLat, maxLon, minLon, Query_V3.carFilter);
        List<Node[]> carsFilter = generateNodePairs(rs);
        System.out.println("Generating links");
        generateLinks(carsFilter, linkLengths, oneway, linestring);
        System.out.println(" Creating servers");
        Link.createServers(linkMap);
        System.out.println("Finished server creation");
       // Map.getInstance().drawMapMarkers(carsFilter);

        // Number of connections
//        List<Node[]> unconnected = new ArrayList<>();
//        for(java.util.Map.Entry<Integer, Link> entry : linkMap.entrySet()){
//            int connections = MapUtil.connections(entry.getValue());
//            if(connections==0)
//                unconnected.add(new Node[]{entry.getValue().getSource(), entry.getValue().getTarget()});
//            entry.getValue().setConnectivity(connections);
//            MapUtil.cache.clear();
//        }
//
//        int totalConnections = linkMap.entrySet().stream().mapToInt(l->l.getValue().getConnectivity()).sum();
//        int averageconnections = totalConnections/linkMap.size();
//        setAverageConnectivity(averageconnections);

//        //One way
//        int oneWayCount = 0;
//        for(java.util.Map.Entry<Integer, Link> entry : linkMap.entrySet()) {
//            boolean oneWay = MapUtil.isLinkOneWay(entry.getValue(), linkMap);
//            if (oneWay) oneWayCount++;
//        }
//
//        int totalCap = linkMap.entrySet().stream()
//                .mapToInt(l->l.getValue().getQueue().getCapacity())
//                .sum();

    }

    private List<Node[]> generateNodePairs(ResultSet rs){
        List<Node[]> pairs = new ArrayList<>();
        linkLengths = new ArrayList<>();
        oneway = new ArrayList<>();
        linestring = new ArrayList<>();

        try {
            while(rs.next()){
                Node one = new Node(rs.getDouble("Source lat"), rs.getDouble("Source long"));
                Node two = new Node(rs.getDouble("Target lat"), rs.getDouble("Target long"));
                int ow = rs.getInt("oneway");
                double length = rs.getDouble("length");
                String line = rs.getString("line");

                // Parse line string
                List<Coordinate> coords = new ArrayList<>();
                String[] rem = line.substring(10, line.length()-1).replaceAll("\\(|\\)", "").split(",");
                for(String str : rem){
                    String[] split = str.split(" ");
                    coords.add(new Coordinate(Double.parseDouble(split[1]), Double.parseDouble(split[0])));
                }

                pairs.add(new Node[]{one, two});
                linestring.add(coords);
                oneway.add(ow == 0 ? Boolean.TRUE : Boolean.FALSE);
                linkLengths.add(length);
            }
            return pairs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<Link> oneRouteDemo(HashMap<Integer, Link> linkMap){
        int[] ids = new int[]{1170, 1081, 1210, 911};//911, 845, 548, 684, 428, 1143, 344, 377, 678, 398, 856, 754, 1027, 752, 405, 193, 477, 832, 265, 29};
        //int[] ids = new int[]{218, 388, 230};

        List<Link> route = new ArrayList<>();
        for(Integer id : ids){
            route.add(MapUtil.getLinkById(linkMap, id));
        }
        return route;
    }

    private void generateLinks(List<Node[]> nodePairs, List<Double> lengths, List<Boolean> oneway, List<List<Coordinate>> linestring) {
        int seen = 0;
        List<MapPolygon> polys = new ArrayList<>();
        for(int i = 0; i<nodePairs.size(); i++) {
            boolean ow = oneway.get(i);
            int rev = ow ? 1 : 2;
            for(int j = 0 ; j < rev ; j++){
                int id = seen;
                seen++;

                double length = lengths.get(i);
                int capacity = (int)(Math.ceil(length/maxCarLength));
                if(capacity==0) capacity=1;

                Node[] pair = nodePairs.get(i);
                Node start = pair[j==0?0:1];
                Node end = pair[j==0?1:0];

                Link link = new Link(id, capacity, start, end);
                link.setLength(length);
                double lookBack = capacity==1 ? 1 : Math.ceil(((double)capacity)/4);
                link.setLookBackLimit(lookBack == 0 ? 1 : (int)lookBack);

                //b
                int lanes = 1;//ran.nextInt(maxLane - minLanes + 1) + minLanes;
                link.setLanes(lanes);

                List<Coordinate> coords = linestring.get(i);
                if(j==1){
                    List<Coordinate> reversed = new ArrayList<>();
                    for(int k = coords.size()-1; k>=0; k--){
                        reversed.add(new Coordinate(coords.get(k).getLat(), coords.get(k).getLon()));
                    }
                    link.setPolyline(reversed);
                }else{
                    link.setPolyline(coords);
                }

                polys.add(link.getPolyline());
                //Map.getInstance().getMap().addMapPolygon(link.getPolyline());

                link.setkMin(0);
                link.setkMax(((double)capacity)/length);
                link.setvMin(1);
                link.setvFree(12);
                linkMap.put(id, link);
            }
        }
        Map.getInstance().getMap().setMapPolygonList(polys);
        //Map.getInstance().getMap().paintImmediately(0, 0, Map.getInstance().getMap().getWidth(), Map.getInstance().getMap().getHeight());
    }

    public void generateOneWayLinks(List<Node[]> nodePairs, List<Double> lengths, List<Boolean> oneway, List<List<Coordinate>> linestring) {
        int seen = 0;
        List<MapPolygon> polys = new ArrayList<>();
        for(int i = 0; i<nodePairs.size(); i++) {
            boolean ow = oneway.get(i);
            int rev = 1;
            for(int j = 0 ; j < rev ; j++){
                int id = seen;
                seen++;

                double length = lengths.get(i);
                int capacity = (int)(Math.ceil(length/maxCarLength));
                if(capacity==0) capacity=1;

                Node[] pair = nodePairs.get(i);
                Node start = pair[j==0?0:1];
                Node end = pair[j==0?1:0];

                Link link = new Link(id, capacity, start, end);
                link.setLength(length);
                double lookBack = capacity==1 ? 1 : Math.ceil(((double)capacity)/4);
                link.setLookBackLimit(lookBack == 0 ? 1 : (int)lookBack);

                //b
                int lanes = 1;//ran.nextInt(maxLane - minLanes + 1) + minLanes;
                link.setLanes(lanes);

                List<Coordinate> coords = linestring.get(i);
                if(j==1){
                    List<Coordinate> reversed = new ArrayList<>();
                    for(int k = coords.size()-1; k>=0; k--){
                        reversed.add(new Coordinate(coords.get(k).getLat(), coords.get(k).getLon()));
                    }
                    link.setPolyline(reversed);
                }else{
                    link.setPolyline(coords);
                }

                polys.add(link.getPolyline());
                //Map.getInstance().getMap().addMapPolygon(link.getPolyline());

                link.setkMin(0);
                link.setkMax(((double)capacity)/length);
                link.setvMin(1);
                link.setvFree(12);
                linkMap.put(id, link);
            }
        }
        Map.getInstance().getMap().setMapPolygonList(polys);
    }


    public List<List<Link>> generateRoutes2(List<Link> inputLinks){
        return GH.generateRoutes(linkMap, 1);
    }


    public static List<Link> generateRoute(List<Link> inputLinks){
        List<Link> route = new ArrayList<>();
        int randomStartingLink = ran.nextInt((inputLinks.size()-1) + 1);
        Link next = inputLinks.get(randomStartingLink);
        route.add(next);
        int serverSize = next.getServers().size();

        int serverIndex = -1;
        int count = 0;
        while(serverSize!=0){
            try {
                serverIndex = serverSize == 1 ? 0 : ThreadLocalRandom.current().nextInt(0, (serverSize - 1) + 1); //ran.nextInt((serverSize - 1) + 1);
            }catch (IllegalArgumentException e){
                System.out.println("Caught");
                System.out.println("Server in; " + serverSize);
            }
            try{
                next = next.getServers().get(serverIndex).getOutgoing();

            }catch (NullPointerException e){
                e.printStackTrace();
            }
            if(route.contains(next))
                break;
            route.add(next);
            serverSize = next.getServers().size();
            count++;
            if(count==20) break;
        }
        return route;
    }

    public static double maxLat(List<double[]> latLon){
        return latLon.stream()
                .mapToDouble(d->d[0])
                .reduce(0,(l1, l2)->l2>l1?l2:l1);
    }

    public static double minLat(List<double[]> latLon){
        return latLon.stream()
                .mapToDouble(d->d[0])
                .reduce(Double.MAX_VALUE, (l1, l2)->l2<l1?l2:l1);
    }


    public static double maxLon(List<double[]> latLon){
        return latLon.stream()
                .mapToDouble(d->d[1])
                .reduce((l1, l2)->Double.min(l1, l2))
                .getAsDouble();
    }

    public static double minLon(List<double[]> latLon){
        return latLon.stream()
                .mapToDouble(d->d[1])
                .reduce((l1, l2)->Double.max(l1, l2))
                .getAsDouble();
    }

    private void generateArtificialNodes(Node[] nodes, int totalNodes){
        nodes = new Node[totalNodes];
        for(int i = 0; i<totalNodes;i++){
            nodes[i] = new Node(i+1);
        }
    }

    private List<Integer[]> generateArtificialNodePairs(List<Integer[]> nodePairs){
        nodePairs = new ArrayList<>();
        nodePairs.add(new Integer[]{1, 2});
        nodePairs.add(new Integer[]{2, 3});
        nodePairs.add(new Integer[]{2, 4});
        nodePairs.add(new Integer[]{2, 6});
        nodePairs.add(new Integer[]{6, 5});
        nodePairs.add(new Integer[]{6, 7});
        nodePairs.add(new Integer[]{6, 8});

        return nodePairs;
    }

    public LinkedHashMap<Integer, Link> getLinkMap(){ return linkMap; }
    public List<Node[]> getPairs(){return pairs;}
    public List<Double> getLinkLengths(){return linkLengths;}

    public static void main(String[] args){
//        List<double[]> latlon = new ArrayList<>();
//        latlon.add(new double[]{52.4432354, -1.9366254});
//        latlon.add(new double[]{52.437009, -1.9293171});
//        latlon.add(new double[]{52.4469844, -1.9277054});
//        latlon.add(new double[]{52.4407134, -1.9255364});
//
//        double maxLat = Grid.maxLat(latlon);
//        double minLat = Grid.minLat(latlon);
//        double minLon = Grid.minLon(latlon);
//        double maxLon = Grid.maxLon(latlon);
//
//        System.out.println("Max lat: " +maxLat);
//        System.out.println("Min lat: " +minLat);
//        System.out.println("Max lon: " +maxLon);
//        System.out.println("Min lon: " +minLon);
//        Grid grid = new Grid(52.4469844, 52.437009, -1.9366254, -1.9255364);

        String toMatch = "LINESTRING(-1.9366622 52.443261,-1.9365741 52.4433323,-1.9359548 52.4438552)";
        //Pattern patt = Pattern.compile("LINESTRING\\(((\\d\\.\\d+\\s\\d+\\.\\d+),)+");
        String[] rem = toMatch.substring(10, toMatch.length()-1).replaceAll("\\(|\\)", "").split(",");
        for(String str : rem){
            System.out.println(str.split(" ")[1] + ", "  + str.split(" ")[0]);
        }
        Pattern patt = Pattern.compile("LINESTRING\\(" +
                "((-?\\d+\\.\\d+\\s-?\\d+\\.\\d+),?+)" +
                "\\)");

    }

    public int getAverageConnectivity() {
        return averageConnectivity;
    }

    public void setAverageConnectivity(int averageConnectivity) {
        this.averageConnectivity = averageConnectivity;
    }
}
