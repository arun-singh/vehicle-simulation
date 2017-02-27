package Graph;

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
}
