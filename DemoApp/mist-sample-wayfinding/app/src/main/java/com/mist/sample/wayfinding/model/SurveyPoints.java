package com.mist.sample.wayfinding.model;

import android.graphics.PointF;

public class SurveyPoints {

    private PointF point;
    private String name;

    public SurveyPoints(PointF point, String name) {
        this.point = point;
        this.name = name;
    }

    public PointF getPoint() {
        return point;
    }

    public void setPoint(PointF point) {
        this.point = point;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
