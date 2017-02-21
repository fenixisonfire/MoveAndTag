package com.company;

import java.util.ArrayList;

/**
 * Created by wumengyang on 20/02/2017.
 */
public class Robot {
    public int id;
    public int currentPosition;
    public String status;
    public ArrayList<Path> route;
    public double x;
    public double y;
    public double distanceAccumulated;
    public double remainingDistance;
    public int closest;

    Robot(double x, double y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
        currentPosition = id;
        status = "sleeping";
        distanceAccumulated = 0;
        remainingDistance = 0;
        closest = -1;
        route = new ArrayList<Path>();
    }
}
