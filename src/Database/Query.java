package Database;

import java.sql.*;

import Graph.Node;

/**
 * Created by Arun on 03/03/2017.
 */
public class Query {
    private static Query query = new Query();
    private static Connection conn;
    public Query(){
        connect();
    }

    public void connect(){
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String url = "jdbc:postgresql://mojo-maps-test.cnci99rpgxpu.eu-west-2.rds.amazonaws.com:5432/maps";
        String user = "inventivecogs";
        String pass = "***REMOVED***";
        try {
            conn = DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static ResultSet getLinkFromBox(double maxLat, double minLat, double maxLon, double minLon){
        PreparedStatement stmt;
        ResultSet rs = null;
        String query = "SELECT e.length as \"length\", n1.latitude as \"Source lat\", n1.longitude as \"Source long\", n.latitude as \"Target lat\", n.longitude as \"Target long\" " +
                "from edges e, nodes n, nodes n1 where e.target = n.id AND source = n1.id " +
                "AND ((n.latitude <= ? AND n.latitude >= ?) AND (n.longitude >= ? AND n.longitude <= ?)) AND e.length >50";
        try {
            stmt = conn.prepareStatement(query);
            stmt.setDouble(1, maxLat);
            stmt.setDouble(2, minLat);
            stmt.setDouble(3, maxLon);
            stmt.setDouble(4, minLon);

            rs = stmt.executeQuery();
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }


    public Node getNode(long id){
        PreparedStatement stmt = null;
        ResultSet rs = null;
        double lon;
        double lat;
        String query = "SELECT longitude, latitude from nodes WHERE id = ?";
        try {
            stmt = conn.prepareStatement(query);
            stmt.setLong(1, id);
            rs = stmt.executeQuery();
            if (rs != null) {
                while(rs.next()){
                    lon = rs.getDouble("longitude");
                    lat = rs.getDouble("latitude");
                    return new Node(lat, lon);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Query getQuery() {
        return query;
    }

    public static void main(String[] args) {
        Query conn = new Query();
    }
}
