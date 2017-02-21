package com.company;

import java.util.ArrayList;

/**
 * Created by wumengyang on 21/02/2017.
 */
public class Path {
    public Node start;
    public Node end;
    ArrayList<Node> path;
    public int numberOfSegments;
    public double length;
    public void finish() {
        length = 0;
        numberOfSegments = path.size();
        for (int i = 1; i < numberOfSegments; i++) {
            length += Main.distance(path.get(i-1), path.get(i));
        }
    }

    Path(Node start, Node end) {
        this.start = start;
        this.end = end;
        path = new ArrayList<Node>();
        path.add(start);
    }
}
