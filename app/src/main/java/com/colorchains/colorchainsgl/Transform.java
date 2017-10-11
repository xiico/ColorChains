package com.colorchains.colorchainsgl;

import android.graphics.PointF;

/**
 * Created by francisco.cnmarao on 09/10/2017.
 */

class Transform{
    public boolean isScalingIn = false;
    public boolean isScalingOut = false;
    public float minScale = 0.5f;
    public float maxScale = 1.5f;
    public float defaultScale = 0f;
    public float scaleStep = 0.0125f;//0.025f;
    public float startAngle = 0;
    public float endAngle = 0;
    public float startAlpha = 1;
    public float endAlpha = 1;
    public PointF startPosition = new PointF();
    public PointF endPosition = new PointF();
    public boolean loopAlpha = false;
    public boolean loopScale = false;
    public boolean loopAngle = false;
    public float scaleDuration = 0.5f;
    public float angleDuration = 0.5f;
    public float alphaDuration = 0.5f;
    public float translationDuration = 1;
    private Shape parent;
    private int targetDelta = 16;
    public Transform(Shape shape){
        this.parent = shape;
    }

    public void scaleIn(float startScale, float endScale, float duration){
        this.maxScale = startScale;
        this.minScale = endScale;
        this.scaleStep = ((startScale - endScale) / (60f * duration)) / this.targetDelta;
        this.isScalingIn = true;
    }

    public void scaleOut(float startScale, float endScale, float duration){
        this.minScale = startScale;
        this.maxScale = endScale;
        this.scaleStep =  ((endScale - startScale) / (60f * duration)) / this.targetDelta;
        this.isScalingOut = true;
    }
}
