package Graph;

import java.util.Objects;

/**
 * Created by Arun on 17/01/2017.
 */
public class Node{

    private double latitude;
    private double longitude;
    private int id;
    public Node(int id){
        this.id = id;
    }

    public Node(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Double.compare(node.latitude, latitude) == 0 &&
                Double.compare(node.longitude, longitude) == 0 &&
                    Integer.compare(node.id, id) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}