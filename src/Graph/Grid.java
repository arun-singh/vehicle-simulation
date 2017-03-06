package Graph;

import Database.Query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Arun on 22/01/2017.
 */
public class Grid {

    private LinkedHashMap<Integer, Link> linkMap;
    Random ran = new Random();
    private List<Node[]> pairs;
    private List<Double> linkLengths;

    int maxCarLength = 6;
    int minLanes = 1;
    int maxLane = 2;

    public Grid(double maxLat, double minLat, double maxLon, double minLon){
        linkMap = new LinkedHashMap<>();

        ResultSet rs = Query.getLinkFromBox(maxLat, minLat, maxLon, minLon);
        generateNodePairs(rs);
        generateLinks(pairs, linkLengths);
        Link.createServers(linkMap);
    }

    private void generateNodePairs(ResultSet rs){
        pairs = new ArrayList<>();
        linkLengths = new ArrayList<>();
        try {
            while(rs.next()){
                Node one = new Node(rs.getDouble("Source lat"), rs.getDouble("Source long"));
                Node two = new Node(rs.getDouble("Target lat"), rs.getDouble("Target long"));
                double length = rs.getDouble("length");
                pairs.add(new Node[]{one, two});
                linkLengths.add(length);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void generateLinks(List<Node[]> nodePairs, List<Double> lengths) {
        for(int i = 0; i<nodePairs.size(); i++) {
            for(int j = 0 ; j < 2 ; j++){
                double length = lengths.get(i);
                int capacity = (int)(Math.floor(length/maxCarLength));
                int id = (i*2)+j;

                Node[] pair = nodePairs.get(i);
                Node start = pair[j==0?0:1];
                Node end = pair[j==0?1:0];

                Link link = new Link(id, capacity, start, end);
                link.setLength(length);
                int maxLookBack = 10;
                int minLookBack = 1;

                int lookBack = ran.nextInt(maxLookBack - minLookBack + 1) + minLookBack;
                link.setLookBackLimit(lookBack);
                int lanes =  ran.nextInt(maxLane - minLanes + 1) + minLanes;
                link.setLanes(lanes);

                link.setkMin(0);
                link.setkMax(5);
                link.setvMin(1);
                link.setvFree(5);

                linkMap.put(id, link);
            }
        }
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
//
//    public static boolean inBox(){
//
//    }

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
        List<double[]> latlon = new ArrayList<>();
        latlon.add(new double[]{52.4432354, -1.9366254});
        latlon.add(new double[]{52.437009, -1.9293171});
        latlon.add(new double[]{52.4469844, -1.9277054});
        latlon.add(new double[]{52.4407134, -1.9255364});

        double maxLat = Grid.maxLat(latlon);
        double minLat = Grid.minLat(latlon);
        double minLon = Grid.minLon(latlon);
        double maxLon = Grid.maxLon(latlon);

        System.out.println("Max lat: " +maxLat);
        System.out.println("Min lat: " +minLat);
        System.out.println("Max lon: " +maxLon);
        System.out.println("Min lon: " +minLon);
        Grid grid = new Grid(52.4469844, 52.437009, -1.9366254, -1.9255364);
    }
}
