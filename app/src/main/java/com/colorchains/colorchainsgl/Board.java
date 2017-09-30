package com.colorchains.colorchainsgl;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.colorchains.colorchainsgl.GameView.GLRenderer.updateMarioTexture;
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
    private UI.Control.Button nextButton;
    private UI.Control.ProgressBar progressBar;
    private UI.Control.Confirm confirm;

    public boolean clearChains = false;
    private static final String APP_STATES = "appStates";
    private static final String BOARD_STATE = "boardState";
    private static final String STAGE_INDEX = "stageIndex";
    public SharedPreferences settings;
    private Integer stageIndex;
    private String loadResult = "";
    private UI.Font font;
    public GemCollection gemCol;
    public GemCollection marioCol;
    public boolean parseBoard = false;

    public Board(Context context)  throws JSONException {
        super("board", TYPE.BOARD,0f,0f,1280,768);
        this.context = context;
        font = new UI.Font(R.drawable.oldskol, 2.0f);
        font.setText("Loading");
        font.setX(GameView.metrics.widthPixels / 2);
        font.setY(GameView.metrics.heightPixels / 2);
        stages = Stage.getStageList(loadJSONFromAsset("levels.json"));

        //App settings
        settings = this.context.getSharedPreferences(APP_STATES, 0);

//        gemOffsetX = 16;//(GameView.metrics.widthPixels - (92*7)) / 2;
//        gemOffsetY = 252;//(126*(int)GameView.scale);
        this.border = 8 + (GameView.is16x9 ? 4 : 0);
        gemOffsetX = (border * (int)GameView.scale);
        gemOffsetY = (126*(int)GameView.scale);
        //Log.d("Board","border: " + this.border +",gemOffsetX: " + gemOffsetX + ",gemOffsetY: " + gemOffsetY);
        addControls();
        //UI.hide();
    }

    public Board(String id, TYPE type, Float x, Float y, Integer cx, Integer cy) {
        super(id, type, x, y, cx, cy);
    }

    public void addControls(){
        nextButton = new UI.Control.Button("next","Next",
                gemOffsetX + (GameView.scaledDefaultSide * 6),
                gemOffsetY + (GameView.scaledDefaultSide * 8) + (20 * GameView.scale) ,0,0);
        UI.addControl(nextButton);
        nextButton.addUIListener(new UI.Control.Button.UIListener() {
            @Override
            public void clicked(Object sender) {
                nextStage();
            }
        });
        //if(nextButton != null) return;
        nextButton.visible = false;
        progressBar = new UI.Control.ProgressBar();
        progressBar.setX(gemOffsetX);
        progressBar.setY(gemOffsetY + (GameView.scaledDefaultSide * 8f) + (4*GameView.scale));
        progressBar.width = GameView.scaledDefaultSide * 8;
        progressBar.height = GameView.scaledDefaultSide / 4;
        progressBar.bgColor = Color.parseColor("#000000");
        progressBar.fgColor = Color.parseColor("#FFFFFF");
        UI.addControl(progressBar);
        progressBar.visible = false;

        confirm = new UI.Control.Confirm("confirm",
                "There is a previously saved game.\n Do you want to continue it?", (float) GameView.scaledDefaultSide, GameView.metrics.heightPixels / 2f);
        confirm.addUIListener(new UI.Control.Button.UIListener() {
            @Override
            public void clicked(Object arg) {
                loadResult = arg.toString();
            }
        });
        UI.addControl(confirm);
        confirm.visible = false;

        UI.InfoBox infoBox = new UI.InfoBox("infoBox", 0f, GameView.scale * 5);
        infoBox.setX((GameView.metrics.widthPixels / 2f) - (infoBox.width / 2));
        infoBox.visible = false;
        UI.addControl(infoBox);
    }
    public int calculateScore(){
        Float score = 0f;
        if (chains.size() == 0) return 0;
        for (int index = 0; index < chains.size(); index++) {
            Chain chain = chains.get(index);
            float iScore = 0;
            for (int i = 0; i < chain.chained.size(); i++) {
                iScore = ((i + 1) + i / 10);
            }
            score += iScore * 10 * (chain.loop ? 2 : 1);
        }
        return Math.round(score);
    }
    public void createEntities(Stage stage){
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
    public void setCurStage(int index){
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

    private void addEntity(String row, TYPE type, int i, int k, Integer cx, Integer cy, Integer idx) {
        this.entities[i][k] = (Gem)Entity.create(i + "-" + k, type, (float)GameView.scaledDefaultSide * k, (float)GameView.scaledDefaultSide * i, cx, cy, this);
        if(this.entities[i][k] == null) return;
        this.entities[i][k].setOffSetX(gemOffsetX);
        this.entities[i][k].setOffSetY(gemOffsetY);
        if (this.entities[i][k] == null) return;
        //if (this.entities[i][k].setYs) this.entities[i][k].setYs(null, null);
        if (this.entities[i][k].type == TYPE.MARIO) this.marioBuffer.add(this.entities[i][k]);
        if((this.entities[i][k]).id.equals("3-5")) this.entities[i][k].doScale = true;
    }

    private TYPE getEntity(String type) {
        String color = "";
        if(type != null && type.trim().isEmpty())
            color = String.valueOf(Math.round(Math.random() * 7));
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

    void update(){
        if(this.clearChains) {
            this.chains = null;
            this.clearChains = false;
        }
        if(!GameView.started) {
            return;
        } else {
            if (!loaded){
                loaded = true;
                String boardState = settings.getString(BOARD_STATE,"");
                if(boardState != null && boardState.length() > 0)
                    confirm.visible = true;
                else {
                    loadStage(0);
                }
            } else if (confirm.visible) {
                if(loadResult == "") return;
                if (loadResult == "OK") {
                    stageIndex = settings.getInt(STAGE_INDEX, 0);
                    loadStage(new Stage(settings.getString("boardState", "")), stageIndex);
                    ((UI.InfoBox)UI.findControlById("infoBox")).setTargetScore(curStage.targetScore);
                } else {
                    stageIndex = 0;
                    loadStage(stageIndex);
                }
                confirm.visible = false;
            }
        }
        if(!GameView.GLRenderer._boardReady) return;
        GameView.disableTouch = false;
        for (Integer i = 0; i < rowsCount; i++) {
            for (Integer k = 0; k < colsCount; k++) {
                Gem entity = entities[i][k];
                if(entity == null) continue;
                if(entity.moveTo != null) GameView.disableTouch = true;
                entity.update();
                if (entity.getClass().equals(Gem.class) && chains == null) entity.checked = false;
            }
        }
        if(this.levelComplete){
            nextButton.visible = true;
        }
        progressBar.setValue(curStage.score / (float)curStage.targetScore);
        UI.update();
    }

    @Override
    public void draw() {
        //Render.clearScreen();
        //Render.clearScreen(bg);//Render.clearScreen(Render.cached.get(TYPE.FONT) == null ?  bg: Render.cached.get(TYPE.FONT));//Render.clearScreen(bg);//Render.clearScreen();
        //texture.draw(gl, x, y, width, height, 0, true);
        if (!GameView.GLRenderer._boardReady) {
            if (this.curStage != null) font.draw();
            return;
        }
        ((UI.InfoBox) UI.findControlById("infoBox")).visible = true;
        //Render.draw(bg, 0, 0, false);
        /********** Use GemCollection **********/
//        for (Integer i = 0; i < rowsCount; i++) {
//            for (Integer k = 0; k < colsCount; k++) {
//                Gem entity = /*(Gem) */entities[i][k];
//                if (entity != null && !entity.selected) entity.draw();
//            }
//        }

        this.marioCol.draw();
        this.gemCol.draw();
        /********** Use GemCollection **********/
    }

    private void nextStage(){
        GameView.GLRenderer._boardReady = false;
        if (this.curStage == null) this.setCurStage(0);
        this.curStage.score += this.calculateScore();
        if (this.curStage.score >= this.curStage.targetScore) {

            if (GameView.cicleBG) {
                if (GameView.renderer.backGround.index + 1 < GameView.renderer.backGround.programs.size()) {
                    GameView.renderer.backGround.index++;
                } else GameView.renderer.backGround.index = 0;
            }
            GameView.renderer.backGround.mProgram = GameView.renderer.backGround.programs.get(GameView.renderer.backGround.index);

            this.setCurStage(++stageIndex);
            //this.createEntities(this.curStage);
            loadStage(stageIndex);
            ((UI.InfoBox)UI.findControlById("infoBox")).setTargetScore(curStage.targetScore);
            updateMarioTexture = true;
        } else {
            reloadStage();
        }
        this.levelComplete = false;
        this.selectedGem = null;
        this.chains = null;
        nextButton.visible = false;
        progressBar.visible = true;
    }

    public boolean parseBoard(){
        if (chains == null && (selectedGem == null || ((Gem)selectedGem).moveTo == null )) {
            this.chains = new ArrayList<>();
            this.findChainTypes();
            for (Chain chain : chains) {
                chain.checks = 0;
                while (chain.elements.size() > 0) {
                    Gem element = chain.elements.get(0);
                    if (chain.count == 1) {
                        chain.checks = 1;
                        chain.chained.add(chain.elements.remove(chain.elements.indexOf(element)));
                        element.checked = true;
                        continue;
                    }
                    checkSides(element, chain);
                }
                if(chain.complete && this.contactPoints(chain.chained.get(0), chain).indexOf(chain.chained.get(chain.count - 1)) >= 0) chain.loop = true;
            }
            if(curStage.score == 0) curStage.score = calculateScore();
            saveState();
            ((UI.InfoBox)UI.findControlById("infoBox")).setScore(curStage.score);
            ((UI.InfoBox)UI.findControlById("infoBox")).setPuzzleScore(calculateScore());
        }
        if (chains == null || chains.size() <= 0) return false;
        this.levelComplete = true;
//        this.chains.sort(function (a, b) {
//            if (a.id < b.id) return -1;
//            if (a.id > b.id) return 1;
//        });
        Collections.sort(chains, new Comparator<Chain>() {
            @Override
            public int compare(Chain a, Chain b) {
                // -1 - less than, 1 - greater than, 0 - equal, all inverted for descending
                return a.id.compareTo(b.id) > 0 ? -1 : 1;
            }
        });
        for (int index = 0; this.chains != null && index < this.chains.size(); index++) {
            Chain chain = this.chains.get(index);
            //this.drawChainIcon(chain.id, 8, 390 + (index * 8));
            //this.mainFontSmall.draw(": " + chain.count + (chain.count == chain.checks ? " $" : ""), 16, 390 + (index * 8));
            if (chain.count != chain.checks) this.levelComplete = false;
        }
        return false;
    }

    private void saveState(){
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("boardState", this.boardToJSON());
        editor.putInt("stageIndex", this.stageIndex);
        editor.commit();
    }

    public void findChainTypes(){
        for (int k = 0; k < entities.length; k++) {
            Entity[] row = entities[k];
            for (int l = 0; l < row.length; l++) {
                Gem element = (Gem) row[l];
                if(element == null || !((Gem)element).getClass().equals(Gem.class)) continue;
                Chain chainType = null;
                for (Chain chn: chains) {
                    if(chn.id == element.type.toString()) chainType = chn;
                }
                List<Gem> gemList = new ArrayList<>();
                gemList.add(element);
                if (chainType == null)
                    this.chains.add(new Chain(element.type.toString(), gemList, 1, false));
                else
                {
                    chainType.elements.add(element);
                    chainType.count++;
                }
            }
        }
    }

    public void checkSides(Gem gem, Chain  chain){
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

//    public boolean ready(){
//        return GameView.GLRenderer._boardReady;
//    }
    public void updateStageProgress(){
        if(curStage == null) return;
        //fg.Game.drawLoading(10, fg.System.canvas.height - 122, fg.System.canvas.width - 20, 10, this.currentLevel.curStage.score / this.currentLevel.curStage.targetScore);
    }
    public void loadStage(Integer index){
        GameView.GLRenderer._boardReady = false;
        this.curStage = stages.get(index);
        this.stageIndex = index;
        this.createEntities(this.curStage);
        /******* GemCollection *********/
        BuildGemsCollections();
        /******* GemCollection *********/
    }

    private void BuildGemsCollections() {
        this.gemCol = new GemCollection(R.drawable.atlas, entities, false);
        this.gemCol.offSetX = gemOffsetX;
        this.gemCol.offSetY = gemOffsetY;
        this.gemCol.buildTextureMap(80,10,8);

        this.marioCol = new GemCollection(-1/*R.drawable.mario_tiles_46*/, entities, true);
        this.marioCol.offSetX = gemOffsetX;
        this.marioCol.offSetY = gemOffsetY;
        //this.marioCol.buildTextureMap(80,10,8);
        this.marioCol.buildTextureMap(100,10,10);
        GameView.GLRenderer._boardReady = true;
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
        GameView.GLRenderer._boardReady = false;
        Integer curScore = curStage != null ? curStage.score : 0;
        clearGems();
        this.curStage = stages.get(stageIndex);
        curStage.score = curScore;
        this.createEntities(this.curStage);
        BuildGemsCollections();
    }

    public void loadStage(Stage stage, Integer index){
        GameView.GLRenderer._boardReady = false;
        this.curStage = stage;
        this.stageIndex = index;
        this.createEntities(this.curStage);
        BuildGemsCollections();
    }
}
