package com.colorchains.colorchainsgl;

import android.graphics.PointF;

/**
 * Created by francisco.cnmarao on 19/04/2017.
 */

class Animation {
    Entity parent;
    public String id;
    public Float x;
    public Float y;
    public Integer[] frames;
    public float duration;
    public boolean loop;
    public Float frameInterval;
    public Integer curFrame = 0;
    public Boolean playing = false;
    private int targetDelta = 16;
    private float _curFrame = 0;

    public Animation(String id, Integer x, Integer y, Integer[] frames, Float duration, Boolean loop, Entity parent) {
        this.parent = (Entity) parent;
        this.id = id;
        this.x = (float)x;
        this.y = (float)y;
        this.frames = frames;
        this.duration = duration;
        this.loop = loop;
        this.frameInterval = 1 / ((duration * 60 * this.targetDelta) / frames.length);
    }
    public Animation play(){
        if (!this.playing) this.playing = true;
        return this;
    }

    public void update(){
        this._curFrame += this.frameInterval * Timer.deltaTime;
        if (!this.loop && this._curFrame > this.frames.length - 1) {
            this.playing = false;
            parent.cacheX = 0;
            parent.cacheY = 0;
            this.curFrame = this.frames[0];
            return;
        }
        if (this.curFrame > this.frames.length) this._curFrame = 0f;
        this.curFrame = this.frames[(int)_curFrame];
    }

    class Tranform{
        public float startScale = 0.5f;
        public float endScale = 1.5f;
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
        private Entity parent;
        public Tranform(Entity entity){
            this.parent = entity;
        }
    }
}
