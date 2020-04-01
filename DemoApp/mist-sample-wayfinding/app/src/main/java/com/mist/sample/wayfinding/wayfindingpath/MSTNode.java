package com.mist.sample.wayfinding.wayfindingpath;

import com.mist.android.MSTPoint;

import org.json.JSONObject;

/**
 * Created by cta on 4/22/16.
 */
public class MSTNode {

    private String nodeName;
    private MSTPoint nodePoint;
    private JSONObject edges;

    public MSTNode(String name, MSTPoint nodePoint, JSONObject edges){
        this.nodeName = name;
        this.nodePoint = nodePoint;
        this.edges = edges;
    }

    public String getName() {
        return this.nodeName;
    }

    public JSONObject getEdges() {
        return edges;
    }

    public void setEdges(JSONObject edges) {
        this.edges = edges;
    }

    public MSTPoint getNodePoint() {
        return nodePoint;
    }

    public String showEdges(){
        return this.edges.toString();
    }
}