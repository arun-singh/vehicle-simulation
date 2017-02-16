package Graph;

import GUI.LinkController;
import GUI.Map;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Arun on 22/01/2017.
 */
public class Grid {

    LinkController controller = LinkController.getInstance();
    private LinkedHashMap<Integer, Link> linkMap;
    Map map = Map.getInstance();

    List<Node> testNodes = new ArrayList<Node>() {{
        add(new Node(51.29766, -0.84528));
        add(new Node(51.30153, -0.80835));
    }};

    //52.457792, -1.964535, 52.457662, -1.965453

    public Grid(/* Map section */){
        create(2);
    }

    // TODO: Create links from nodes using map section
    private void create(int linkCount /* In future would have a section of map to create links/nodes from */){
        linkMap = new LinkedHashMap<Integer, Link>(linkCount);

        for(int i = 0; i < linkCount; i++){
            Link link = new Link(i, testNodes.get(0), testNodes.get(1));
            link.setLanes(2);
            link.setLength(50);
            link.setLookBackLimit(2);

            link.setkMin(0);
            link.setkMax(5);
            link.setvMin(3);
            link.setvFree(7);

            Queue queue = new Queue(8, link, controller);
            link.setQueue(queue);

            map.getMap().addMapPolygon(link.getPolyline());
            linkMap.put(i, link);
        }

        // Assign server
        QueueServer server = new QueueServer(linkMap.get(0), linkMap.get(1), QueueServer.Type.NORMAL);
        linkMap.get(0).getServers().add(server);

    }

    public LinkedHashMap<Integer, Link> getLinkMap(){ return linkMap; }

}
