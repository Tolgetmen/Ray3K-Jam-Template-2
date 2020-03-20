package com.ray3k.template.transitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TransitionCrossFade implements Transition {
    private TransitionEngine te;
    
    @Override
    public void create(TransitionEngine transitionEngine) {
        te = transitionEngine;
        te.frameBuffer.begin();
        te.screen.draw(0);
        te.frameBuffer.end();
    
        te.jamGame.setScreen(te.nextScreen);
        te.nextFrameBuffer.begin();
        te.nextScreen.draw(0);
        te.nextFrameBuffer.end();
    }
    
    @Override
    public void act() {
    
    }
    
    @Override
    public void draw(Batch batch) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    
        te.textureRegion.setRegion(new TextureRegion(te.nextFrameBuffer.getFbo().getColorBufferTexture()));
        te.textureRegion.flip(false, true);
    
        batch.setColor(1, 1, 1, 1f);
        batch.draw(te.textureRegion, 0, 0);
    
        te.textureRegion.setRegion(new TextureRegion(te.frameBuffer.getFbo().getColorBufferTexture()));
        te.textureRegion.flip(false, true);
    
        batch.setColor(1, 1, 1, (te.duration - te.time) / te.duration);
        batch.draw(te.textureRegion, 0, 0);
    }
}
