package com.colorchains.colorchainsgl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.util.DisplayMetrics;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Random;

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
    public static volatile boolean playing;
    public static volatile GLRenderer renderer;
    public static Context context;
    public static Integer defaultSide = 46;//46
    public static Integer scaledDefaultSide = 46;//46
    public static boolean is16x9;
    public static float scale;
    private FloatBuffer vertexBuffer;
    public static boolean cycleBG = true;
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
        //Media.setBackGroundMusic(R.raw.title);
        //Media.setBackGroundMusic(R.raw.file1);
//        try {
//            Media.playBGM();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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

            case MotionEvent.ACTION_MOVE:
                this.touchMove(motionEvent);
                break;
        }
        return true;
    }

    private void touchMove(MotionEvent motionEvent) {
        UI.touchMoveUI(motionEvent);
    }

    public void touchStart(MotionEvent evt){

        /************/
//        if (GameView.cycleBG) {
//            if (GameView.renderer.backGround.index + 1 < GameView.renderer.backGround.programs.size()) {
//                GameView.renderer.backGround.index++;
//            } else GameView.renderer.backGround.index = 0;
//        }
        GameView.renderer.backGround.mProgram = GameView.renderer.backGround.programs.get(GameView.renderer.backGround.index);
        /***********/

        if(GameView.paused) GameView.paused = false;
        UI.touchStartUI(evt);
        if(GameView.disableTouch) return;
        if (board.levelComplete) {
            return;
        }
        if(UI.isInModalMode()) return;
        if(!GameView.started) {
            GameView.started = true;
            renderer.title.visible = false;
            //board.loadStage(0);
            return;
        }
        float rawX = evt.getRawX();
        float rawY = evt.getRawY();
        Integer row = (int)Math.floor((rawY - board.gemOffsetY) / GameView.scaledDefaultSide);
        Integer col = (int)Math.floor((rawX - board.gemOffsetX) / GameView.scaledDefaultSide);
        //Log.d("TOUCH_START","row: " + row +",col: " + col + ",rawX: " + rawX + ",rawY: " + rawY + ", gemOffsetY: " + board.gemOffsetY + ", gemOffsetX: " + board.gemOffsetX + ", ds: " + GameView.scaledDefaultSide);
        if(!GameView.GLRenderer._boardReady || row >= board.entities.length || col >= board.entities[0].length || row < 0 || col < 0) return;
        Gem entity = board.entities[row][col];
        board.selectedGem = entity;
        //entity.selected = true;
        //if(((Gem)board.selectedGem).curAnimation != null) ((Gem)board.selectedGem).curAnimation.play();
        if(((Gem)board.selectedGem).curAnimation != null) ((Gem)board.selectedGem).transform.scaleOut(0.75f,1.05f,0.4f); //.scaleIn();
    }

    public void touchEnd(MotionEvent evt) {
        UI.touchEndUI(evt);
        if(GameView.disableTouch) return;
        if (board.levelComplete) return;
        if (!GameView.started) return;
        float rawX = evt.getRawX();
        float rawY = evt.getRawY();
        Integer row = (int)Math.floor((rawY - board.gemOffsetY /*+ ((defaultSide *2)/2)*/) / GameView.scaledDefaultSide);
        Integer col = (int)Math.floor((rawX - board.gemOffsetX /*+ ((defaultSide *2)/2)*/) / GameView.scaledDefaultSide);
        //Log.d("TOUCH_END","row: " + row +",col: " + col + ",rawX: " + rawX + ",rawY: " + rawY + ", gemOffsetY: " + board.gemOffsetY + ", gemOffsetX: " + board.gemOffsetX + ", ds: " + GameView.scaledDefaultSide);
        //if(board.selectedGem != null) Log.d("selectedGem: " + ((Gem)board.selectedGem).id,"getRow(): " + ((Gem)board.selectedGem).getRow() + ", getCol(): " + ((Gem)board.selectedGem).getCol());
        //if(evt != null) return;
        if(!GameView.GLRenderer._boardReady || row >= board.entities.length || col >= board.entities[0].length || row < 0 || col < 0) return;
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
        /****** Test *****/
        entity.color[0] = 1.0f;
        entity.color[1] = 1.0f;
        entity.color[2] = 1.0f;
        /**** End Test ***/
        ((Gem)board.selectedGem).moveTo = new Point();
        ((Gem)board.selectedGem).moveTo.y = row;
        ((Gem)board.selectedGem).moveTo.x = col;
        entity.moveTo = new Point();
        entity.moveTo.y = ((Gem)board.selectedGem).getRow();
        entity.moveTo.x = ((Gem)board.selectedGem).getCol();
        board.clearChains = true;
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
        public BackGround backGround;
        public UI.Title title;
        public static volatile boolean _boardReady = false;
        public static volatile boolean updateMarioTexture = false;
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
            //font.doScale = true;
        }

        private void setUpEnvironment(int width, int height){
            GameView.is16x9 = metrics.heightPixels / (float)metrics.widthPixels >= 1.6f;
            //scale = Math.round(metrics.widthPixels / 360f);
            scale = (metrics.widthPixels / 360f);
            GameView.defaultSide = GameView.is16x9 ? 42 : (metrics.widthPixels < 800 ? 46 : 48);
            GameView.defaultSide = (metrics.widthPixels <= 600 ? 36 : GameView.defaultSide);
            GameView.defaultSide = (metrics.widthPixels <= 540 ? 32 : GameView.defaultSide);
            GameView.defaultSide = (metrics.widthPixels <= 480 ? 58 : GameView.defaultSide);
            if(metrics.widthPixels <= 600) UI.fontScaleBig = 6.0f;
            GameView.scaledDefaultSide = GameView.defaultSide * (int)scale;
            Shape.setPixelWidth(1f / width);
            Shape.setPixelHeight(1f / height);
            GameView.screenW = width;
            GameView.screenH = height;
        }
        private boolean testProgram = false;
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
            if(board == null) {
                setUpEnvironment(width, height);
                /********************/
                if(!testProgram) {
                    title = new UI.Title();
                    title.setX(width / 2);
                    title.setY(height / 2);
                    UI.addControl(title);

                    try {
                        board = new Board(context);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    font = new UI.Font(R.drawable.oldskol, 2.0f);
                    font.setText("Hey you!");
                    font.setX(32);
                    font.setY(32);

                    backGround = new BackGround();
                    backGround.setX(width / 2f);
                    backGround.setY(height / 2f);
                    backGround.setWidth(width);
                    backGround.setHeight(height);
                    backGround.setOffSetX(0);
                    backGround.setOffSetY(0);
                    backGround.doScale = false;
                    backGround.rotate = false;
                    int randint = 14;
                    if (GameView.cycleBG) {
                        Random r = new Random();
                        randint = Math.abs(r.nextInt()) % backGround.programs.size();
                    }
                    backGround.index = randint;

                    /******* end onSurfaceChanged GL2 **********/
                    w = metrics.widthPixels;//600
                    h = height * w / width;
                    screenH = height;
                    screenW = width;
                } else {
                    UI.LevelSelect levelSelect = new UI.LevelSelect(Stage.getStageList(loadJSONFromAsset("levels.json")), board);
                    UI.addControl(levelSelect);
                }
            }
        }

        public String loadJSONFromAsset(String fileName) {
            String json;
            try {
                InputStream is = context.getAssets().open(fileName);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, "UTF-8");
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
            return json;
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            Timer.update();
            /********** onDrawFrame GL2 ****************************/
            // Redraw background color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            if(!testProgram) {
                backGround.draw();
                /********** end onDrawFrame GL2 ****************************/
                board.update();
                if (GameView.GLRenderer._boardReady) {
                    if (updateMarioTexture) {
                        updateTexture(R.drawable.mario_tiles_46, Mario.marioTexture);
                        updateMarioTexture = false;
                    }
                    if (board.gemCol != null) {
                        board.draw();
                        if (!GameView.disableTouch) board.parseBoard();
                    }
                }

                if (GameView.showFPS) {
                    changeProgram(font.mProgram.getProgramId(), font.vertexBuffer);
                    font.setText(String.valueOf(Timer.fps));
                    font.draw();
                }
            }

            UI.update();
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

        public void updateTexture(Integer textureId, Bitmap bmp){
            Shape.bindTexture(textureId);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
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

        public static void updateVertexBuffer(FloatBuffer vertexBuffer){
//            curProgram = program;
//            // Add program to OpenGL ES environment
//            GLES20.glUseProgram(curProgram);

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
