package com.mist.sample.wayfinding.wayfindingpath;

import android.graphics.Path;
import android.graphics.PathMeasure;

import com.mist.android.MSTMap;
import com.mist.android.MSTPoint;
import com.mist.sample.wayfinding.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MSTWayFinder {
    public JSONObject jsonPath;
    private JSONArray nodesJsonArray = null;
    private MSTPoint startingPoint;
    private MSTPoint destinationPoint;
    ArrayList<MSTPath> showPathArrayList = new ArrayList<>();
    ArrayList<MSTEdges> edgesPointArrayList = new ArrayList<>();
    ArrayList<MSTPath> pathArrayList = new ArrayList<>();
    boolean isActualData;


    public MSTWayFinder(JSONObject jsonPath, boolean isActualData) {

        this.jsonPath = jsonPath;
        this.isActualData = isActualData;

        if (jsonPath != null) {
            try {
                nodesJsonArray = jsonPath.getJSONArray("nodes");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public MSTPoint getStartingPoint() {
        return startingPoint;
    }

    public MSTPoint getDestinationPoint() {
        return destinationPoint;
    }

    public void setStartingPoint(MSTPoint startingPoint) {
        this.startingPoint = startingPoint;
    }

    public void setDestinationPoint(MSTPoint destinationPoint) {
        this.destinationPoint = destinationPoint;
    }

    public ArrayList<MSTPath> getShowPathList(HashMap<String, Object> nodes,
                                              double scaleXFactor, double scaleYFactor, MSTMap mstMap) {

        showPathArrayList.clear();
        edgesPointArrayList.clear();

        try {

            if (!isActualData) {
                scaleXFactor = scaleXFactor * mstMap.getPpm();
                scaleYFactor = scaleYFactor * mstMap.getPpm();
            }

            Iterator<Map.Entry<String, Object>> it = nodes.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> node = it.next();
                String nodeName = node.getKey();
                MSTNode mMSTNode = (MSTNode) node.getValue();
                MSTPoint mstPoint = mMSTNode.getNodePoint();
                JSONObject edges = mMSTNode.getEdges();

                MSTEdges mstEdges = new MSTEdges();
                mstEdges.setMstPoint(mstPoint);
                mstEdges.setsEdgeName(nodeName);
                this.edgesPointArrayList.add(mstEdges);

                Iterator<?> keys = edges.keys();
                if (mstPoint != null && edges != null) {
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        if (edges.get(key) instanceof String) {
                            MSTNode mstNode = (MSTNode) nodes.get(key);
                            if (mstNode != null) {
                                MSTPoint mstPoint1 = mstNode.getNodePoint();
                                JSONObject edges1 = mstNode.getEdges();
                                if (edges1 != null && mstPoint1 != null) {

                                    Path path = new Path();
                                    path.moveTo((float) (mstPoint.getX() * scaleXFactor),
                                            (float) (mstPoint.getY() * scaleYFactor));
                                    path.lineTo((float) (mstPoint1.getX() * scaleXFactor),
                                            (float) (mstPoint1.getY() * scaleYFactor));

                                    if (!checkDuplicatePath(nodeName, key, showPathArrayList)) {
                                        MSTPath mstPath = new MSTPath();
                                        mstPath.setPath(path);
                                        mstPath.setStartingPoint(mstPoint);
                                        mstPath.setEndPoint(mstPoint1);
                                        mstPath.setStartingEdgeName(nodeName);
                                        mstPath.setWayfinding(false);
                                        mstPath.setEndEdgeName(key);
                                        mstPath.setMstPointArrayList(getPoints(path));
                                        showPathArrayList.add(mstPath);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return showPathArrayList;
    }

    public ArrayList<MSTPath> getShowPathArrayList() {
        return this.showPathArrayList;
    }

    public ArrayList<MSTPath> drawWayfingPath(ArrayList<String> pathArr,
                                              double scaleXFactor, double scaleYFactor, MSTMap mstMap) {
        ArrayList<MSTPath> pathArrayList = new ArrayList<>();
        ArrayList<String> pathNameArrayList = new ArrayList<>();

        if (pathArr != null) {
            for (int i = 0; i < pathArr.size() - 1; i++) {
                String name = pathArr.get(i);
                String name1 = pathArr.get(i + 1);
                pathNameArrayList.add(name + "-" + name1);
            }
        }

        if (!isActualData) {
            scaleXFactor = scaleXFactor * mstMap.getPpm();
            scaleYFactor = scaleYFactor * mstMap.getPpm();
        }

        for (MSTPath mstPath : this.showPathArrayList) {
            if (checkKeyName(mstPath.getStartingEdgeName(), mstPath.getEndEdgeName(), pathNameArrayList)) {
                MSTPath mstPath1 = new MSTPath();
                mstPath1.setPath(mstPath.getPath());
                mstPath1.setStartingPoint(mstPath.getStartingPoint());
                mstPath1.setEndPoint(mstPath.getEndPoint());
                mstPath1.setStartingEdgeName(mstPath.getStartingEdgeName());
                mstPath1.setEndEdgeName(mstPath.getEndEdgeName());
                mstPath1.setWayfinding(true);
                mstPath1.setMstPointArrayList(mstPath.getMstPointArrayList());
                pathArrayList.add(mstPath1);
            }

            if (true &&
                    pathArr != null && pathArr.size() > 0 && startingPoint != null) {
                String key1 = pathArr.get(pathArr.size() - 1);
                Path path1 = null;
                boolean isNodeName = false;
                if (mstPath.getStartingEdgeName().equals(key1)) {
                    path1 = new Path();
                    path1.moveTo((float) (mstPath.getStartingPoint().getX() * scaleXFactor), (float) (mstPath.getStartingPoint().getY() * scaleYFactor));
                    if (isActualData)
                        path1.lineTo((float) (startingPoint.getX() * scaleXFactor * mstMap.getPpm()), (float) (startingPoint.getY() * scaleYFactor * mstMap.getPpm()));
                    else
                        path1.lineTo((float) (startingPoint.getX() * scaleXFactor), (float) (startingPoint.getY() * scaleYFactor));
                    isNodeName = true;
                } else if (mstPath.getEndEdgeName().equals(key1)) {
                    path1 = new Path();
                    if (isActualData)
                        path1.moveTo((float) (startingPoint.getX() * scaleXFactor * mstMap.getPpm()), (float) (startingPoint.getY() * scaleYFactor * mstMap.getPpm()));
                    else
                        path1.moveTo((float) (startingPoint.getX() * scaleXFactor), (float) (startingPoint.getY() * scaleYFactor));
                    path1.lineTo((float) (mstPath.getEndPoint().getX() * scaleXFactor), (float) (mstPath.getEndPoint().getY() * scaleYFactor));
                    isNodeName = false;
                }

                if (path1 != null) {
                    MSTPath mstPath1 = new MSTPath();
                    mstPath1.setPath(path1);
                    mstPath1.setWayfinding(true);
                    if (isNodeName) {
                        mstPath1.setStartingPoint(mstPath.getStartingPoint());
                        mstPath1.setEndPoint(startingPoint);
                        mstPath1.setStartingEdgeName(mstPath.getStartingEdgeName());
                        mstPath1.setEndEdgeName("startingPoint");
                    } else {
                        mstPath1.setStartingPoint(startingPoint);
                        mstPath1.setEndPoint(mstPath.getEndPoint());
                        mstPath1.setStartingEdgeName("startingPoint");
                        mstPath1.setEndEdgeName(mstPath.getEndEdgeName());
                    }
                    mstPath1.setMstPointArrayList(getPoints(path1));
                    pathArrayList.add(mstPath1);
                }
            }
        }

        return pathArrayList;
    }

    public void drawShowPath(HashMap<String, Object> nodes,
                             double scaleXFactor, double scaleYFactor, MSTMap mstMap) {


        edgesPointArrayList.clear();
        pathArrayList.clear();
        try {
            if (!isActualData) {
                scaleXFactor = scaleXFactor * mstMap.getPpm();
                scaleYFactor = scaleYFactor * mstMap.getPpm();
            }

            Iterator<Map.Entry<String, Object>> it = nodes.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> node = it.next();
                String nodeName = node.getKey();
                MSTNode mMSTNode = (MSTNode) node.getValue();
                MSTPoint mstPoint = mMSTNode.getNodePoint();
                JSONObject edges = mMSTNode.getEdges();

                MSTEdges mstEdges = new MSTEdges();
                mstEdges.setMstPoint(mstPoint);
                mstEdges.setsEdgeName(nodeName);
                this.edgesPointArrayList.add(mstEdges);

                Iterator<?> keys = edges.keys();
                if (mstPoint != null && edges != null) {
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        if (edges.get(key) instanceof String) {
                            MSTNode mstNode = (MSTNode) nodes.get(key);
                            if (mstNode != null) {
                                MSTPoint mstPoint1 = mstNode.getNodePoint();
                                JSONObject edges1 = mstNode.getEdges();
                                if (edges1 != null && mstPoint1 != null) {
                                    Path path = new Path();
                                    path.moveTo((float) (mstPoint.getX() * scaleXFactor), (float) (mstPoint.getY() * scaleYFactor));
                                    path.lineTo((float) (mstPoint1.getX() * scaleXFactor), (float) (mstPoint1.getY() * scaleYFactor));

                                    if (!checkDuplicatePath(nodeName, key, pathArrayList)) {
                                        MSTPath mstPath = new MSTPath();
                                        mstPath.setPath(path);
                                        mstPath.setStartingPoint(mstPoint);
                                        mstPath.setEndPoint(mstPoint1);
                                        mstPath.setStartingEdgeName(nodeName);
                                        mstPath.setEndEdgeName(key);
                                        mstPath.setWayfinding(false);
                                        mstPath.setMstPointArrayList(getPoints(path));
                                        pathArrayList.add(mstPath);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<MSTPath> getPathArrayList() {
        return this.pathArrayList;
    }


    private boolean checkDuplicatePath(String sEdgeName, String eEdgeName, ArrayList<MSTPath> mstPathArrayList) {
        if (mstPathArrayList == null || mstPathArrayList.size() == 0)
            return false;
        else {
            for (MSTPath mstPath : mstPathArrayList) {
                if ((mstPath.getStartingEdgeName().equals(sEdgeName) && mstPath.getEndEdgeName().equals(eEdgeName)) ||
                        (mstPath.getStartingEdgeName().equals(eEdgeName) && mstPath.getEndEdgeName().equals(sEdgeName)))
                    return true;

            }
        }
        return false;
    }

    public ArrayList<MSTPoint> getPoints(Path path) {
        ArrayList<MSTPoint> mstPointArrayList = new ArrayList<>();
        PathMeasure pm = new PathMeasure(path, false);
        float length = pm.getLength();
        float distance = 0f;
        float speed = 1;// length / length;
        float[] aCoordinates = new float[2];

        while ((distance < length)) {
            // get point from the path
            pm.getPosTan(distance, aCoordinates, null);
            mstPointArrayList.add(new MSTPoint(aCoordinates[0], aCoordinates[1]));
            distance = distance + speed;
        }

        return mstPointArrayList;
    }

    private boolean checkKeyName(String key, String key1, ArrayList<String> pathArrList) {

        for (String keyName : pathArrList) {
            if (keyName.equals(key + "-" + key1) || keyName.equals(key1 + "-" + key))
                return true;
        }
        return false;
    }

    /**
     * Get nearest position name from nodes json array
     */
    public String getNearestPositionName(MSTPoint point, MSTMap mstMap) {
        String finalName = "";
        double minDistance = 0;
        if (point != null) {
            try {

                boolean isActualData;
                String sCoordinate = jsonPath.optString("coordinate");
                if (!Utils.isEmptyString(sCoordinate) && sCoordinate.equals("actual"))
                    isActualData = true;
                else
                    isActualData = false;

                for (int i = 0; i < nodesJsonArray.length(); i++) {
                    JSONObject jsonObject = nodesJsonArray.getJSONObject(i);
                    String sName = jsonObject.getString("name");
                    JSONObject positionJsonObject = jsonObject.getJSONObject("position");
                    if (positionJsonObject != null) {
                        String xValue = positionJsonObject.getString("x");
                        String yValue = positionJsonObject.getString("y");

                        double distance;
                        if (isActualData)
                            distance = Utils.distanceBetweenTwoPoints(point.getX() * mstMap.getPpm(), point.getY() * mstMap.getPpm(), Double.valueOf(xValue), Double.valueOf(yValue));
                        else
                            distance = Utils.distanceBetweenTwoPoints(point.getX(), point.getY(), Double.valueOf(xValue), Double.valueOf(yValue));
                        if (i == 0 || distance == 0 || distance < minDistance) {
                            minDistance = distance;
                            finalName = sName;
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return finalName;
    }

    /**
     * Get nearest position name from nodes json array
     */
    public String getNearestPositionName(MSTPoint point) {
        String finalName = "";
        double minDistance = 0;
        if (point != null) {
            try {
                for (int i = 0; i < edgesPointArrayList.size(); i++) {
                    MSTEdges mstEdges = edgesPointArrayList.get(i);
                    String sName = mstEdges.getsEdgeName();


                    MSTPoint screenPoint = mstEdges.getMstScreenPoint();

                    if (screenPoint != null) {

                        double distance = Utils.distanceBetweenTwoPoints(point.getX(), point.getY(), screenPoint.getX(), screenPoint.getY());

                        if (i == 0 || distance == 0 || distance < minDistance) {
                            minDistance = distance;
                            finalName = sName;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return finalName;
    }


    /**
     * Get nearest position name from nodes json array
     */
    public MSTPoint getNearestPosition(String sKeyName) {
        MSTPoint mstPoint = null;
        if (sKeyName != null) {
            try {
                for (int i = 0; i < nodesJsonArray.length(); i++) {
                    JSONObject jsonObject = nodesJsonArray.getJSONObject(i);
                    String sName = jsonObject.getString("name");

                    if (sName.equals(sKeyName)) {
                        JSONObject positionJsonObject = jsonObject.getJSONObject("position");
                        if (positionJsonObject != null) {
                            String xValue = positionJsonObject.getString("x");
                            String yValue = positionJsonObject.getString("y");
                            mstPoint = new MSTPoint(Double.valueOf(xValue), Double.valueOf(yValue));
                            return mstPoint;
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return mstPoint;
    }

    /**
     * Get All edges name from nodes json array
     */
    public ArrayList<MSTEdges> getEdges() {
        return this.edgesPointArrayList;
    }
}
