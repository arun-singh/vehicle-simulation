package Database;

import java.sql.*;

/**
 * Created by Arun on 08/03/2017.
 */
public class Query_V3 {
    private static Query_V3 query = new Query_V3();
    public static String noFilter, waysFilter, carFilter, removed;
    private static Connection conn;
    public Query_V3(){
        noFilter = "SELECT e.length as \"length\", e.car as \"car\", n1.lat as \"Source lat\", n1.lon as \"Source long\", n.lat as \"Target lat\", n.lon as \"Target long\" " +
                "from edges e, nodes n, nodes n1 " +
                "where e.target = n.original_id AND source = n1.original_id " +
                "AND (((n.lat BETWEEN ? AND ?) AND (n.lon BETWEEN ? AND ?)) OR ((n1.lat BETWEEN ? AND ?) AND (n1.lon BETWEEN ? AND ?)))";
        waysFilter = noFilter + "AND (e.source IN (select nodes.original_id FROM nodes, ways WHERE (nodes.original_id = ANY(ways.nodes)))" +
                " AND e.target IN (select nodes.original_id FROM nodes, ways WHERE (nodes.original_id = ANY(ways.nodes))))";
        carFilter = waysFilter + " AND e.car <> 0";
        connect();
    }

    private void connect(){
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String url = "jdbc:postgresql://localhost/osm_new";
        String user = "Arun";
        String pass = "";
        try {
            conn = DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static ResultSet getRemoved(double maxLat, double minLat, double maxLon, double minLon){
        PreparedStatement stmt;
        ResultSet rs = null;
        String str = noFilter + " EXCEPT (SELECT e2.length as \"length\", e2.car as \"car\", n1.lat as \"Source lat\", n1.lon as \"Source long\", n.lat as \"Target lat\", n.lon as \"Target long\"" +
                " from edges e2, nodes n, nodes n1 WHERE e2.target = n.original_id AND e2.source = n1.original_id " +
                "AND ((n.lat <= ? AND n.lat >= ?) AND (n.lon >= ? AND n.lon <= ?))" +
                "AND (e2.source IN (select nodes.original_id FROM nodes, ways WHERE (nodes.original_id = ANY(ways.nodes)))" +
                " AND e2.target IN (select nodes.original_id FROM nodes, ways WHERE (nodes.original_id = ANY(ways.nodes)))))";
        try {
            stmt = conn.prepareStatement(str);
            stmt.setDouble(1, maxLat);
            stmt.setDouble(2, minLat);
            stmt.setDouble(3, maxLon);
            stmt.setDouble(4, minLon);

            stmt.setDouble(5, maxLat);
            stmt.setDouble(6, minLat);
            stmt.setDouble(7, maxLon);
            stmt.setDouble(8, minLon);

            rs = stmt.executeQuery();
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }
    public static ResultSet getLinkFromBox(double maxLat, double minLat, double maxLon, double minLon, String query){
        PreparedStatement stmt;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setDouble(1, minLat);
            stmt.setDouble(2, maxLat);
            stmt.setDouble(3, maxLon);
            stmt.setDouble(4, minLon);
            stmt.setDouble(5, minLat);
            stmt.setDouble(6, maxLat);
            stmt.setDouble(7, maxLon);
            stmt.setDouble(8, minLon);

            rs = stmt.executeQuery();
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    public static Query_V3 getQuery() {
        return query;
    }
}
