package com.colorchains.colorchainsgl;

import android.opengl.GLES20;
import android.support.annotation.Nullable;

/**
 * Created by francisco.cnmarao on 17/10/2017.
 */

public class GemCollection extends EntityCollection {
    public boolean checkMario = false;
    public GemCollection(Integer resourceId, Board board, boolean checkMario) {
        super(resourceId, board.entities, 8, 8);
        this.checkMario = checkMario;
        entityWidthGl = (1 / 10f);
        entityHeightGl = (1 / (checkMario ? 10f : 8f));
        if(checkMario){
            mProgram = setupImage(R.drawable.mario_tiles_46, vs_Gem, fs_Gem, GLES20.GL_NEAREST);
        }
        this.setWidth(GameView.scaledDefaultSide);//92
        this.setHeight(GameView.scaledDefaultSide);//92
    }

    @Nullable
    @Override
    public Integer getUVIndex(Entity entity) {
        Integer uvIndex;
        Gem gem = (Gem) entity;
        if(gem.type == TYPE.MARIO) {
            if(!checkMario) return null;
            uvIndex = (int)(Mario.marioCache.get(((Mario) gem).tileSet) / gem.getWidth());
        } else {
            if(checkMario) return null;
            uvIndex = gem.getGemType().getValue() + gem.curAnimation.curFrame;
        }
        return uvIndex;
    }
}
