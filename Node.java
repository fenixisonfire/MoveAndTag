package com.company;

import java.util.ArrayList;

/**
 * Created by wumengyang on 20/02/2017.
 */
public class Node {
    public double x;
    public double y;
    public ArrayList<Node> adjacentList;
    public int name;
    public Node left;
    public Node right;
    public ArrayList<Path> pathList;
    public boolean isVisited;
    public int from;
    Node(int name, double x, double y) {
        this.name = name;
        this.x = x;
        this.y = y;
        adjacentList = new ArrayList<Node>();
        pathList = new ArrayList<Path>();
    }
}
