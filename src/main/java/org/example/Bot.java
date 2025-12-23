package org.example;

import static org.lwjgl.opengl.GL11.*;

public class Bot {
    float x, y;
    boolean alive = true;
    float[] color = Game.randomColor();

    public Bot(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update(float dt) {
        if (!alive) return;
        // simple random movement
        x += (Math.random() - 0.5f) * 60 * dt;
        y += (Math.random() - 0.5f) * 60 * dt;
    }

    public void draw() {
        if (!alive) return;
        glColor3f(color[0], color[1], color[2]);
        Game.quad(x, y, 18,18);
    }
}
