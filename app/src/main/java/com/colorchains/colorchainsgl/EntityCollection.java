package com.colorchains.colorchainsgl;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by francisco.cnmarao on 19/09/2017.
 */

public class EntityCollection extends Shape{
    static float fontCoords[] = {
            -0.5f,  0.5f, 0.0f,  // top left      0
            -0.5f, -0.5f, 0.0f,  // bottom left   1
            0.5f, -0.5f, 0.0f,  // bottom right  2
            0.5f,  0.5f, 0.0f }; // top right     3
    public static short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices
    //public boolean checkMario = false;
    public float[] transformationMatrix = new float[16];
    public static final String vs_Gem =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec4 a_Color;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec4 v_Color;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  v_texCoord = a_texCoord;" +
                    "  v_Color = a_Color;" +
                    "}";
    public static final String fs_Gem =
            "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform vec4 color;" +
                    "uniform vec2 resolution;" +
                    "uniform float time;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, v_texCoord ) * vec4(color.r,color.g,color.b,color.a);\n" +
                    "}";
    //Board board;
    Entity[][] entities;
    Integer rowCount = 0;
    Integer colCount = 0;
    public EntityCollection(Integer resourceId, Entity[][] entities/*Board board*/, Integer rowCount, Integer colCount) {
        super(fontCoords, drawOrder, vs_Gem/*vs_Text*/, fs_Gem/*fs_Text*/, resourceId/*, GLES20.GL_LINEAR*/);
        this.entities = entities;
        this.rowCount = rowCount;
        this.colCount = colCount;
    }

    @Override
    public void buildTextureMap(Integer totalItems, Integer width, Integer height){
//        entityWidthGl = 1f / width;// colCount;
//        entityHeightGl = 1f / height;// rowCount;
//        uvMap = new float[totalItems][8];
//        for (int i = 0; i < totalItems; i++) {
//            int hPos = i;
//            float u = (hPos * entityWidthGl) % (entityWidthGl * width);
//            float v = (((int)Math.floor((hPos * entityWidthGl) / (entityWidthGl * width)) * entityHeightGl));
//            uvMap[i][0] = u;
//            uvMap[i][1] = v + entityHeightGl;
//            uvMap[i][2] = u;
//            uvMap[i][3] = v;
//            uvMap[i][4] = u + entityWidthGl;
//            uvMap[i][5] = v;
//            uvMap[i][6] = u + entityWidthGl;
//            uvMap[i][7] = v + entityHeightGl;
//        }
        super.buildTextureMap(totalItems,width,height);
        setBuffers();
    }

    public FloatBuffer vertexBuffer;
    @Override
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

    public boolean setBuffers() {
        try {
            //E[][] gems = board.entities;
            float[] uvData = new float[entities.length * entities[0].length * 8];
            short[] drawOrder = {0, 1, 2, 0, 2, 3};
            short[] drawOrderFinal = new short[entities.length * entities[0].length * 6];
            float[] vertexData = new float[entities.length * entities[0].length * 12];
            for (int i = 0; i < (entities.length * entities[0].length); i++) {
                int row = (int)Math.floor(i / rowCount), col = i % rowCount;
                Entity entity = entities[row][col];
                if(entity == null) continue;//{entity = new UI.Button("btn","",0f,0f,0,0); }
                float posX = (entity.getX() / entity.getWidth()), posY = (entity.getY() / entity.getHeight());
                Integer uvIndex;
                uvIndex = getUVIndex(entity);//Get current frame animation
                if (uvIndex == null) continue;

                for (int j = 0; j < 8; j++) {
                    uvData[(i * 8) + j] = uvMap[uvIndex][j];
                }
                for (int j = 0; j < 6; j++) {
                    drawOrderFinal[(i * 6) + j] = (short) (drawOrder[j] + (i * 4));
                }

                /******************** test transformations **************************/
                if(entity.rotate || entity.doScale) {
                    vertexData[(i * 12) + 0] = -0.5f;   //x
                    vertexData[(i * 12) + 1] = 0.5f;  //y
                    vertexData[(i * 12) + 3] = -0.5f;   //x
                    vertexData[(i * 12) + 4] = -0.5f;  //y
                    vertexData[(i * 12) + 6] = 0.5f;   //x
                    vertexData[(i * 12) + 7] = -0.5f;  //y
                    vertexData[(i * 12) + 9] = 0.5f;   //x
                    vertexData[(i * 12) + 10] = 0.5f;  //y
                    /*** roatation ***/
                    if(entity.rotate) {
                        long time = SystemClock.uptimeMillis() % 16000L;
                        entity.angle = 0.72f * ((int) time) * this.angularSpeed;
                    }
                    /*** scale ***/
//                    if(gem.doScale) {
//                        if (gem.scale <= gem.minScale || gem.scale >= gem.maxScale)
//                            gem.scaleStep = gem.scaleStep * -1;
//                        gem.scale += gem.scaleStep;
//                    }
                    transformationMatrix = Shape.doTransformations(0, 0, entity.scale, entity.scale, entity.angle, 1);
                    float[] result = new float[4];
                    for (int j = 0; j < 12; j += 3) {
                        float[] data = new float[]{vertexData[(i * 12) + j + 0],
                                vertexData[(i * 12) + j + 1],
                                0/*vertexData[(i * 12) + j + 2]*/,
                                1};
                        Matrix.multiplyMV(result, 0, transformationMatrix, 0, data, 0);
                        vertexData[(i * 12) + j + 0] = result[0] + posX;
                        vertexData[(i * 12) + j + 1] = result[1] + posY;
                    }
                } else {
                    vertexData[(i * 12) + 0] = -0.5f + posX;  //x
                    vertexData[(i * 12) + 1] = 0.5f + posY;   //y
                    vertexData[(i * 12) + 3] = -0.5f + posX;  //x
                    vertexData[(i * 12) + 4] = -0.5f + posY;  //y
                    vertexData[(i * 12) + 6] = 0.5f + posX;   //x
                    vertexData[(i * 12) + 7] = -0.5f + posY;  //y
                    vertexData[(i * 12) + 9] = 0.5f + posX;   //x
                    vertexData[(i * 12) + 10] = 0.5f + posY;  //y
                }
                /******************** test transformations **************************/
            }
            setVertexBuffer(vertexData);
            setUVBuffer(uvData);
            setDrawListBuffer(drawOrderFinal);
        } catch (Exception e) {
            e.printStackTrace();
            if(e.getClass().equals(java.lang.NullPointerException.class) && count < 10){
                count++;
                setBuffers();
            }
        }

        return true;
    }

    @Nullable
    public Integer getUVIndex(Entity entity) {
        Integer uvIndex = 0;
        if(entity.curAnimation != null) uvIndex = entity.curAnimation.curFrame;
        return uvIndex;
    }

    private Integer count = 0;

    public void draw() {
        count = 0;
        setBuffers();
        GameView.GLRenderer.changeProgram(mProgram.getProgramId(), vertexBuffer);
        super.draw();
    }
}
