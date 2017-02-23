import Graph.Link;
import Graph.Node;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Arun on 10/02/2017.
 */
public class TestGrid {

    @Test
    public void testSetup(){
        int totalNodes = 8;
        int roadLengthMax = 500;
        int roadLengthMin = 50;
        int minLookBack = 1;
        int maxLookBack = 10;
        int minLanes = 1;
        int maxLane = 2;
        int minCap = 20;
        int maxCap = 100;

        Random ran = new Random();
        HashMap<Integer, Link> linkMap = new HashMap<>();
        Node[] nodes = new Node[totalNodes];
        for(int i = 1; i<=totalNodes;i++){
            nodes[i] = new Node(i);
        }
        List<Integer[]> nodePairs = new ArrayList<>();
        nodePairs.add(new Integer[]{1, 2});
        nodePairs.add(new Integer[]{2, 3});
        nodePairs.add(new Integer[]{2, 4});
        nodePairs.add(new Integer[]{2, 6});
        nodePairs.add(new Integer[]{6, 5});
        nodePairs.add(new Integer[]{6, 7});
        nodePairs.add(new Integer[]{6, 8});

        for(int i = 0; i<nodePairs.size(); i++) {
            for(int j = 0 ; j < 2 ; j++){
                int capacity = ran.nextInt(maxCap - minCap + 1) + minCap;
                Node start = nodes[nodePairs.get(i)[j==0?0:1]-1];
                Node end = nodes[nodePairs.get(i)[j==0?1:0]-1];

                Link link = new Link(i, capacity, start, end);
                int length = ran.nextInt(roadLengthMax - roadLengthMin + 1) + roadLengthMin;
                link.setLength(length);
                int lookBack = ran.nextInt(maxLookBack - minLookBack + 1) + minLookBack;
                link.setLookBackLimit(lookBack);
                int lanes = ran.nextInt(maxLane - minLanes + 1) + minLanes;
                link.setLanes(lanes);

                link.setkMin(0);
                link.setkMax(5);
                link.setvMin(3);
                link.setvFree(7);

                linkMap.put((i*2)+j, link);
            }
        }

        Link.getAdjacent(linkMap);


    }
}
