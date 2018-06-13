package com.mist.sample.wayfinding.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import com.mist.android.MSTMap;
import com.mist.android.MSTPoint;
import com.mist.android.deadReckoning.path.PreferenceConstants;
import com.mist.android.deadReckoning.path.PreferenceHelper;


import java.util.ArrayList;

public class DrawLine extends View {
    Paint paint = new Paint();
    Paint paint1 = new Paint();
    Path path = new Path();
    ArrayList<MSTPath> pathArrayList;
    ArrayList<String> pathArr;
    MSTPoint nearestMstPoint;
    PreferenceHelper preferenceHelper;
    private double scaleXFactor;
    private double scaleYFactor;
    MSTMap mstMap;
    boolean isActualData;

    public DrawLine(Context context, ArrayList<MSTPath> pathArrayList,
                    ArrayList<String> pathArr, MSTPoint nearestMstPoint, double scaleXFactor,
                    double scaleYFactor, MSTMap mstMap, boolean isActualData) {
        super(context);
        this.pathArrayList = pathArrayList;
        this.pathArr = pathArr;
        this.nearestMstPoint = nearestMstPoint;
        this.scaleXFactor = scaleXFactor;
        this.scaleYFactor = scaleYFactor;
        this.mstMap = mstMap;
        this.isActualData = isActualData;

        if(!isActualData) {
            this.scaleXFactor =  scaleXFactor * mstMap.getPpm();
            this.scaleYFactor =  scaleYFactor * mstMap.getPpm();
        }

        preferenceHelper = new PreferenceHelper(context);

        paint.setColor(Color.parseColor("#d5e4fb"));
        paint.setStrokeWidth(6);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        paint1.setColor(Color.parseColor("#3f77d5"));
        paint1.setStrokeWidth(6);
        paint1.setAntiAlias(true);
        paint1.setStyle(Paint.Style.STROKE);

    }



    @Override
    public void onDraw(Canvas canvas) {
        if (pathArrayList != null && pathArrayList.size() > 0) {
            boolean nearestPointAdded = false;
            boolean hasWayfindingValue = false;
            for (MSTPath path : pathArrayList) {
                if (path.isWayfinding()) {
                    hasWayfindingValue = true;
                    if (pathArr != null && pathArr.size() > 1 && nearestMstPoint != null) {
                        boolean hasNearestPoint = path.getMstPointArrayList().contains(nearestMstPoint);
                        if (hasNearestPoint) {
                            nearestPointAdded = true;
                            String sName = pathArr.get(pathArr.size() - 2);
                            String eName = pathArr.get(pathArr.size() - 1);
                            if (path.getStartingEdgeName().equals(sName) && path.getEndEdgeName().equals(eName)) {
                                if (preferenceHelper.getBooleanValue(PreferenceConstants.KEY_SHOW_PATHS, false)) {
                                    Path path1 = new Path();
                                    path1.moveTo((float) (nearestMstPoint.getX()), (float) (nearestMstPoint.getY()));
                                    path1.lineTo((float) (path.getStartingPoint().getX() * scaleXFactor), (float) (path.getStartingPoint().getY() * scaleYFactor));
                                    canvas.drawPath(path1, paint1);
                                }

                                Path path2 = new Path();
                                path2.moveTo((float) (nearestMstPoint.getX()), (float) (nearestMstPoint.getY()));
                                path2.lineTo((float) (path.getEndPoint().getX() * scaleXFactor), (float) (path.getEndPoint().getY() * scaleYFactor));
                                canvas.drawPath(path2, paint);
                            } else {
                                if (preferenceHelper.getBooleanValue(PreferenceConstants.KEY_SHOW_PATHS, false)) {
                                    Path path1 = new Path();
                                    path1.moveTo((float) (nearestMstPoint.getX()), (float) (nearestMstPoint.getY()));
                                    path1.lineTo((float) (path.getStartingPoint().getX() * scaleXFactor), (float) (path.getStartingPoint().getY() * scaleYFactor));
                                    canvas.drawPath(path1, paint);
                                }

                                Path path2 = new Path();
                                path2.moveTo((float) (nearestMstPoint.getX()), (float) (nearestMstPoint.getY()));
                                path2.lineTo((float) (path.getEndPoint().getX() * scaleXFactor), (float) (path.getEndPoint().getY() * scaleYFactor));
                                canvas.drawPath(path2, paint1);
                            }

                        } else {
                            canvas.drawPath(path.getPath(), paint1);
                        }
                    } else
                        canvas.drawPath(path.getPath(), paint1);
                } else if (preferenceHelper.getBooleanValue(PreferenceConstants.KEY_SHOW_PATHS, false)) {
                    canvas.drawPath(path.getPath(), paint);
                }
            }

            if (hasWayfindingValue && !nearestPointAdded) {
                if (pathArr != null && pathArr.size() > 1 && nearestMstPoint != null) {
                    String eName = pathArr.get(pathArr.size() - 1);
                    for (MSTPath path : pathArrayList) {
                        if (path.getStartingEdgeName().equals(eName)) {
                            Path path2 = new Path();
                            path2.moveTo((float) (nearestMstPoint.getX()), (float) (nearestMstPoint.getY()));
                            path2.lineTo((float) (path.getStartingPoint().getX() * scaleXFactor), (float) (path.getStartingPoint().getY() * scaleYFactor));
                            canvas.drawPath(path2, paint1);
                            break;
                        } else if (path.getEndEdgeName().equals(eName)) {
                            Path path2 = new Path();
                            path2.moveTo((float) (nearestMstPoint.getX()), (float) (nearestMstPoint.getY()));
                            path2.lineTo((float) (path.getEndPoint().getX() * scaleXFactor), (float) (path.getEndPoint().getY() * scaleYFactor));
                            canvas.drawPath(path2, paint1);
                            break;
                        }
                    }
                }
            }

        } else
            canvas.drawPath(path, paint);
    }

}

