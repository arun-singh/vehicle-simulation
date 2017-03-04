package Graph;

import Database.Query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Arun on 22/01/2017.
 */
public class Grid {

    private LinkedHashMap<Integer, Link> linkMap;
    private int totalNodes = 8;
    private List<Integer[]> nodePairs;
    private Node[] nodes;

    public Grid(/* Map section */){
        linkMap = new LinkedHashMap<>();
        generateNodes();
        generateNodePairs();
    }

    private static List<Node[]> generateNodePairs(ResultSet rs){
        List<Node[]> nodePairs = new ArrayList<>();
        try {
            while(rs.next()){
                Node one = new Node(rs.getDouble("Source lat"), rs.getDouble("Source long"));
                Node two = new Node(rs.getDouble("Target lat"), rs.getDouble("Target long"));
                nodePairs.add(new Node[]{one, two});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nodePairs;
    }

    private static double maxLat(List<double[]> latLon){
        return latLon.stream()
                .mapToDouble(d->d[0])
                .reduce(0,(l1, l2)->l2>l1?l2:l1);
    }

    private static double minLat(List<double[]> latLon){
        return latLon.stream()
                .mapToDouble(d->d[0])
                .reduce(Double.MAX_VALUE, (l1, l2)->l2<l1?l2:l1);
    }


    private static double maxLon(List<double[]> latLon){
        return latLon.stream()
                .mapToDouble(d->d[1])
                .reduce((l1, l2)->Double.min(l1, l2))
                .getAsDouble();
    }

    private static double minLon(List<double[]> latLon){
        return latLon.stream()
                .mapToDouble(d->d[1])
                .reduce((l1, l2)->Double.max(l1, l2))
                .getAsDouble();
    }

    private void generateNodes(){
        nodes = new Node[totalNodes];
        for(int i = 0; i<totalNodes;i++){
            nodes[i] = new Node(i+1);
        }
    }

    private List<Integer[]> generateNodePairs(){
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
    public List<Integer[]> getNodePairs(){return nodePairs;}
    public Node[] getNodes(){return nodes;}

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

        List<Node[]> pairs = Grid.generateNodePairs(Query.getLinkFromBox(52.4469844, 52.437009, -1.9366254, -1.9255364));
    }
}
