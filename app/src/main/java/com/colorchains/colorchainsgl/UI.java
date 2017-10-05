package com.colorchains.colorchainsgl;

import android.content.Context;
import android.graphics.Color;
import android.opengl.Matrix;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.os.SystemClock;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import static com.colorchains.colorchainsgl.GameView.GLRenderer.changeProgram;

/**
 * Created by francisco.cnmarao on 17/04/2017.
 */

public class UI {

    private static boolean _modal = false;
    private static Context _context;
    private static Control mainContainer;
    public static void init() {
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

    public static void draw() {
        //GameView.GLRenderer.changeProgram(mainContainer.controls.get(0).mProgram, Shape.vertexBuffer);
        for (Control ctrl: mainContainer.controls ) {
            //Shape.bindTexture(ctrl.getResourceId());
            if(ctrl.visible)
                ctrl.draw();
        }
    }

    public static void showAll(){
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
            RectF ctrlRect = new RectF(ctrl.getX() - (ctrl.getWidth() / 2), ctrl.getY() - (ctrl.getHeight() / 2) , ctrl.getX() + ctrl.cacheWidth, ctrl.getY() + ctrl.cacheHeight);
            RectF touchRect = new RectF(rawX, rawY, rawX + 1, rawY + 1);
            if (touchRect.intersect(ctrlRect) && ctrl.visible && ctrl instanceof Control.UIListener) {
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

        public interface UIListener{
            void clicked(Object args);
        }

        static class Button extends Control implements UIListener {
            private List<UI.Control.UIListener> listeners = new ArrayList<>();
            private final String text;
            private Font font;
            public Button(String id, String text, Float x, Float y, Integer cx, Integer cy) {
                super(id, TYPE.BUTTON, x, y, cx, cy);
                cacheWidth = Math.round(super.getWidth() / 2);
                cacheHeight = (int) super.getHeight();
                this.text = text;
                font = new Font(R.drawable.oldskol, 2.0f);
                font.setText(text);
                font.setX(this.getX() /*+ (cacheWidth / 2)*/);
                font.setY(this.getY() /*+ (cacheHeight / 2)*/);
            }

            @Override
            public float getWidth(){
                return  cacheWidth;
            }

            @Override
            public float getHeight(){
                return  cacheHeight;
            }

            @Override
            public void draw()
            {
                super.draw();
                changeProgram(font.mProgram.getProgramId(), font.vertexBuffer);
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
            private float tempValue = 0f;
            //Background
            public int bgColor = Color.parseColor("#000000");
            //Foreground
            public int fgColor= Color.parseColor("#FFFFFF");
            public float border = 2;
            private static String fragmentShader =
                    "precision mediump float;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "uniform vec4 color;\n" +
                    "uniform vec2 resolution;\n" +
                    "uniform sampler2D s_texture;\n" +
                    "uniform float value;\n" +
                    "void main() {\n" +
                    "  vec2 pos = gl_FragCoord.xy / resolution.xy;\n" +
                    "  vec3 bar = vec3(1. - step(value, pos.x));\n" +
                    "  vec4 tex = texture2D( s_texture, v_texCoord );\n" +
                    "  gl_FragColor = vec4(vec3((1. - tex.a) * bar.r),1.);\n" +
                    "}";
            public ProgressBar(String id,  Float x, Float y, float width, float height, int bgColor, int fgColor) {
                super(id, TYPE.PROGRESSBAR, x, y, 0, 0);
                this.bgColor = bgColor;
                this.fgColor = fgColor;
            }
            public ProgressBar(){
                super("pb", TYPE.PROGRESSBAR, 0f, 0f, 50, 10);
                this.mProgram = Shape.createProgram(vs_Image, fragmentShader, -1);
                this.mProgram.setTimeLimit(100);
                this.mProgram.setTimeStep(.16f);
            }
            public void setValue(float value){
                tempValue = value;
                //_value = value;
            }

            public void reset(){
                tempValue = 0;
                _value = 0;
            }

            public float getValue(){
                if(tempValue > _value) _value += 0.01f;
                if(tempValue < _value) _value -= 0.01f;
                if(Math.abs(tempValue - _value) <= 0.01) _value = tempValue;
                return _value;
            }

            @Override
            public float getX() {
                return super.getX() + getWidth() / 2;
            }

            @Override
            public float getY() {
                return super.getY() + getHeight() / 2;
            }

            @Override
            public void draw() {
                super.draw();
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
                this.setWidth(GameView.scaledDefaultSide * 3);
                this.setHeight(GameView.scaledDefaultSide * 2);
                this.setWidth(this.getWidth() + 1);
                this.text = text;
                this.setWidth(GameView.metrics.widthPixels - (GameView.scaledDefaultSide * 2));
                okButton = new Button("okButton","OK", this.getX() + (this.getWidth() / 2f) - 152f /*- (152f * 1.5f)*/, this.getY() + (this.getHeight() * .4f), 0, 0);
                okButton.addUIListener(this);
                cancelButton = new Button("cancelButton","Cancel", this.getX() + (this.getWidth() / 2f) + 152f /*+  (152f * .5f)*/, this.getY() + (this.getHeight() * .4f), 0, 0);
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
            public void draw()
            {
                font.setX(this.getX() + (this.getWidth() / 2));
                font.setY(this.getY() + (this.getHeight() / 2) - GameView.scaledDefaultSide);
                font.setText(this.text);
                changeProgram(font.mProgram.getProgramId(), font.vertexBuffer);
                font.draw();
                okButton.draw();
                cancelButton.draw();
            }
        }
    }

    public static class InfoBox extends Control{
        public Integer width = 600;
        public Integer height = 200;
        private Integer score = 0;
        private Integer targetScore = 0;
        private Integer puzzleScore = 0;
        private Font levelAndTargetScoreLabel;
        private Font puzzleScoreLabel;
        public InfoBox(String id, Float x, Float y) {
            super(id, TYPE.INFOBOX, x, y, 0, 0);
            levelAndTargetScoreLabel = new Font(R.drawable.oldskol, 4.0f);
            levelAndTargetScoreLabel.hAlign = Font.HorizontalAlignment.RIGHT;

            puzzleScoreLabel = new Font(R.drawable.oldskol, 4.0f);
            puzzleScoreLabel.hAlign = Font.HorizontalAlignment.RIGHT;

            //puzzleScoreLabel.charRotate = true;
            puzzleScoreLabel.doCharScale = true;
        }

        public void setScore(Integer score){
            this.score = score;
            isTranferingScore = false;
        }

        public void setTargetScore(Integer score){
            this.targetScore = score;
        }

        public void setPuzzleScore(Integer score){
            this.puzzleScore = score;
        }

        private boolean isTranferingScore = false;
        public void transferScore(){
            isTranferingScore = true;
        }

        @Override
        public float getX(){
            return super.getX() + this.getWidth() / 2;
        }

        @Override
        public float getY(){
            return super.getY() + this.getHeight() / 2;
        }

        @Override
        public void draw()
        {
            super.draw();

            if(isTranferingScore){
                if(this.puzzleScore > 0){
                    this.score++;
                    this.puzzleScore--;
                } else isTranferingScore = false;
            }

            levelAndTargetScoreLabel.setX(this.getX() - (this.getWidth() / 2) + 220);
            levelAndTargetScoreLabel.setY(this.getY() - (this.getHeight() / 2) + 48);
            levelAndTargetScoreLabel.setText(score.toString());
            changeProgram(levelAndTargetScoreLabel.mProgram.getProgramId(), levelAndTargetScoreLabel.vertexBuffer);
            levelAndTargetScoreLabel.draw();
            levelAndTargetScoreLabel.setText( targetScore.toString());
            levelAndTargetScoreLabel.setX(this.getX() - (this.getWidth() / 2)  + 220);
            levelAndTargetScoreLabel.setY(this.getY() - (this.getHeight() / 2) + 100);
            changeProgram(levelAndTargetScoreLabel.mProgram.getProgramId(), levelAndTargetScoreLabel.vertexBuffer);
            levelAndTargetScoreLabel.draw();

            puzzleScoreLabel.setText( puzzleScore.toString());
            puzzleScoreLabel.setX(this.getX() - (this.getWidth() / 2) + 220);
            puzzleScoreLabel.setY(this.getY() - (this.getHeight() / 2)+ 152);
            changeProgram(puzzleScoreLabel.mProgram.getProgramId(), puzzleScoreLabel.vertexBuffer);
            puzzleScoreLabel.draw();
        }
    }

    public static class Title extends Control{
        public Title() {
            super("title", TYPE.TITLE, 0f, 0f, 0, 0);
            setOffSetX(0);
            setOffSetY(0);
        }

        @Override
        public void draw(GL10 gl)
        {
            super.draw();
        }
    }

    public static class LevelCompleted extends Control{
        private final Font font;
        private String text;
        private ProgressBar _pb;

        public boolean canLoadNextLevel() {
            return _pb.tempValue == _pb._value ||  _pb._value >= 1f;
        }

        public String getText() {
            return text;
        }

        @Override
        public float getWidth(){
            return  cacheWidth;
        }

        @Override
        public float getHeight(){
            return  cacheHeight;
        }

        public void setText(String text) {
            this.text = text;
        }

        private static String fragmentShader =
                "#ifdef GL_ES\n" +
                "precision mediump float;\n" +
                "#endif\n" +
                "#extension GL_OES_standard_derivatives : enable\n" +
                "uniform float time;\n" +
                "uniform vec2 resolution;\n" +
                "void main( void ) {\n" +
                "  gl_FragColor = vec4(vec3(0.2,0.2,0.2), 0.4);\n" +
                "}";

        public LevelCompleted(ProgressBar pb) {
            super("levelcomplete", TYPE.LEVELCOMPLETE, 0f, 0f, 0, 0);
            this.mProgram = Shape.createProgram(vs_Image, fragmentShader, -1);
            setOffSetX(0);
            setOffSetY(0);
            setText("Level\nCompleted!");
            cacheWidth = (int) super.getWidth();
            cacheHeight = (int) super.getHeight();
            font = new Font(R.drawable.oldskol, 8.0f);
            font.vAlign = Font.VerticalAlignment.BOTTOM;
            this.setX(GameView.metrics.widthPixels / 2);
            this.setY(GameView.metrics.heightPixels / 2);
            font.setText(text);
            font.setX(this.getX());
            font.setY(this.getY());
            _pb = pb;
            this.visible = false;
        }

        @Override
        public void draw() {
            super.draw();
            changeProgram(font.mProgram.getProgramId(), font.vertexBuffer);
            font.draw();
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
        float charAngle = 0;
        float charAngularSpeed = 0.25f;
        float charScale = 1;
        float charScaleStep = 0.025f;
        float minCharScale = 0.5f;
        float maxCharScale = 1.5f;
        boolean charRotate = false;
        boolean doCharScale = false;
        HorizontalAlignment hAlign = HorizontalAlignment.CENTER;
        VerticalAlignment vAlign = VerticalAlignment.MIDDLE;
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
            fontWidth = Math.round(getWidth() / 10)/* *Math.round(scale)*/;
            fontHeight = Math.round(getHeight() / 10)/* *Math.round(scale)*/;
            fontWidthGl = (1 / 10f)/* *scale*/;
            fontHeightGl = (1 / 10f)/* *scale*/;
            this.scale = scale;
            setWidth(fontWidth*scale);
            setHeight(fontHeight*scale);
            offSetX = 0;
            offSetY = 0;
            useStaticVBO = true;
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
            return super.getX();
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

        private float[] transformationMatrix = new float[16];
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
                String lineText = lines[l];
                _getCharCodes(lineText);
                if(hAlign == HorizontalAlignment.LEFT) halgn = 0.5f;
                else if(hAlign == HorizontalAlignment.RIGHT) halgn = -(lineText.length()-0.5f);
                else halgn = -(lineText.length() == 1 ? 0 : (lineText.length() / 2f)-0.5f);
                if(vAlign == VerticalAlignment.TOP) valgn = (1 / 2f);
                else if(vAlign == VerticalAlignment.BOTTOM) valgn = -(1/2f);

                for (int i = 0; i < charCodes.length; i++) {
                    for (int j = 0; j < 8; j++) {
                        uvData[(lastUVIndex)+(i*8)+j] = uvMap[Math.min(charCodes[i], 95)][j];
                    }
                    for (int j = 0; j < 6; j++) {
                        drawOrderFinal[(lastDOFIndex)+(i*6)+j] = (short) (drawOrder[j]+(((lastDOIndex+i)*4)));
                    }



                    if(this.charRotate || this.doCharScale) {
                        vertexData[(lastVerIndex) + (i*12)+0]  = -0.5f;// + i + (halgn);  //x
                        vertexData[(lastVerIndex) + (i*12)+1]  =  0.5f;// + (valgn) + l;  //y
                        vertexData[(lastVerIndex) + (i*12)+3]  = -0.5f;// + i + (halgn);  //x
                        vertexData[(lastVerIndex) + (i*12)+4]  = -0.5f;// + (valgn) + l;  //y
                        vertexData[(lastVerIndex) + (i*12)+6]  =  0.5f;// + i + (halgn);  //x
                        vertexData[(lastVerIndex) + (i*12)+7]  = -0.5f;// + (valgn) + l;  //y
                        vertexData[(lastVerIndex) + (i*12)+9]  =  0.5f;// + i + (halgn);  //x
                        vertexData[(lastVerIndex) + (i*12)+10] =  0.5f;// + (valgn) + l;  //y

                        /*** roatation ***/
                        if(this.charRotate) {
                            long time = SystemClock.uptimeMillis() % 16000L;
                            this.charAngle = 0.72f * ((int) time) * this.charAngularSpeed;
                        }
                        /*** scale ***/
                        if(this.doCharScale) {
                            if (this.charScale <= this.minCharScale || this.charScale >= this.maxCharScale)
                                this.charScaleStep = this.charScaleStep * -1;
                            this.charScale += this.charScaleStep;
                        }
                        transformationMatrix = Shape.doTransformations(0, 0, this.charScale, this.charScale, this.charAngle, 1);
                        float[] result = new float[4];
                        for (int j = 0; j < 12; j += 3) {
                            float[] data = new float[]{vertexData[(lastVerIndex) + (i * 12) + j + 0],
                                    vertexData[(lastVerIndex) + (i * 12) + j + 1],
                                    0/*vertexData[(lastVerIndex) + (i * 12) + j + 2]*/,
                                    1};
                            Matrix.multiplyMV(result, 0, transformationMatrix, 0, data, 0);
                            vertexData[(lastVerIndex) + (i * 12) + j + 0] = result[0] + i + (halgn);
                            vertexData[(lastVerIndex) + (i * 12) + j + 1] = result[1]+ (valgn) + l;
                        }
                    } else {
                        vertexData[(lastVerIndex) + (i*12)+0]  = -0.5f + i + (halgn);  //x
                        vertexData[(lastVerIndex) + (i*12)+1]  =  0.5f + (valgn) + l;  //y
                        vertexData[(lastVerIndex) + (i*12)+3]  = -0.5f + i + (halgn);  //x
                        vertexData[(lastVerIndex) + (i*12)+4]  = -0.5f + (valgn) + l;  //y
                        vertexData[(lastVerIndex) + (i*12)+6]  =  0.5f + i + (halgn);  //x
                        vertexData[(lastVerIndex) + (i*12)+7]  = -0.5f + (valgn) + l;  //y
                        vertexData[(lastVerIndex) + (i*12)+9]  =  0.5f + i + (halgn);  //x
                        vertexData[(lastVerIndex) + (i*12)+10] =  0.5f + (valgn) + l;  //y
                    }
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
            return super.getY();
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
