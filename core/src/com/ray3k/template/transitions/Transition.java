package com.ray3k.template.transitions;

import com.badlogic.gdx.graphics.g2d.Batch;

public interface Transition {
    public void create();
    public void act();
    public void draw(Batch batch);
}
