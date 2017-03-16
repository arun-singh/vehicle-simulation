package GUI;

import Graph.Link;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Arun on 29/01/2017.
 */
public class LinkPolyline extends MapPolygonImpl{

    private Link link;
    int roadBoundaryIndex;
    private List<Coordinate> coordinates;
    private Path2D linkPath;
    private Path2D runningPath;
    private Path2D queuePath;
    private List<Point> linkPoints;
    Color[] colours = new Color[]{Color.black, Color.WHITE, Color.RED, Color.blue};

    public LinkPolyline(Link link, List<Coordinate> points){
        super(null, null, points);
        this.link = link;
        setColor(Color.BLACK);
        setStroke(new BasicStroke(20));
        setVisible(true);
        setCoordinates(points);
    }

    @Override
    public void paint(Graphics g, List<Point> wayPoints) {
//        if (!SwingUtilities.isEventDispatchThread()) {
//            throw new RuntimeException("Repaint attempt is not on event dispatch thread");
//        }
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setStroke(new BasicStroke(8));

//        float alpha = 0.8f;
//        AlphaComposite alcom = AlphaComposite.getInstance(
//        AlphaComposite.SRC_OVER, alpha);
//        g2d.setComposite(alcom);
//        g2d.setColor(Color.GREEN);
//        g2d.draw(runningPath);
//        g2d.setColor(Color.red);
//        g2d.draw(queuePath);

        double prop = link.getQueue().getRunningProportion();
        Point.Double sep = calculateRoadBoundary(wayPoints, prop);

        Path2D path1 = updateRunning(wayPoints, sep);
        g2d.setColor(Color.GREEN);
        g2d.draw(path1);

        Path2D path2 = updateQueue(wayPoints, sep);
        g2d.setColor(Color.RED);
        g2d.draw(path2);

        g2d.dispose();
    }

    private Path2D buildPath1(List<Point> wayPoints) {
        Path2D path = new Path2D.Double();
        if (wayPoints != null && wayPoints.size() > 0) {
            Point firstPoint = wayPoints.get(0);
            path.moveTo(firstPoint.getX(), firstPoint.getY());
            for (int i = 0; i <= 2; i++) {
                path.lineTo(wayPoints.get(i).getX(), wayPoints.get(i).getY());
            }
        }
        return path;
    }

    private Path2D buildPath2(List<Point> wayPoints) {
        Path2D path = new Path2D.Double();
        if (wayPoints != null && wayPoints.size() > 0) {
            Point firstPoint = wayPoints.get(wayPoints.size()/2);
            path.moveTo(firstPoint.getX(), firstPoint.getY());
            for (int i = 2; i < wayPoints.size(); i++) {
                path.lineTo(wayPoints.get(i).getX(), wayPoints.get(i).getY());
            }
        }
        return path;
    }

    public Point.Double calculateRoadBoundary(List<Point> linkPoints, double runningProportion){
        Point firstPoint = linkPoints.get(0);
        double distance = firstPoint.distance(linkPoints.get(linkPoints.size()-1));
        double runningDistance = distance * runningProportion;

        for(int i = 1 ; i <= linkPoints.size()-1; i++){
            Point.Double currentPoint = new Point.Double(linkPoints.get(i).getX(), linkPoints.get(i).getY());;
            double currD = firstPoint.distance(currentPoint);
            if(currD == runningDistance) {
                roadBoundaryIndex = i;
                return new Point.Double(currentPoint.getX(), currentPoint.getY());
            }
            if(currD > runningDistance){
                roadBoundaryIndex = i;
                double diff = currD-runningDistance;
                Point.Double previousPoint =  new Point.Double(linkPoints.get(i-1).getX(), linkPoints.get(i-1).getY());
                double sectionDiff = previousPoint.distance(currentPoint);
                double dx = currentPoint.getX() - previousPoint.getX();
                double dy = currentPoint.getY() - previousPoint.getY();
                dx /= sectionDiff;
                dy /= sectionDiff;
                double px = currentPoint.getX() - (dx * diff);
                double py = currentPoint.getY() - (dy * diff);
                return new Point.Double(px, py);
            }
        }
        return null;
    }

    public Path2D updateRunning(List<Point> linkPoints, Point.Double seperator){
        Path2D runningPath = new Path2D.Double();

        Point.Double firstPoint = new Point.Double(linkPoints.get(0).getX(), linkPoints.get(0).getY());
        runningPath.moveTo(firstPoint.getX(), firstPoint.getY());

        for(int i = 0; i < roadBoundaryIndex; i++){
            if(i+1 == roadBoundaryIndex)
                runningPath.lineTo(seperator.getX(), seperator.getY());
            runningPath.lineTo(linkPoints.get(i).getX(), linkPoints.get(i).getY());
        }

        return runningPath;
    }

    public Path2D updateQueue(List<Point> linkPoints, Point.Double seperator){
        Path2D queueingPath = new Path2D.Double();

        Point.Double sep = new Point2D.Double(seperator.getX(), seperator.getY());
        queueingPath.moveTo(sep.getX(), sep.getY());
        for(int i = roadBoundaryIndex; i < linkPoints.size(); i++){
            queueingPath.lineTo(linkPoints.get(i).getX(), linkPoints.get(i).getY());
        }
        return queueingPath;
    }

    public List<Coordinate> getCoordinates() { return coordinates; }
    public Path2D getLinkPath() {
        return linkPath;
    }
    public void setLinkPath(Path2D path) {
        this.linkPath = path;
    }
    public List<Point> getLinkPoints() { return linkPoints; }
    public void setLinkPoints(List<Point> points) { this.linkPoints = points;}
    public void setCoordinates(List<Coordinate> coordinates) {this.coordinates = coordinates;}
    public Link getLink(){return link;}
    public Path2D getRunningPath() {
        return runningPath;
    }
    public void setRunningPath(Path2D runningPath) {
        this.runningPath = runningPath;
    }
    public Path2D getQueuePath() {
        return queuePath;
    }
    public void setQueuePath(Path2D queuePath) {
        this.queuePath = queuePath;
    }
}
