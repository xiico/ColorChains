package com.colorchains.colorchainsgl;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by francisco.cnmarao on 19/09/2017.
 */

public class GemCollection extends Shape{
    float defaultWidth = 10, defaultHeight = 10;//92
    public List<Gem> gems = new ArrayList<>();
    float gemWidthGl, gemHeightGl;
//    float valgn = 0;
//    float halgn = 0;
    private String text = "";
    static float fontCoords[] = {
            -0.5f,  0.5f, 0.0f,  // top left      0
            -0.5f, -0.5f, 0.0f,  // bottom left   1
            0.5f, -0.5f, 0.0f,  // bottom right  2
            0.5f,  0.5f, 0.0f }; // top right     3
    private static short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices
    private static float[][] uvMap;//textureMap
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

    public GemCollection(Integer resourceId) {
        super(fontCoords,drawOrder,  vs_Gem/*vs_Text*/ , fs_Gem/*fs_Text*/, resourceId, GLES20.GL_LINEAR);
        gemWidthGl = (1 / 10f)/* *scale*/;//(1 / 10f)
        gemHeightGl = (1 / 8f)/* *scale*/;//(1 / 8f)
//        width = 92;//92
//        height = 92;//92
    }
    public void buildTextureMap(){
        uvMap = new float[80][8];
        for (int i = 0; i < 80; i++) {
            int hPos = i;
//            float u = 0;//(hPos * defaultWidth) % (defaultWidth * 8);
//            float v = (hPos * fontHeightGl);
            float u = (hPos * gemWidthGl) % (gemWidthGl * 10);
            float v = (((int)Math.floor((hPos * gemWidthGl) / (gemWidthGl * 10)) * gemHeightGl));
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

    public void setBuffers() {
        float[] uvData = new float[gems.size() * 8];
        short[] drawOrder = {0, 1, 2, 0, 2, 3};
        short[] drawOrderFinal = new short[gems.size() * 6];
        float[] vertexData = new float[gems.size() * 12];
        for (int i = 0; i < gems.size(); i++) {
            Gem gem = gems.get(i);
            for (int j = 0; j < 8; j++) {
                uvData[(i * 8) + j] = uvMap[gem.getGemType().getValue() + gem.curAnimation.curFrame.intValue()][j];
            }
            for (int j = 0; j < 6; j++) {
                drawOrderFinal[(i * 6) + j] = (short) (drawOrder[j] + (((i) * 4)));
            }
            vertexData[(i * 12) + 0] = -0.5f + (gem.getX() / gem.getWidth());   //x
            vertexData[(i * 12) + 1] =  0.5f + (gem.getY() / gem.getHeight());  //y
            vertexData[(i * 12) + 2] = 0.0f;  //z
            vertexData[(i * 12) + 3] = -0.5f + (gem.getX() / gem.getWidth());   //x
            vertexData[(i * 12) + 4] = -0.5f + (gem.getY() / gem.getHeight());  //y
            vertexData[(i * 12) + 5] = 0.0f;  //z
            vertexData[(i * 12) + 6] =  0.5f + (gem.getX() / gem.getWidth());   //x
            vertexData[(i * 12) + 7] = -0.5f + (gem.getY() / gem.getHeight());  //y
            vertexData[(i * 12) + 8] = 0.0f;  //z
            vertexData[(i * 12) + 9] =  0.5f + (gem.getX() / gem.getWidth());   //x
            vertexData[(i * 12) + 10] = 0.5f + (gem.getY() / gem.getHeight());  //y
            vertexData[(i * 12) + 11] = 0.0f;  //z
        }
        setVertexBuffer(vertexData);
        setUVBuffer(uvData);
        setDrawListBuffer(drawOrderFinal);
    }

    public void update(){
        for (Gem gem: gems ) {
            gem.update();
        }
        setBuffers();
    }

    public void draw() {
        super.draw();
    }
}
