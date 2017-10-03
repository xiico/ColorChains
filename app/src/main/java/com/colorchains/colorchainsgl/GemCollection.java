package com.colorchains.colorchainsgl;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by francisco.cnmarao on 19/09/2017.
 */

public class GemCollection extends Shape{
    public Gem[][] gems;
    float gemWidthGl, gemHeightGl;
    private String text = "";
    static float fontCoords[] = {
            -0.5f,  0.5f, 0.0f,  // top left      0
            -0.5f, -0.5f, 0.0f,  // bottom left   1
            0.5f, -0.5f, 0.0f,  // bottom right  2
            0.5f,  0.5f, 0.0f }; // top right     3
    private static short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices
    private float[][] uvMap;//textureMap
    public boolean checkMario = false;
    private float[] transformationMatrix = new float[16];
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

    public GemCollection(Integer resourceId, Gem[][] gems, boolean checkMario) {
        super(fontCoords, drawOrder, vs_Gem/*vs_Text*/, fs_Gem/*fs_Text*/, resourceId/*, GLES20.GL_LINEAR*/);
        this.checkMario = checkMario;
        gemWidthGl = (1 / 10f)/* *scale*/;//(1 / 10f)
        gemHeightGl = (1 / (checkMario ? 10f : 8f))/* *scale*/;//(1 / 8f)
        if(checkMario){
            //textures.remove(Gem.getGemSprites(TYPE.MARIO));
            //if(textures.containsKey(R.drawable.mario_tiles_46)) updateTexture(R.drawable.mario_tiles_46,Mario.marioTexture);
            mProgram = setupImage(R.drawable.mario_tiles_46, vs_Gem, fs_Gem, GLES20.GL_NEAREST /*GLES20.GL_NEAREST*/);
        }
        this.setWidth(GameView.scaledDefaultSide);//92
        this.setHeight(GameView.scaledDefaultSide);//92
        this.gems = gems;
    }
    public void buildTextureMap(Integer totalItems, Integer width, Integer height){
        uvMap = new float[totalItems][8];
        for (int i = 0; i < totalItems; i++) {
            int hPos = i;
            float u = (hPos * gemWidthGl) % (gemWidthGl * width);
            float v = (((int)Math.floor((hPos * gemWidthGl) / (gemWidthGl * width)) * gemHeightGl));
            uvMap[i][0] = u;
            uvMap[i][1] = v +  gemHeightGl;
            uvMap[i][2] = u;
            uvMap[i][3] = v;
            uvMap[i][4] = u +  gemWidthGl;
            uvMap[i][5] = v;
            uvMap[i][6] = u +  gemWidthGl;
            uvMap[i][7] = v +  gemHeightGl;
        }

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
        float[] uvData = new float[gems.length * gems[0].length * 8];
        short[] drawOrder = {0, 1, 2, 0, 2, 3};
        short[] drawOrderFinal = new short[gems.length * gems[0].length * 6];
        float[] vertexData = new float[gems.length * gems[0].length * 12];
        for (int i = 0; i < (gems.length * gems[0].length); i++) {
            int row = (int)Math.floor(i / 8), col = i % 8;
            Gem gem = gems[row][col];
            //if(gem == null) return false;
            float posX = (gem.getX() / gem.getWidth()), posY = (gem.getY() / gem.getHeight());
            Integer uvIndex;
            if(gem.type == TYPE.MARIO) {
                if(!checkMario) continue;
                uvIndex = (int)(Mario.marioCache.get(((Mario) gem).tileSet) / gem.getWidth());
            } else {
                if(checkMario) continue;
                uvIndex = gem.getGemType().getValue() + gem.curAnimation.curFrame.intValue();
            }

            for (int j = 0; j < 8; j++) {
                uvData[(i * 8) + j] = uvMap[uvIndex][j];
            }
            for (int j = 0; j < 6; j++) {
                drawOrderFinal[(i * 6) + j] = (short) (drawOrder[j] + (i * 4));
            }

            /******************** test transformations **************************/
            if(gem.rotate || gem.doScale) {
                vertexData[(i * 12) + 0] = -0.5f;   //x
                vertexData[(i * 12) + 1] = 0.5f;  //y
                vertexData[(i * 12) + 3] = -0.5f;   //x
                vertexData[(i * 12) + 4] = -0.5f;  //y
                vertexData[(i * 12) + 6] = 0.5f;   //x
                vertexData[(i * 12) + 7] = -0.5f;  //y
                vertexData[(i * 12) + 9] = 0.5f;   //x
                vertexData[(i * 12) + 10] = 0.5f;  //y
                /*** roatation ***/
                long time = SystemClock.uptimeMillis() % 16000L;
                float angle = 0.72f * ((int) time) * this.angularSpeed;
                /*** roatation ***/
                if (scale <= minScale || scale >= maxScale) scaleStep = scaleStep * -1;
                scale += scaleStep;
                transformationMatrix = Shape.doTransformations(0, 0, scale, scale, angle, 1);
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
                vertexData[(i * 12) + 0] = -0.5f + posX;   //x
                vertexData[(i * 12) + 1] = 0.5f + posY;  //y
                vertexData[(i * 12) + 3] = -0.5f + posX;   //x
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

        return true;
    }

    public void draw() {
        setBuffers();
        GameView.GLRenderer.changeProgram(mProgram.getProgramId(), vertexBuffer);
        super.draw();
    }
}
