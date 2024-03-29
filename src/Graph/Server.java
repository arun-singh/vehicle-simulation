package Graph;

/**
 * Created by Arun on 26/01/2017.
 */
public class Server {

    private Node location;
    private Link incoming, outgoing;
    private Type type;
    private int serverDelay = 1; //TODO: Research intersection average delays
    private double pocketDelayedUntil = -1;
    private int entriesDeniedCount = 0;

    public Node getLocation() {
        return location;
    }

    public enum Type{
        NORMAL,
        MERGE
    }

    public Server(Link incoming, Link outgoing, Type type){
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
    public void setEntriesDeniedCount(int entriesDeniedCount) {
        this.entriesDeniedCount = entriesDeniedCount;
    }
    public int getEntriesDeniedCount() {
        return entriesDeniedCount;
    }
}
