package com.colorchains.colorchainsgl;

import android.graphics.Color;
import android.opengl.Matrix;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.os.SystemClock;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import static com.colorchains.colorchainsgl.GameView.GLRenderer.changeProgram;

/**
 * Created by francisco.cnmarao on 17/04/2017.
 */

class UI {

    public static float fontScaleBig = 8;
    private static boolean _modal = false;
    private static Control mainContainer;
    private static List<Control> selectedControls;
    static MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();

    static void init() {
        mainContainer = new Control("mainContainer", TYPE.CONTAINER, 0f, 0f, GameView.metrics.widthPixels, GameView.metrics.heightPixels);
        selectedControls = new ArrayList<>();
    }
    static boolean isInModalMode(){
        return _modal;
    }

    static void addControl(Control ctrl){
        mainContainer.controls.add(ctrl);
    }

    static void update() {
        for (Control ctrl: mainContainer.controls ) {
            if(ctrl.visible) ctrl.update();
        }
    }

    static Control findControlById(String id){
        Control result = null;
        for (Control ctrl: mainContainer.controls) if(ctrl.id.equals(id)) result = ctrl;
        return  result;
    }

    static void draw() {
        for (Control ctrl: mainContainer.controls ) {
            if(ctrl.visible) ctrl.draw();
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

    static void touchStartUI(MotionEvent evt) {
        checkTouch(mainContainer,evt);
        evt.getPointerCoords(0, coords);
        if(selectedControls.size() > 0){
            for (Control selectedControl: selectedControls) {
                //((UIListener) selectedControl).clicked(selectedControl);
                ((UIListener) selectedControl).onTouchStart(selectedControl, evt);
            }
        }
    }

    public static void touchEndUI(MotionEvent evt) {
        if(selectedControls.size() > 0){
            for (Control selectedControl: selectedControls) {
                ((UIListener) selectedControl).onTouchEnd(selectedControl, evt);
            }
            selectedControls = new ArrayList<>();
            coords = new MotionEvent.PointerCoords();
        }
    }

    public static void touchMoveUI(MotionEvent evt) {
        if(selectedControls.size() > 0){
            for (Control selectedControl: selectedControls) {
                //((UIListener) selectedControl).clicked(selectedControl);
                ((UIListener) selectedControl).onMove(selectedControl, evt);
            }
        }
    }

    private static void checkTouch(Control ctrl, MotionEvent evt) {
        if (!ctrl.visible || !ctrl.enabled) return;
        if(ctrl instanceof UIListener) {
            float rawX = evt.getRawX();
            float rawY = evt.getRawY();
            RectF ctrlRect = new RectF(ctrl.offSetX + ctrl.getX() - (ctrl.getWidth() / 2), ctrl.offSetY + ctrl.getY() - (ctrl.getHeight() / 2), (ctrl.offSetX + ctrl.getX() - (ctrl.getWidth() / 2)) + ctrl.cacheWidth, (ctrl.offSetY + ctrl.getY() - (ctrl.getHeight() / 2)) + ctrl.cacheHeight);
            RectF touchRect = new RectF(rawX, rawY, rawX + 1, rawY + 1);
            if (touchRect.intersect(ctrlRect)) {
                //((UIListener) ctrl).clicked(ctrl);
                selectedControls.add(ctrl);
            }
        }
        if (ctrl.controls.size() > 0) {
            for (int index = 0; index < ctrl.controls.size(); index++) {
                Control _ctrl = ctrl.controls.get(index);
                checkTouch(_ctrl, evt);
            }
        }
    }

    static class Control extends Entity  {
        public boolean visible = true;
        public boolean enabled = true;
        public List<Control> controls = new ArrayList<>();
        public Control(String id, TYPE type, Float x, Float y, Integer cx, Integer cy) {
            super(id, type, x, y, cx, cy);
        }
        public void addControl(Control ctrl){
            ctrl.parent = this;
            this.controls.add(ctrl);
        }
    }

    public interface UIListener{
        void onTouchStart(Object args, MotionEvent evt);
        void onTouchEnd(Object args, MotionEvent evt);
        void onMove(Object args, MotionEvent evt);
    }

    static class Button extends Control implements UIListener {
        private List<UIListener> listeners = new ArrayList<>();
        private final String text;
        private Font font;
        private float[] uvs;
        public Button(String id, String text, Float x, Float y, Integer cx, Integer cy) {
            super(id,TYPE.BUTTON,x,y,cx,cy);
            cacheWidth = Math.round(super.getWidth() / 2);
            cacheHeight = (int) super.getHeight();
            this.text = text;
            font = new Font(R.drawable.oldskol, 2.0f);
            font.setText(text);
            font.setX(this.getX());
            font.setY(this.getY());
        }

        public Button(String id, String text, Float x, Float y, Integer cx, Integer cy, TYPE type, Integer textureIndex) {
            super(id, type, x, y, cx, cy);
            cacheWidth = Math.round(super.getWidth() / 2);
            cacheHeight = (int) super.getHeight();
            this.text = text;
            font = new Font(R.drawable.oldskol, 2.0f);
            font.setText(text);
            font.setX(this.getX());
            font.setY(this.getY());
//            this.uvs = new float[8];
//            for (int i = 0; i < 8 ; i++) {
//                uvs[i] = (super.uvs[(textureIndex * 8) + i]);
//            }
//            setUVBuffer(uvs);
            buildTextureMap(2,2,1);
            setAnimationIndex(textureIndex);
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
            if(font.text.length() > 0) {
                changeProgram(font.mProgram.getProgramId(), font.vertexBuffer);
                font.draw();
            }
        }

        public void onTouchStart(Object args, MotionEvent evt) {}

        @Override
        public void onTouchEnd(Object args, MotionEvent evt) {
            for (UIListener listener: listeners) {
                listener.onTouchEnd(this, evt);
            }
        }

        @Override
        public void onMove(Object args, MotionEvent evt) {}

        // Assign the listener implementing events interface that will receive the events
        public void addUIListener(UIListener listener) {
            this.listeners.add(listener);
        }
        // Assign the listener implementing events interface that will receive the events
        public void removeUIListener(UIListener listener) {
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
            return _value + 0.04f;
        }

        public void updateBar(){
            if(tempValue > _value) _value += 0.006f;
            if(tempValue < _value) _value -= 0.006f;
            if(Math.abs(tempValue - _value) <= 0.006) _value = tempValue;
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
        private List<UIListener> listeners = new ArrayList<>();
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
            if(GameView.screenW <= 480) {
                this.font.scale = 0.9f;
                this.font.doScale = true;
            }
        }

        // Assign the listener implementing events interface that will receive the events
        public void addUIListener(UIListener listener) {
            this.listeners.add(listener);
        }

        @Override
        public void onTouchStart(Object args, MotionEvent evt) {

        }

        @Override
        public void onTouchEnd(Object args, MotionEvent evt) {
            for (UIListener listener: listeners) {
                listener.onTouchEnd(((Button)args).text, evt);
            }
        }

        @Override
        public void onMove(Object args, MotionEvent evt) {

        }

        @Override
        public void draw()
        {
            font.setX(this.getX() + (this.getWidth() / 2));
            font.setY(this.getY() + (this.getHeight() / 2) - (okButton.getHeight() * 1.5f));
            font.setText(this.text);
            changeProgram(font.mProgram.getProgramId(), font.vertexBuffer);
            font.draw();
            okButton.draw();
            cancelButton.draw();
        }
    }

    public static class InfoBox extends Control{
        private Integer score = 0;
        private Integer targetScore = 0;
        private Integer puzzleScore = 0;
        private Font highScoreNumber;
        private Font puzzleScoreNumber;
        private List<Font> chainsLabels = new ArrayList<>();
        private List<Chain> chains;
        private Font puzzleTitle;
        public InfoBox(String id, Float x, Float y) {
            super(id, TYPE.INFOBOX, x, y, 0, 0);
            highScoreNumber = new Font(R.drawable.oldskol, 4.0f);
            highScoreNumber.hAlign = Font.HorizontalAlignment.RIGHT;

            puzzleScoreNumber = new Font(R.drawable.oldskol, 4.0f);
            puzzleScoreNumber.hAlign = Font.HorizontalAlignment.RIGHT;

            puzzleTitle = new Font(R.drawable.oldskol, 2.0f);
        }

        public void setScore(Integer score){
            this.score = score;
            isTransferringScore = false;
        }

        public void setTitle(String title){
            puzzleTitle.setX(this.getX());
            puzzleTitle.setY(this.getY() - 82);
            puzzleTitle.setText(title);
        }

        public void setChains(List<Chain> chains){
            if(chains == null) chainsLabels = new ArrayList<>();
            this.chains = chains;
        }

        public void setTargetScore(Integer score){
            this.targetScore = score;
        }

        public void setPuzzleScore(Integer score){
            this.puzzleScore = score;
        }

        public void setHighScore(Integer score){
            highScoreNumber.setText(score.toString());
        }

        private boolean isTransferringScore = false;
        public void transferScore(){
            isTransferringScore = true;
        }

        @Override
        public float getX(){
            return super.getX() + (this.getWidth() / 2);
        }

        @Override
        public float getY(){
            return super.getY() + this.getHeight() / 2;
        }

        @Override
        public void draw()
        {
            super.draw();

            if(isTransferringScore){
                if(this.puzzleScore > 0){
                    int step = Math.max(this.puzzleScore / 20, 1);
                    this.score+= step;
                    this.puzzleScore-= step;
                } else isTransferringScore = false;
            }

            //highScoreNumber.setText(score.toString());
            changeProgram(highScoreNumber.mProgram.getProgramId(), highScoreNumber.vertexBuffer);
            highScoreNumber.setX(this.getX() - (this.getWidth() / 2) + 220);
            highScoreNumber.setY(this.getY() - (this.getHeight() / 2) + 60);
            highScoreNumber.draw();

            puzzleScoreNumber.setText( score.toString());
            puzzleScoreNumber.setX(this.getX() - (this.getWidth() / 2) + 220);
            puzzleScoreNumber.setY(this.getY() - (this.getHeight() / 2)+ 140);
            GameView.GLRenderer.updateVertexBuffer(puzzleScoreNumber.vertexBuffer);
            puzzleScoreNumber.draw();

            GameView.GLRenderer.updateVertexBuffer(puzzleTitle.vertexBuffer);
            puzzleTitle.draw();

            String[] colors = new String[]{"YELLOWGEM","WHITEGEM","REDGEM","PURPLEGEM", "ORANGEGEM", "GREENGEM", "CYANGEM", "BLUEGEM"};
            if(chains != null && chainsLabels.size() == 0){
                for (int i = 0; i < 8; i++) {
                    chainsLabels.add(new Font(R.drawable.oldskol, 3f));
                    Font chainsLabel = chainsLabels.get(chainsLabels.size() - 1);
                    chainsLabel.hAlign = Font.HorizontalAlignment.CENTER;

                    int index = i;//chains.indexOf(chn);
                    int line = (int)Math.floor(index / 4);
                    chainsLabel.setText("0");

                    chainsLabel.setX(this.getX() - (this.getWidth() / 2) + 248 + (64 / 2 ) + ((index % 4) * 64));
                    chainsLabel.setY(this.getY() - (this.getHeight() / 2) + 32 + (68 / 2 ) + (line * 68));//56
                }
                for (Chain chn: chains) {
                    //chainsLabels.add(new Font(R.drawable.oldskol, 3f));
                    Font chainsLabel = chainsLabels.get(Arrays.asList(colors).indexOf(chn.id));
                    //chainsLabel.hAlign = Font.HorizontalAlignment.CENTER;

                    int index = Arrays.asList(colors).indexOf(chn.id);//chains.indexOf(chn);
                    int line = (int)Math.floor(index / 4);
                    chainsLabel.setText(chn.count.toString());
                    boolean allWhite = true;
                    if(chn.id == "WHITEGEM" || allWhite)
                        chainsLabel.color = new float[]{235f/255,235f/255,235f/255,1};//W
                    else if(chn.id == "BLUEGEM")
                        chainsLabel.color = new float[]{47f/255,130f/255,255f/255,1};//B
                    else if(chn.id == "CYANGEM")
                        chainsLabel.color = new float[]{52f/255,206f/255,206f/255,1};//C
                    else if(chn.id == "ORANGEGEM")
                        chainsLabel.color = new float[]{251f/255,113f/255,0,1};//O
                    else if(chn.id == "PURPLEGEM")
                        chainsLabel.color = new float[]{139f/255,15f/255,254f/255,1};//P
                    else if(chn.id == "YELLOWGEM")
                        chainsLabel.color = new float[]{236f/255,216f/255,0,1};//Y
                    else if(chn.id == "REDGEM")
                        chainsLabel.color = new float[]{255f/255,50f/255,50f/255,1};//R
                    else if(chn.id == "GREENGEM")
                        chainsLabel.color = new float[]{60f/255,216f/255,0,1};//G


                    chainsLabel.setX(this.getX() - (this.getWidth() / 2) + 248 + (64 / 2 ) + ((index % 4) * 64));
                    chainsLabel.setY(this.getY() - (this.getHeight() / 2) + 32 + (68 / 2 ) + (line * 68));//56
                }
            } else {
                for (Font label: chainsLabels) {
                    GameView.GLRenderer.updateVertexBuffer(label.vertexBuffer);
                    label.draw();
                }
            }
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

    public static class Stars extends Control{
        private int stars = 1;
        float flStars = 0;
        public Stars() {
            super("title", TYPE.STARS, 0f, 0f, 0, 0);
            //this.cacheHeight = (int)this.getHeight() / 4;
            this.setHeight(this.getHeight() / 4);
            buildTextureMap(4,1,4);
            //uvs = uvMap[stars];
            setAnimationIndex(stars);
            setOffSetX(0);
            setOffSetY(0);
        }

//        @Override
//        public void draw()
//        {
//            if(flStars+.01f <= 4)
//                flStars+=.01f;
//            else
//                flStars =  0;
//            setAnimationIndex((int)flStars);
//            super.draw();
//        }

        public void setStars(int stars) {
            if(stars < 0) this.stars = 0;
            else if (stars > 3) this.stars = 3;
            else this.stars = stars;
            setAnimationIndex(this.stars);
        }
    }

    static class LevelSelect extends Control implements UIListener{
        //EntityCollection stageCol;
        int colSize = 5;
        int rowSize = 5;
        Integer curPage = 0;
        List<List<Font>> stageFonts = new ArrayList<>();
        List<Font> curStageFonts;
        Board board;
        List<Stage> stages;
        Font title;
        float defaultOffsetX = 0;
        float defaultOffsetY = 0;
        float touchOffsetX = 0;
        float moveOffsetX = 0;
        private ArrayList<UIListener> listeners = new ArrayList<>();
        private boolean dragging = false;
        private boolean changingPage = false;
        private float lastSpeed = 0;
        public boolean enableAll = false;

        public LevelSelect(List<Stage> stages, Board board) {
            super("levelSelect", TYPE.LEVELSELECT, 0f, 0f, 0, 0);
            //EntityCollection stagePage = new EntityCollection(R.drawable.levelselect, new Entity[colSize][rowSize], rowSize, colSize);
            this.board = board;
            this.stages = stages;
            this.cacheWidth = GameView.screenW;
            this.cacheHeight = GameView.screenH;
            title = new Font(R.drawable.oldskol, (GameView.screenW > 480 ? 5 : 4));
            title.setY((GameView.screenW > 480 ? 192 : 124));
            title.setX(GameView.metrics.widthPixels / 2);
            title.setText("Level Select");
            init(stages, board);
        }

        public void setTitle(String title){
            this.title.setText(title);
        }

        public void init(List<Stage> stages, Board board) {
            curPage = 0;
            pagesView = null;
            stageFonts = new ArrayList<>();
            LevelSelectPage stagePage = new LevelSelectPage(colSize, rowSize);
            controls.clear();
            controls.add(stagePage);
            curStageFonts = new ArrayList<>();
            Integer lastHighScore = 0;
            defaultOffsetX = (GameView.metrics.widthPixels / (colSize + 1f));
            defaultOffsetY = (GameView.metrics.heightPixels / 3);
            for (Stage stage : stages) {
                Board.HighScore highScore = board.getHighScore(stage.id);
                int index = stages.indexOf(stage);
                int i = index % (colSize * rowSize);
                int row = (int)Math.floor(i / rowSize), col = i % colSize;
                Button btn = new Button(stage.id, String.valueOf((index) + 1), col * GameView.metrics.widthPixels / (colSize + 1f), row * (GameView.screenW > 480 ? 120f : 96f), 0, 0);
                //btn.addAnimation("idle", 0, 0, new Integer[]{0}, 0.5f, false);
                btn.curAnimation = btn.addAnimation("idle", 0, 0, new Integer[]{0}, 0.5f, false);
                btn.curAnimation.curFrame = highScore.highScore > 0 || (highScore.highScore == 0 && lastHighScore != 0) || index == 0 || enableAll ? getButtonFrame(stage, highScore) : 0;
                btn.enabled = btn.curAnimation.curFrame > 0 || (highScore.highScore == 0 && lastHighScore != 0) || index == 0 || enableAll;
                lastHighScore = highScore.highScore;
                addLevel(btn, row, col, stagePage.page.entities);
                if((i > 0 && i % ((colSize * rowSize) - 1) == 0) || index == stages.size() - 1){
                    ((LevelSelectPage)controls.get(controls.size() - 1)).page.buildTextureMap(5,5,1);
                    ((LevelSelectPage)controls.get(controls.size() - 1)).page.offSetX = GameView.metrics.widthPixels / (colSize + 1f);
                    ((LevelSelectPage)controls.get(controls.size() - 1)).page.offSetY = GameView.metrics.heightPixels / 3;
                    stageFonts.add(curStageFonts);
                    stagePage.page.offSetX = controls.size() == 1 ? stagePage.page.offSetX : GameView.screenW;
                    if(index != stages.size() - 1) {
                        curStageFonts = new ArrayList<>();
                        stagePage = new LevelSelectPage(colSize, rowSize);
                        controls.add(stagePage);
                        stagePage.visible = (controls.size() == 1);
                    }
                }
            }
            setPaging(0,controls.size());
            this.getCurPage().page.transform.fadeIn(0,1f,0.5f);
        }

        private int getButtonFrame(Stage stage, Board.HighScore highScore) {
            float ratio = highScore.moves > 0 ? (stage.targetMoves / (float)highScore.moves) : 0;
            if(ratio < 1) return 1;
            if(ratio >= 1 && ratio < 1.5) return 2;
            if(ratio >= 1.5 && ratio < 2) return 3;
            return 4;
        }

        public void update(List<Stage> stages, Board board){
            init(stages, board);
        }

        @Override
        public void update(){
            if(changingPage) changePage();
            else centerPage();
            getCurPage().page.update();
        }

        private void centerPage(){
            if(getCurPage().page.offSetX != defaultOffsetX && !dragging){
                if(getCurPage().page.offSetX > defaultOffsetX){
                    if(getCurPage().page.offSetX > defaultOffsetX)
                        getCurPage().page.offSetX -= Math.max((getCurPage().page.offSetX - defaultOffsetX) / 6, 1 );
                } else {
                    if(getCurPage().page.offSetX < defaultOffsetX)
                        getCurPage().page.offSetX += Math.max((defaultOffsetX - getCurPage().page.offSetX) / 6, 1 );
                }
            }
            lastSpeed = 0;
        }


        private void changePage(){
            if(lastSpeed < 0){
                if(curPage == controls.size() - 1) {
                    changingPage = false;
                    return;
                } else {
                    if(getCurPage().page.offSetX + lastSpeed > -(GameView.screenW)){
                        getCurPage().page.offSetX += lastSpeed;
                    } else {
                        changingPage = false;
                        getCurPage().visible = false;
                        curPage++;
                        getCurPage().visible = true;
                        getCurPage().transform.fadeIn(0,1,1f);
                        setPaging(curPage,controls.size());
                    }
                }
            } else {
                if(curPage == 0) {
                    changingPage = false;
                    return;
                } else {
                    if(getCurPage().page.offSetX + lastSpeed < (GameView.screenW)){
                        getCurPage().page.offSetX += lastSpeed;
                    } else {
                        changingPage = false;
                        getCurPage().visible = false;
                        curPage--;
                        getCurPage().visible = true;
                        getCurPage().transform.fadeIn(0,1,1f);
                        setPaging(curPage,controls.size());
                    }
                }
            }
        }

        @Override
        public void draw()
        {
            if(dragging)
                getCurPage().page.offSetX = defaultOffsetX + moveOffsetX - touchOffsetX;
            ((LevelSelectPage)controls.get(curPage)).page.draw();
            changeProgram(stageFonts.get(curPage).get(0).mProgram.getProgramId(), stageFonts.get(curPage).get(0).vertexBuffer);
            for(Control btn: controls.get(curPage).controls){
                if(btn.curAnimation.curFrame > 0){
                    Integer index = controls.get(curPage).controls.indexOf(btn);
                    Font font = stageFonts.get(curPage).get(index);
                    font.offSetX = getCurPage().page.offSetX;
                    GameView.GLRenderer.updateVertexBuffer(font.vertexBuffer);
                    font.draw();
                }
            }
            GameView.GLRenderer.updateVertexBuffer(title.vertexBuffer);
            title.draw();

            pagesView.draw();
        }

        private void addLevel(Button stage, Integer row, Integer col, Entity[][] entities){
            stage.addUIListener(new UI.UIListener() {
                @Override
                public void onTouchStart(Object sender, MotionEvent evt) {
                }

                @Override
                public void onTouchEnd(Object sender, MotionEvent evt) {
                    if(Math.abs(lastSpeed) < 1 )
                        GameView.board.loadResult = ((Button) sender).id;
                }

                @Override
                public void onMove(Object sender, MotionEvent evt) {

                }
            });

            Font fnt = new Font(R.drawable.oldskol, GameView.screenW > 480 ? 3 : 2);
            //fnt.scale = .75f;
            //fnt.doScale = true;
            fnt.setText(stage.text);
            fnt.offSetX = defaultOffsetX;
            fnt.offSetY = defaultOffsetY;
            stage.offSetX = fnt.offSetX;
            stage.offSetY = fnt.offSetY;
            fnt.setX(stage.getX());

            fnt.setY(stage.getY());
            curStageFonts.add(fnt);
            if(GameView.screenW <= 800) {
                stage.cacheWidth = GameView.screenW > 480 ? 92 : 58;//92;
                stage.cacheHeight = GameView.screenW > 480 ? 92 : 58;//92;
            } else {
                stage.cacheWidth = (int)(92f * 1.25f);
                stage.cacheHeight = (int)(92f * 1.25f);
            }
            entities[col][row] = stage;
            controls.get(controls.size() - 1).addControl(stage);
        }

        public LevelSelectPage getCurPage(){
            return ((LevelSelectPage)controls.get(curPage));
        }

        EntityCollection pagesView;
        public void setPaging(Integer selected, Integer total){
            if(pagesView == null){
                pagesView = new EntityCollection(R.drawable.paging, new Entity[total][1], 1, total);
                pagesView.setWidth(pagesView.getWidth() / 2);
                for (int i = 0; i < total; i++) {
                    int row = 0, col = i % total;
                    Control ctrl = new Control("page",TYPE.PAGING,col * 48f,0f,0,0);
                    ctrl.curAnimation = ctrl.addAnimation("idle", 0, 0, new Integer[]{0}, 0.5f, false);
                    ctrl.curAnimation.curFrame = i == selected ? 1 : 0;
                    pagesView.entities[col][row] = ctrl;
                }
                pagesView.setX((GameView.screenW / 2) + 12 - (total * 24f / 2));
                pagesView.setY(getCurPage().page.offSetY + ((GameView.screenW > 480 ? 120 : 96) * 5f));
            } else {
                for (int i = 0; i < total; i++) {
                    int row = 0, col = i % total;
                    pagesView.entities[col][row].curAnimation.curFrame = i == selected ? 1 : 0;
                }
            }
            pagesView.buildTextureMap(total,2,1);
        }

        private VelocityTracker mVelocityTracker = null;
        @Override
        public void onTouchStart(Object args, MotionEvent evt) {
            if(mVelocityTracker == null) {
                // Retrieve a new VelocityTracker object to watch the
                // velocity of a motion.
                mVelocityTracker = VelocityTracker.obtain();
            } else {
                 // Reset the velocity tracker back to its initial state.
                 mVelocityTracker.clear();
            }
            // Add a user's movement to the tracker.
            mVelocityTracker.addMovement(evt);
            touchOffsetX = evt.getX() /*- offSetX*/;
            moveOffsetX = Math.round(evt.getRawX());
            dragging = true;
        }

        @Override
        public void onTouchEnd(Object args, MotionEvent evt) {
            int pointerId = evt.getPointerId(evt.getActionIndex());
            lastSpeed = VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId);
            if(Math.abs(lastSpeed) > 12)
                changingPage = true;
            dragging = false;
            //Log.d("", "offSetX: " + getCurPage().page.offSetX);
            // Return a VelocityTracker object back to be re-used by others.
            mVelocityTracker.clear();
        }

        @Override
        public void onMove(Object args, MotionEvent evt) {
            int pointerId = evt.getPointerId(evt.getActionIndex());
            //setTitle(Math.round(evt.getRawX()) + " - " + Math.round(evt.getRawY()));
            moveOffsetX = Math.round(evt.getRawX());

            mVelocityTracker.addMovement(evt);
            // When you want to determine the velocity, call
            // computeCurrentVelocity(). Then call getXVelocity()
            // and getYVelocity() to retrieve the velocity for each pointer ID.
            mVelocityTracker.computeCurrentVelocity(17);
            // Log velocity of pixels per second
            // Best practice to use VelocityTrackerCompat where possible.
            //Log.d("", "X velocity: " + VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId));
            //Log.d("", "Y velocity: " + VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId));
            //Log.d("", "offSetX: " + getCurPage().page.offSetX);
        }

        // Assign the listener implementing events interface that will receive the events
        public void addUIListener(UIListener listener) {
            this.listeners.add(listener);
        }

        public static class LevelSelectPage extends Control{
            public EntityCollection page;
            public LevelSelectPage(Integer colSize, Integer rowSize){
                super("LevelPage", TYPE.LEVELSELECT, 0f, 0f, 0, 0);
                this.setWidth(GameView.screenW);
                this.setHeight(GameView.screenH);
                this.cacheWidth = GameView.screenW;
                this.cacheHeight = GameView.screenH;
                page = new EntityCollection(R.drawable.levelselect, new Entity[colSize][rowSize], rowSize, colSize);
                if(GameView.screenW <= 800) {
                    page.setWidth(GameView.screenW > 480 ? page.getWidth() / 5 : 58);
                    if (GameView.screenW <= 480) page.setHeight(58);
                } else {
                    page.setWidth(92f*1.25f);
                    page.setHeight(92f*1.25f);
                }
            }
        }
    }

    public static class LevelCompleted extends Control{
        private final Font font;
        private String text;
        private ProgressBar _pb;

        public boolean canLoadNextLevel() {
            return _pb.tempValue == _pb._value || _pb._value >= 1f;
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
        /*private static String fragmentShader =
                "#ifdef GL_ES\n" +
                        "precision mediump float;\n" +
                        "#endif\n" +
                        "\n" +
                        "#extension GL_OES_standard_derivatives : enable\n" +
                        "\n" +
                        "uniform float time;\n" +
                        "uniform vec2 mouse;\n" +
                        "uniform vec2 resolution;\n" +
                        "\n" +
                        "\n" +
                        "void main(){\n" +
                        "\tvec2 pos = 2.0 * (gl_FragCoord.xy / resolution.xy) - 1.0;\n" +
                        "\tpos.x *= resolution.x/resolution.y;\n" +
                        "\tpos.y -= 4.4;\n" +
                        "   \t\n" +
                        "\tpos *= 40.0;\n" +
                        "\t\n" +
                        "\tfloat th = atan(pos.y, pos.x) + time / 2.;\n" +
                        "\tfloat rays = smoothstep(.4, .6, sin(th * 7.) - .1);\n" +
                        "\tfloat d =1. - smoothstep(0., 200., length(pos));\n" +
                        "\trays *= d;\n" +
                        "\tvec4 color = vec4(.6, .3, .0, 1.) * rays;\n" +
                        "\tcolor.g += 1. - sqrt(length(pos)) / (10. + sin(th * 60.) * .1 + .2 * sin(th * 20.));\n" +
                        "\tcolor.r += 1. - sqrt(length(pos)) / (10. + sin(th * 60.) * .1 + .2 * sin(th * 20.));\n" +
                        "    \tgl_FragColor = color + vec4(.0, .0, .0, 1.);\n" +
                        "}";*/

        public LevelCompleted(ProgressBar pb) {
            super("levelcomplete", TYPE.LEVELCOMPLETE, 0f, 0f, 0, 0);
            this.mProgram = Shape.createProgram(vs_Image, fragmentShader, -1);
            setOffSetX(0);
            setOffSetY(0);
            cacheWidth = (int) super.getWidth();
            cacheHeight = (int) super.getHeight();
            font = new Font(R.drawable.oldskol, fontScaleBig);
            setText("Level\nCompleted!");
            font.vAlign = Font.VerticalAlignment.BOTTOM;
            if(GameView.screenW > 800){
                font.doScale = true;
                font.scale = 1.5f;
            }
            this.setX(GameView.metrics.widthPixels / 2);
            this.setY(GameView.metrics.heightPixels / 2);
            font.setText(text);
            font.setX(this.getX());
            font.setY(this.getY() - 60);
            _pb = pb;
            this.visible = false;
        }

        @Override
        public void draw() {
            super.draw();
            changeProgram(font.mProgram.getProgramId(), font.vertexBuffer);
            font.draw();
        }

        @Override
        public void update() {
            if(this.transform.isScalingIn && !font.transform.isScalingIn) font.transform.scaleIn(4,1,0.5f);
            font.update();
            super.update();
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
        float padding = 0;
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
                float totalPaddingWidth = lineText.length() * this.padding;
                if(hAlign == HorizontalAlignment.LEFT) halgn = 0.5f;
                else if(hAlign == HorizontalAlignment.RIGHT) halgn = -(lineText.length() + totalPaddingWidth -0.5f);
                else halgn = -(lineText.length() == 1 ? 0 : ((lineText.length() + totalPaddingWidth) / 2f)-0.5f);
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
                        vertexData[(lastVerIndex) + (i*12)+ 0] = -0.5f;// + i + (halgn);  //x
                        vertexData[(lastVerIndex) + (i*12)+ 1] =  0.5f;// + (valgn) + l;  //y
                        vertexData[(lastVerIndex) + (i*12)+ 3] = -0.5f;// + i + (halgn);  //x
                        vertexData[(lastVerIndex) + (i*12)+ 4] = -0.5f;// + (valgn) + l;  //y
                        vertexData[(lastVerIndex) + (i*12)+ 6] =  0.5f;// + i + (halgn);  //x
                        vertexData[(lastVerIndex) + (i*12)+ 7] = -0.5f;// + (valgn) + l;  //y
                        vertexData[(lastVerIndex) + (i*12)+ 9] =  0.5f;// + i + (halgn);  //x
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
                            float[] data = new float[]{
                                    vertexData[(lastVerIndex) + (i * 12) + j + 0],
                                    vertexData[(lastVerIndex) + (i * 12) + j + 1],
                                    0,
                                    1};
                            Matrix.multiplyMV(result, 0, transformationMatrix, 0, data, 0);
                            vertexData[(lastVerIndex) + (i * 12) + j + 0] = result[0] + i + (halgn);
                            vertexData[(lastVerIndex) + (i * 12) + j + 1] = result[1] + (valgn) + l;
                        }
                    } else {
                        vertexData[(lastVerIndex) + (i*12)+0]  = -0.5f + i + (halgn) + (this.padding * i) ;  //x
                        vertexData[(lastVerIndex) + (i*12)+1]  =  0.5f + (valgn) + l + (this.padding * l) ;  //y
                        vertexData[(lastVerIndex) + (i*12)+3]  = -0.5f + i + (halgn) + (this.padding * i);  //x
                        vertexData[(lastVerIndex) + (i*12)+4]  = -0.5f + (valgn) + l + (this.padding * l) ;  //y
                        vertexData[(lastVerIndex) + (i*12)+6]  =  0.5f + i + (halgn) + (this.padding * i);  //x
                        vertexData[(lastVerIndex) + (i*12)+7]  = -0.5f + (valgn) + l + (this.padding * l) ;  //y
                        vertexData[(lastVerIndex) + (i*12)+9]  =  0.5f + i + (halgn) + (this.padding * i);  //x
                        vertexData[(lastVerIndex) + (i*12)+10] =  0.5f + (valgn) + l + (this.padding * l) ;  //y
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
//        @Override
//        public float getX() {
//            return offSetX + super.getX();
//        }
//        @Override
//        public float getY() {
//            return offSetY + super.getY();
//        }

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
