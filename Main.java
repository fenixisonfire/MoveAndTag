package com.company;

import java.awt.geom.Line2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Main {

    public static ArrayList<Robot> robotArrayList;
    public static ArrayList<Node> robotNodeList;
    public static ArrayList<Obstacle> obstacleArrayList;
    public static ArrayList<Node> verticesNodeList;
    public static ArrayList<Line2D.Double> segmentList;
    public static ArrayList<Robot> targetList;

    public static boolean equal(double x, double y) {
        return (Math.abs(x - y) < 0.000000001);
    }

    public static boolean isShareEndPoint(Line2D line1, Line2D line2) {
        if (equal(line1.getX1(), line2.getX1()) && equal(line1.getY1(), line2.getY1()))
            return true;
        if (equal(line1.getX1(), line2.getX2()) && equal(line1.getY1(), line2.getY2()))
            return true;
        if (equal(line1.getX2(), line2.getX1()) && equal(line1.getY2(), line2.getY1()))
            return true;
        if (equal(line1.getX2(), line2.getX2()) && equal(line1.getY2(), line2.getY2()))
            return true;
        return false;
    }

    public static void antiClockWise(Obstacle o) {
        double x[] = o.x;
        double y[] = o.y;
        double acc = 0;
        for (int i = 0; i < x.length; i++) {
            acc += (x[(i+1) % x.length] - x[i]) * (y[(i+1) % x.length] + y[i]);
        }
        System.out.println(acc);
        if (acc > 0) {
            System.out.println("Clockwise");
            for (int i = 0; i < x.length / 2; i++) {
                double t = 0;
                t = o.x[i];
                o.x[i] = o.x[x.length-1-i];
                o.x[x.length-1-i] = t;
                t = o.y[i];
                o.y[i] = o.y[x.length-1-i];
                o.y[x.length-1-i] = t;
            }
        }
    }

    public static boolean isInside(Node n, Line2D.Double line) {
        Node left = n.left;
        Node right = n.right;
        double x = (equal(line.getX1(), n.x)) ? line.getX2() : line.getX1();
        double y = (equal(line.getY1(), n.y)) ? line.getY2() : line.getY1();
        double vectorLeftX = left.x - n.x;
        double vectorRightX = right.x - n.x;
        double vectorLeftY = left.y - n.y;
        double vectorRightY = right.y - n.y;
        double vectorX = x - n.x;
        double vectorY = y - n.y;
//        System.out.println(vectorLeftX + " " + vectorLeftY);
//        System.out.println(vectorRightX + " " + vectorRightY);
//        System.out.println(vectorX + " " + vectorY);
        double polarLeft = Math.atan2(vectorLeftX, vectorLeftY);
//        System.out.println(polarLeft);
        double polarRight = Math.atan2(vectorRightX, vectorRightY);
//        System.out.println(polarRight);
        double polar = Math.atan2(vectorX, vectorY);
//        System.out.println(polar);
        if (polarRight > polarLeft) {
            if (polarRight < polar || polar < polarLeft) return false;
            else return true;
        } else {
            if (polar < polarRight && polar > polarLeft) return false;
            else return true;
        }
    }

    public static double distance(Node a, Node b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    public static void dijkstra(Node start) {
        ArrayList<Node> allNodeList = new ArrayList<Node>();
        double dist[] = new double[robotNodeList.size() + verticesNodeList.size()];
        Node from[] = new Node[dist.length];

        for (Node r : robotNodeList) {
            allNodeList.add(r);
            r.isVisited = false;
            from[r.name] = r;
            if (r.name != start.name) {
                dist[r.name] = Double.MAX_VALUE;
            } else {
                dist[r.name] = 0;
            }
        }
        for (Node v : verticesNodeList) {
            allNodeList.add(v);
            dist[v.name] = Double.MAX_VALUE;
            v.isVisited = false;
            from[v.name] = v;
        }

        int counter = dist.length;
        while (counter > 0) {
            int nearestNode = -1;
            double min = Double.MAX_VALUE;

            for (int i = 0; i < dist.length; i++) {
                if ((!allNodeList.get(i).isVisited) && (dist[i] < min)) {
                    nearestNode = i;
                    min = dist[i];
                }
            }
            if (nearestNode == -1) {
                break;
            }
            Node target = allNodeList.get(nearestNode);
            target.isVisited = true;
            Path p = new Path(target, start);
            Node current = from[nearestNode];
            while (current.name != start.name) {
                p.path.add(current);
                current = from[current.name];
            }
            p.path.add(start);
            p.finish();
            target.pathList.add(p);
            for (Node n : target.adjacentList) {
                if ((!n.isVisited) && (distance(target, n) + dist[target.name] < dist[n.name])) {
                    dist[n.name] = distance(target, n) + dist[target.name];
                    from[n.name] = target;
                }
            }
            counter--;
        }
        int i = 0;
    }

    public static Path getShortestPath(Robot r, Robot rr) {
        Node nr = robotNodeList.get(r.currentPosition);
        for (Path p : nr.pathList) {
            if (p.end.name == rr.currentPosition) {
                return p;
            }
        }
        return null;
    }

    public static void greedyDelayed() {

        robotArrayList.get(0).status = "awake";
        //initialize the target list
        targetList = new ArrayList<Robot>();

        targetList.add(robotArrayList.get(0));

        //fill in the target list
        for (int i = 1; i < robotArrayList.size(); i++) {

            //calculate distance to nearest robot for all awake robots
            for (Robot r : robotArrayList) {

                if (r.status == "awake") {
                    r.closest = -1;
                    r.remainingDistance = Double.MAX_VALUE;
                    for (Robot rr : robotArrayList) {
                        if (rr.status == "sleeping") {
                            double d = getShortestPath(r, rr).length - r.distanceAccumulated;
                            if (d < r.remainingDistance) {
                                r.remainingDistance = d;
                                r.closest = rr.id;
                            }
                        }
                    }
                }
            }

            // which robot will reach it's target first
            int nextRobotToReachTarget = -1;
            double d = Double.MAX_VALUE;
            for (Robot r : robotArrayList) {
                if (r.status == "awake" && r.remainingDistance < d) {
                    d = r.remainingDistance;
                    nextRobotToReachTarget = r.id;
                }
            }

            if (equal(d, Double.MAX_VALUE)) {
                break;
            }

            // update acquired distance for all awake robots
            for (Robot r : robotArrayList) {
                if (r.status == "awake") {
                    r.distanceAccumulated += d;
                }
            }

            //reset robot that made the jump
            Robot movingRobot = robotArrayList.get(nextRobotToReachTarget);
            movingRobot.distanceAccumulated = 0;
            movingRobot.route.add(getShortestPath(movingRobot, robotArrayList.get(movingRobot.closest)));
            movingRobot.currentPosition = movingRobot.closest;
            // add next target into target list
            targetList.add(robotArrayList.get(movingRobot.closest));
            // update newly awakened robot
            robotArrayList.get(movingRobot.closest).status = "awake";

        }

    }

    public static void main(String[] args) throws IOException {
        Stream<String> stream = Files.lines(Paths.get("robots.mat"));
        String regexFloat = "(-?[.0-9]*)";
        stream.forEach(line -> {
            robotArrayList = new ArrayList<Robot>();
            obstacleArrayList = new ArrayList<Obstacle>();
            int gitgud = 0;

            String currentLine = line
                    .replaceAll("\\s", "");

            String[] divideString = currentLine.split("#");

            Matcher m = Pattern
                    .compile("\\(" + regexFloat + "," + regexFloat + "\\)")
                    .matcher(divideString[0]);

            while (m.find()) {
                double x = Double.parseDouble(m.group(1));
                double y = Double.parseDouble(m.group(2));
                robotArrayList.add(new Robot(x, y, gitgud));
                gitgud++;
            }

            if (divideString.length > 1) {
                String[] wallString = divideString[1].split(";");

                for (int j = 0; j < wallString.length; j++) {
                    ArrayList<Double> x = new ArrayList<Double>();
                    ArrayList<Double> y = new ArrayList<Double>();

                    m = Pattern
                            .compile("\\(" + regexFloat + "," + regexFloat + "\\)")
                            .matcher(wallString[j]);

                    while (m.find()) {
                        double a = Double.parseDouble(m.group(1));
                        double b = Double.parseDouble(m.group(2));
                        x.add(a);
                        y.add(b);
                    }
                    int arrLength = x.size();
                    double[] xs = new double[arrLength];
                    double[] ys = new double[arrLength];
                    for (int i = 0; i < arrLength; i++) {
                        xs[i] = x.get(i);
                        ys[i] = y.get(i);
                    }

                    obstacleArrayList.add(new Obstacle(xs, ys, arrLength));
                }
            }

            // Set the segment list and Node list

            segmentList = new ArrayList<Line2D.Double>();

            robotNodeList = new ArrayList<Node>();

            verticesNodeList = new ArrayList<Node>();
            int numberNode = 0;

            for (Robot r : robotArrayList) {
                robotNodeList.add(new Node(numberNode, r.x, r.y));
                numberNode++;
            }

            for (Obstacle o : obstacleArrayList) {
                antiClockWise(o);
                double[] xList = o.x;
                double[] yList = o.y;
                for (int i = 0; i < xList.length; i++) {
                    Node newNode = new Node(numberNode, xList[i], yList[i]);
                    verticesNodeList.add(newNode);
                    o.verticesList.add(newNode);
                    numberNode++;
                    segmentList.add(new Line2D.Double(xList[i], yList[i], xList[(i + 1) % xList.length], yList[(i + 1) % xList.length]));
                }
            }


            // Construct the edges
            // Consider 3 types of edges
            // Node of robots - Node of robots
            for (Node r : robotNodeList) {
                for (Node rr : robotNodeList) {
                    if (r.name != rr.name && !r.adjacentList.contains(rr)) {
                        Line2D.Double newLine = new Line2D.Double(r.x, r.y, rr.x, rr.y);
                        boolean flag = true;
                        for (Line2D.Double s : segmentList) {
                            if (s.intersectsLine(newLine) && !isShareEndPoint(newLine, s)) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            r.adjacentList.add(rr);
                            rr.adjacentList.add(r);
                        }
                    }
                }
            }
            // Node of robots - Node of vertices
            for (Node r : robotNodeList) {
                for (Node rr : verticesNodeList) {
                    if (r.name != rr.name && !r.adjacentList.contains(rr)) {
                        Line2D.Double newLine = new Line2D.Double(r.x, r.y, rr.x, rr.y);
                        boolean flag = true;
                        for (Line2D.Double s : segmentList) {
                            if (s.intersectsLine(newLine) && !isShareEndPoint(newLine, s)) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            r.adjacentList.add(rr);
                            rr.adjacentList.add(r);
                        }
                    }
                }
            }
            // Node of vertices - Node of vertices
            for (Obstacle o : obstacleArrayList) {
                for (int i = 0; i < o.verticesList.size(); i++) {
                    o.verticesList.get(i).adjacentList.add(o.verticesList.get((i + o.verticesList.size() - 1) % o.verticesList.size()));
                    o.verticesList.get(i).left = o.verticesList.get((i + o.verticesList.size() - 1) % o.verticesList.size());
                    o.verticesList.get(i).adjacentList.add(o.verticesList.get((i + 1) % o.verticesList.size()));
                    o.verticesList.get(i).right = o.verticesList.get((i + 1) % o.verticesList.size());
                }
            }

            for (Node v : verticesNodeList) {
                for (Node vv : verticesNodeList) {
                    if (v.name != vv.name && !v.adjacentList.contains(vv)) {
                        Line2D.Double newLine = new Line2D.Double(v.x, v.y, vv.x, vv.y);
                        boolean flag = true;
//                        System.out.println(v.name + " " + vv.name);
//                        isInside(v, newLine);
//                        if (v.name == 30 && vv.name == 33)
//                                return;
                        if (isInside(v, newLine)) {
//                            System.out.println("in");
//                            if (v.name == 30 && vv.name == 33)
//                                return;
                            continue;
                        }
//                        System.out.println("out");
//                        System.out.println(v.name + " " + vv.name);
                        for (Line2D.Double s : segmentList) {
                            if (s.intersectsLine(newLine) && !isShareEndPoint(newLine, s)) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
//                            System.out.println("match");
                            v.adjacentList.add(vv);
                            vv.adjacentList.add(v);
                        }
                    }
                }
            }

            // Find shortest path between the robots

            for (Node r : robotNodeList) {
                dijkstra(r);
            }

//             Starting Greedy Delayed Algorithm
            greedyDelayed();

//             Output
            output();

            robotArrayList.clear();
            robotNodeList.clear();
            obstacleArrayList.clear();
            verticesNodeList.clear();
            segmentList.clear();
            targetList.clear();
        });
    }
//    public static void input() {
//        Stream<String> stream = null;
//        try {
//            stream = Files.lines(Paths.get("robots.mat"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        String regexFloat = "(-?[.0-9]*)";
//        stream.forEach(line -> {
//            robotArrayList = new ArrayList<Robot>();
//            obstacleArrayList = new ArrayList<Obstacle>();
//            int gitgud = 0;
//
//            String currentLine = line
//                    .replaceAll("\\s", "");
//
//            String[] divideString = currentLine.split("#");
//
//            Matcher m = Pattern
//                    .compile("\\(" + regexFloat + "," + regexFloat + "\\)")
//                    .matcher(divideString[0]);
//
//            while (m.find()) {
//                double x = Double.parseDouble(m.group(1));
//                double y = Double.parseDouble(m.group(2));
//                robotArrayList.add(new Robot(x, y, gitgud));
//                gitgud++;
//            }
//
//            if (divideString.length > 1) {
//                String[] wallString = divideString[1].split(";");
//
//                for (int j = 0; j < wallString.length; j++) {
//                    ArrayList<Double> x = new ArrayList<>();
//                    ArrayList<Double> y = new ArrayList<>();
//
//                    m = Pattern
//                            .compile("\\(" + regexFloat + "," + regexFloat + "\\)")
//                            .matcher(wallString[j]);
//
//                    while (m.find()) {
//                        double a = Double.parseDouble(m.group(1));
//                        double b = Double.parseDouble(m.group(2));
//                        x.add(a);
//                        y.add(b);
//                    }
//                    int arrLength = x.size();
//                    double[] xs = new double[arrLength];
//                    double[] ys = new double[arrLength];
//                    for (int i = 0; i < arrLength; i++) {
//                        xs[i] = x.get(i);
//                        ys[i] = y.get(i);
//                    }
//
//                    obstacleArrayList.add(new Obstacle(xs, ys, arrLength));
//                }
//            }
//
//    }

//    public static void main(String[] args) {
//        // write your code here
//        // Read Input
//        // For example
//        // Robots : (-1,-1),(4,4)
//        // Obstacles : (1,6),(1,1),(5,1),(5,5),(3,5),(3,3),(4,3),(4,2),(2,2),(2,6),(6,6),(6,0),(0,0),(0,6)
////        robotArrayList = new ArrayList<Robot>();
////        robotArrayList.add(new Robot(0, 1, 0));
////        robotArrayList.add(new Robot(2, 0, 1));
////        robotArrayList.add(new Robot(3, 5, 2));
////        robotArrayList.add(new Robot(6, 2, 3));
////        robotArrayList.add(new Robot(9, 0, 4));
////
////        obstacleArrayList = new ArrayList<Obstacle>();
////        double x[] = {1, 1, 3, 3};
////        double y[] = {2, 4, 4, 2};
////        obstacleArrayList.add(new Obstacle(x, y, x.length));
////        double xx[] = {8, 4, 4, 5};
////        double yy[] = {1, 1, 4, 2};
////        obstacleArrayList.add(new Obstacle(xx, yy, xx.length));
//        // Set the segment list and Node list
//
//        input();
//        segmentList = new ArrayList<Line2D.Double>();
//
//        robotNodeList = new ArrayList<Node>();
//
//        verticesNodeList = new ArrayList<Node>();
//        int numberNode = 0;
//
//        for (Robot r : robotArrayList) {
//            robotNodeList.add(new Node(numberNode, r.x, r.y));
//            numberNode++;
//        }
//
//        for (Obstacle o : obstacleArrayList) {
//            double[] xList = o.x;
//            double[] yList = o.y;
//            for (int i = 0; i < xList.length; i++) {
//                Node newNode = new Node(numberNode, xList[i], yList[i]);
//                verticesNodeList.add(newNode);
//                o.verticesList.add(newNode);
//                numberNode++;
//                segmentList.add(new Line2D.Double(xList[i], yList[i], xList[(i + 1) % xList.length], yList[(i + 1) % xList.length]));
//            }
//        }
//
//
//        // Construct the edges
//        // Consider 3 types of edges
//        // Node of robots - Node of robots
//        for (Node r : robotNodeList) {
//            for (Node rr : robotNodeList) {
//                if (r.name != rr.name && !r.adjacentList.contains(rr)) {
//                    Line2D.Double newLine = new Line2D.Double(r.x, r.y, rr.x, rr.y);
//                    boolean flag = true;
//                    for (Line2D.Double s : segmentList) {
//                        if (s.intersectsLine(newLine) && !isShareEndPoint(newLine, s)) {
//                            flag = false;
//                            break;
//                        }
//                    }
//                    if (flag) {
//                        r.adjacentList.add(rr);
//                        rr.adjacentList.add(r);
//                    }
//                }
//            }
//        }
//        // Node of robots - Node of vertices
//        for (Node r : robotNodeList) {
//            for (Node rr : verticesNodeList) {
//                if (r.name != rr.name && !r.adjacentList.contains(rr)) {
//                    Line2D.Double newLine = new Line2D.Double(r.x, r.y, rr.x, rr.y);
//                    boolean flag = true;
//                    for (Line2D.Double s : segmentList) {
//                        if (s.intersectsLine(newLine) && !isShareEndPoint(newLine, s)) {
//                            flag = false;
//                            break;
//                        }
//                    }
//                    if (flag) {
//                        r.adjacentList.add(rr);
//                        rr.adjacentList.add(r);
//                    }
//                }
//            }
//        }
//        // Node of vertices - Node of vertices
//        for (Obstacle o : obstacleArrayList) {
//            for (int i = 0; i < o.verticesList.size(); i++) {
//                o.verticesList.get(i).adjacentList.add(o.verticesList.get((i + o.verticesList.size() - 1) % o.verticesList.size()));
//                o.verticesList.get(i).left = o.verticesList.get((i + o.verticesList.size() - 1) % o.verticesList.size());
//                o.verticesList.get(i).adjacentList.add(o.verticesList.get((i + 1) % o.verticesList.size()));
//                o.verticesList.get(i).right = o.verticesList.get((i + 1) % o.verticesList.size());
//            }
//        }
//
//        for (Node v : verticesNodeList) {
//            for (Node vv : verticesNodeList) {
//                if (v.name != vv.name && !v.adjacentList.contains(vv)) {
//                    Line2D.Double newLine = new Line2D.Double(v.x, v.y, vv.x, vv.y);
//                    boolean flag = true;
//                    if (isInside(v, newLine)) {
//                        continue;
//                    }
//                    for (Line2D.Double s : segmentList) {
//                        if (s.intersectsLine(newLine) && !isShareEndPoint(newLine, s)) {
//                            flag = false;
//                            break;
//                        }
//                    }
//                    if (flag) {
//                        v.adjacentList.add(vv);
//                        vv.adjacentList.add(v);
//                    }
//                }
//            }
//        }
//
//        // Find shortest path between the robots
//
//        for (Node r : robotNodeList) {
//            dijkstra(r);
//        }
//
//        // Starting Greedy Delayed Algorithm
//        greedyDelayed();
//
//        // Output
//        output();
//    }

    public static void output() {
        for (Robot r : targetList) {
            for (Path p : r.route) {
                for (Node n : p.path) {
                    System.out.print("(" + n.x + ", " + n.y + "), ");
                }
            }
            if (r.route.size() != 0) System.out.print("; ");
        }
    }
}
