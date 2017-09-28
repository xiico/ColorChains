package com.colorchains.colorchainsgl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;

import static java.lang.Integer.parseInt;

/**
 * Created by francisco.cnmarao on 19/04/2017.
 */

public class Gem extends Entity {
    public Boolean checked = false;
    private float movingSpeed = 0.24f * GameView.scale;
    public Point moveTo;
    public Board board;
    public Animation curAnimation;
    public static Integer gemProgram = 0;
    public boolean selected = false;

    public Gem (String id, TYPE type, Float x, Float y, Integer cx, Integer cy, Board board) {
        super(id, type, x, y, cx, cy);
        this.board = board;
        setGemType();
        setWidth(getWidth() / 10);
        setHeight(getHeight() / 3);
        /*Bitmap resizedBitmap = null;
        if (Render.cached.get(type) == null) {
            Bitmap bmp = BitmapFactory.decodeResource(board.context.getResources(), getGemSprites());//
            resizedBitmap = Bitmap.createScaledBitmap(bmp, GameView.scaledDefaultSide * 10, GameView.scaledDefaultSide * 3, false);
            Render.cache(type, resizedBitmap);
        } else {
            resizedBitmap = Render.cached.get(type);
        }*/
        /***************/

        /***************/
        this.curAnimation = this.addAnimation("idle", 0, 0, new Integer[]{0}, 0.5f, false);
        this.addAnimation("vanish", 0, GameView.scaledDefaultSide * 1, new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, 0.3f, false);
        this.addAnimation("appear", 0, GameView.scaledDefaultSide * 2, new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, 0.5f, false);//0.5
    }

    private GemType _gemType;
    public GemType getGemType() {
        return _gemType;
    }
    public void setGemType() {
        switch (this.type) {
            case GREENGEM:
                _gemType = GemType.green;
                break;
            case REDGEM:
                _gemType = GemType.red;
                break;
            case YELLOWGEM:
                _gemType = GemType.yellow;
                break;
            case PURPLEGEM:
                _gemType = GemType.purple;
                break;
            case BLUEGEM:
                _gemType = GemType.blue;
                break;
            case WHITEGEM:
                _gemType = GemType.white;
                break;
            case ORANGEGEM:
                _gemType = GemType.orange;
                break;
            case CYANGEM:
                _gemType = GemType.cyan;
                break;
            case MARIO:
                _gemType = GemType.mario;
                break;
        }
    }

    @Override
    public float getY() {
        return super.getY() + (GameView.scaledDefaultSide / 2);
    }
    @Override
    public void setY(float y){
        super.setY(y);
    }

    @Override
    public float getX() {
        return super.getX() + (GameView.scaledDefaultSide / 2);
    }
    @Override
    public void setX(float x){
        super.setX(x);
    }
    public Gem(String id, GemType gemType, Float x, Float y, Integer cx, Integer cy, Board board) {
        super(id, TYPE.GEM, x, y, cx, cy);
        setGemType();
    }

    public Integer getRow(){
        //Log.d(this.type + " Row","movingSpeed: " + this.movingSpeed + ", return: " + ((this.getY() + (GameView.scaledDefaultSide / 2)) / GameView.scaledDefaultSide)+ ", getY: " +this.getY());
        if(this.movingSpeed > 0)
            return (int) Math.floor((super.getY() /*- (GameView.scaledDefaultSide / 2)*/) / GameView.scaledDefaultSide);
        else
            return (int) Math.ceil((super.getY() /*- (GameView.scaledDefaultSide / 2)*/) / GameView.scaledDefaultSide);
    }

    public Integer getCol(){
        //Log.d(this.type + " Col","movingSpeed: " + this.movingSpeed + ", return: " + ((this.getX() + (GameView.scaledDefaultSide / 2)) / GameView.scaledDefaultSide) + ", getX: " +this.getX());
        if(this.movingSpeed > 0)
            return (int) Math.floor((super.getX() /*- (GameView.scaledDefaultSide / 2)*/) / GameView.scaledDefaultSide);
        else
            return (int) Math.ceil((super.getX() /*- (GameView.scaledDefaultSide / 2)*/) / GameView.scaledDefaultSide);
    }

    public int getGemSprites(){
        switch (this.type) {
            case GREENGEM:
                return R.drawable.greengem;
            case REDGEM:
                return R.drawable.redgem;
            case YELLOWGEM:
                return R.drawable.yellowgem;
            case PURPLEGEM:
                return R.drawable.purplegem;
            case BLUEGEM:
                return R.drawable.bluegem;
            case WHITEGEM:
                return R.drawable.whitegem;
            case ORANGEGEM:
                return R.drawable.orangegem;
            case CYANGEM:
                return R.drawable.cyangem;
            case MARIO:
                return R.drawable.mario_tiles_46;
            default:
                return -1;
        }
    }

    public enum GemType {
        blue(0),
        cyan(10),
        orange(20),
        green(30),
        purple(40),
        red(50),
        white(60),
        yellow(70),
        mario(100);

        private final int id;
        GemType(int id) { this.id = id; }
        public int getValue() { return id; }
    }

    public static int getGemSprites(TYPE type){
        switch (type) {
            case GREENGEM:
                return R.drawable.greengem;
            case REDGEM:
                return R.drawable.redgem;
            case YELLOWGEM:
                return R.drawable.yellowgem;
            case PURPLEGEM:
                return R.drawable.purplegem;
            case BLUEGEM:
                return R.drawable.bluegem;
            case WHITEGEM:
                return R.drawable.whitegem;
            case ORANGEGEM:
                return R.drawable.orangegem;
            case CYANGEM:
                return R.drawable.cyangem;
            case MARIO:
                return R.drawable.mario_tiles_46;
            default:
                return -1;
        }
    }

    public Point update(){
        if (this.moveTo != null) {
            if (this.moveTo.y != this.getRow()) {
                this.movingSpeed = this.moveTo.y > this.getRow() ? Math.abs(this.movingSpeed) : -Math.abs(this.movingSpeed);
                this.setY(super.getY() + this.movingSpeed * Timer.deltaTime);
            } else {
                this.movingSpeed = this.moveTo.x > this.getCol() ? Math.abs(this.movingSpeed) : -Math.abs(this.movingSpeed);
                this.setX(super.getX() + this.movingSpeed * Timer.deltaTime);
            }
            if (this.moveTo.y == this.getRow() && this.moveTo.x == this.getCol()) {
                this.setX((float)this.moveTo.x * GameView.scaledDefaultSide);
                this.setY((float)this.moveTo.y * GameView.scaledDefaultSide);
                if (board.entities[this.moveTo.y][this.moveTo.x] != this) {
                    Integer lastRow = parseInt(this.id.split("-")[0]), lastCol = parseInt(this.id.split("-")[1]);
                    Gem obj = board.entities[this.moveTo.y][this.moveTo.x];
                    //obj.draw(false);
                    obj.id = String.valueOf(lastRow + "-" + lastCol);
                    board.entities[lastRow][lastCol] = obj;
                    this.id = String.valueOf(this.moveTo.y + "-" + this.moveTo.x);
                    board.entities[this.moveTo.y][this.moveTo.x] = this;
                }
                this.movingSpeed = Math.abs(this.movingSpeed);
                if(!board.selectedGem.equals(this)) board.parseBoard = true;
                return this.moveTo = null;
            }
        }
        if (this.curAnimation == null) this.curAnimation = this.animations.get("appear").play();
        this.curAnimation.update();
        return null;
    }
}
