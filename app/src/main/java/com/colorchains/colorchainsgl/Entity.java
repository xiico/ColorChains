package com.colorchains.colorchainsgl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by francisco.cnmarao on 19/04/2017.
 */

public abstract class Entity extends Shape {
    public Integer width = 10;
    public Integer height = 10;
    public Integer cacheWidth = 10;
    public Integer cacheHeight = 10;
    public String id;
    private Float x;
    private Float y;
    float ratio = 1;
    public final TYPE type;
    public Integer cacheX;
    public Integer cacheY;
    public HashMap<String, Animation> animations = new HashMap<>();
    public Animation curAnimation;
    public Entity parent;
    private float offSetX;
    private float offSetY;
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

    public void setX(float x){
        super.x = x;
    }
    public void setY(float y){
        super.y = y;
    }

    public float getX() {
        return x * GameView.w / GameView.screenW;
    }

    public float getY() {
        return y * GameView.h / GameView.screenH;
    }

    static float squareCoords[] = {
            -0.5f,  0.5f, 0.0f,  // top left      0
            -0.5f, -0.5f, 0.0f,  // bottom left   1
            0.5f, -0.5f, 0.0f,  // bottom right  2
            0.5f,  0.5f, 0.0f}; // top right     3
    private static short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices
    public static final String vs_Image =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  v_texCoord = a_texCoord;" +
                    "}";

    public static final String fs_Image =
            "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform float color;" +
                    "uniform vec2 resolution;" +
                    "uniform float time;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, v_texCoord );\n" +
                    "}";
    public Entity(String id, TYPE type, Float x, Float y, Integer cx, Integer cy){
        super(squareCoords, drawOrder,vs_Image, fs_Image, getGemSprites(type));
        this.type = type;
        this.id = id;
        this.x = x;
        this.y = y;
        this.cacheX = cx;
        this.cacheY = cy;
        this.width = GameView.scaledDefaultSide;
        this.height = GameView.scaledDefaultSide;
    }

    public static int getGemSprites(TYPE type){
        switch (type) {
            case GREENGEM:
                return R.drawable.greengem;
            case REDGEM:
                return R.drawable.redgem;
            case YELLOWGEM:
                return R.drawable.yellowgem;
            case PURPLEGEM:
                return R.drawable.purplegem;
            case BLUEGEM:
                return R.drawable.bluegem;
            case WHITEGEM:
                return R.drawable.whitegem;
            case ORANGEGEM:
                return R.drawable.orangegem;
            case CYANGEM:
                return R.drawable.cyangem;
            case MARIO:
                return R.drawable.mario_tiles_46;
            case BACKGROUND:
                return R.drawable.bg_color;
            default:
                return -1;
        }
    }

    protected void loadTexture(Integer resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bmp = BitmapFactory.decodeResource(GameView.context.getResources(), resourceId, options);//
        //texture = GameView.renderer.loadTexture(this.type, bmp);
        if(textureMap == null) buildTextureMap();
        this.cacheWidth = bmp.getWidth() / 10;
        this.cacheHeight = bmp.getHeight() / 3;
    }

    void draw(GL10 gl){
        /*Render.draw(Render.cached.get(this.type), this.cacheX, this.cacheY, this.cacheWidth, this.cacheHeight, this.x, this.y, offSetX, offSetY);
        if (GameView.showIds) Render.showId(this);*/
        /******  gl 1.1  ************/
//        texture.setMapping(textureMap[0]);
//        texture.draw(gl, getX(), getY(), width * ratio, height * ratio, 0, true);
        /****** end gl 1.1  ************/
        super.draw();
    }

    void addAnimation(String id, Integer x, Integer y, Integer[] frames, Float duration,Boolean loop){
        this.animations.put(id,new Animation(id, x, y, frames, duration, loop, this));
    }

    public void buildTextureMap(){

        textureMap = new float[1][8];
        /******  gl 1.1  ************/
//        textureMap[0] = texture.textureMap;
        /****** end  gl 1.1  ************/
    }

    public float getOffSetX() {
        return offSetX;
    }

    public void setOffSetX(float offSetX) {
        super.offSetX = offSetX;
    }

    public float getOffSetY() {
        return offSetY;
    }

    public void setOffSetY(float offSetY) {
        super.offSetY = offSetY;
    }
}
