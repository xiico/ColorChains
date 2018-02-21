package com.colorchains.colorchainsgl;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.MotionEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static com.colorchains.colorchainsgl.GameView.GLRenderer.updateMarioTexture;
import static com.colorchains.colorchainsgl.Media.playPuzzleBGM;
import static com.colorchains.colorchainsgl.Media.playingPuzzleBGM;
import static java.lang.Integer.parseInt;

/**
 * Created by francisco.cnmarao on 19/04/2017.
 */

public class Board extends Entity {
    public Stage curStage;
    public Integer rowsCount = 0;
    public Integer colsCount = 0;
    public Gem[][] entities;
    private boolean randomEntities = true;
    private List<Entity> marioBuffer = new ArrayList<>();
    public List<Chain> chains;
    Context context;
    public List<Stage> stages;
    public volatile boolean loaded = false;
    public boolean levelComplete;
    public Entity selectedGem;
    //static volatile boolean _ready = false;
    public float gemOffsetX = /*(92/2) +*/ 16;
    public float gemOffsetY = 252;//92*3;//(92/2) + 16;
    public Integer border = 8;
    //UI elements
    private UI.Button nextButton;
    private UI.ProgressBar progressBar;
    private UI.Confirm confirm;

    private UI.Button showExit;
    private UI.Confirm exitPuzzle;

    private UI.Button showReload;
    private UI.Confirm reloadPuzzle;
    private UI.LevelCompleted levelCompleted;

    private UI.Button audioControl;

    private UI.Stars stars;

    public boolean clearChains = false;
    private static final String BOARD_STATE = "boardState";
    private static final String STAGE_INDEX = "stageIndex";
    private static final String STAGE_SCORES = "stageScores.";
    private Integer stageIndex;
    public String loadResult = "";
    private UI.Font loading;
    private UI.LevelSelect levelSelect;
    public EntityCollection gemCol;
    public EntityCollection marioCol;
    public boolean parseBoard = false;
    public List<String> moveHistory = new ArrayList<>();

    public Board(Context context)  throws JSONException {
        super("board", TYPE.BOARD,0f,0f,1280,768);
        this.context = context;
        loading = new UI.Font(R.drawable.oldskol, 2.0f);
        loading.setText("Loading");
        loading.setX(GameView.metrics.widthPixels / 2);
        loading.setY(GameView.metrics.heightPixels / 2);
        stages = Stage.getStageList(loadJSONFromAsset("levels.json"));

        this.border = 8 + (GameView.is16x9 && GameView.metrics.widthPixels > 480 ? 4 : 0);
        this.border = (GameView.metrics.widthPixels <= 600 ? this.border / 2 : this.border);
        this.border = (GameView.metrics.widthPixels <= 540 ? 7 : this.border);
        gemOffsetX = (border * (int)GameView.scale);
        gemOffsetY = (150/*126*/*/*(int)*/GameView.scale);
        //Log.d("Board","border: " + this.border +",gemOffsetX: " + gemOffsetX + ",gemOffsetY: " + gemOffsetY);
        addControls();
        //UI.hide();
    }

    public Board(String id, TYPE type, Float x, Float y, Integer cx, Integer cy) {
        super(id, type, x, y, cx, cy);
    }

    public void addControls(){
        nextButton = new UI.Button("next",context.getResources().getString(R.string.button_next),
                gemOffsetX + (GameView.scaledDefaultSide * 7) + (this.getWidth() / 2),
                gemOffsetY + (GameView.scaledDefaultSide * 8.5f) + (20 * GameView.scale) ,0,0);
        UI.addControl(nextButton);
        nextButton.addUIListener(new UI.UIListener() {
            @Override
            public void onTouchStart(Object sender, MotionEvent evt) {}

            @Override
            public void onTouchEnd(Object args, MotionEvent evt) {nextStage();}

            @Override
            public void onMove(Object sender, MotionEvent evt) {}
        });
        //if(nextButton != null) return;
        nextButton.visible = false;
        progressBar = new UI.ProgressBar();
        progressBar.setX(gemOffsetX);
        progressBar.setY(gemOffsetY + (GameView.scaledDefaultSide * 8f) + (4*GameView.scale));
        progressBar.setWidth(GameView.scaledDefaultSide * 8);
        progressBar.setHeight(GameView.scaledDefaultSide / 4);

        progressBar.bgColor = Color.parseColor("#000000");
        progressBar.fgColor = Color.parseColor("#FFFFFF");
        progressBar.color = new float[]{.25f,.25f,.25f,1};
        UI.addControl(progressBar);
        progressBar.visible = false;

        confirm = new UI.Confirm("confirm",
                context.getResources().getString(R.string.continue_game), (float) GameView.scaledDefaultSide, GameView.metrics.heightPixels / 2f);
        confirm.addUIListener(new UI.UIListener() {
            @Override
            public void onTouchStart(Object sender, MotionEvent evt) {}

            @Override
            public void onTouchEnd(Object sender, MotionEvent evt) {loadResult = sender.toString();}

            @Override
            public void onMove(Object sender, MotionEvent evt) {}
        });
        UI.addControl(confirm);
        confirm.visible = false;


        audioControl = new UI.Button("audioControl","",
                gemOffsetX + (GameView.scaledDefaultSide * 4f) + (this.getWidth() / 2),
                gemOffsetY + (GameView.scaledDefaultSide * 8.5f) + (20 * GameView.scale) ,0,0, TYPE.AUDIOCONTROL, GameView.playBGM ? 0 : 1);
        audioControl.addUIListener(new UI.UIListener() {
            @Override
            public void onTouchStart(Object sender, MotionEvent evt) {}

            @Override
            public void onTouchEnd(Object sender, MotionEvent evt) {
                GameView.playBGM = !GameView.playBGM;
                saveAudioPref();
                try {
                    playPuzzleBGM();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ((UI.Button) sender).setAnimationIndex(GameView.playBGM ? 0 : 1);
            }

            @Override
            public void onMove(Object sender, MotionEvent evt) {}
        });
        UI.addControl(audioControl);
        audioControl.visible = false;

        showExit = new UI.Button(context.getResources().getString(R.string.exit_game),"",
                gemOffsetX + (GameView.scaledDefaultSide * .5f) + (this.getWidth() / 2),
                gemOffsetY + (GameView.scaledDefaultSide * 8.5f) + (20 * GameView.scale) ,0,0, TYPE.CANCELRELOAD, 0);
        showExit.addUIListener(new UI.UIListener() {
            @Override
            public void onTouchStart(Object sender, MotionEvent evt) {}

            @Override
            public void onTouchEnd(Object sender, MotionEvent evt) {
                exitPuzzle.visible = true;
                selectedGem = null;
                GameView.disableTouch = true;
                GameView.GLRenderer._boardReady = false;
                showExit.visible  = false;
                showReload.visible = false;
                audioControl.visible = false;
            }

            @Override
            public void onMove(Object sender, MotionEvent evt) {}
        });
        UI.addControl(showExit);
        showExit.visible = false;

        exitPuzzle = new UI.Confirm("exitPuzzle",context.getResources().getString(R.string.confirm_exit), (float) GameView.scaledDefaultSide, GameView.metrics.heightPixels / 2f);
        exitPuzzle.addUIListener(new UI.UIListener() {
            @Override
            public void onTouchStart(Object sender, MotionEvent evt) {}

            @Override
            public void onTouchEnd(Object sender, MotionEvent evt) {
                if(sender == context.getResources().getString(R.string.button_cancel)) {
                    GameView.GLRenderer._boardReady = true;
                    showExit.visible  = true;
                    showReload.visible = true;
                    audioControl.visible = true;
                } else {
                    curStage.moves = 0;
                    levelSelect.init(stages,GameView.board);
                    updateMarioTexture = true;
                    ((UI.InfoBox)UI.findControlById("infoBox")).visible = false;
                    progressBar.visible = false;
                    levelSelect.visible = true;
                    showExit.visible  = false;
                    audioControl.visible = false;
                    showReload.visible = false;
                    confirm.visible = false;
                    Collections.shuffle(Media.tracks);
                    Media.stopPuzzleBGM();
                    if(GameView.playBGM)
                        Media.setBackGroundMusic(R.raw.title);
                    //GameView.GLRenderer.SetRandomBackGround(-1);
                }
                exitPuzzle.visible = false;
            }

            @Override
            public void onMove(Object sender, MotionEvent evt) {}
        });
        UI.addControl(exitPuzzle);
        exitPuzzle.visible = false;

        showReload = new UI.Button("reload","",
                gemOffsetX + (GameView.scaledDefaultSide * 2) + (this.getWidth() / 2),
                gemOffsetY + (GameView.scaledDefaultSide * 8.5f) + (20 * GameView.scale) ,0,0, TYPE.CANCELRELOAD, 1);
        showReload.addUIListener(new UI.UIListener() {
            @Override
            public void onTouchStart(Object sender, MotionEvent evt) {}

            @Override
            public void onTouchEnd(Object sender, MotionEvent evt) {
                reloadPuzzle.visible = true;
                selectedGem = null;
                GameView.disableTouch = true;
                GameView.GLRenderer._boardReady = false;
                showExit.visible  = false;
                showReload.visible = false;
            }

            @Override
            public void onMove(Object sender, MotionEvent evt) {}
        });
        UI.addControl(showReload);
        //showReload.updateVertexBuffer = true;
        showReload.visible = false;
        reloadPuzzle = new UI.Confirm("exitPuzzle",context.getResources().getString(R.string.confirm_reload), (float) GameView.scaledDefaultSide, GameView.metrics.heightPixels / 2f);
        reloadPuzzle.addUIListener(new UI.UIListener() {
            @Override
            public void onTouchStart(Object sender, MotionEvent evt) {}

            @Override
            public void onTouchEnd(Object sender, MotionEvent evt) {
                if(sender == context.getResources().getString(R.string.button_cancel)) {
                    GameView.GLRenderer._boardReady = true;
                    showExit.visible  = true;
                    showReload.visible = true;
                } else {
//                    String stage = moveHistory.size() > 0 ? moveHistory.get(0) : settings.getString("boardState", "");
//                    loadStage(new Stage(stage), stageIndex);
//                    showExit.visible  = true;
//                    showReload.visible = true;

                    //reloadStage();

                    String stage = moveHistory.size() > 0 ? moveHistory.get(0) : GameView.settings.getString(BOARD_STATE, "");
                    loadStage(new Stage(stage), stageIndex);

                    progressBar.reset();
                    ((UI.InfoBox)UI.findControlById("infoBox")).setHighScore(getHighScore(curStage.id).highScore);
                    curStage.moves = 0;
                    GameView.board.levelComplete = false;
                    GameView.board.selectedGem = null;
                    GameView.board.chains = null;
                    nextButton.visible = false;
                    levelCompleted.visible = false;
                    stars.visible = false;
                    checkComplete = true;
                    showExit.visible  = true;
                    showReload.visible = true;
                }
                reloadPuzzle.visible = false;
            }

            @Override
            public void onMove(Object sender, MotionEvent evt) {}
        });
        UI.addControl(reloadPuzzle);
        reloadPuzzle.visible = false;

        UI.InfoBox infoBox = new UI.InfoBox("infoBox", 0f, GameView.scale * 5);
        infoBox.setX((GameView.metrics.widthPixels / 2f) - (infoBox.getWidth() / 2));
        infoBox.setY( GameView.scale >= 2 ? 42f : 0);
        infoBox.visible = false;
        UI.addControl(infoBox);

        levelCompleted = new UI.LevelCompleted(progressBar);
        levelCompleted.setX(GameView.metrics.widthPixels / 2);
        levelCompleted.setY(GameView.metrics.heightPixels / 2);
        UI.addControl(levelCompleted);

        levelSelect = new UI.LevelSelect(stages, this);
        levelSelect.visible = false;
        levelSelect.setWidth(GameView.screenW);
        levelSelect.setHeight(GameView.screenH);
        levelSelect.offSetX = GameView.screenW / 2;
        levelSelect.offSetY = GameView.screenH / 2;
//        levelSelect.addUIListener(new UI.UIListener() {
//            @Override
//            public void onTouchStart(Object sender, MotionEvent evt) {
//                //GameView.board.loadResult = ((UI.Button) sender).id;
//            }
//
//            @Override
//            public void onTouchEnd(Object args, MotionEvent evt) {
//
//            }
//
//            @Override
//            public void onMove(Object sender, MotionEvent evt) {
//
//            }
//        });
        UI.addControl(levelSelect);

        stars = new UI.Stars();
        stars.setX(GameView.screenW / 2);
        stars.setY((GameView.screenH / 2) + 68);
        stars.visible = false;
        stars.setStars(0);
        UI.addControl(stars);
    }

    private int calculateScore(boolean lastScore){
        Float score = 0f;
        if (chains == null || chains.size() == 0) return 0;
        for (int index = 0; index < chains.size(); index++) {
            Chain chain = chains.get(index);
            float iScore = 0;
            for (int i = 0; i < chain.chained.size(); i++) {
                iScore = ((i + 1) * (float)Math.cbrt(i)) * (i > 4 ? 4f/i : 1);
            }
            score += iScore * 10 * (chain.loop ? (int)Math.sqrt(chain.chained.size()) : 1) * (lastScore ? Math.max(getScoreMultiplier(),1f) : 1)/*( lastScore ? Math.max( 16 / (16 - Math.min((curStage.targetMoves - curStage.moves),15)) , 1) : 1 )*/;
        }
        return Math.round(score);
    }

    private float getScoreMultiplier(){
        return curStage.moves > 0 ? ((float)curStage.targetMoves / curStage.moves) : 0;
    }

    private void createEntities(Stage stage){
        //fg.Render.cached[TYPE.MARIO] = null;
        String[] rows = stage.tiles;
        rowsCount = rows.length;
        colsCount = rows[0].length();
        entities = new Gem[rowsCount][colsCount];
        String row;
        for (int i = 0; i < rowsCount; i++) {
            row = rows[i];
            char col;
            for (int k = 0;k < row.length(); k++) {
                col = row.charAt(k);
                if (this.randomEntities || !String.valueOf(col).matches("[#0-9]")) {
                    Integer cx = 0, cy = 0, idx = 0;
                    if (this.randomEntities || ((!checkChar(row,k + 1,null) || !checkChar(row,k + 1,"[0-9]")) && (!checkChar(row,i + 1,null) || (i+1 < rows.length && !checkChar(rows[i + 1],k,"[0-9]"))))) {
                        this.addEntity(row, this.randomEntities || (!this.randomEntities && !String.valueOf(col).matches("[ #0-9]")) ? this.getEntity(String.valueOf(col)) : this.getEntity(null)  , i, k, cx, cy, idx);
                    } else {
                        if ((!String.valueOf(row.charAt(k + 1)).trim().isEmpty() && String.valueOf(row.charAt(k + 1)).matches("[0-9]")
                                && (String.valueOf(row.charAt(i + 1)).trim().isEmpty() || !String.valueOf(rows[i + 1].charAt(k)).matches("[0-9]")))) //multiply rows
                            this.addEntityColumn(row, col, i, k, cx, cy, idx);
                        else if ((!rows[i + 1].isEmpty() && String.valueOf(rows[i + 1].charAt(k)).matches("[0-9]"))
                                && (String.valueOf(row.charAt(k + 1)).trim().isEmpty() || !String.valueOf(row.charAt(i + 1)).matches("[0-9]"))) //multiply columns
                            this.addEntityRow(rows, row, col, i, k, cx, cy, idx);
                        else
                            this.addEntityArea(rows, row, col, i, k, cx, cy, idx);
                    }
                }
            }
        }
        this.curStage.gemCount = 64 - marioBuffer.size();
        this.loadLevelCompleted();
    }

    private boolean checkChar(String str, Integer pos, String rgex){
        if(pos >= str.length() || pos < 0) return false;
        if(rgex != null && !str.matches(rgex)) return false;
        return true;
    }

    private void loadLevelCompleted() {
        while (this.marioBuffer.size() > 0) {
            ((Mario)this.marioBuffer.get(this.marioBuffer.size() - 1)).setSubTiles(false);
            if (Mario.marioCache.get(((Mario)this.marioBuffer.get(this.marioBuffer.size() - 1)).tileSet) == null) {
                Mario.marioCache.put(((Mario)this.marioBuffer.get(this.marioBuffer.size() - 1)).tileSet, (Mario.marioCache.size()) * GameView.scaledDefaultSide);
            }
            this.marioBuffer.get(this.marioBuffer.size() - 1).cacheX = Mario.marioCache.get(((Mario)this.marioBuffer.get(this.marioBuffer.size() - 1)).tileSet) % (GameView.scaledDefaultSide * 10);
            this.marioBuffer.get(this.marioBuffer.size() - 1).cacheY = (int)Math.floor(Mario.marioCache.get(((Mario)this.marioBuffer.get(this.marioBuffer.size() - 1)).tileSet) / (GameView.scaledDefaultSide * 10)) * GameView.scaledDefaultSide;
            this.marioBuffer.remove(this.marioBuffer.size() - 1);
        }
        //this.stages = window['levelStages'];
        if (this.curStage == null) this.setCurStage(0);
        Mario.RenderTiles(this);
        progressBar.visible = true;
    }
    private void setCurStage(int index){
        this.curStage = this.stages.get(index);
    }

    //<editor-fold>
    private void addEntityArea(String[] rows, String row, char col, int i, int k, Integer cx, Integer cy, Integer idx) {
//        HashMap<String,Integer> computedPos;
//        for (int kIndex = 0; kIndex <= row.charAt(k + 1); kIndex++) {
//            for (int iIndex = 0; iIndex <= rows[i + 1].charAt(k); iIndex++) {
//                //if (this.entities[i + iIndex] == null) this.entities[i + iIndex] = [];
//                if (iIndex == 0) {
//                    if (kIndex == 0) computedPos = this.computeEntityAreaPos(5, 1, 1);
//                    else if (kIndex == row.charAt(k + 1)) computedPos = this.computeEntityAreaPos(7, 3, 1);
//                    else computedPos = this.computeEntityAreaPos(6, 2, 1);
//                } else if (iIndex == rows[i + 1].charAt(k)) {
//                    if (kIndex == 0) computedPos = this.computeEntityAreaPos(13, 1, 3);
//                    else if (kIndex == row.charAt(k + 1)) computedPos = this.computeEntityAreaPos(15, 3, 3);
//                    else computedPos = this.computeEntityAreaPos(14, 2, 3);
//                } else {
//                    if (kIndex == 0) computedPos = this.computeEntityAreaPos(9, 1, 2);
//                    else if (kIndex == row.charAt(k + 1)) computedPos = this.computeEntityAreaPos(11, 3, 2);
//                    else computedPos = this.computeEntityAreaPos(10, 2, 2);
//                }
//                this.entities[i + iIndex][k + kIndex] = Entity.create((i + iIndex) + "-" + (k + kIndex), this.getEntity(String.valueOf(col)), (float)GameView.defaultSide * (k + kIndex), (float)GameView.defaultSide * (i + iIndex), computedPos.get("cx"), computedPos.get("cy"), this);
//            }
//        }
    }

//    public HashMap<String,Integer> computeEntityAreaPos (Integer idx, Integer xMultiplyer, Integer yMultiplyer) {
//        int cx = Math.round(GameView.defaultSide) * xMultiplyer;
//        int cy = Math.round(GameView.defaultSide) * yMultiplyer;
//        HashMap<String,Integer> response = new HashMap<>();
//        response.put("idx",idx);
//        response.put("cx",cx);
//        response.put("cy",cy);
//        return response;
//    }

    private void addEntityRow(String[] rows, String row, char col, int i, int k, Integer cx, Integer cy, Integer idx) {
//        for (int index = 0; index <= rows[i + 1].charAt(k); index++) {
//            //if (this.entities[i + index] ) this.entities[i + index] = [];
//            cy = GameView.defaultSide;
//            if ("╝╚╗╔".indexOf(col) < 0) {
//                if (index == 0) idx = 4;
//                else if (index == rows[i + 1].charAt(k)) cy *= (idx = (12 / 4));
//                else cy *= (idx = (8 / 4));
//            } else
//                cy = ((parseInt(String.valueOf(rows[i + 1].charAt(k))) * (parseInt(String.valueOf(rows[i + 1].charAt(k))) + 1)) / 2 * GameView.defaultSide) + (index * GameView.defaultSide);
//            this.entities[i + index][k] = Entity.create((i + index) + "-" + k, this.getEntity(String.valueOf(col)), (float)GameView.defaultSide * k, (float)GameView.defaultSide * (i + index), cx, cy, this);
//            //if (this.entities[i + index][k].setYs) this.entities[i + index][k].setYs(null, rows[i + 1][k]);
//        }
    }

    private void addEntityColumn(String row, char col, int i, int k, Integer cx, Integer cy, Integer idx) {
//        for (int index = 0; index <= (int)row.charAt(k + 1); index++) {
//            cx = Math.round(GameView.defaultSide);
//            if ("╝╚╗╔".indexOf(col) < 0) {
//                if (index == 0) idx = 1;
//                else if (index == row.charAt(k + 1)) cx *= (idx = 3);
//                else cx *= (idx = 2);
//            } else
//                cx = ((parseInt(String.valueOf(row.charAt(k + 1))) * (parseInt(String.valueOf(row.charAt(k + 1))) + 1)) / 2 * Math.round(GameView.defaultSide)) + (index * Math.round(GameView.defaultSide));
//            this.entities[i][k + index] = Entity.create(i + "-" + (k + index), this.getEntity(String.valueOf(col)), (float)GameView.defaultSide * (k + index), (float)GameView.defaultSide * i, cx, cy, this);
//            //if (this.entities[i][k + index].setYs) this.entities[i][k + index].setYs(row[k + 1], null);
//            //if (index > 0) this.entities[i][k].segments.push({ l: i, c: k + index });
//        }
    }
    //</editor-fold>

    Random r = new Random();
    private void addEntity(String row, TYPE type, int i, int k, Integer cx, Integer cy, Integer idx) {
        this.entities[i][k] = (Gem)Entity.create(i + "-" + k, type, (float)GameView.scaledDefaultSide * k, (float)GameView.scaledDefaultSide * i, cx, cy, this);
        if(this.entities[i][k] == null) return;
        this.entities[i][k].setOffSetX(gemOffsetX);
        this.entities[i][k].setOffSetY(gemOffsetY);
        if (this.entities[i][k] == null) return;
        //if (this.entities[i][k].setYs) this.entities[i][k].setYs(null, null);
        if (this.entities[i][k].type == TYPE.MARIO) this.marioBuffer.add(this.entities[i][k]);
        //if((this.entities[i][k]).id.equals("3-5") && this.entities[i][k].type != TYPE.MARIO) this.entities[i][k].doScale = this.entities[i][k].rotate = true;
        if(Math.abs(r.nextInt()) % 5 == 0//(this.entities[i][k]).id.equals("4-4")
                && this.entities[i][k].type != TYPE.MARIO) this.entities[i][k].transform.scaleIn(4f,this.entities[i][k].scale,0.5f);
    }

    private TYPE getEntity(String type) {
        String color = "";
        if(type != null && type.trim().isEmpty())
        {
            if(curStage.sets != null) {
                if (curStage.curSet == null) {
                    curStage.colorSet = new ArrayList<>(); //new String[Arrays.asList(curStage.sets).indexOf(0) - 1];
                    curStage.curSet = new Integer[8];
                    curStage.colorSetSize = Arrays.asList(curStage.sets).indexOf(0);
                    Integer setsCount = curStage.sets.length / 8;
                    Integer selectedSet = (int) Math.floor(Math.random() * setsCount);
                    for (int i = 0; i < 8; i++) {
                        curStage.curSet[i] = curStage.sets[(selectedSet * 8) + i];
                    }
                }
                color = String.valueOf(Math.round(Math.random() * 7));
                Integer colorIndex = curStage.colorSet.indexOf(color);
                if (colorIndex < 0) {
                    if (curStage.colorSetSize < 0 || curStage.colorSet.size() < curStage.colorSetSize) {
                        colorIndex = curStage.colorSet.size();
                        curStage.colorSet.add(color);
                    }
                }
                int loopCount = 0;
                while (colorIndex < 0 || curStage.curSet[colorIndex] - 1 < 0) {
                    color = String.valueOf(Math.round(Math.random() * 7));
                    colorIndex = curStage.colorSet.indexOf(color);
                    loopCount++;
                    if(loopCount > 64) return null;
                }
                curStage.curSet[colorIndex]--;
            } else color = String.valueOf(Math.round(Math.random() * 7));
        }
        else if (type != null)
            color = type;
        switch (color) {
            case "G":
            case "0":
                return TYPE.GREENGEM;//4
            case "R":
            case "1":
                return TYPE.REDGEM;//9
            case "Y":
            case "2":
                return TYPE.YELLOWGEM;//13
            case "P":
            case "3":
                return TYPE.PURPLEGEM;//9
            case "B":
            case "4":
                return TYPE.BLUEGEM;//11
            case "W":
            case "5":
                return TYPE.WHITEGEM;//13
            case "O":
            case "6":
                return TYPE.ORANGEGEM;//5
            case "C":
            case "7":
                return TYPE.CYANGEM;//5
            case "M":
                return TYPE.MARIO;//5
            default:
                return null;
        }
    }

    public boolean autoLoad = true;
    @Override
    public void update(){
        if(this.clearChains) {
            this.chains = null;
            this.clearChains = false;
        }
        UI.update();
        if(Timer.ticks > 180 && !GameView.started && autoLoad){
            GameView.started = true;
            GameView.renderer.title.visible = false;
        }
        if(Timer.ticks > 360 && autoLoad){
            autoLoad = false;
            loadResult = "24";
        }
        if(!GameView.started) {
            return;
        } else {
            if (!loaded){
                loaded = true;
                String boardState = GameView.settings.getString(BOARD_STATE,"");
                if(boardState.length() > 0) {
                    confirm.visible = true;
                } else {
                    //loadStage(0);
                    levelSelect.visible = true;
                    levelSelect.getCurPage().page.transform.fadeIn(0,1f,0.5f);
                }
            } else if (confirm.visible || levelSelect.visible) {
                if(loadResult.equals("")) return;
                if (loadResult.equals("OK")) {
                    stageIndex = GameView.settings.getInt(STAGE_INDEX, 0);
                    loadStage(new Stage(GameView.settings.getString(BOARD_STATE, "")), stageIndex);
                    ((UI.InfoBox)UI.findControlById("infoBox")).setTargetScore(curStage.targetScore);
                    ((UI.InfoBox)UI.findControlById("infoBox")).setTitle(this.curStage.name);
                } else {
                    confirm.visible = false;
                    if(!levelSelect.visible) levelSelect.getCurPage().page.transform.fadeIn(0,1f,0.5f);
                    levelSelect.visible = true;

                    if(loadResult.equals(context.getResources().getString(R.string.button_cancel))) return;
                    stageIndex = Integer.parseInt(loadResult);
                    loadStage(stageIndex);
                    ((UI.InfoBox)UI.findControlById("infoBox")).setTargetScore(curStage.targetScore);
                    //levelSelect.visible = true;
                }
                ((UI.InfoBox)UI.findControlById("infoBox")).setHighScore(getHighScore(curStage.id).highScore);
                confirm.visible = false;
                loadResult = "";
            }
        }
        if(GameView.GLRenderer._boardReady) {
            GameView.disableTouch = false;
            for (Integer i = 0; i < rowsCount; i++) {
                for (Integer k = 0; k < colsCount; k++) {
                    Gem entity = entities[i][k];
                    if (entity == null) continue;
                    if (entity.moveTo != null) GameView.disableTouch = true;
                    entity.update();
                    if (entity.getClass().equals(Gem.class) && chains == null)
                        entity.checked = false;
                }
            }
            //progressBar.setValue(curStage.score / (float)curStage.targetScore);
            showExit.visible  = !levelCompleted.visible;
            showReload.visible = !levelCompleted.visible;
            audioControl.visible = !levelCompleted.visible;
        }
        if(this.levelComplete && checkComplete){
            //GameView.GLRenderer._boardReady = false;
            ((UI.InfoBox)UI.findControlById("infoBox")).setPuzzleScore(calculateScore(true));
            this.curStage.score += this.calculateScore(true);
            //this.curStage.score = this.curStage.score * Math.max( 16 / (16 - Math.min((curStage.targetMoves - curStage.moves),15)) , 1);
            //progressBar.setValue(curStage.score / (float)curStage.targetScore);
            progressBar.setValue(this.getScoreMultiplier() / 2f);
            //if(curStage.score >= (float)curStage.targetScore) {
            //if(this.getScoreMultiplier() >= 1) {
                levelCompleted.visible = true;
                levelCompleted.transform.scaleIn(4,levelCompleted.scale,0.5f);
                stars.visible = true;
            //}
            checkComplete = false;
            ((UI.InfoBox)UI.findControlById("infoBox")).transferScore();
        } else nextButton.visible = this.levelComplete && levelCompleted.canLoadNextLevel();
        if(this.levelComplete) {
            if(progressBar.getValue() < .5f) stars.setStars(0);
            else if(progressBar.getValue() >= .5f && progressBar.getValue() < .75f) stars.setStars(1);
            else if(progressBar.getValue() >= .75f && progressBar.getValue() < 1f) stars.setStars(2);
            else stars.setStars(3);
        }

    }
    private boolean checkComplete = true;

    @Override
    public void draw() {

        if (!GameView.GLRenderer._boardReady) {
            if (this.curStage != null) {
                GameView.GLRenderer.changeProgram(loading.mProgram.getProgramId(), loading.vertexBuffer);
                loading.draw();
            }
            return;
        }
        ((UI.InfoBox) UI.findControlById("infoBox")).visible = true;

        /********** Use EntityCollection **********/
        this.marioCol.draw();
        this.gemCol.draw();
        /********** Use EntityCollection **********/
    }

    float minValue = 1f;
    public void nextStage(){
        GameView.GLRenderer._boardReady = false;
        if (this.curStage == null) this.setCurStage(0);
        //this.curStage.score += this.calculateScore();
        saveScore();
        //if (this.curStage.score >= this.curStage.targetScore) {
        if (this.getScoreMultiplier() >= 0) {
            //progressBar.setValue(curStage.score / (float)curStage.targetScore);
            this.levelComplete = false;
            loadNextLevel();
            //showLevelCompleted = true;
            //levelCompleted.visible = true;
        } else {
            //this.curStage.score += this.calculateScore();
            reloadStage();
            progressBar.reset();
        }
        ((UI.InfoBox)UI.findControlById("infoBox")).setHighScore(getHighScore(curStage.id).highScore);
        curStage.moves = 0;
        this.levelComplete = false;
        this.selectedGem = null;
        this.chains = null;
        nextButton.visible = false;
        levelCompleted.visible = false;
        stars.visible = false;
        checkComplete = true;
    }

    private void loadNextLevel() {
        progressBar.reset();
        if (GameView.cycleBG) {
            if (GameView.renderer.backGround.index + 1 < GameView.renderer.backGround.programs.size()) {
                GameView.renderer.backGround.index++;
            } else GameView.renderer.backGround.index = 0;
        }
        GameView.renderer.backGround.mProgram = GameView.renderer.backGround.programs.get(GameView.renderer.backGround.index);

        if(stageIndex + 1 < this.stages.size())
            this.setCurStage(stageIndex++);
        else
            this.setCurStage(stageIndex = 0);
        //this.createEntities(this.curStage);
        loadStage(stageIndex);
        ((UI.InfoBox)UI.findControlById("infoBox")).setTargetScore(curStage.targetScore);
        updateMarioTexture = true;
    }

    boolean parseBoard(){
        if (chains == null && (selectedGem == null || ((Gem)selectedGem).moveTo == null )) {
            //if(selectedGem != null) curStage.moves++;
            this.chains = new ArrayList<>();
            this.findChainTypes();
            for (Chain chain : chains) {
                chain.checks = 0;
                while (chain.elements.size() > 0) {
                    Gem element = chain.elements.get(0);
                    if (chain.count == 1) {
                        chain.checks = 1;
                        chain.complete = true;
                        chain.chained.add(chain.elements.remove(chain.elements.indexOf(element)));
                        element.checked = true;
                        continue;
                    }
                    checkSides(element, chain);
                }
                if(chain.complete && this.contactPoints(chain.chained.get(0), chain).indexOf(chain.chained.get(chain.count - 1)) >= 0) chain.loop = true;
            }
            trySave = 0;
            saveState();
            ((UI.InfoBox)UI.findControlById("infoBox")).setScore(curStage.score);
            ((UI.InfoBox)UI.findControlById("infoBox")).setPuzzleScore(calculateScore(false));
            ((UI.InfoBox)UI.findControlById("infoBox")).setChains(chains);
        }
        if (chains == null || chains.size() <= 0) return false;
        this.levelComplete = true;
        Collections.sort(chains, new Comparator<Chain>() {
            @Override
            public int compare(Chain a, Chain b) {
                // -1 - less than, 1 - greater than, 0 - equal, all inverted for descending
                return a.id.compareTo(b.id) > 0 ? -1 : 1;
            }
        });
        for (int index = 0; this.chains != null && index < this.chains.size(); index++) {
            Chain chain = this.chains.get(index);
            if (chain.count != chain.checks) this.levelComplete = false;
        }
        return false;
    }

    int trySave = 0;
    private void saveState(){

        try{
            String boardState = this.boardToJSON();
            moveHistory.add(boardState);
            SharedPreferences.Editor editor = GameView.settings.edit();
            editor.putString(BOARD_STATE, boardState);
            editor.putInt(STAGE_INDEX, this.stageIndex);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
//            if(e.getClass().equals(java.lang.NullPointerException.class) && trySave < 20){
//                trySave++;
//                saveState();
//            }
        }
    }

    private void saveScore(){
        SharedPreferences.Editor editor = GameView.settings.edit();
        String stageScores = GameView.settings.getString(STAGE_SCORES + curStage.id,"{\"highScore\":0, \"highScoreMoves\":0, \"scores\":[], \"moves\":[]}");
        JSONObject obj;
        JSONArray scores = null;
        JSONArray moves = null;
        Integer highScore = null;
        Integer highScoreMoves = 0;
        try {
            obj = new JSONObject(stageScores);
            highScore = Integer.parseInt(obj.getString("highScore"));
            highScoreMoves = Integer.parseInt(obj.getString("highScoreMoves"));
            scores = obj.getJSONArray("scores");
            moves = obj.getJSONArray("moves");
            scores.put(calculateScore(true));
            moves.put(curStage.moves);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(curStage.score > highScore) {
            highScore = curStage.score;
            highScoreMoves = curStage.moves;
        }
        editor.putString(STAGE_SCORES + curStage.id,
                String.format("{\"highScore\":%s,\"highScoreMoves\":%s, \"scores\":%s, \"moves\":%s}", highScore, highScoreMoves, scores.toString(),moves.toString()));
        editor.commit();
    }

    private void saveAudioPref(){
        try{
            SharedPreferences.Editor editor = GameView.settings.edit();
            editor.putBoolean(GameView.PLAY_BGM, GameView.playBGM);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HighScore getHighScore(String id){
        String stageScores = GameView.settings.getString(STAGE_SCORES + id,"");
        //if(stageScores.length() == 0) return new HighScore();
        JSONObject obj;
        HighScore highScore = new HighScore();
        try {
            obj = new JSONObject(stageScores.length() != 0 ? stageScores : "{\"highScore\":0}");
            highScore.highScore = stageScores.length() != 0 ? Integer.parseInt(obj.getString("highScore")) : this.stages.get(Integer.parseInt(id)).highScore;
            highScore.moves =  obj.has("highScoreMoves") ? Integer.parseInt(obj.getString("highScoreMoves")) : 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return highScore;
    }

    class HighScore{
        public Integer highScore = 0;
        public Integer moves = 0;
        public HighScore(){}
    }

    private void findChainTypes(){
        for (Gem[] row : entities) {
            for (Gem aRow : row) {
                if (aRow == null || !((Gem) aRow).getClass().equals(Gem.class)) continue;
                Chain chainType = null;
                for (Chain chn : chains) {
                    if (chn.id.equals(aRow.type.toString())) chainType = chn;
                }
                List<Gem> gemList = new ArrayList<>();
                gemList.add(aRow);
                if (chainType == null)
                    this.chains.add(new Chain(aRow.type.toString(), gemList, 1, false));
                else {
                    chainType.elements.add(aRow);
                    chainType.count++;
                }
            }
        }
    }

    private void checkSides(Gem gem, Chain chain){
        Integer posX = parseInt(gem.id.split("-")[1]), posY = parseInt(gem.id.split("-")[0]);
        chain.elements.remove(chain.elements.indexOf(gem));
        List<Gem> elements = new ArrayList<>();
        List<Gem> cPoints;
        elements.addAll(chain.chained);
        elements.addAll(chain.elements);
        //Gem[] elements = chain.elements.concat(chain.chained),
        cPoints = this.contactPoints(gem, chain);
        if(cPoints.size() > 2) chain.checks = 0;
        for (int i = 0; i < 4; i++) {
            Gem check;
            switch (i) {
                case 0:
                    if (posX - 1 < 0) break;
                    check = null;
                    for (Gem gn: elements) if(gn.id.equals((posY) + "-" + (posX - 1))) check = gn;
                    if (check != null) this.checkGemType(check, gem, chain);
                    if (chain.complete) return;
                    break;
                case 1:
                    if (posY - 1 < 0) break;
                    check = null;//var check = elements.find(function (e) { return e.id == (posY - 1) + '-' + (posX); });
                    for (Gem gn: elements) if(gn.id.equals((posY - 1) + "-" + (posX))) check = gn;
                    if (check != null) this.checkGemType(check, gem, chain);
                    if (chain.complete) return;
                    break;
                case 2:
                    if (posX + 1 >= entities[0].length) break;
                    check = null;//elements.find(function (e) { return e.id == (posY) + '-' + (posX + 1); });
                    for (Gem gn: elements) if(gn.id.equals((posY) + "-" + (posX + 1))) check = gn;
                    if (check != null) this.checkGemType(check, gem, chain);
                    if (chain.complete) return;
                    break;
                default:
                    if (posY + 1 >= entities.length) break;
                    check = null;//elements.find(function (e) { return e.id == (posY + 1) + '-' + (posX); });
                    for (Gem gn: elements) if(gn.id.equals((posY + 1) + "-" + (posX))) check = gn;
                    if (check != null) this.checkGemType(check, gem, chain);
                    if (chain.complete) return;
                    break;
            }
        }
        if (chain.checks >= chain.count) chain.complete = true;
    }

    public void checkGemType(Gem check, Gem gem, Chain chain){
        if (check != null && check.type == gem.type) {
            if(chain.chained.size() == 0) chain.entryPoint = gem.id;
            if (check.checked && (gem.id != chain.entryPoint || chain.checks >= chain.count || (gem.id == chain.entryPoint && chain.checks < chain.count))) return;
            check.checked = true;
            if(chain.checks == 0) chain.checks = 1;
            chain.checks++;
            if (chain.chained.size() == 0) chain.chained.add(gem);
            chain.chained.add(check);
            if (!gem.checked) gem.checked = true;
            this.checkSides(check, chain);
        }
    }

    public List<Gem> contactPoints(Gem gem, Chain chain){
        Integer posX = parseInt(gem.id.split("-")[1]);
        Integer posY = parseInt(gem.id.split("-")[0]);
        List<Gem> cPoints = new ArrayList<>();
        List<Gem> elements = new ArrayList<>();
        elements.addAll(chain.chained);
        elements.addAll(chain.elements);
        for (int i = 0; i < 4; i++) {
            Gem check;
            switch (i) {
                case 0:
                    if (posX - 1 < 0) break;
                    check = null;//var check = elements.find(e.id == (posY) + "-" + (posX - 1); });
                    for (Gem gn: elements) if(gn.id.equals((posY) + "-" + (posX - 1))) check = gn;
                    if (check != null) cPoints.add(check);
                    break;
                case 1:
                    if (posY - 1 < 0) break;
                    check = null;//var check = elements.find(function (e) { return e.id == (posY - 1) + "-" + (posX); });
                    for (Gem gn: elements) if(gn.id.equals((posY - 1) + "-" + (posX))) check = gn;
                    if (check != null) cPoints.add(check);
                    break;
                case 2:
                    if (posX + 1 >= entities[0].length) break;
                    check = null;//var check = elements.find(function (e) { return e.id == (posY) + "-" + (posX + 1); });
                    for (Gem gn: elements) if(gn.id.equals((posY) + "-" + (posX + 1))) check = gn;
                    if (check != null) cPoints.add(check);
                    break;
                default:
                    if (posY + 1 >= entities.length) break;
                    check = null;//var check = elements.find(function (e) { return e.id == (posY + 1) + "-" + (posX); });
                    for (Gem gn: elements) if(gn.id.equals((posY + 1) + "-" + (posX))) check = gn;
                    if (check != null) cPoints.add(check);
                    break;
            }
        }
        return cPoints;
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

    public String boardToJSON(){
        String[] ents = new String[8];
        for (int i = 0; i < this.entities.length; i++) {
            for (int j = 0; j < this.entities[i].length; j++) {
                if(ents[i] == null) ents[i] = "";
                ents[i] += String.valueOf(this.entities[i][j].type.toString().charAt(0));
            }
        }
        return curStage.toJsonString(ents);
    }
    public void loadStage(Integer index){
        moveHistory = new ArrayList<>();
        levelSelect.visible = false;
        GameView.GLRenderer._boardReady = false;
        this.curStage = stages.get(index);
        this.curStage.curSet = null;
        this.curStage.moves = 0;
        this.curStage.score = 0;
        this.stageIndex = index;

        ((UI.InfoBox)UI.findControlById("infoBox")).setTitle(this.curStage.name);
        //levelSelect.update(stages,this);
        this.createEntities(this.curStage);
        /******* EntityCollection *********/
        BuildGemsCollections();
        /******* EntityCollection *********/
        showReload.visible = true;
        showExit.visible = true;
        audioControl.visible = true;
    }

    private void BuildGemsCollections() {
        this.gemCol = new GemCollection(R.drawable.atlas, this, false);
        this.gemCol.offSetX = gemOffsetX;
        this.gemCol.offSetY = gemOffsetY;
        this.gemCol.buildTextureMap(80,10,8);

        this.marioCol = new GemCollection(-1/*R.drawable.mario_tiles_46*/, this, true);
        this.marioCol.offSetX = gemOffsetX;
        this.marioCol.offSetY = gemOffsetY;
        //this.marioCol.buildTextureMap(80,10,8);
        this.marioCol.buildTextureMap(100,10,10);
        GameView.GLRenderer._boardReady = true;
        ((UI.InfoBox)UI.findControlById("infoBox")).setChains(null);
        chains = null;
        try {
            if(!playingPuzzleBGM) playPuzzleBGM();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearGems(){
        for (int k = 0; k < entities.length; k++) {
            Entity[] row = entities[k];
            for (int l = 0; l < row.length; l++) {
                Gem element = (Gem) row[l];
                if(element != null && ((Gem)element).getClass().equals(Gem.class)) entities[k][l] = null;
            }
        }
    }

    public void reloadStage(){
        moveHistory = new ArrayList<>();
        GameView.GLRenderer._boardReady = false;
        Integer curScore = curStage != null ? curStage.score : 0;
        clearGems();
        this.curStage = stages.get(stageIndex);
        curStage.score = curScore;
        this.createEntities(this.curStage);
        BuildGemsCollections();
    }

    public void loadStage(Stage stage, Integer index){
        moveHistory = new ArrayList<>();
        GameView.GLRenderer._boardReady = false;
        this.curStage = stage;
        this.stageIndex = index;
        this.createEntities(this.curStage);
        BuildGemsCollections();
        showReload.visible = true;
        showExit.visible = true;
        audioControl.visible = true;
    }
}
