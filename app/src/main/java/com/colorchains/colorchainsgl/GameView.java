package com.colorchains.colorchainsgl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
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
    //private final UI.Title title;
    // A boolean which we will set and unset
    // when the game is running- or not.
    volatile boolean playing;
    public static volatile GLRenderer renderer;
    public static Context context;
    public static Integer defaultSide = 46;//46
    public static Integer scaledDefaultSide = 46;//46
    public static boolean is16x9;
    public static float scale;
    private FloatBuffer vertexBuffer;
    static Board board;
    public GameView(Context context) {
        super(context);

        /******* init GL2 **********/
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        /******* end GL2 **********/
        renderer = new GLRenderer();
        setRenderer(renderer);
        GameView.context = context;
        metrics = context.getResources().getDisplayMetrics();
        UI.init();
        Media.init();
        Media.setBackGroundMusic(R.raw.title);

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
        if(renderer.backGround.index + 1 < renderer.backGround.programs.size()){
            renderer.backGround.index++;
        } else  renderer.backGround.index = 0;
        renderer.backGround.mProgram = renderer.backGround.programs.get(renderer.backGround.index);
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


    public static class GLRenderer implements GLSurfaceView.Renderer{
        public UI.Font font;
        public HashMap<TYPE,Shape> textureIds = new HashMap<>();
        public boolean hasTexture(TYPE type){
            return textureIds.containsKey(type);
        }
        /******* fields GL2 **********/
        // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
        public static final float[] mProjectionMatrix = new float[16];
        public static final float[] mViewMatrix = new float[16];
        public static Integer curProgram = -1;
        public static Integer curTexture = -1;
        private BackGround backGround;
        private UI.Title title;
        /******* fields GL2 **********/
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            /**************** onSurfaceCreated GL2 **********************/
            // Set the background frame color
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);

            // Draw objects back to front
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            /*************** end onSurfaceCreated GL2 ***********************/

            /********* Environment **********/
            GameView.is16x9 = metrics.heightPixels / (float)metrics.widthPixels >= 1.6f;
            scale = Math.round(metrics.widthPixels / 360f);
            GameView.defaultSide = GameView.is16x9 ? 42 : (metrics.widthPixels < 800 ? 46 : 48);
            GameView.scaledDefaultSide = GameView.defaultSide * (int)scale;
            /********************/
            /*try {
                board = new Board(context);
            } catch (JSONException e) {
                e.printStackTrace();
            }*/


            title = new UI.Title();
            title.setX((metrics.widthPixels / 2) - (title.width / 2f));
            title.setY(scaledDefaultSide * 4f);
            UI.addControl(title);

            font = new UI.Font(R.drawable.oldskol, 2.0f);
            font.x = 32;
            font.y = 32;
            //font.doScale = true;
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            /******* onSurfaceChanged GL2 **********/
            final float left = 0;
            final float right = width;
            final float bottom = height;
            final float top = 0;
            final float near = 0;//-1f;
            final float far = 1f;
            GLES20.glViewport(0, 0, width, height);
            Matrix.orthoM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
            // Set the camera position (View matrix)
            Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            /*title = new UI.Title();
            title.setX((metrics.widthPixels / 2) - (title.width / 2f));
            title.setY(scaledDefaultSide * 4f);*/

            backGround = new BackGround();
            backGround.setX(width/2f);
            backGround.setY(height/2f);
            backGround.setOffSetX(0);
            backGround.setOffSetY(0);
            backGround.doScale = false;
            backGround.rotate = false;
            backGround.index = 0;

            /******* end onSurfaceChanged GL2 **********/
            w = metrics.widthPixels;//600
            h = height * w / width;
            screenH = height;
            screenW = width;
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            Timer.update();
            /********** onDrawFrame GL2 ****************************/
            // Redraw background color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            changeProgram(backGround.mProgram, Shape.vertexBuffer);
            //Shape.bindTexture(backGround.getResourceId());
            backGround.draw();
            //Shape.bindTexture(title.getResourceId());
            //title.draw();
            /********** end onDrawFrame GL2 ****************************/
            if(GameView.showFPS) {
                changeProgram(font.mProgram, font.vertexBuffer);
                //Shape.bindTexture(font.getResourceId());
                // Display the current fps on the screen
                font.setText(String.valueOf(Timer.fps));
                font.draw();
            }
            UI.draw();

        }
        public static int loadShader(int type, String shaderCode) {
            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
            int shader = GLES20.glCreateShader(type);

            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);

            return shader;
        }

        public static void changeProgram(Integer program, FloatBuffer vertexBuffer){
            curProgram = program;
            // Add program to OpenGL ES environment
            GLES20.glUseProgram(curProgram);

            // get handle to vertex shader's vPosition member
            int mPositionHandle = GLES20.glGetAttribLocation(curProgram, "vPosition");

            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(mPositionHandle);

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(mPositionHandle, Shape.COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    Shape.vertexStride, vertexBuffer);
        }
    }
}
