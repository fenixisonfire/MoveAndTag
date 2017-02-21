package com.company;

import java.util.ArrayList;

/**
 * Created by wumengyang on 20/02/2017.
 */
public class Obstacle {
    public double x[], y[];
    public int n;
    public ArrayList<Node> verticesList;
    Obstacle(double x[], double y[], int n) {
        this.x = x;
        this.y = y;
        this.n = n;
        verticesList = new ArrayList<Node>();
    }
}
