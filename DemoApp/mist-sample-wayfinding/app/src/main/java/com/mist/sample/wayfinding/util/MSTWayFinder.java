package com.mist.sample.wayfinding.util;

import android.graphics.Path;
import android.graphics.PathMeasure;

import com.mist.android.MSTMap;
import com.mist.android.MSTPoint;

import com.mist.android.deadReckoning.path.MSTEdges;
import com.mist.android.deadReckoning.path.MSTNode;

import com.mist.android.deadReckoning.path.PreferenceConstants;
import com.mist.android.deadReckoning.path.PreferenceHelper;
import com.mist.android.deadReckoning.path.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MSTWayFinder {
    public JSONObject jsonPath;
    private JSONArray nodesJsonArray = null;
    private MSTPoint startingPoint;
    private MSTPoint destinationPoint;
    private PreferenceHelper preferenceHelper;
    ArrayList<MSTPath> showPathArrayList = new ArrayList<>();
    ArrayList<MSTEdges> edgesPointArrayList = new ArrayList<>();
    ArrayList<MSTPath> pathArrayList = new ArrayList<>();
    boolean isActualData;

    public MSTWayFinder(JSONObject jsonPath, boolean isActualData, PreferenceHelper preferenceHelper) {

        this.jsonPath = jsonPath;
        this.preferenceHelper = preferenceHelper;
        this.isActualData = isActualData;

        if (jsonPath != null) {
            try {
                nodesJsonArray = jsonPath.getJSONArray("nodes");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

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

    public MSTPoint closestPointOnAllPaths(MSTPoint point) {

        MSTPoint closestPoint = new MSTPoint(1, 1); // FIX ME


        return closestPoint;
    }

    public Path renderWayfinding(ArrayList pathArr) {
        //pathArr
        // loop through the path and draw a line from each node
        Path path = new Path();

        return path;
    }

    /**
     * Get way finding path value
     */
    /**
     * Get way finding path value
     */
    public Path drawWayfindingPath(ArrayList<String> keyNameList, double scaleXFactor, double scaleYFactor, MSTMap mstMap) {

        Path path = new Path();

        try {
            String xValue = "", yValue = "";
            if (!isActualData) {
                scaleXFactor = scaleXFactor * mstMap.getPpm();
                scaleYFactor = scaleYFactor * mstMap.getPpm();
            }

            if (startingPoint != null && nodesJsonArray != null && nodesJsonArray.length() > 0) {
                Collections.reverse(keyNameList);
                path.moveTo((float) (startingPoint.getX() * scaleXFactor), (float) (startingPoint.getY() * scaleYFactor));
                for (String keyName : keyNameList) {

                    JSONObject positionJsonObject = getPositionValues(keyName);
                    if (positionJsonObject != null) {
                        xValue = positionJsonObject.getString("x");
                        yValue = positionJsonObject.getString("y");
                        path.lineTo((float) (Double.valueOf(xValue) * scaleXFactor), (float) (Double.valueOf(yValue) * scaleYFactor));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return path;
    }


    /**
     * Get position of node from nodeJsonArray
     */
    private JSONObject getPositionValues(String keyName) {
        try {
            for (int i = 0; i < nodesJsonArray.length(); i++) {
                JSONObject jsonObject = nodesJsonArray.getJSONObject(i);
                if (jsonObject.getString("name").equals(keyName)) {
                    return jsonObject.getJSONObject("position");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
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

            if (preferenceHelper.getBooleanValue(PreferenceConstants.KEY_ENABLE_WAYFINDING) &&
                    !preferenceHelper.getBooleanValue(PreferenceConstants.KEY_SNAP_TO_PATH) &&
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

    public ArrayList<MSTPoint> getPoints(Path path, float speed) {
        ArrayList<MSTPoint> mstPointArrayList = new ArrayList<>();
        PathMeasure pm = new PathMeasure(path, false);
        float length = pm.getLength();
        float distance = 0f;
        float[] aCoordinates = new float[2];

        while ((distance < length)) {
            // get point from the path
            pm.getPosTan(distance, aCoordinates, null);
            mstPointArrayList.add(new MSTPoint(aCoordinates[0], aCoordinates[1]));
            distance = distance + speed;
        }

        return mstPointArrayList;
    }

    public MSTPoint getSnapPathPosition(float xPos, float yPos, ArrayList<MSTPath> pathArrayList, String sEdgeName) {
        MSTPoint mstPointSnapPosition = null;
        double minDistance = 0;
        boolean isFirstTime = true;
        if (sEdgeName != null && pathArrayList != null) {
            for (int i = 0; i < pathArrayList.size(); i++) {
                MSTPath mstPath = pathArrayList.get(i);
                if (mstPath != null && mstPath.getStartingEdgeName() != null && mstPath.getEndEdgeName() != null
                        && (mstPath.getStartingEdgeName().equals(sEdgeName) || mstPath.getEndEdgeName().equals(sEdgeName))) {
                    ArrayList<MSTPoint> mstPointArrayList = mstPath.getMstPointArrayList();
                    for (MSTPoint mstPoint : mstPointArrayList) {
                        double distance = Utility.distanceBetweenTwoPoints(xPos, yPos, mstPoint.getX(), mstPoint.getY());

                        if (isFirstTime || distance == 0 || distance < minDistance) {
                            minDistance = distance;
                            mstPointSnapPosition = mstPoint;
                            isFirstTime = false;

                        }
                    }
                }

            }
        }


        return mstPointSnapPosition;
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
                if (!Utility.isEmptyString(sCoordinate) && sCoordinate.equals("actual"))
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
                            distance = Utility.distanceBetweenTwoPoints(point.getX() * mstMap.getPpm(), point.getY() * mstMap.getPpm(), Double.valueOf(xValue), Double.valueOf(yValue));
                        else
                            distance = Utility.distanceBetweenTwoPoints(point.getX(), point.getY(), Double.valueOf(xValue), Double.valueOf(yValue));
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

                        double distance = Utility.distanceBetweenTwoPoints(point.getX(), point.getY(), screenPoint.getX(), screenPoint.getY());

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
    public String getNearestPositionName1(MSTPoint point, MSTMap mstMap, double scaleXFactor, double scaleYFactor) {
        String finalName = "";
        double minDistance = 0;
        if (point != null) {
            try {

                boolean isActualData;
                String sCoordinate = jsonPath.optString("coordinate");
                if (!Utility.isEmptyString(sCoordinate) && sCoordinate.equals("actual"))
                    isActualData = true;
                else
                    isActualData = false;

                for (int i = 0; i < edgesPointArrayList.size(); i++) {
                    MSTEdges mstEdges = edgesPointArrayList.get(i);
                    String sName = mstEdges.getsEdgeName();

                    MSTPoint screenPoint = null;

                    if (mstEdges.getMstScreenPoint() != null) {
                        screenPoint = mstEdges.getMstScreenPoint();

                        double distance = Utility.distanceBetweenTwoPoints(point.getX(), point.getY(), screenPoint.getX(), screenPoint.getY());

                        if (i == 0 || distance == 0 || distance < minDistance) {
                            minDistance = distance;
                            finalName = sName;
                        }
                    } else if (mstEdges.getMstPoint() != null) {
                        {
                            screenPoint = mstEdges.getMstPoint();
                            double distance;
                            if (isActualData)
                                distance = Utility.distanceBetweenTwoPoints(point.getX() * scaleXFactor * mstMap.getPpm(), point.getY() * scaleYFactor * mstMap.getPpm(), screenPoint.getX() * scaleXFactor * mstMap.getPpm(), screenPoint.getY() * scaleYFactor * mstMap.getPpm());
                            else
                                distance = Utility.distanceBetweenTwoPoints(point.getX() * scaleXFactor, point.getY() * scaleYFactor, screenPoint.getX() * scaleXFactor, screenPoint.getY() * scaleYFactor);
                            if (i == 0 || distance == 0 || distance < minDistance) {
                                minDistance = distance;
                                finalName = sName;
                            }
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
