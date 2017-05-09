package com.colorchains.colorchainsgl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by franc on 16/04/2017.
 */

public class Shape implements Comparable<Shape> {
    // Use to access and set the view transformation
    private int mMVPMatrixHandle;
    public int mProgram;
    public static FloatBuffer vertexBuffer;
    // number of coordinates per vertex in this array
    public static final int COORDS_PER_VERTEX = 3;
    float[] coords;
    // Set color with red, green, blue and alpha (opacity) values
    float color[] = {1.0f, 1.0f, 1.0f, 1.0f};
    private int vertexCount;
    private short[] drawOrder;
    public float width,height = 0;
    public float scale = 1;
    float stepScale = 0.05f;
    public float x = 0;
    public float y = 0;
    public static HashMap<Integer,TextureInfo> textures = new HashMap<>();
    public float angularSpeed = 0.25f;
    public float minScale;
    public float maxScale;
    public float scaleStep = 0.025f;
    public float offSetX = (92/2) + 16;
    public float offSetY = 92*3;//(92/2) + 16;
    public boolean doScale = false;
    public boolean rotate = false;
    public String id;
    public List<Integer> programs = new ArrayList<>();

    public Shape(float[] coords, short[] drawOrder , String vertexShaderCode, String fragmentShaderCode, Integer resourceId, Integer filtering){
        mProgram = setupImage(resourceId, vertexShaderCode, fragmentShaderCode, filtering);
        init(coords, drawOrder);
    }

    public Shape(float[] coords, short[] drawOrder , String vertexShaderCode, String fragmentShaderCode, Integer resourceId){
        if(resourceId != -1) {
            mProgram = setupImage(resourceId, vertexShaderCode, fragmentShaderCode, GLES20.GL_LINEAR /*GLES20.GL_NEAREST*/);
        }
        init(coords, drawOrder);
    }

    private void init(float[] coords, short[] drawOrder ){
        this.coords = coords;
        vertexCount = coords.length / COORDS_PER_VERTEX;

        setVertexBuffer(coords);

        setDrawListBuffer(drawOrder);

        this.id = resourceId + "_" + this.hashCode();
    }

    public void setVertexBuffer(float[] coords){
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                coords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(coords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);
    }

    public void setDrawListBuffer(short[] drawOrder){
        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
        this.drawOrder = drawOrder;
    }

    public static Integer createProgram(String vertexShaderCode, String fragmentShaderCode, Integer resourceId){

        int vertexShader = GameView.GLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = GameView.GLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        int program = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(program, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(program, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(program);

        if(resourceId > 0) {
            textures.get(resourceId).program = program;
            //programs.add(program);
        }

        return program;
    }

    private int mPositionHandle;
    public static final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    //private int mColorHandle;
    public final float[] mMVPMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];
    private float colour1 = 0;
    private float colour2 = 0;
    private float colour3 = 0;
    private float time = 0;
    float[] mvpMatrix;
    public void draw() { // pass in the calculated transformation matrix
    //public void draw() { // pass in the calculated transformation matrix
        if(GameView.GLRenderer.curProgram != this.mProgram) GameView.GLRenderer.changeProgram(this.mProgram, Shape.vertexBuffer);
        if(GameView.GLRenderer.curTexture != this.getResourceId()) Shape.bindTexture(this.getResourceId());
        mvpMatrix = this.doTransformations();
        /**************old was inside shape *********/

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        /*********** Texture Coordinates *********/
        Shape.setUpUVBuffer(uvBuffer, mProgram);
        /*********** end Texture Coordinates *********/

        if(this.time > 2*Math.PI) this.time = 0;
        this.time += 0.01666/2;


        /************ effect *************/
        if(getResourceId() == R.drawable.bg_color) {
            /********** set colors *************/
            colour1 += (0.01666f / 8);
            if(colour1 > 1) colour1 = 0;
            colour2 += (0.01666f / 16);
            if(colour2 > 1) colour2 = 0;
            colour3 += (0.01666f / 32);
            if(colour3 > 1) colour3 = 0;

            int color =  GLES20.glGetUniformLocation(mProgram, "color");
            GLES20.glUniform4f(color, colour1, colour2, colour3, 1);
            int resolution = GLES20.glGetUniformLocation(mProgram, "resolution");
            GLES20.glUniform2f(resolution, this.width, this.height);
            int time = GLES20.glGetUniformLocation(mProgram, "time");
            GLES20.glUniform1f(time, this.time);
            /********** teste *************/

        } else if (getResourceId() == R.drawable.oldskol){
            int color =  GLES20.glGetUniformLocation(mProgram, "color");
            GLES20.glUniform4f(color, this.color[0], this.color[1], this.color[2], this.color[3]);
        }
        /************ effect **********/

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    float[] scratch = new float[16];
    public float[] doTransformations(){

        /***************** Start move to Shape *******************/
        // Calculate the projection and view transformation
        Matrix.multiplyMM(this.mMVPMatrix, 0, GameView.GLRenderer.mProjectionMatrix, 0, GameView.GLRenderer.mViewMatrix, 0);

        // Calculate translation and scale
        Matrix.translateM(this.mMVPMatrix, 0, x + offSetX, y + offSetY,0);
        if(doScale) {
            if (scale <= minScale || scale >= maxScale) scaleStep = scaleStep * -1;
            scale += scaleStep;
            Matrix.scaleM(this.mMVPMatrix, 0, width * scale, height * scale, 1);
        } else Matrix.scaleM(this.mMVPMatrix, 0, width, height, 1);

        // Create a rotation transformation for the triangle
        if(rotate){
            long time = SystemClock.uptimeMillis() % 4000L;
            float angle = 0.090f * ((int) time) * this.angularSpeed;
            Matrix.setRotateM(this.mRotationMatrix, 0, angle, 0, 0, -1.0f);
        } else {
            Matrix.setRotateM(mRotationMatrix, 0, 0, 0, 0, -1.0f);
        }

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, this.mMVPMatrix, 0, this.mRotationMatrix, 0);
        /***************** End move to Shape ****************************/
        return scratch;
    }


    public static float[] doTransformations(float x, float y, float scaleX, float scaleY, float angle){
        float[] scratch = new float[16];
        float[] transformation = new float[16];
        float[] mRotationMatrix = new float[16];
        Matrix.setIdentityM(transformation,0);
        /***************** Start move to Shape *******************/
        // Calculate the projection and view transformation
        //Matrix.multiplyMM(mMVPMatrix, 0, GameView.GLRenderer.mProjectionMatrix, 0, GameView.GLRenderer.mViewMatrix, 0);

//        // Calculate translation and scale
//        Matrix.translateM(mMVPMatrix, 0, x, y ,0);
//        Matrix.scaleM(mMVPMatrix, 0, width * scale, height * scale, 1);
//        Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 0, -1.0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        //Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);
        /***************** End move to Shape ****************************/
        Matrix.translateM(transformation, 0, x, y ,0);
        Matrix.scaleM(transformation, 0, scaleX, scaleY, 1);
        Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 0, -1.0f);
        //return mMVPMatrix;
//        Matrix.multiplyMM(scratch, 0, new float[]{1,0,0,0,
//                                                  0,1,0,0,
//                                                  0,0,1,0,
//                                                  2,0,0,1} , 0, mMVPMatrix, 0);
//        scratch = new float[]{1.5f,  0.0f,  0.0f,  0.0f,
//                              1.5f, -1.0f,  0.0f,  0.0f,
//                              2.5f, -1.0f,  0.0f,  0.0f,
//                              2.5f,  0.0f,  0.0f,  0.0f};
        //Matrix.translateM(mMVPMatrix, 0, 16, 0 ,0);
        Matrix.multiplyMM(scratch, 0,transformation,0,mRotationMatrix,0);
        return scratch;
    }

    public Integer resourceId = -1;
    public Integer index = -1;
    @Override
    public int compareTo(Shape p) {
        /*if(this.getResourceId() < p.getResourceId()) return -1;
        else if(this.getResourceId() == p.getResourceId()) return 0;
        else return 1;*/
        int compareQuantity = p.getResourceId();

        //ascending order
        return this.getResourceId() - compareQuantity;
    }

    public Integer getResourceId(){
        return resourceId;
    }

    public void setResourceId(Integer resourceId){
        this.resourceId = resourceId;
    }

    // Geometric variables
    public static float vertices[];
    public float uvs[];
    public ShortBuffer drawListBuffer;
    public FloatBuffer uvBuffer;
    public Integer setupImage(Integer resourceId, String vertexShaderCode, String fragmentShaderCode, Integer filtering)
    {
        // Create our UV coordinates.
        switch (resourceId){
            case R.drawable.greengem:
            case R.drawable.redgem:
            case R.drawable.orangegem:
            case R.drawable.bluegem:
                uvs = new float[] {
                        0.1f    , 0.0f    ,// top right     0|//0.0f, 1.0f,// top left      0|0.1f    , 0.0f    ,// top right     0
                        0.0f    , 0.0f    ,// top left      1|//0.0f, 0.0f,// bottom left   1|0.0f    , 0.0f    ,// top left      1
                        0.0f    , 0.33333f,// bottom left   2|//1.0f, 0.0f,// bottom right  2|0.0f    , 0.33333f,// bottom left   2
                        0.1f    , 0.33333f // bottom right  3|//1.0f, 1.0f // top right     3|0.1f    , 0.33333f // bottom right  3
                };
                break;
            default:
                uvs = new float[] {
                        0.0f    , 1.0f    ,// top left      0|//0.0f, 1.0f,// top left      0
                        0.0f    , 0.0f    ,// bottom left   1|//0.0f, 0.0f,// bottom left   1
                        1.0f    , 0.0f    ,// bottom right  2|//1.0f, 0.0f,// bottom right  2
                        1.0f    , 1.0f     // top right     3|//1.0f, 1.0f // top right     3
                };
        }

        setUVBuffer(uvs);

        if(textures.containsKey(resourceId)) {
            this.width = textures.get(resourceId).width;
            this.height = textures.get(resourceId).height;
            this.setResourceId(resourceId);
            return textures.get(resourceId).program;
        }


        // Retrieve our image from resources.
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;
        Bitmap bmp = BitmapFactory.decodeResource(GameView.context.getResources(), resourceId, opts);

        this.width = bmp.getWidth();
        this.height = bmp.getHeight();

        // Generate Textures, if more needed, alter these numbers.
        int[] textureNames = new int[1];
        GLES20.glGenTextures(1, textureNames, 0);
        textures.put(resourceId, new TextureInfo(textureNames[0], this.width, this.height));
        this.setResourceId(resourceId);

        bindTexture(resourceId);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                filtering/*GLES20.GL_LINEAR*/);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                filtering/*GLES20.GL_LINEAR*/);

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

        // We are done using the bitmap so we should recycle it.
        bmp.recycle();

        return createProgram(vertexShaderCode,fragmentShaderCode, resourceId);
    }

    public static void setUpUVBuffer(FloatBuffer uvBuffer, Integer program) {
        // Get handle to texture coordinates location
        int mTexCoordLoc = GLES20.glGetAttribLocation(program,
                "a_texCoord" );

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray ( mTexCoordLoc );

        // Prepare the texture coordinates
        GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false,
                0, uvBuffer);
    }

    public void setUVBuffer(float[] uvs)
    {
        // The texture buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);
    }

    public static void bindTexture(Integer resourceId){
        GameView.GLRenderer.curTexture = resourceId;
        if(resourceId < 0) return;
        // Bind texture to textureNames
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures.get(resourceId).textureId);
    }

    public class TextureInfo{
        public Integer textureId = 0;
        public float width = 0;
        public float height = 0;
        public Integer program = -1;

        public TextureInfo(){}
        public TextureInfo(Integer textureId, float width, float height){
            this.textureId = textureId;
            this.width = width;
            this.height = height;
        }
    }
}
