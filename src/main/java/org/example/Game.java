package org.example;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    private final long window;
    private int width, height;

    private final MapGen mapGen;
    private float playerX, playerY;
    private final float playerSize = MapGen.TILE_SIZE;
    private final float speed = 120f;
    private boolean showMini = false;

    private float[] playerColor; // random color
    private boolean isImpostor = true; // only this player is impostor
    private float killCooldown = 0; // seconds remaining

    // Buttons
    private final float btnWidth = 100, btnHeight = 50;
    private float killBtnX, killBtnY, shapeshiftBtnX, shapeshiftBtnY;

    // Bots
    private final List<Bot> bots = new ArrayList<>();
    private final int BOT_COUNT = 5;

    private static class Bot {
        float x, y;
        float[] color;
        float moveTimer = 0;
        float dx = 0, dy = 0;
        Bot(float x, float y, float[] color) { this.x=x; this.y=y; this.color=color; }
    }

    public Game(long window, int width, int height) {
        this.window = window;
        this.width = width;
        this.height = height;
        this.mapGen = new MapGen(12345L);

        MapGen.Room r = mapGen.getRooms().get(0);
        playerX = (r.x + r.w / 2f) * MapGen.TILE_SIZE;
        playerY = (r.y + r.h / 2f) * MapGen.TILE_SIZE;

        Random rng = new Random();
        playerColor = new float[]{ rng.nextFloat(), rng.nextFloat(), rng.nextFloat() };
        updateButtonPositions();

        // Spawn bots in random rooms
        for (int i=0;i<BOT_COUNT;i++){
            MapGen.Room room = mapGen.getRooms().get(rng.nextInt(mapGen.getRooms().size()));
            float bx = (room.x + rng.nextInt(room.w)) * MapGen.TILE_SIZE;
            float by = (room.y + rng.nextInt(room.h)) * MapGen.TILE_SIZE;
            float[] c = new float[]{ rng.nextFloat(), rng.nextFloat(), rng.nextFloat() };
            bots.add(new Bot(bx, by, c));
        }
    }

    public void setSize(int w, int h) {
        this.width = w; this.height = h;
        updateButtonPositions();
    }

    private void updateButtonPositions() {
        killBtnX = width - btnWidth - 20;
        killBtnY = 20;
        shapeshiftBtnX = width - 2*btnWidth - 40;
        shapeshiftBtnY = 20;
    }

    public void handleKey(int key, int action) {
        if (action == GLFW_PRESS) {
            if (key == GLFW_KEY_M) showMini = !showMini;
        }
    }

    public void handleMouse(int button, int action, double mx, double my) {
        if (button == 0 && action == GLFW_PRESS) {
            float mouseX = (float) mx;
            float mouseY = height - (float) my;

            if (isImpostor && killCooldown <= 0 &&
                    mouseX >= killBtnX && mouseX <= killBtnX + btnWidth &&
                    mouseY >= killBtnY && mouseY <= killBtnY + btnHeight) {
                killAction();
                killCooldown = 10f;
            }

            if (isImpostor &&
                    mouseX >= shapeshiftBtnX && mouseX <= shapeshiftBtnX + btnWidth &&
                    mouseY >= shapeshiftBtnY && mouseY <= shapeshiftBtnY + btnHeight) {
                shapeshift();
            }
        }
    }

    private void killAction() { System.out.println("Killed a player!"); }

    private void shapeshift() {
        Random rng = new Random();
        playerColor = new float[]{ rng.nextFloat(), rng.nextFloat(), rng.nextFloat() };
        System.out.println("Shapeshifted!");
    }

    public void update(float dt) {
        killCooldown -= dt;
        if (killCooldown < 0) killCooldown = 0;

        // Player movement
        float dx=0, dy=0;
        if (glfwGetKey(window, GLFW_KEY_W)==GLFW_PRESS) dy+=speed*dt;
        if (glfwGetKey(window, GLFW_KEY_S)==GLFW_PRESS) dy-=speed*dt;
        if (glfwGetKey(window, GLFW_KEY_A)==GLFW_PRESS) dx-=speed*dt;
        if (glfwGetKey(window, GLFW_KEY_D)==GLFW_PRESS) dx+=speed*dt;
        tryMove(dx, dy);

        // Update bots
        for (Bot b : bots) updateBot(b, dt);
    }

    private void tryMove(float dx, float dy) {
        if (!mapGen.collides(playerX+dx, playerY)) playerX+=dx;
        if (!mapGen.collides(playerX, playerY+dy)) playerY+=dy;
    }

    private void updateBot(Bot b, float dt) {
        b.moveTimer -= dt;
        if (b.moveTimer <= 0) {
            Random rng = new Random();
            b.dx = (rng.nextFloat()*2-1) * speed/2f;
            b.dy = (rng.nextFloat()*2-1) * speed/2f;
            b.moveTimer = 1f + rng.nextFloat()*2f; // move in direction 1-3s
        }

        float nx = b.x + b.dx*dt;
        float ny = b.y + b.dy*dt;
        if (!mapGen.collides(nx, b.y)) b.x=nx;
        if (!mapGen.collides(b.x, ny)) b.y=ny;
    }

    private void applyCamera() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, 0, height, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glScalef(MapGen.CAMERA_ZOOM, MapGen.CAMERA_ZOOM, 1f);
        float visibleW = width / MapGen.CAMERA_ZOOM;
        float visibleH = height / MapGen.CAMERA_ZOOM;
        float camX = playerX + playerSize/2f - visibleW/2f;
        float camY = playerY + playerSize/2f - visibleH/2f;
        camX = Math.max(0, Math.min(camX, MapGen.MAP_WIDTH*MapGen.TILE_SIZE - visibleW));
        camY = Math.max(0, Math.min(camY, MapGen.MAP_HEIGHT*MapGen.TILE_SIZE - visibleH));
        glTranslatef(-camX, -camY, 0);
    }

    public void drawGame() {
        applyCamera();
        mapGen.drawMap();

        // Draw player
        glColor3f(playerColor[0], playerColor[1], playerColor[2]);
        glBegin(GL_QUADS);
        glVertex2f(playerX, playerY);
        glVertex2f(playerX+playerSize, playerY);
        glVertex2f(playerX+playerSize, playerY+playerSize);
        glVertex2f(playerX, playerY+playerSize);
        glEnd();

        // Draw bots
        for (Bot b : bots) {
            glColor3f(b.color[0], b.color[1], b.color[2]);
            glBegin(GL_QUADS);
            glVertex2f(b.x, b.y);
            glVertex2f(b.x+playerSize, b.y);
            glVertex2f(b.x+playerSize, b.y+playerSize);
            glVertex2f(b.x, b.y+playerSize);
            glEnd();
        }

        // Mini-map
        if (showMini) {
            glPushMatrix();
            glMatrixMode(GL_PROJECTION);
            glPushMatrix();
            glLoadIdentity();
            glOrtho(0,width,0,height,-1,1);
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            mapGen.drawMiniMap(playerX,playerY,height);
            glPopMatrix();
            glMatrixMode(GL_PROJECTION);
            glPopMatrix();
            glMatrixMode(GL_MODELVIEW);
        }

        drawButtons();
    }

    private void drawButtons() {
        glPushMatrix();
        glLoadIdentity();

        // Kill button
        glColor3f(0.5f,0.1f,0.1f);
        glBegin(GL_QUADS);
        glVertex2f(killBtnX, killBtnY);
        glVertex2f(killBtnX+btnWidth, killBtnY);
        glVertex2f(killBtnX+btnWidth, killBtnY+btnHeight);
        glVertex2f(killBtnX, killBtnY+btnHeight);
        glEnd();

        // Kill cooldown overlay
        if(killCooldown>0){
            float pct = 1f - killCooldown/10f;
            glColor3f(0.8f,0f,0f);
            glBegin(GL_QUADS);
            glVertex2f(killBtnX, killBtnY);
            glVertex2f(killBtnX + btnWidth*pct, killBtnY);
            glVertex2f(killBtnX + btnWidth*pct, killBtnY+btnHeight);
            glVertex2f(killBtnX, killBtnY+btnHeight);
            glEnd();
        }

        // Shapeshift button
        glColor3f(0.1f,0.1f,0.5f);
        glBegin(GL_QUADS);
        glVertex2f(shapeshiftBtnX, shapeshiftBtnY);
        glVertex2f(shapeshiftBtnX+btnWidth, shapeshiftBtnY);
        glVertex2f(shapeshiftBtnX+btnWidth, shapeshiftBtnY+btnHeight);
        glVertex2f(shapeshiftBtnX, shapeshiftBtnY+btnHeight);
        glEnd();

        glPopMatrix();
    }
}
