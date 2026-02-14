package org.example;

import static org.lwjgl.opengl.GL11.*;

public class DeadBody {
    public float x, y;
    public float[] color;
    public boolean reported = false;

    public DeadBody(float x, float y, float[] color) {
        this.x = x;
        this.y = y;
        this.color = color.clone();
    }

    public void draw() {
        glColor3f(color[0], color[1], color[2]);
        Game.quad(x, y, 18, 18);
    }
}
