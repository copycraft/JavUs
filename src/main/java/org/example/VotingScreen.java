package org.example;

import java.util.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.GLFW.*;

public class VotingScreen {

    public class Votable {
        String name;
        boolean isPlayer;
        float x, y, w = 200, h = 40;
        boolean voted = false;

        Votable(String name, boolean isPlayer, float x, float y) {
            this.name = name;
            this.isPlayer = isPlayer;
            this.x = x;
            this.y = y;
        }

        boolean isHovered(float mx, float my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }

        void draw() {
            glColor3f(voted ? 1f : 0.5f, 0.5f, 0.5f);
            Game.quad(x, y, w, h);
            glColor3f(0,0,0);
            Game.drawBorder(x, y, w, h);
        }
    }

    List<Votable> candidates = new ArrayList<>();
    boolean voteFinished = false;

    public VotingScreen(Game game, int width, int height) {
        candidates.add(new Votable("You", true, width/2f - 100, 100f));
        float startY = 160f;
        float gap = 60f;
        int i = 0;
        for (Bot b : game.bots) {
            candidates.add(new Votable("Bot" + i, false, width/2f - 100, startY + i * gap));
            i++;
        }
    }

    public void handleMouse(float mx, float my, int button, int action) {
        if (action != GLFW_PRESS) return;

        for (Votable v : candidates) {
            if (v.isHovered(mx, my)) {
                v.voted = true;
                System.out.println("Voted for: " + v.name);
                voteFinished = true;
                break;
            }
        }
    }

    public void draw() {
        glColor4f(0, 0, 0, 0.75f);
        glBegin(GL_QUADS);
        glVertex2f(0, 0);
        glVertex2f(1920, 0);
        glVertex2f(1920, 1080);
        glVertex2f(0, 1080);
        glEnd();
        for (Votable v : candidates) v.draw();
    }
}
