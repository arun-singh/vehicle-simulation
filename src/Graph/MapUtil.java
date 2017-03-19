package Graph;

import org.openstreetmap.gui.jmapviewer.Coordinate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Arun on 09/03/2017.
 */
public class MapUtil {

    public static List<Node[]> getSourceLinks(List<Node[]> map, Node target){
        return map.stream()
                .filter(l->l[1].equals(target))
                .map(l->l)
                .collect(Collectors.toList());
    }

    public static List<Node[]> getTargetLinks(List<Node[]> map, Node source){
        return map.stream()
                .filter(l->l[0].equals(source))
                .map(l->l)
                .collect(Collectors.toList());
    }

    public static List<Link> getTargetLinks(HashMap<Integer, Link> map, Node source, List<Link> route){
        List<Node> sources;
        Predicate<Link> pred;
        if(route.size()>1){
            sources = route.stream().map(l->l.getSource()).collect(Collectors.toList());
            pred = l -> l.getSource().equals(source) && !sources.contains(l.getTarget());
        }else{
            pred = l -> l.getSource().equals(source);
        }

        return map.entrySet().stream()
                .filter(l->pred.test(l.getValue()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public static boolean isUnconnected(List<Node[]> pairs, Node[] pair){
        return getSourceLinks(pairs, pair[0]).size() == 0
                && getTargetLinks(pairs, pair[1]).size() == 0;
    }

    public static List<Node[]> getUnconnected(List<Node[]> pairs){
        return pairs.stream()
                .filter(p->isUnconnected(pairs, p))
                .collect(Collectors.toList());
    }


    public static List<Integer> cache = new ArrayList<>();
    public static int connections(Link source){
        List<QueueServer> servers = source.getServers();
        if(servers.size()==0)
            return 0;
        List<QueueServer> filtered = new ArrayList<>();
        for(QueueServer qs : servers){
            if(cache.contains(qs.getOutgoing().getId()))
                continue;
            filtered.add(qs);
            cache.add(qs.getOutgoing().getId());
        }
        if(filtered.size()==0)
            return 0;
        return servers.size() + filtered.stream().mapToInt(s->connections(s.getOutgoing())).sum();
    }


    public static boolean isLinkOneWay(Link link, HashMap<Integer, Link> linkMap){
        Node source = link.getSource();
        Node target = link .getTarget();
        return linkMap.entrySet()
                .stream()
                .filter(l->l.getValue().getSource().equals(target) && l.getValue().getTarget().equals(source))
                .map(l->l.getValue())
                .collect(Collectors.toList())
                .size() == 0;
    }

    public static List<Link> getInputLinks(HashMap<Integer, Link> linkMap, int averageConnectivity){
        return linkMap.entrySet().stream()
                .filter(l->l.getValue().getServers().size()>1 && l.getValue().getConnectivity()>averageConnectivity)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public static String maxLat(Coordinate one, Coordinate two, Coordinate three, Coordinate four){
        double largest = one.getLat();
        if(two.getLat()>largest)
            largest = two.getLat();
        if(three.getLat()>largest)
            largest = three.getLat();
        if(four.getLat()>largest)
            largest = four.getLat();
        return Double.toString(round(largest,7));
    }

    public static String minLat(Coordinate one, Coordinate two, Coordinate three, Coordinate four){
        double smallest = one.getLat();
        if(two.getLat()<smallest)
            smallest = two.getLat();
        if(three.getLat()<smallest)
            smallest = three.getLat();
        if(four.getLat()<smallest)
            smallest = four.getLat();
        return Double.toString(round(smallest, 7));
    }

    public static String minLon(Coordinate one, Coordinate two, Coordinate three, Coordinate four){
        double smallest = one.getLon();
        if(two.getLon()<smallest)
            smallest = two.getLon();
        if(three.getLon()<smallest)
            smallest = three.getLon();
        if(four.getLon()<smallest)
            smallest = four.getLon();
        return Double.toString(round(smallest, 7));
    }

    public static String maxLon(Coordinate one, Coordinate two, Coordinate three, Coordinate four){
        double largest = one.getLon();
        if(two.getLon()>largest)
            largest = two.getLon();
        if(three.getLon()>largest)
            largest = three.getLon();
        if(four.getLon()>largest)
            largest = four.getLon();
        return Double.toString(round(largest, 7));
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


    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
