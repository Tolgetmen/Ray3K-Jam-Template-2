package com.ray3k.template.transitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.ray3k.template.JamGame;

public class TransitionColorFade implements Transition {
    private TransitionEngine te;
    public Color backgroundColor;
    
    public TransitionColorFade(Color backgroundColor) {
        te = JamGame.transitionEngine;
        this.backgroundColor = backgroundColor;
    }
    
    @Override
    public void create() {
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
        Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        if (te.time < te.duration / 2) {
            te.textureRegion.setRegion(new TextureRegion(te.frameBuffer.getFbo().getColorBufferTexture()));
            te.textureRegion.flip(false, true);

            batch.setColor(1, 1, 1, (te.duration - te.time * 2) / te.duration);
        } else {
            te.textureRegion.setRegion(new TextureRegion(te.nextFrameBuffer.getFbo().getColorBufferTexture()));
            te.textureRegion.flip(false, true);

            batch.setColor(1, 1, 1, (te.time - te.duration / 2) / te.duration * 2);
        }
        batch.draw(te.textureRegion, 0, 0);
    }
}
