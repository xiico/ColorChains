package com.colorchains.colorchainsgl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.util.DisplayMetrics;

import org.json.JSONException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by francisco.cnmarao on 17/04/2017.
 */

public class GameView extends GLSurfaceView {
    public static float w, h;
    public static int screenW,screenH;
    private static boolean paused;
    public static boolean disableTouch = false;
    public static DisplayMetrics metrics;
    private static boolean showFPS = true;
    public static boolean started = false;
    // A boolean which we will set and unset
    // when the game is running- or not.
    volatile boolean playing;
    public static volatile GLRenderer renderer;
    public UI.Font font;
    public static Context context;
    public static Integer defaultSide = 46;//46
    public static Integer scaledDefaultSide = 46;//46
    public static boolean is16x9;
    public static float scale;
    private FloatBuffer vertexBuffer;
    Board board;
    public GameView(Context context) {
        super(context);
        setEGLConfigChooser(8,8,8,8,0,0);
        renderer = new GLRenderer();
        setRenderer(renderer);
        GameView.context = context;
        metrics = context.getResources().getDisplayMetrics();
        Media.init();
        Media.setBackGroundMusic(R.raw.title);

        float vertices[] = {
                -0.5f,-0.5f,0.0f,
                0.5f,-0.5f,0.0f,
                -0.5f,0.5f,0.0f,
                0.5f,0.5f,0.0f,
        };

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
    }

    // Everything that needs to be updated goes in here
    // In later projects we will have dozens (arrays) of objects.
    // We will also do other things like collision detection.
    public void update() {
        //board.update();
    }

    // If SimpleGameEngine Activity is paused/stopped
    // shutdown our thread.
    public void pause() {
        playing = false;
        Media.pause();
    }

    // If SimpleGameEngine Activity is started then
    // start our thread.
    public void resume() {
        playing = true;
        Media.unpause();
    }

    // The SurfaceView class implements onTouchListener
    // So we can override this method and detect screen touches.
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:
                this.touchStart(motionEvent);
                break;

            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:
                this.touchEnd(motionEvent);
                break;
        }
        return true;
    }

    public void touchStart(MotionEvent evt){
        /*if(GameView.paused) GameView.paused = false;
        if(GameView.disableTouch) return;
        UI.checkUITouch(evt);
        if (board.levelComplete) {
            return;
        }
        if(UI.isInModalMode()) return;
        if(!GameView.started) {
            GameView.started = true;
            title.visible = false;
            //board.loadStage(0);
            return;
        }
        float rawX = evt.getRawX();
        float rawY = evt.getRawY();
        Integer row = (int)Math.floor((rawY - board.gemOffsetY) / GameView.scaledDefaultSide);
        Integer col = (int)Math.floor((rawX - board.gemOffsetX) / GameView.scaledDefaultSide);
        if(!board.ready() || row >= board.entities.length || col >= board.entities[0].length || row < 0 || col < 0) return;
        Gem entity = (Gem)board.entities[row][col];
        board.selectedGem = entity;
        if(((Gem)board.selectedGem).curAnimation != null) ((Gem)board.selectedGem).curAnimation.play();*/
    }

    public void touchEnd(MotionEvent evt) {
        /*if(GameView.disableTouch) return;
        if (board.levelComplete) return;
        if (!GameView.started) return;
        float rawX = evt.getRawX();
        float rawY = evt.getRawY();
        Integer row = (int)Math.floor((rawY - board.gemOffsetY) / GameView.scaledDefaultSide);
        Integer col = (int)Math.floor((rawX - board.gemOffsetX) / GameView.scaledDefaultSide);
        if(!board.ready() || row >= board.entities.length || col >= board.entities[0].length || row < 0 || col < 0) return;
        if (board.selectedGem == null || !board.selectedGem.getClass().getName().endsWith("Gem")) return;
        float diffRow = Math.abs(row - ((Gem)board.selectedGem).getRow());
        float diffCol = Math.abs(col - ((Gem)board.selectedGem).getCol());
        if (diffRow > diffCol) {
            row = ((Gem)board.selectedGem).getRow() + (row > ((Gem)board.selectedGem).getRow() ? 1 : -1);
            if (row < 0 || row >= board.entities.length) return;
            col = ((Gem)board.selectedGem).getCol();
        } else {
            col = ((Gem)board.selectedGem).getCol() + (col > ((Gem)board.selectedGem).getCol() ? 1 : -1);
            if (col < 0 || col >= board.entities[0].length) return;
            row = ((Gem)board.selectedGem).getRow();
        }
        Gem entity = (Gem)board.entities[row][col];
        if (entity == null || !entity.getClass().getName().endsWith("Gem") || (diffCol == 0 && diffRow == 0)) return;
        // entity.selected = !entity.selected;
        ((Gem)board.selectedGem).moveTo = new Point();
        ((Gem)board.selectedGem).moveTo.y = row;
        ((Gem)board.selectedGem).moveTo.x = col;
        entity.moveTo = new Point();
        entity.moveTo.y = ((Gem)board.selectedGem).getRow();
        entity.moveTo. x = ((Gem)board.selectedGem).getCol();
        board.clearChains = true;*/
    }


    final class GLRenderer implements GLSurfaceView.Renderer{
        public HashMap<TYPE,Texture> textureIds = new HashMap<>();
        public boolean hasTexture(TYPE type){
            return textureIds.containsKey(type);
        }

        public Texture loadTexture(TYPE type, Bitmap bmp){
            Texture texture = new Texture();
            if(!textureIds.containsKey(type)) {
                int[] temp = new int[1];
                GLES10.glGenTextures(1, temp, 0);
                texture.textureId = temp[0];
                texture.width = bmp.getWidth();
                texture.height = bmp.getHeight();
                //textureId = newTextureID(type);
                GLES10.glBindTexture(GL10.GL_TEXTURE_2D, texture.textureId);
                GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);
                GLES10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

                if(texture.mipmaps) {
                    GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
                    GLES10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_LINEAR);
                } else GLES10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);

                bmp.recycle();
                texture.buildMapping();
                textureIds.put(type, texture);
                return  texture;
            } else {
                return textureIds.get(type);
            }
        }

        public void loadTexture(TYPE type, Integer resourceId){
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inScaled = false;
            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), resourceId, opts);
            loadTexture(type, bmp);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //set up alpha blending
            gl.glEnable(GL10.GL_ALPHA_TEST);
            gl.glEnable(GL10.GL_BLEND);
            gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
            //we are doing 2d
            gl.glDisable(GL10.GL_DEPTH_TEST);
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

            /********* Environment **********/
            GameView.is16x9 = metrics.heightPixels / (float)metrics.widthPixels >= 1.6f;
            scale = Math.round(metrics.widthPixels / 360f);
            GameView.defaultSide = GameView.is16x9 ? 42 : (metrics.widthPixels < 800 ? 46 : 48);
            GameView.scaledDefaultSide = GameView.defaultSide * (int)scale;
            /********************/
            try {
                board = new Board(context);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            font = new UI.Font(UI.Font.FontSize.Normal);
            font.text = "OK!";
            font.x = 32;
            font.y = 32;
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glClearColor(0,0,1,1);
            w = metrics.widthPixels;//600
            h = height * w / width;
            screenH = height;
            screenW = width;
            gl.glViewport(0,0,screenW,screenH);
            gl.glMatrixMode(GL10.GL_MODELVIEW);//GL_PROJECTION
            gl.glLoadIdentity();
            gl.glOrthof(0,w,h,0,-1,1);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glClear(GLES10.GL_COLOR_BUFFER_BIT);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
            Timer.update();
            /***** TEST *****/
            gl.glEnable(GL10.GL_TEXTURE_2D);
            //gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            /***** TEST *****/
            if(GameView.showFPS) {
                // Display the current fps on the screen
                font.text = String.valueOf(Timer.fps);
                font.draw(gl);
            }

        }
    }
}
