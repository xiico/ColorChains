package com.colorchains.colorchainsgl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by francisco.cnmarao on 17/04/2017.
 */

public class UI {

    private static boolean _modal = false;
    private static Context _context;
    private static Control mainContainer;
    public static void init(Context context) {
        _context = context;
        mainContainer = new Control("mainContainer", TYPE.CONTAINER, 0f, 0f, GameView.metrics.widthPixels, GameView.metrics.heightPixels);
        /*
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bmp = BitmapFactory.decodeResource(_context.getResources(), R.drawable.button);
        Render.cache(TYPE.BUTTON, bmp);

        Bitmap ib = BitmapFactory.decodeResource(_context.getResources(), R.drawable.infobox, options);
        Render.cache(TYPE.INFOBOX, ib);

        Control.Button.defaultHeight = bmp.getHeight();
        Control.Button.defaultWidth = bmp.getWidth() / 2;*/
    }
    public static boolean isInModalMode(){
        return _modal;
    }

    public static void addControl(Control ctrl){
        mainContainer.controls.add(ctrl);
    }

    public static void update() {
    }

    public static Control findControlById(String id){
        Control result = null;
        for (Control ctrl: mainContainer.controls) if(ctrl.id.equals(id)) result = ctrl;
        return  result;
    }

    public static void draw(GL10 gl) {
        for (Control ctrl: mainContainer.controls ) {
            if(ctrl.visible) ctrl.draw(gl);
        }
    }

    public static void show(){
        for (Control ent: mainContainer.controls ) {
            ent.visible = true;
        }
    }

    public static void hide(){
        for (Control ent: mainContainer.controls ) {
            ent.visible = false;
        }
    }

    public static void checkUITouch(MotionEvent evt) {
        checkTouch(mainContainer,evt);
    }

    private static void checkTouch(Control ctrl, MotionEvent evt)    {
        if (!ctrl.visible) return;
        if(ctrl.controls.size()  == 0) {
            float rawX = evt.getRawX();
            float rawY = evt.getRawY();
            RectF ctrlRect = new RectF(ctrl.x, ctrl.y, ctrl.x + ctrl.cacheWidth, ctrl.y + ctrl.cacheHeight);
            RectF touchRect = new RectF(rawX, rawY, rawX + 1, rawY + 1);
            if (touchRect.intersect(ctrlRect) && ctrl.visible) {
                ((Control.UIListener) ctrl).clicked(ctrl);
            }
        } else {
            for (int index = 0; index < ctrl.controls.size(); index++) {
                Control _ctrl = ctrl.controls.get(index);
                checkTouch(_ctrl, evt);
            }
        }
    }

    static class Control extends Entity  {
        public boolean visible = true;
        public List<Control> controls = new ArrayList<>();
        public Control(String id, TYPE type, Float x, Float y, Integer cx, Integer cy) {
            super(id, type, x, y, cx, cy);
        }

        public static interface UIListener{
            void clicked(Object args);
        }

        static class Button extends Control implements UIListener {
            private List<UI.Control.UIListener> listeners = new ArrayList<>();
            private final String text;
            public static Integer defaultWidth = 76;
            public static Integer defaultHeight = 38;
            private Font font;
            public Button(String id, String text, Float x, Float y, Integer cx, Integer cy) {
                super(id, TYPE.BUTTON, x, y, cx, cy);
                cacheWidth = defaultWidth;
                cacheHeight = defaultHeight;
                this.width = cacheWidth;
                this.height = cacheHeight;
                this.text = text;
                loadTexture(R.drawable.button);
                font = new Font(R.drawable.oldskol, 2.0f);
                font.text = text;
                font.x = this.x + (cacheWidth / 2);
                font.y = this.y + (cacheHeight / 2);
            }
            @Override
            public void draw(GL10 gl)
            {
                super.draw(gl);
                font.draw();
            }

            public void clicked(Object args) {
                for (UIListener listener: listeners) {
                    listener.clicked(this);
                }
            }

            // Assign the listener implementing events interface that will receive the events
            public void addUIListener(UI.Control.UIListener listener) {
                this.listeners.add(listener);
            }
            // Assign the listener implementing events interface that will receive the events
            public void removeUIListener(UI.Control.UIListener listener) {
                this.listeners.remove(listener);
            }
        }

        static class ProgressBar extends Control {
            private float _value = 0f;
            //Background
            public int bgColor = Color.parseColor("#000000");
            //Foreground
            public int fgColor= Color.parseColor("#FFFFFF");
            public float width = 50;
            public float height = 10;
            public float border = 2;
            public ProgressBar(String id,  Float x, Float y, float width, float height, int bgColor, int fgColor) {
                super(id, TYPE.PROGRESSBAR, x, y, 0, 0);
                this.bgColor = bgColor;
                this.fgColor = fgColor;
            }

            public ProgressBar(){
                super("pb", TYPE.PROGRESSBAR, 0f, 0f, 50, 10);
            }
            public void setValue(float value){
                _value = value;
            }

            public void draw(GL10 gl) {
                /*int color = Render.paint.getColor();
                Render.paint.setColor(bgColor);
                Render.canvas.drawRect(x, y, x + width, y + height, Render.paint);
                Render.paint.setColor(fgColor);
                Render.canvas.drawRect(x + (border * (int)GameView.scale), y + (border * (int)GameView.scale), (x + (border * (int)GameView.scale)) + (width * Math.min(_value, 1)) - (border * (int)GameView.scale * 2), (y + (border * (int)GameView.scale)) + height - (border * (int)GameView.scale * 2), Render.paint);
                Render.paint.setColor(color);*/
            }
        }

        static class Confirm extends Control implements UIListener{
            private List<UI.Control.UIListener> listeners = new ArrayList<>();
            private final String text;
            //Background
            public int bgColor = Color.parseColor("#FFFFFF");
            //Foreground
            public int fgColor= Color.parseColor("#000000");
            private Button okButton;
            private Button cancelButton;
            public float border = 2;
            private Font font;

            public Confirm(String id, String text, Float x, Float y) {
                super(id, TYPE.CONFIRM, x, y, GameView.scaledDefaultSide * 3, GameView.scaledDefaultSide * 2);
                this.width = GameView.scaledDefaultSide * 3;
                this.height = GameView.scaledDefaultSide * 2;
                this.text = text;
                this.width = GameView.metrics.widthPixels - (GameView.scaledDefaultSide * 2);
                okButton = new Button("okButton","OK", this.x + (this.width / 2f) - (Button.defaultWidth * 1.5f), this.y + (this.height * .4f), 0, 0);
                okButton.addUIListener(this);
                cancelButton = new Button("cancelButton","Cancel", this.x + (this.width / 2f) +  (Button.defaultWidth * .5f), this.y + (this.height * .4f), 0, 0);
                cancelButton.addUIListener(this);
                this.controls.add(okButton);
                this.controls.add(cancelButton);
                this.font = new Font(R.drawable.oldskol, 2.0f);
            }

            // Assign the listener implementing events interface that will receive the events
            public void addUIListener(UI.Control.UIListener listener) {
                this.listeners.add(listener);
            }

            @Override
            public void clicked(Object args) {
                for (UIListener listener: listeners) {
                    listener.clicked(((Button)args).text);
                }
            }

            @Override
            public void draw(GL10 gl)
            {
                /*int color = Render.paint.getColor();
                Render.paint.setColor(bgColor);
                Render.canvas.drawRect(x, y, x + width, y + height, Render.paint);
                Render.paint.setColor(fgColor);
                Render.canvas.drawRect(x + (border * (int)GameView.scale), y + (border * (int)GameView.scale), (x + (border * (int)GameView.scale)) + (width) - (border * (int)GameView.scale * 2), (y + (border * (int)GameView.scale)) + height - (border * (int)GameView.scale * 2), Render.paint);
                Render.paint.setColor(color);
                okButton.draw(true);
                cancelButton.draw(true);
                String lines[] = this.text.split("\\r?\\n");
                for (int i = 0; i < lines.length; i++) {
                    font.x = this.x + (width / 2);
                    font.y = this.y + (height * .2f) + (i * font.fontHeight);
                    font.text = lines[i];
                    font.draw(gl);
                }*/
            }
        }
    }

    public static class InfoBox extends Control{
        public Integer width = 600;
        public Integer height = 200;
        private Integer score = 0;
        private Integer targetScore = 0;
        private Integer puzzleScore = 0;
        private Font font;
        private Font fontBig;
        public InfoBox(String id, Float x, Float y) {
            super(id, TYPE.INFOBOX, x, y, 0, 0);
            font = new Font(R.drawable.oldskol, 2.0f);
            fontBig = new Font(R.drawable.oldskol, 2.0f);
            loadTexture(R.drawable.infobox);
        }

        public void setScore(Integer score){
            this.score = score;
        }

        public void setTargetScore(Integer score){
            this.targetScore = score;
        }

        public void setPuzzleScore(Integer score){
            this.puzzleScore = score;
        }

        @Override
        public void draw(GL10 gl)
        {
            super.draw(gl);
            fontBig.text = score.toString();
            fontBig.x = this.x + 224;
            fontBig.y = this.y + 64;
            fontBig.draw();
            fontBig.text = targetScore.toString();
            fontBig.x = this.x + 224;
            fontBig.y = this.y + 116;
            fontBig.draw();
            fontBig.text = puzzleScore.toString();
            fontBig.x = this.x + 224;
            fontBig.y = this.y + 168;
            fontBig.draw();
        }
    }

    public static class Image extends Control{
        public Bitmap image;
        public Image(String id, Bitmap bmp) {
            super(id, TYPE.IMAGE, 0f, 0f, 0, 0);
            this.image = bmp;
        }

        @Override
        public void draw(GL10 gl)
        {
            /*Render.draw(this.image, Math.round(this.x), Math.round(this.y));*/
        }
    }
    public static class Font extends Shape{
        String fontImagePath;
        int fontHeight = 40;
        int fontWidth = 20;
        float fontWidthGl, fontHeightGl;
        int startChar = 32;
        int[] charCodes = new int[96];
        float size = 2f;
        int defaultSize = 8;
        FontSize fontSize = FontSize.Normal;
        float r = 1;
        HorizontalAlignment hAlign = HorizontalAlignment.LEFT;
        VerticalAlignment vAlign = VerticalAlignment.BOTTOM;
        float valgn = 0;
        float halgn = 0;
        private String text = "";
        static float fontCoords[] = {
                -0.5f,  0.5f, 0.0f,  // top left      0
                -0.5f, -0.5f, 0.0f,  // bottom left   1
                0.5f, -0.5f, 0.0f,  // bottom right  2
                0.5f,  0.5f, 0.0f }; // top right     3
        private static short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices
        private static float[][] uvMap;//textureMap
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
                        "uniform vec4 color;" +
                        "uniform vec2 resolution;" +
                        "uniform float time;" +
                        "uniform sampler2D s_texture;" +
                        "void main() {" +
                        "  gl_FragColor = texture2D( s_texture, v_texCoord ) * vec4(color.r,color.g,color.b,color.a);\n" +
                        "}";

        public Font(Integer resourceId, float scale) {
            super(fontCoords,drawOrder,  vs_Image/*vs_Text*/ , fs_Image/*fs_Text*/, resourceId, GLES20.GL_NEAREST);
            fontWidth = Math.round(width / 10)/* *Math.round(scale)*/;
            fontHeight = Math.round(height / 10)/* *Math.round(scale)*/;
            fontWidthGl = (1 / 10f)/* *scale*/;
            fontHeightGl = (1 / 10f)/* *scale*/;
            this.scale = scale;
            width = fontWidth*scale;
            height = fontHeight*scale;
            offSetX = 0;
            offSetY = 0;
            if(uvMap == null) buildTextureMap();
        }
        public void buildTextureMap(){
            uvMap = new float[96][8];
            for (int i = 0; i < (96); i++) {
                int code = i;
                float u = (code * fontWidthGl) % (fontWidthGl * 10);
                float v = (((int)Math.floor((code * fontWidthGl) / (fontWidthGl * 10)) * fontHeightGl));
                uvMap[i][0] = u;
                uvMap[i][1] = v +  fontHeightGl;
                uvMap[i][2] = u;
                uvMap[i][3] = v;
                uvMap[i][4] = u +  fontWidthGl;
                uvMap[i][5] = v;
                uvMap[i][6] = u +  fontWidthGl;
                uvMap[i][7] = v +  fontHeightGl;
            }
        }
        public float getX() {
            return x;
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

        public void setText(String text){
            this.text = text;
            int textLength = text.replace("\n","").length();
            float[] uvData = new float[textLength*8];
            short[] drawOrder = {0, 1, 2, 0, 2, 3};
            short[] drawOrderFinal = new short[textLength*6];
            float[] vertexData = new float[textLength*12];

            String lines[] = this.text.split("\\r?\\n");
            Integer lastVerIndex = 0;
            Integer lastUVIndex = 0;
            Integer lastDOFIndex = 0;
            Integer lastDOIndex = 0;
            for (int l = 0; l < lines.length; l++) {
                /*
                font.x = this.x + (width / 2);
                font.y = this.y + (height * .2f) + (i * font.fontHeight);
                font.text = lines[i];
                font.draw(gl);*/
                String lineText = lines[l];
                _getCharCodes(lineText);
                if(hAlign == HorizontalAlignment.CENTER) halgn = -(lineText.length() / 2f);
                else if(hAlign == HorizontalAlignment.RIGHT) halgn = -(lineText.length());
                if(vAlign == VerticalAlignment.TOP) valgn = (1 / 2f);
                else if(vAlign == VerticalAlignment.BOTTOM) valgn = -(1/2f);

                for (int i = 0; i < charCodes.length; i++) {
//                texture.setMapping(textureMap[charCodes[i]]);
//                texture.draw(gl, getX() + (i * (fontWidth)) + halgn, getY() + valgn, fontWidth * r, fontHeight * r, 0, true);
                    for (int j = 0; j < 8; j++) {
                        uvData[(lastUVIndex)+(i*8)+j] = uvMap[Math.min(charCodes[i], 95)][j];
                    }
                    for (int j = 0; j < 6; j++) {
                        drawOrderFinal[(lastDOFIndex)+(i*6)+j] = (short) (drawOrder[j]+(((lastDOIndex+i)*4)));
                    }
                    vertexData[(lastVerIndex) + (i*12)+0]  = -0.5f + i + (halgn);  //x
                    vertexData[(lastVerIndex) + (i*12)+1]  =  0.5f + (valgn) + l;  //y
                    vertexData[(lastVerIndex) + (i*12)+2]  =  0.0f;  //z
                    vertexData[(lastVerIndex) + (i*12)+3]  = -0.5f + i + (halgn); //x
                    vertexData[(lastVerIndex) + (i*12)+4]  = -0.5f + (valgn) + l;  //y
                    vertexData[(lastVerIndex) + (i*12)+5]  =  0.0f;  //z
                    vertexData[(lastVerIndex) + (i*12)+6]  =  0.5f + i + (halgn);  //x
                    vertexData[(lastVerIndex) + (i*12)+7]  = -0.5f + (valgn) + l;  //y
                    vertexData[(lastVerIndex) + (i*12)+8]  =  0.0f;  //z
                    vertexData[(lastVerIndex) + (i*12)+9]  =  0.5f + i + (halgn);  //x
                    vertexData[(lastVerIndex) + (i*12)+10] =  0.5f + (valgn) + l;  //y
                    vertexData[(lastVerIndex) + (i*12)+11] =  0.0f;  //z
                }
                lastVerIndex += lineText.length()*12;
                lastUVIndex += lineText.length()*8;
                lastDOFIndex += lineText.length()*6;
                lastDOIndex += lineText.length();
                //if(l == 2) break;
            }

            setVertexBuffer(vertexData);
            setUVBuffer(uvData);
            setDrawListBuffer(drawOrderFinal);
        }
        public float getY() {
            return y;
        }

        private void _getCharCodes(String text){
            charCodes = new int[text.length()];
            for (int i = 0; i < text.length(); i++) {
                charCodes[i] = (((int)text.charAt(i)) - startChar);
            }
        }
        public void draw() {
            super.draw();
        }

        public enum FontSize{
            Normal,
            Large
        }

        public enum HorizontalAlignment
        {
            LEFT,
            CENTER,
            RIGHT
        }
        public enum VerticalAlignment
        {
            TOP,
            MIDDLE,
            BOTTOM
        }
    }
}
