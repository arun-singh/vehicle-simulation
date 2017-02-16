package Graph;

/**
 * Created by Arun on 26/01/2017.
 */
public class QueueServer {

    private Node location;
    private Link incoming, outgoing;
    private Type type;

    public Link getOutgoing() {
        return outgoing;
    }

    public enum Type{
        NORMAL,
        MERGE
    }

    public QueueServer(Link incoming, Link outgoing, Type type){
        this.incoming = incoming;
        this.outgoing = outgoing;
        this.location = incoming.getOutput();
        this.type = type;
    }
}
