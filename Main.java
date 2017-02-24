package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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

    public static String teamName = "qilin";
    public static String password = "aspcqkrt7jjcde67482oj3r2pa";
    public static String fileName = "output.txt";
    public static int questionNumber = 1;
    public static PrintWriter outputStream;

    public static void draw() throws InterruptedException{
        JFrame frame = new JFrame("Move and Tag Problem");
        frame.setSize(800, 800);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.createBufferStrategy(2);
        BufferStrategy strategy = frame.getBufferStrategy();

        double x_max, y_max, x_min, y_min;
        ArrayList<Double> xs = new ArrayList<>();
        ArrayList<Double> ys = new ArrayList<>();
        for (Robot r : drawRobots) {
            xs.add(r.x);
            ys.add(r.y);
        }
        for (Obstacle o : drawWalls) {
            for (int i =0; i<o.x.length; i++) {
                xs.add(o.x[i]);
            }
            for (int i =0; i<o.y.length; i++) {
                ys.add(o.y[i]);
            }
        }
        x_max = Collections.max(xs);
        y_max = Collections.max(ys);
        x_min = Collections.min(xs);
        y_min = Collections.min(ys);

        while (true) {
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            AffineTransform old = g.getTransform();
            Dimension size = frame.getSize();

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, (int) size.getWidth(), (int) size.getHeight());
            g.scale(size.getWidth() / (x_max - x_min + 10), size.getHeight() / (y_max - y_min + 10));
            g.translate(-x_min + 5, -y_min + 5);

            g.setColor(Color.RED);
            //draw obstacles
            for (Obstacle o : drawWalls) {
                Path2D.Double path = new Path2D.Double();
                boolean first = true;
                for (Node v : o.verticesList) {
                    if (first) {
                        path.moveTo(v.x, v.y);
                        first = false;
                    } else
                        path.lineTo(v.x, v.y);
                }
                g.fill(path);
            }

            //starting positions (inclusive of end positions)
            g.setColor(Color.CYAN);
            double radius = 0.1;
            for (Robot robot : drawRobots) {
                Ellipse2D.Double r = new Ellipse2D.Double(robot.x - radius, robot.y - radius, radius * 2, radius * 2);
                g.fill(r);
            }

            g.setColor(Color.GREEN);
            for (Robot r : drawLines) {
                //paths for all robots
                for (Path p : r.route) {
                    //path for one robot
                    Line2D line = new Line2D.Double();
                    for (int i = 0; i < p.path.size() - 1; i++) {
                        //nodes in paths
                        line.setLine(p.path.get(i).x, p.path.get(i).y, p.path.get(i + 1).x, p.path.get(i + 1).y);
                        g.setStroke(new BasicStroke(0.05f));
                        g.draw(line);
                    }
                }
            }

            g.setTransform(old);
            g.dispose();
            strategy.show();

            Thread.sleep(100);
        }
    }

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
    public static int drawNumber = 27;

// Precision control for different cases
    public static boolean equalLine(double x, double y) {
        double e = 0.000000001;
        switch (questionNumber) {
            case 14:
                e = 0.01;

                break;
            case 15:
                e = 0.0001;
                break;
            case 12:
                e = 0.05;
                break;
            case 18:
//                e = 0.05892683925;
//                e = 0.0001;
                break;
            case 25:
                e = 0.05;
                break;
            case 27:
//                e = 0.05;
//                e = 0.5;
                e = 0.1065608835;
                break;
            case 29:
                e = 0.235;
//                e = 0.3;
                break;
            default:
                break;
        }
        return (Math.abs(x - y) < e);
    }

    public static boolean equalLine2(double x, double y) {
        double e = 0.000000001;
        switch (questionNumber) {
            case 14:
                e = 0.01;
                break;
            case 15:
                e = 0.0001;
                break;
            case 12:
                e = 0.05;
                break;
            case 18:
//                e = 0.5;
//                e = 0.22;
//                e = 1;
                break;
            case 25:
                e = 0.05;
                break;
            case 27:
//                e = 0.05;
//                e = 0.265;
//                e = 0.1065608835;
                break;
            case 29:
//                e = 0.2475;
//                e = 0.235;
//                e = 0.0000000000001;
                break;
            default:
                break;
        }
        return (Math.abs(x - y) < e);
    }

// Check intersection between two lines
    public static boolean isIntersect(Line2D.Double s, Line2D.Double newline) {
        Point2D p1 = newline.getP1();
        Point2D p2 = newline.getP2();
        Point2D ps1 = s.getP1();
        Point2D ps2 = s.getP2();
        if (equalLine(p1.distance(ps1) + p1.distance(ps2), ps1.distance(ps2)))
            return true;
        if (equalLine(p2.distance(ps1) + p2.distance(ps2), ps1.distance(ps2)))
            return true;
        if (equalLine2(ps1.distance(p1) + ps1.distance(p2), p1.distance(p2)))
            return true;
        if (equalLine2(ps2.distance(p1) + ps2.distance(p2), p1.distance(p2)))
            return true;
        return s.intersectsLine(newline);
    }

// Another method for intersection check

//    public static boolean isIntersect(Line2D.Double s, Line2D.Double newline) {
//        Point2D p1 = newline.getP1();
//        Point2D p2 = newline.getP2();
//        Point2D ps1 = s.getP1();
//        Point2D ps2 = s.getP2();
//        if (isOnSegment(s, p1) || isOnSegment(s, p2) || isOnSegment(newline, ps1) || isOnSegment(newline, ps2))
//            return true;
//        return s.intersectsLine(newline);
//    }

//     public static boolean equal3(double x, double y) {
//         double e = 0.00001;
//         switch (questionNumber) {
//             case 14:
//                 e = 0.01;

//                 break;
//             case 15:
//                 e = 0.0001;
//                 break;
//             case 12:
//                 e = 0.05;
//                 break;
//             case 18:
//                 e = 0.45;
// //                e = 0.0001;
//                 break;
//             case 25:
//                 e = 0.06;
//                 break;
//             case 1:
// //                e = 0.05;
// //                e = 0.5;
//                 e = 1.02721;
// //                e = 1.05;
//                 break;
//             case 29:
//                 e = 4.466;
// //                e = 0.3;
//                 break;
//             default:
//                 break;
//         }
//         return (Math.abs(x - y) < e);
//     }

//     public static boolean isOnSegment(Line2D.Double s, Point2D p) {
//         double x = p.getX();
//         double y = p.getY();
//         double x1 = s.getX1();
//         double y1 = s.getY1();
//         double x2 = s.getX2();
//         double y2 = s.getY2();
//         if((x > x1 && x > x2) || (x < x1 && x < x2) || (y > y1 && y > y2) || (y < y1 && y < y2))
//             return false;
//         else {
//             if (equal3((x-x1) * (y-y2), (x-x2) * (y-y1)))
//                 return true;
//             else return false;
//         }
//     }

// Check if a line point into a obstacle
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
        double polarLeft = Math.atan2(vectorLeftX, vectorLeftY);
        double polarRight = Math.atan2(vectorRightX, vectorRightY);
        double polar = Math.atan2(vectorX, vectorY);
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


    public static ArrayList<Robot> drawRobots;
    public static ArrayList<Obstacle> drawWalls;
    public static ArrayList<Robot> drawLines;
    public static int drawCount = 1;

    // Main function for visualization
    public static void main(String[] args) throws IOException, InterruptedException {
        outputStream = new PrintWriter(fileName);
        outputStream.println(teamName);
        outputStream.println(password);

        Stream<String> stream = Files.lines(Paths.get("robots.mat"));
        String regexFloat = "(-?[.0-9E-]*)";

        stream.forEach(line -> {
            System.out.println("drawCount: " + drawCount);
            System.out.println("drawNumber: " + drawNumber);
            System.out.println("----------------------");
            if (drawCount++ != drawNumber) {return;}
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
                    newNode.from = o.n;
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
                           if (isIntersect(s, newLine) && !isShareEndPoint(newLine, s)) {
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
                            if (isIntersect(s, newLine) && !isShareEndPoint(newLine, s)) {
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
                       if (isInside(v, newLine)) {
                           continue;
                       }
                       if (v.from == vv.from) {
                           continue;
                       }
                       for (Line2D.Double s : segmentList) {
                           if (isIntersect(s, newLine) && !isShareEndPoint(newLine, s)) {
                               flag = false;
                               break;
                           }
                       }
                       if (flag) {
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

            drawCount--;
            if (drawCount++ == drawNumber) {
                drawRobots = new ArrayList<>(robotArrayList);
                drawWalls = new ArrayList<>(obstacleArrayList);
                drawLines = new ArrayList<>(targetList);
            }

            robotArrayList.clear();
            robotNodeList.clear();
            obstacleArrayList.clear();
            verticesNodeList.clear();
            segmentList.clear();
            targetList.clear();
        });

        outputStream.close();
        draw();
    }

// Main function for submission results

//    public static void main(String[] args) throws IOException {
//        outputStream = new PrintWriter(fileName);
//        outputStream.println(teamName);
//        outputStream.println(password);

//        Stream<String> stream = Files.lines(Paths.get("robots.mat"));
//        String regexFloat = "(-?[.0-9E-]*)";
//        stream.forEach(line -> {
//            robotArrayList = new ArrayList<Robot>();
//            obstacleArrayList = new ArrayList<Obstacle>();
//            int gitgud = 0;

//            String currentLine = line
//                    .replaceAll("\\s", "");

//            String[] divideString = currentLine.split("#");

//            Matcher m = Pattern
//                    .compile("\\(" + regexFloat + "," + regexFloat + "\\)")
//                    .matcher(divideString[0]);

//            while (m.find()) {
//                double x = Double.parseDouble(m.group(1));
//                double y = Double.parseDouble(m.group(2));
//                robotArrayList.add(new Robot(x, y, gitgud));
//                gitgud++;
//            }

//            if (divideString.length > 1) {
//                String[] wallString = divideString[1].split(";");

//                for (int j = 0; j < wallString.length; j++) {
//                    ArrayList<Double> x = new ArrayList<Double>();
//                    ArrayList<Double> y = new ArrayList<Double>();

//                    m = Pattern
//                            .compile("\\(" + regexFloat + "," + regexFloat + "\\)")
//                            .matcher(wallString[j]);

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

//                    obstacleArrayList.add(new Obstacle(xs, ys, arrLength));
//                }
//            }

//            // Set the segment list and Node list

//            segmentList = new ArrayList<Line2D.Double>();

//            robotNodeList = new ArrayList<Node>();

//            verticesNodeList = new ArrayList<Node>();
//            int numberNode = 0;

//            for (Robot r : robotArrayList) {
//                robotNodeList.add(new Node(numberNode, r.x, r.y));
//                numberNode++;
//            }

//            int c = 0;
//            for (Obstacle o : obstacleArrayList) {
//                antiClockWise(o);
//                double[] xList = o.x;
//                double[] yList = o.y;
//                for (int i = 0; i < xList.length; i++) {
//                    Node newNode = new Node(numberNode, xList[i], yList[i]);
//                    newNode.from = c;
//                    verticesNodeList.add(newNode);
//                    o.verticesList.add(newNode);
//                    numberNode++;

//                    segmentList.add(new Line2D.Double( xList[i],  yList[i],  xList[(i + 1) % xList.length],  yList[(i + 1) % xList.length]));
//                }
//                c++;
//            }

//            // Construct the edges
//            // Consider 3 types of edges
//            // Node of robots - Node of robots
//            for (Node r : robotNodeList) {
//                for (Node rr : robotNodeList) {
//                    if (r.name != rr.name && !r.adjacentList.contains(rr)) {
//                        Line2D.Double newLine = new Line2D.Double( r.x, r.y, rr.x, rr.y);
//                        boolean flag = true;
//                        for (Line2D.Double s : segmentList) {
//                            if (isIntersect(s, newLine) && !isShareEndPoint(newLine, s)) {
//                                flag = false;
//                                break;
//                            }
//                        }
//                        if (flag) {
//                            r.adjacentList.add(rr);
//                            rr.adjacentList.add(r);
//                        }
//                    }
//                }
//            }
//            // Node of robots - Node of vertices
//            for (Node r : robotNodeList) {
//                for (Node rr : verticesNodeList) {
//                    if (r.name != rr.name && !r.adjacentList.contains(rr)) {
//                        Line2D.Double newLine = new Line2D.Double(r.x, r.y, rr.x, rr.y);
//                        boolean flag = true;
//                        for (Line2D.Double s : segmentList) {
//                            if (isIntersect(s, newLine) && !isShareEndPoint(newLine, s)) {
//                                flag = false;
//                                break;
//                            }
//                        }
//                        if (flag) {
//                            r.adjacentList.add(rr);
//                            rr.adjacentList.add(r);
//                        }
//                    }
//                }
//            }
//            // Node of vertices - Node of vertices
//            for (Obstacle o : obstacleArrayList) {
//                for (int i = 0; i < o.verticesList.size(); i++) {
//                    o.verticesList.get(i).adjacentList.add(o.verticesList.get((i + o.verticesList.size() - 1) % o.verticesList.size()));
//                    o.verticesList.get(i).left = o.verticesList.get((i + o.verticesList.size() - 1) % o.verticesList.size());
//                    o.verticesList.get(i).adjacentList.add(o.verticesList.get((i + 1) % o.verticesList.size()));
//                    o.verticesList.get(i).right = o.verticesList.get((i + 1) % o.verticesList.size());
//                }
//            }

//            for (Node v : verticesNodeList) {
//                for (Node vv : verticesNodeList) {
//                    if (v.name != vv.name && !v.adjacentList.contains(vv)) {
//                        Line2D.Double newLine = new Line2D.Double(v.x, v.y, vv.x, vv.y);
//                        boolean flag = true;
//                        if (isInside(v, newLine)) {
//                            continue;
//                        }
//                        for (Line2D.Double s : segmentList) {
//                            if (isIntersect(s, newLine) && !isShareEndPoint(newLine, s)) {
//                                flag = false;
//                                break;
//                            }
//                        }
//                        if (flag) {
//                            v.adjacentList.add(vv);
//                            vv.adjacentList.add(v);
//                        }
//                    }
//                }
//            }

//            // Find shortest path between the robots

//            for (Node r : robotNodeList) {
//                dijkstra(r);
//            }

// //             Starting Greedy Delayed Algorithm
//            greedyDelayed();

//            output();

//            robotArrayList.clear();
//            robotNodeList.clear();
//            obstacleArrayList.clear();
//            verticesNodeList.clear();
//            segmentList.clear();
//            targetList.clear();
//        });
//        outputStream.close();
//    }


    public static void output() {
        boolean skip = false;

        ArrayList<String> results = new ArrayList<>();
        System.out.print(questionNumber + ": ");
        outputStream.print(questionNumber + ": ");
        questionNumber++;
        for (Robot r : targetList) {
            for (Path p : r.route) {
                for (Node n : p.path) {
                    skip = true;
                    results.add("(" + n.x + ", " + n.y + "), ");
                }
            }
            if (skip) {
                String last = results.remove(results.size() - 1);
                results.add(last.substring(0, last.length() - 2).concat("; "));
            }
            skip = false;
        }
        String last = results.remove(results.size() - 1);
        results.add(last.substring(0, last.length() - 2).concat("\n"));

        ArrayList<String> removeList = new ArrayList<>();
        for (int i = 0; i < results.size() - 1; i++) {
            String s = results.get(i);
            if (s.equals(results.get(i + 1))) {
                removeList.add(s);
            }
        }
        removeList.forEach(results::remove);
        results.forEach(System.out::print);
        results.forEach(outputStream::print);
    }
}