package com.colorchains.colorchainsgl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by francisco.cnmarao on 17/04/2017.
 */

public class UI {
    public Texture texture;
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
                font = new Font(UI.Font.FontSize.Normal);
                font.text = text;
                font.x = this.x + (cacheWidth / 2);
                font.y = this.y + (cacheHeight / 2);
            }
            @Override
            public void draw(GL10 gl)
            {
                super.draw(gl);
                font.draw(gl);
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
                this.font = new Font(UI.Font.FontSize.Normal);
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
            font = new Font(UI.Font.FontSize.Normal);
            fontBig = new Font(Font.FontSize.Large);
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
            fontBig.draw(gl);
            fontBig.text = targetScore.toString();
            fontBig.x = this.x + 224;
            fontBig.y = this.y + 116;
            fontBig.draw(gl);
            fontBig.text = puzzleScore.toString();
            fontBig.x = this.x + 224;
            fontBig.y = this.y + 168;
            fontBig.draw(gl);
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

    public static class Font{
        String fontImagePath;
        int fontHeight = 40;
        int fontWidth = 20;
        float fontWidthGl, fontHeightGl;
        int startChar = 32;
        int[] charCodes = new int[95];
        float size = 2f;
        int defaultSize = 8;
        TYPE type;
        FontSize fontSize = FontSize.Normal;
        float x, y = 0;
        float r = 1;
        HorizontalAlignment hAlign = HorizontalAlignment.CENTER;
        VerticalAlignment vAlign = VerticalAlignment.MIDDLE;
        float valgn = 0;
        float halgn = 0;
        public String text = "";
        Texture texture;

        private static float[][] textureMap;

        public Font(FontSize fontSize) {
            switch (fontSize) {
                case Large:
                    size = 4f;
                    this.type = TYPE.FONT_LARGE;
                    break;
                case Normal:
                default:
                    size = 2f;
                    this.type = TYPE.FONT_NORMAL;
                    break;
            }
            Bitmap resizedBitmap;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            Bitmap bmp = BitmapFactory.decodeResource(GameView.context.getResources(), R.drawable.oldskol, options);//
            resizedBitmap = Bitmap.createScaledBitmap(bmp, defaultSize * (int) this.size * 10, defaultSize * (int) this.size * 10, false);
            texture = GameView.renderer.loadTexture(this.type, resizedBitmap);
            fontWidth = resizedBitmap.getWidth() / 10;
            fontHeight = resizedBitmap.getHeight() / 10;
            fontWidthGl = 1 / 10f;
            fontHeightGl = 1 / 10f;
            if(textureMap == null) buildTextureMap();
        }
        public void buildTextureMap(){
            textureMap = new float[1][8];
            for (int i = 0; i < (95); i++) {
                int code = i;
                float s = (code * fontWidthGl) % (fontWidthGl * 10);
                float t = (((int)Math.floor((code * fontWidthGl) / (fontWidthGl * 10)) * fontHeightGl));
                textureMap[i][0] = s;//s
                textureMap[i][1] = t;//t
                textureMap[i][2] = s +  fontWidthGl;
                textureMap[i][3] = t;
                textureMap[i][4] = s;
                textureMap[i][5] = t +  fontHeightGl;
                textureMap[i][6] = s +  fontWidthGl;
                textureMap[i][7] = t +  fontHeightGl;
            }
        }
        public float getX() {
            return x * GameView.w / GameView.screenW;
        }

        public float getY() {
            return y * GameView.h / GameView.screenH;
        }

        private void _getCharCodes(String text){
            charCodes = new int[text.length()];
            for (int i = 0; i < text.length(); i++) {
                charCodes[i] = (((int)text.charAt(i)) - startChar);
            }
        }
        public void draw(GL10 gl) {
            _getCharCodes(text);
            if(hAlign == HorizontalAlignment.CENTER) halgn = -(fontWidth * text.length() / 2);
            else if(hAlign == HorizontalAlignment.RIGHT) halgn = -(fontWidth * text.length());
            if(vAlign == VerticalAlignment.MIDDLE) valgn = -(fontHeight / 2);
            else if(vAlign == VerticalAlignment.BOTTOM) valgn = -(fontHeight);
            for (int i = 0; i < charCodes.length; i++) {
                texture.setMapping(textureMap[charCodes[i]]);
                texture.draw(gl, getX() + (i * (fontWidth)) + halgn, getY() + valgn, fontWidth * r, fontHeight * r, 0, true);
            }
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
