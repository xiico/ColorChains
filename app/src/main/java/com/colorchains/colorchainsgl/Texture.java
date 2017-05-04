package com.colorchains.colorchainsgl;

import android.opengl.GLES10;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by francisco.cnmarao on 18/04/2017.
 */

public class Texture {
    public boolean mipmaps = false;
    int textureId;
    int width;
    int height;

    float textureMap[] = {
            0, 0,
            1, 0,
            0, 1,
            1, 1,
    };
    final ByteBuffer ibb = ByteBuffer.allocateDirect(textureMap.length * 4);
    private FloatBuffer textureBuffer = null;
    public Texture(){
        ibb.order(ByteOrder.nativeOrder());
        textureBuffer = ibb.asFloatBuffer();
    }
    public final void destroy() {
        GLES10.glDeleteTextures(1, new int[] {textureId}, 0);
        textureId = -1;
    }

    public void buildMapping() {
        textureBuffer.put(textureMap);
        textureBuffer.position(0);
    }
    public void setMapping(float[] textureMap) {
        textureBuffer.put(textureMap);
        textureBuffer.position(0);
    }

    public final boolean isLoaded() {
        return textureId >= 0;
    }

    public final void prepare(GL10 gl) {
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
    }

    public final void draw(GL10 gl, float x, float y, float w, float h, float rot, boolean prepare) {
        if(prepare) prepare(gl);
        gl.glPushMatrix();
        gl.glTranslatef(x, y, 0);
        gl.glRotatef(rot, 0, 0, 1);
        gl.glScalef(w, h, 0);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
        gl.glPopMatrix();
    }
}
