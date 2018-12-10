package com.mist.sample.wayfinding.wayfindingpath;


import com.mist.android.MSTPoint;

/**
 * Created by Entappia on 28-09-2016.
 */
public class MSTEdges {

    String sEdgeName;
    MSTPoint mstPoint;
    MSTPoint mstScreenPoint;

    public String getsEdgeName() {
        return sEdgeName;
    }

    public void setsEdgeName(String sEdgeName) {
        this.sEdgeName = sEdgeName;
    }

    public MSTPoint getMstPoint() {
        return mstPoint;
    }

    public void setMstPoint(MSTPoint mstPoint) {
        this.mstPoint = mstPoint;
    }

    public MSTPoint getMstScreenPoint() {
        return mstScreenPoint;
    }

    public void setMstScreenPoint(MSTPoint mstScreenPoint) {
        this.mstScreenPoint = mstScreenPoint;
    }
}
