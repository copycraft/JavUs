package org.example;

import java.util.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.GLFW.*;

public class Game {

    int width, height;

    // Map
    MapGen map;

    // Player
    float playerX, playerY;
    float playerSize = 18;
    float speed = 180;
    float[] playerColor = randomColor();

    // Game state
    boolean meeting = false;
    float meetingTimer = 8f;
    float killCooldown = 0;

    // Bots and dead bodies
    List<Bot> bots = new ArrayList<>();
    List<DeadBody> bodies = new ArrayList<>();

    // Voting screen
    VotingScreen votingScreen;

    public void handleKey(int key, int action) {
        // Movement handled in update()
    }

    // Buttons
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
            glColor3f(0,0,0);
            drawBorder(x, y, w, h);
        }
    }

    Button killButton;
    Button reportButton;

    public Game(int w, int h) {
        width = w;
        height = h;

        map = new MapGen(System.currentTimeMillis());

        // Setup buttons
        killButton = new Button(width - 140, 20, 120, 60, new float[]{1f, 0f, 0f}, "Kill");
        reportButton = new Button(width - 140, 100, 120, 60, new float[]{1f, 1f, 0f}, "Report");

        Random rng = new Random();
        // Add some bots in random rooms
        for (int i = 0; i < 5; i++) {
            MapGen.Room r = map.getRooms().get(rng.nextInt(map.getRooms().size()));
            float bx = r.x * MapGen.TILE_SIZE + MapGen.TILE_SIZE / 2f;
            float by = r.y * MapGen.TILE_SIZE + MapGen.TILE_SIZE / 2f;
            bots.add(new Bot(bx, by));
        }

        // Player starts in center of first room
        MapGen.Room first = map.getRooms().get(0);
        playerX = first.x * MapGen.TILE_SIZE + MapGen.TILE_SIZE / 2f;
        playerY = first.y * MapGen.TILE_SIZE + MapGen.TILE_SIZE / 2f;
    }

    void setSize(int w, int h) {
        width = w;
        height = h;
        // Update buttons on resize
        killButton.x = width - killButton.w - 20;
        reportButton.x = width - reportButton.w - 20;
        reportButton.y = killButton.y + killButton.h + 20;
    }

    void handleMouse(float mx, float my, int button, int action) {
        if (action != GLFW_PRESS) return;
        my = height - my; // invert Y for OpenGL

        if (votingScreen != null) {
            votingScreen.handleMouse(mx, my, button, action);
            if (votingScreen.voteFinished) {
                votingScreen = null;
                meeting = false;
            }
            return;
        }

        if (killButton.isHovered(mx, my)) {
            System.out.println("[BUTTON] Kill clicked!");
            tryKill();
        }
        if (reportButton.isHovered(mx, my)) {
            System.out.println("[BUTTON] Report clicked!");
            tryReport();
        }
    }

    void update(float dt) {
        if (meeting && votingScreen == null) {
            meetingTimer -= dt;
            if (meetingTimer <= 0) meeting = false;
            return;
        }

        if (killCooldown > 0) killCooldown -= dt;

        // --- Sliding player movement ---
        float nx = playerX, ny = playerY;
        if (glfwGetKey(glfwGetCurrentContext(), GLFW_KEY_W) == GLFW_PRESS) ny += speed * dt;
        if (glfwGetKey(glfwGetCurrentContext(), GLFW_KEY_S) == GLFW_PRESS) ny -= speed * dt;
        if (glfwGetKey(glfwGetCurrentContext(), GLFW_KEY_A) == GLFW_PRESS) nx -= speed * dt;
        if (glfwGetKey(glfwGetCurrentContext(), GLFW_KEY_D) == GLFW_PRESS) nx += speed * dt;

        if (!map.collides(nx, playerY)) playerX = nx;
        if (!map.collides(playerX, ny)) playerY = ny;

        // Update bots
        for (Bot b : bots) {
            b.update(dt);
            for (DeadBody d : bodies) {
                if (!d.reported && dist(b.x, b.y, d.x, d.y) < 20) {
                    d.reported = true;
                    meeting = true;
                    meetingTimer = 8;
                    votingScreen = new VotingScreen(this, width, height);
                }
            }
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
            return; // pause map/player rendering
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

        // Draw HUD buttons
        killButton.draw();
        reportButton.draw();
    }

    void drawPlayer() {
        glColor3f(playerColor[0], playerColor[1], playerColor[2]);
        quad(playerX, playerY, playerSize, playerSize);
    }

    // Utilities
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
