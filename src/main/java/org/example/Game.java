package org.example;

import java.util.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.GLFW.*;

public class Game {
    int width, height;
    MapGen map;
    float playerX, playerY;
    float playerSize = 18;
    float speed = 180;
    float[] playerColor = randomColor();
    boolean meeting = false;
    float meetingTimer = 8f;
    float killCooldown = 0;
    List<Bot> bots = new ArrayList<>();
    List<DeadBody> bodies = new ArrayList<>();
    VotingScreen votingScreen;

    public void handleKey(int key, int action) {
        // Movement handled in update()
    }

    class Button {
        float x, y, w, h;
        float[] color;
        String label;

        Button(float x, float y, float w, float h, float[] color, String label) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.color = color;
            this.label = label;
        }

        boolean isHovered(float mx, float my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }

        void draw() {
            glColor3f(color[0], color[1], color[2]);
            quad(x, y, w, h);
            glColor3f(0, 0, 0);
            drawBorder(x, y, w, h);
        }
    }

    Button killButton;
    Button reportButton;

    public Game(int w, int h) {
        width = w;
        height = h;

        map = new MapGen(System.currentTimeMillis());

        float margin = 20;
        killButton = new Button(width - 140 - margin, margin, 120, 60, new float[]{1f, 0f, 0f}, "Kill");
        reportButton = new Button(width - 140 - margin, margin + 80, 120, 60, new float[]{1f, 1f, 0f}, "Report");

        Random rng = new Random();
        List<MapGen.Room> rooms = map.getRooms();
        if (!rooms.isEmpty()) {
            for (int i = 0; i < 5; i++) {
                MapGen.Room r = rooms.get(rng.nextInt(rooms.size()));
                float bx = r.x * MapGen.TILE_SIZE + MapGen.TILE_SIZE / 2f;
                float by = r.y * MapGen.TILE_SIZE + MapGen.TILE_SIZE / 2f;
                bots.add(new Bot(bx, by));
            }

            MapGen.Room first = rooms.get(0);
            playerX = first.x * MapGen.TILE_SIZE + MapGen.TILE_SIZE / 2f;
            playerY = first.y * MapGen.TILE_SIZE + MapGen.TILE_SIZE / 2f;
        }
    }

    void setSize(int w, int h) {
        width = w;
        height = h;
        float margin = 20;
        killButton.x = width - killButton.w - margin;
        reportButton.x = width - reportButton.w - margin;
        reportButton.y = killButton.y + killButton.h + 20;
    }

    void handleMouse(float mx, float my, int button, int action) {
        if (action != GLFW_PRESS) return;

        float invY = height - my;

        if (votingScreen != null) {
            votingScreen.handleMouse(mx, invY, button, action);
            if (votingScreen.voteFinished) {
                votingScreen = null;
                meeting = false;
            }
            return;
        }

        if (killButton.isHovered(mx, invY)) {
            System.out.println("[BUTTON] Kill clicked!");
            tryKill();
            return;
        }
        if (reportButton.isHovered(mx, invY)) {
            System.out.println("[BUTTON] Report clicked!");
            tryReport();
            return;
        }
    }

    void update(float dt) {
        if (meeting && votingScreen == null) {
            meetingTimer -= dt;
            if (meetingTimer <= 0) meeting = false;
            return;
        }

        if (killCooldown > 0) killCooldown -= dt;

        float nx = playerX, ny = playerY;
        long ctx = glfwGetCurrentContext();
        if (ctx != 0) {
            if (glfwGetKey(ctx, GLFW_KEY_W) == GLFW_PRESS) ny += speed * dt;
            if (glfwGetKey(ctx, GLFW_KEY_S) == GLFW_PRESS) ny -= speed * dt;
            if (glfwGetKey(ctx, GLFW_KEY_A) == GLFW_PRESS) nx -= speed * dt;
            if (glfwGetKey(ctx, GLFW_KEY_D) == GLFW_PRESS) nx += speed * dt;
        }

        if (!map.collides(nx, playerY)) playerX = nx;
        if (!map.collides(playerX, ny)) playerY = ny;

        // Update bots
        for (Bot b : bots) {
            b.update(dt);
        }
    }

    void tryKill() {
        if (killCooldown > 0) return;

        Bot closest = null;
        float minDist = Float.MAX_VALUE;
        for (Bot b : bots) {
            if (!b.alive) continue;
            float d = dist(playerX, playerY, b.x, b.y);
            if (d < 25 && d < minDist) {
                minDist = d;
                closest = b;
            }
        }

        if (closest != null) {
            closest.alive = false;
            bodies.add(new DeadBody(closest.x, closest.y, closest.color));
            killCooldown = 10;
        }
    }

    void tryReport() {
        for (DeadBody d : bodies) {
            if (!d.reported && dist(playerX, playerY, d.x, d.y) < 25) {
                d.reported = true;
                meeting = true;
                meetingTimer = 8;
                votingScreen = new VotingScreen(this, width, height);
                break;
            }
        }
    }

    void render() {
        if (votingScreen != null) {
            votingScreen.draw();
            return;
        }

        float zoom = 2.5f;

        glPushMatrix();
        glScalef(zoom, zoom, 1f);
        glTranslatef(-playerX + width / (2f * zoom), -playerY + height / (2f * zoom), 0f);

        map.drawMap();
        for (DeadBody d : bodies) d.draw();
        for (Bot b : bots) b.draw();
        drawPlayer();

        glPopMatrix();

        killButton.draw();
        reportButton.draw();
    }

    void drawPlayer() {
        glColor3f(playerColor[0], playerColor[1], playerColor[2]);
        quad(playerX, playerY, playerSize, playerSize);
    }

    static void quad(float x, float y, float w, float h) {
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(x + w, y);
        glVertex2f(x + w, y + h);
        glVertex2f(x, y + h);
        glEnd();
    }

    static void drawBorder(float x, float y, float w, float h) {
        glBegin(GL_LINE_LOOP);
        glVertex2f(x, y);
        glVertex2f(x + w, y);
        glVertex2f(x + w, y + h);
        glVertex2f(x, y + h);
        glEnd();
    }

    static float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.hypot(x2 - x1, y2 - y1);
    }

    static float[] randomColor() {
        return new float[]{(float) Math.random(), (float) Math.random(), (float) Math.random()};
    }
}
