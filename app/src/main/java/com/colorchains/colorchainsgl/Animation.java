package com.colorchains.colorchainsgl;

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
        //parent.cacheX = Math.round(this.x) + (this.frames[(int) Math.floor(this.curFrame)] * GameView.scaledDefaultSide);
        //parent.cacheY = Math.round(this.y);
    }
}
