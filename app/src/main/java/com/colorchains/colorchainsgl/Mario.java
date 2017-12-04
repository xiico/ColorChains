package com.colorchains.colorchainsgl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.HashMap;

import static java.lang.Integer.parseInt;

/**
 * Created by francisco.cnmarao on 19/04/2017.
 */

public class Mario extends Gem {
    public String tileSet = "";
    public Integer[] edges = new Integer[8];
    public Board board;
    public boolean vanished = false;
    public static Bitmap marioSeed;
    public static HashMap<String,RectF[]> tileHistory = new HashMap<>();
    public static Point[] cachePosition = new Point[]{new Point(GameView.defaultSide / 2 , 0), new Point(GameView.defaultSide / 2 , GameView.defaultSide / 2 ), new Point(0, GameView.defaultSide / 2 ), new Point(0, 0)};
    static Canvas canvas;
    public static Bitmap marioTexture;
    static HashMap<String, Integer> marioCache = new HashMap<>();

    public Mario(String id, TYPE type, Float x, Float y, Integer cx, Integer cy, Board board) {
        super(id, type, x, y, cx, cy, board);
        this.board = board;
        this.cacheWidth = GameView.scaledDefaultSide;
        this.cacheHeight = GameView.scaledDefaultSide;
        this.setWidth(this.cacheWidth);
        this.setHeight(this.cacheHeight);
    }

    public synchronized static boolean RenderTiles(Board board)
    {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;
        Mario.marioSeed = BitmapFactory.decodeResource(GameView.context.getResources(), Gem.getGemSprites(TYPE.MARIO),opts);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(marioSeed, GameView.scaledDefaultSide * 5, GameView.scaledDefaultSide, false);
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmp2 = Bitmap.createBitmap(GameView.scaledDefaultSide * 10, GameView.scaledDefaultSide * 10, conf); // this creates a MUTABLE bitmap
        canvas = new Canvas(bmp2);
        for (String key: marioCache.keySet()) {
            Mario.renderSubTile(canvas, resizedBitmap, key);
        }
        marioTexture = bmp2;
        return true;
    }

    public void setEdges() {
        this.edges = new Integer[8];
        int i = parseInt(this.id.split("-")[0]), k = parseInt(this.id.split("-")[1]);
        Entity[][] objs = board.entities;
        this.edges[0] = (i - 1 >= 0 && objs[i - 1][k + 0] != null && objs[i - 1][k + 0] != null && objs[i - 1][k + 0].type == TYPE.MARIO && !((Mario)objs[i - 1][k + 0]).vanished ? 1 : 0);
        this.edges[1] = (i - 1 >= 0 && k + 1 < objs[i - 1].length && objs[i - 1][k + 1] != null && objs[i - 1][k + 1].type == TYPE.MARIO && !((Mario)objs[i - 1][k + 1]).vanished ? 1 : 0);
        this.edges[2] = (k + 1 < objs[i - 0].length && objs[i - 0][k + 1] != null && objs[i - 0][k + 1].type == TYPE.MARIO && !((Mario)objs[i - 0][k + 1]).vanished ? 1 : 0);
        this.edges[3] = (i + 1 < objs.length && k + 1 < objs[i + 1].length && objs[i + 1][k + 1] != null && objs[i + 1][k + 1].type == TYPE.MARIO && !((Mario)objs[i + 1][k + 1]).vanished ? 1 : 0);
        this.edges[4] = (i + 1 < objs.length && objs[i + 1][k + 0] != null && objs[i + 1][k + 0] != null && objs[i + 1][k + 0].type == TYPE.MARIO && !((Mario)objs[i + 1][k + 0]).vanished ? 1 : 0);
        this.edges[5] = (i + 1 < objs.length && k - 1 >= 0 && objs[i + 1][k - 1] != null && objs[i + 1][k - 1].type == TYPE.MARIO && !((Mario)objs[i + 1][k - 1]).vanished ? 1 : 0);
        this.edges[6] = (k - 1 >= 0 && objs[i - 0][k - 1] != null && objs[i - 0][k - 1].type == TYPE.MARIO && !((Mario)objs[i - 0][k - 1]).vanished ? 1 : 0);
        this.edges[7] = (i - 1 >= 0 && k - 1 >= 0 && objs[i - 1][k - 1] != null && objs[i - 1][k - 1].type == TYPE.MARIO && !((Mario)objs[i - 1][k - 1]).vanished ? 1 : 0);
    }

    public String getSubTiles(Integer tileA,Integer  tileB,Integer  tileC,Integer  index) {
        if (tileA == 1 && tileB == 1 && tileC == 1)
            return "0" + index;
        else if (tileA == 1 && tileB == 0 && tileC == 1)
            return "2" + (2 + index) % 4;
        else if (tileA == 1 && tileC == 0)
            return "4" + index;
        else if (tileA == 0 && tileC == 1)
            return "1" + index;
        else
            return "3" + index;
    }

    public void setSubTiles(Boolean setCacheXY) {
        this.setEdges();
        this.tileSet = "";
        for (int i = 0; i <= 6; i += 2)
            this.tileSet += this.getSubTiles(this.edges[i], this.edges[i + 1], (i + 2 >= this.edges.length ? this.edges[0] : this.edges[i + 2]), i / 2);
    }

    @Override
    public void update(){}

    public synchronized static void renderSubTile(Canvas c, Bitmap bmp, String key) {
        Integer posX = (marioCache.get(key)) % (GameView.scaledDefaultSide * 10);// * (int)board.metrics.density;
        Integer posY = (((int)Math.floor(marioCache.get(key) / (GameView.scaledDefaultSide * 10))) * GameView.scaledDefaultSide);// * (int)board.metrics.density;
        Paint marioPaint = new Paint();
        for (int i = 0; i <= 6; i += 2) {
            int cacheX = ((parseInt(String.valueOf(key.charAt(i))) * GameView.scaledDefaultSide) + (Mario.cachePosition[parseInt(String.valueOf(key.charAt(i + 1)))].x * (int) GameView.scale));
            int cacheY = Mario.cachePosition[parseInt(String.valueOf(key.charAt(i + 1)))].y * (int) GameView.scale;
            int cacheWidth = GameView.scaledDefaultSide / 2;
            int cacheHeight = GameView.scaledDefaultSide / 2;
            c.drawBitmap(bmp, new Rect(cacheX, cacheY, cacheX + cacheWidth, cacheY + cacheHeight),
                    new RectF(posX + (Mario.cachePosition[i / 2].x * (int) GameView.scale),
                            posY + ((float)Mario.cachePosition[i / 2].y * (int) GameView.scale),
                            posX + (Mario.cachePosition[i / 2].x * (int) GameView.scale) + (GameView.scaledDefaultSide / 2),
                            posY + ((float)Mario.cachePosition[i / 2].y * (int) GameView.scale)+ (GameView.scaledDefaultSide / 2)), marioPaint);
        }
    }
}
