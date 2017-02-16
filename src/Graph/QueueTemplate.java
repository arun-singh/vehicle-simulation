package Graph;

/**
 * Created by Arun on 09/02/2017.
 */
public interface QueueTemplate {
    public Vehicle pop();
    public void push(Vehicle vehicle);
    public boolean isFree();
    public int runningSectionCars(double _t);
    public double queueLength(double _t);
    public Vehicle getHead();
}
