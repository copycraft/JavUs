package org.example;

import org.lwjgl.BufferUtils;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import java.nio.DoubleBuffer;
import java.util.Random;

public class Game {
    private final long window;
    private int width, height;

    private final MapGen mapGen;
    private float playerX, playerY;
    private final float playerSize = MapGen.TILE_SIZE;
    private final float speed = 120f;
    private boolean showMini = false;

    public Game(long window, int width, int height) {
        this.window = window;
        this.width = width;
        this.height = height;
        this.mapGen = new MapGen(12345L); // deterministic seed
        MapGen.Room r = mapGen.getRooms().get(0);
        playerX = (r.x + r.w / 2f) * MapGen.TILE_SIZE;
        playerY = (r.y + r.h / 2f) * MapGen.TILE_SIZE;
    }

    public void setSize(int w, int h) { this.width = w; this.height = h; }

    public void handleKey(int key, int action) {
        // toggles (for example) mini map with M
        if (action == GLFW_PRESS && key == GLFW_KEY_M) showMini = !showMini;
    }

    public void handleMouse(int button, int action) {
        // future use
    }

    public void update(float dt) {
        float dx = 0, dy = 0;
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) dy += speed * dt;
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) dy -= speed * dt;
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) dx -= speed * dt;
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) dx += speed * dt;

        tryMove(dx, dy);
    }

    private void tryMove(float dx, float dy) {
        if (!mapGen.collides(playerX + dx, playerY)) playerX += dx;
        if (!mapGen.collides(playerX, playerY + dy)) playerY += dy;
    }

    private void applyCamera() {
        // projection for world in pixels
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, 0, height, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glScalef(MapGen.CAMERA_ZOOM, MapGen.CAMERA_ZOOM, 1f);

        float visibleW = width / MapGen.CAMERA_ZOOM;
        float visibleH = height / MapGen.CAMERA_ZOOM;
        float camX = playerX + playerSize / 2f - visibleW / 2f;
        float camY = playerY + playerSize / 2f - visibleH / 2f;

        camX = Math.max(0, Math.min(camX, MapGen.MAP_WIDTH * MapGen.TILE_SIZE - visibleW));
        camY = Math.max(0, Math.min(camY, MapGen.MAP_HEIGHT * MapGen.TILE_SIZE - visibleH));

        glTranslatef(-camX, -camY, 0);
    }

    public void drawGame() {
        applyCamera();
        mapGen.drawMap();

        // player
        glColor3f(1f, 0.2f, 0.2f);
        glBegin(GL_QUADS);
        glVertex2f(playerX, playerY);
        glVertex2f(playerX + playerSize, playerY);
        glVertex2f(playerX + playerSize, playerY + playerSize);
        glVertex2f(playerX, playerY + playerSize);
        glEnd();

        // draw mini-map in screen coords if toggled
        if (showMini) {
            // switch to screen projection
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, width, 0, height, -1, 1);
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            mapGen.drawMiniMap(playerX, playerY, height);
        }
    }
}
