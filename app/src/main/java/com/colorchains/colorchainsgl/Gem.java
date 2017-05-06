package com.colorchains.colorchainsgl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

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

    public Gem (String id, TYPE type, Float x, Float y, Integer cx, Integer cy, Board board) {
        super(id, type, x, y, cx, cy);
        this.board = board;
        /*Bitmap resizedBitmap = null;
        if (Render.cached.get(type) == null) {
            Bitmap bmp = BitmapFactory.decodeResource(board.context.getResources(), getGemSprites());//
            resizedBitmap = Bitmap.createScaledBitmap(bmp, GameView.scaledDefaultSide * 10, GameView.scaledDefaultSide * 3, false);
            Render.cache(type, resizedBitmap);
        } else {
            resizedBitmap = Render.cached.get(type);
        }*/
        /***************/
        loadTexture(R.drawable.oldskol);
        /***************/
        this.addAnimation("idle", 0, 0, new Integer[]{0}, 0.5f, false);
        this.addAnimation("vanish", 0, GameView.scaledDefaultSide * 1, new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, 0.3f, false);
        this.addAnimation("appear", 0, GameView.scaledDefaultSide * 2, new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, 0.5f, false);//0.5
    }

    public Integer getRow(){
        if(this.movingSpeed > 0)
            return (int) Math.floor((this.getX() /*+ Render.screenOffsetY*/) / GameView.scaledDefaultSide);
        else
            return (int) Math.ceil((this.getY() /*+ Render.screenOffsetY*/) / GameView.scaledDefaultSide);
    }

    public Integer getCol(){
        if(this.movingSpeed > 0)
            return (int) Math.floor((this.getX() /*+ Render.screenOffsetX*/) / GameView.scaledDefaultSide);
        else
            return (int) Math.ceil((this.getY() /*+ Render.screenOffsetX*/) / GameView.scaledDefaultSide);
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
                this.setY(this.getY() + this.movingSpeed * Timer.deltaTime);
            } else {
                this.movingSpeed = this.moveTo.x > this.getCol() ? Math.abs(this.movingSpeed) : -Math.abs(this.movingSpeed);
                this.setX(this.getX() + this.movingSpeed * Timer.deltaTime);
            }
            if (this.moveTo.y == this.getRow() && this.moveTo.x == this.getCol()) {
                this.setX((float)this.moveTo.x * this.cacheWidth);
                this.setY((float)this.moveTo.y * this.cacheHeight);
                if (board.entities[this.moveTo.y][this.moveTo.x] != this) {
                    Integer lastRow = parseInt(this.id.split("-")[0]), lastCol = parseInt(this.id.split("-")[1]);
                    Gem obj = (Gem) board.entities[this.moveTo.y][this.moveTo.x];
                    //obj.draw(false);
                    obj.id = String.valueOf(lastRow + "-" + lastCol);
                    board.entities[lastRow][lastCol] = obj;
                    this.id = String.valueOf(this.moveTo.y + "-" + this.moveTo.x);
                    board.entities[this.moveTo.y][this.moveTo.x] = this;
                }
                this.movingSpeed = Math.abs(this.movingSpeed);
                return this.moveTo = null;
            }
        }
        if (this.curAnimation == null) this.curAnimation = this.animations.get("appear").play();
        this.curAnimation.update();
        return null;
    }
}
