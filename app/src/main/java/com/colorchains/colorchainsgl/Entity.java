package com.colorchains.colorchainsgl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by francisco.cnmarao on 19/04/2017.
 */

public abstract class Entity {
    public Integer width = 10;
    public Integer height = 10;
    public Integer cacheWidth = 10;
    public Integer cacheHeight = 10;
    public String id;
    public Float x;
    public Float y;
    float ratio = 1;
    public final TYPE type;
    public Integer cacheX;
    public Integer cacheY;
    public HashMap<String, Animation> animations = new HashMap<>();
    public Animation curAnimation;
    public Entity parent;
    public float offSetX;
    public float offSetY;
    protected Texture texture;
    protected static float[][] textureMap;

    public static Entity create(String id, TYPE type, Float x, Float y, Integer cx, Integer cy, Object parent){
        if(type == null) return null;
        switch (type) {
            case GREENGEM:
            case REDGEM:
            case YELLOWGEM:
            case PURPLEGEM:
            case BLUEGEM:
            case WHITEGEM:
            case ORANGEGEM:
            case CYANGEM:
                return new Gem(id, type, x, y, cx, cy, (Board) parent);
            case MARIO:
                return new Mario(id, type, x, y, cx, cy, (Board) parent);
            default:
                return null;
        }
    }

    public float getX() {
        return x * GameView.w / GameView.screenW;
    }

    public float getY() {
        return y * GameView.h / GameView.screenH;
    }

    public Entity(String id, TYPE type, Float x, Float y, Integer cx, Integer cy){
        this.type = type;
        this.id = id;
        this.x = x;
        this.y = y;
        this.cacheX = cx;
        this.cacheY = cy;
        this.width = GameView.scaledDefaultSide;
        this.height = GameView.scaledDefaultSide;
    }

    protected void loadTexture(Integer resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bmp = BitmapFactory.decodeResource(GameView.context.getResources(), resourceId, options);//
        texture = GameView.renderer.loadTexture(this.type, bmp);
        if(textureMap == null) buildTextureMap();
        this.cacheWidth = bmp.getWidth() / 10;
        this.cacheHeight = bmp.getHeight() / 3;
    }

    void draw(GL10 gl){
        /*Render.draw(Render.cached.get(this.type), this.cacheX, this.cacheY, this.cacheWidth, this.cacheHeight, this.x, this.y, offSetX, offSetY);
        if (GameView.showIds) Render.showId(this);*/
        texture.setMapping(textureMap[0]);
        texture.draw(gl, getX(), getY(), width * ratio, height * ratio, 0, true);
    }

    void addAnimation(String id, Integer x, Integer y, Integer[] frames, Float duration,Boolean loop){
        this.animations.put(id,new Animation(id, x, y, frames, duration, loop, this));
    }

    public void buildTextureMap(){
        textureMap = new float[1][8];
        textureMap[0] = texture.textureMap;
    }
}
