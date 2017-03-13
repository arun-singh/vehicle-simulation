package Graph;

/**
 * Created by Arun on 26/01/2017.
 */
public class QueueServer {

    private Node location;
    private Link incoming, outgoing;
    private Type type;
    private int serverDelay = 1; //TODO: Research intersection average delays
    private double pocketDelayedUntil = -1;

    public Node getLocation() {
        return location;
    }

    public enum Type{
        NORMAL,
        MERGE
    }

    public QueueServer(Link incoming, Link outgoing, Type type){
        this.incoming = incoming;
        this.outgoing = outgoing;
        this.location = incoming.getTarget();
        this.type = type;
    }

    public Link getOutgoing() {
        return outgoing;
    }

    public int getDelay() {
        return serverDelay;
    }

    public double getPocketDelayedUntil() {
        return pocketDelayedUntil;
    }

    public void setPocketDelayedUntil(double pocketDelayedUntil) {
        this.pocketDelayedUntil = pocketDelayedUntil;
    }
}
