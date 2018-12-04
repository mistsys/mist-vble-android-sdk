package com.mist.sample.wayfinding.utils;

import android.graphics.Path;

import com.mist.android.MSTPoint;

import java.util.ArrayList;

public class MSTPath {
    Path path;
    MSTPoint startingPoint;
    MSTPoint endPoint;
    String startingEdgeName;
    String endEdgeName;
    boolean isWayfinding;
    ArrayList<MSTPoint> mstPointArrayList;

    public ArrayList<MSTPoint> getMstPointArrayList() {
        return mstPointArrayList;
    }

    public void setMstPointArrayList(ArrayList<MSTPoint> mstPointArrayList) {
        this.mstPointArrayList = mstPointArrayList;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public boolean isWayfinding() {
        return isWayfinding;
    }

    public void setWayfinding(boolean wayfinding) {
        isWayfinding = wayfinding;
    }

    public MSTPoint getStartingPoint() {
        return startingPoint;
    }

    public void setStartingPoint(MSTPoint startingPoint) {
        this.startingPoint = startingPoint;
    }

    public MSTPoint getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(MSTPoint endPoint) {
        this.endPoint = endPoint;
    }

    public String getStartingEdgeName() {
        return startingEdgeName;
    }

    public void setStartingEdgeName(String startingEdgeName) {
        this.startingEdgeName = startingEdgeName;
    }

    public String getEndEdgeName() {
        return endEdgeName;
    }

    public void setEndEdgeName(String endEdgeName) {
        this.endEdgeName = endEdgeName;
    }
}

