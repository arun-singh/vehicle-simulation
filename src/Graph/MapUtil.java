package Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public static boolean isUnconnected(List<Node[]> pairs, Node[] pair){
        return getSourceLinks(pairs, pair[0]).size() == 0
                && getTargetLinks(pairs, pair[1]).size() == 0;
    }

    public static List<Node[]> getUnconnected(List<Node[]> pairs){
        return pairs.stream()
                .filter(p->isUnconnected(pairs, p))
                .collect(Collectors.toList());
    }

    public static void filter(List<Node[]> _new, List<Node[]> _removed){
       for(Node[] rem : _removed){
           Node remSource = rem[0];
           // Any target nodes remaining that that had this node as source?
           List<Node[]> target = getTargetLinks(_new, remSource);
           System.out.println(target.size());
           Node targetRemoved = rem[0];
       }
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


}
