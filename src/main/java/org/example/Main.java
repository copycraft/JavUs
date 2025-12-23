package org.example;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    // ===== CONFIG =====
    private static final int TILE_SIZE = 10;
    private static final int MAP_WIDTH = 100;
    private static final int MAP_HEIGHT = 80;
    private static final int MAX_ROOMS = 10;

    private static final float CAMERA_ZOOM = 2.5f; // 🔥 CHANGE THIS FOR MORE / LESS ZOOM

    // ===== WINDOW =====
    private long window;
    private int windowWidth = 800;
    private int windowHeight = 600;

    // ===== MAP =====
    private boolean[][] floor;
    private int[][] roomId;
    private float[][] roomColors;
    private List<Room> rooms;
    private Random rand = new Random(12345);

    // ===== PLAYER =====
    private float playerX, playerY;
    private final float playerSize = TILE_SIZE;
    private final float speed = 120f;

    private static class Room {
        int x, y, w, h;
        Room(int x, int y, int w, int h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        init();
        loop();
        glfwTerminate();
    }

    private void init() {
        glfwInit();

        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(windowWidth, windowHeight, "Among Us Style Prototype", NULL, NULL);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();

        glfwSetFramebufferSizeCallback(window, (w, width, height) -> {
            windowWidth = width;
            windowHeight = height;
            glViewport(0, 0, width, height);
        });

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, windowWidth, 0, windowHeight, -1, 1);
        glMatrixMode(GL_MODELVIEW);

        generateMap();

        Room start = rooms.get(0);
        playerX = (start.x + start.w / 2f) * TILE_SIZE;
        playerY = (start.y + start.h / 2f) * TILE_SIZE;
    }

    // ===== MAP GENERATION =====
    private void generateMap() {
        floor = new boolean[MAP_WIDTH][MAP_HEIGHT];
        roomId = new int[MAP_WIDTH][MAP_HEIGHT];
        rooms = new ArrayList<>();
        roomColors = new float[MAX_ROOMS][3];

        for (int i = 0; i < MAX_ROOMS; i++) {
            int w = 6 + rand.nextInt(10);
            int h = 6 + rand.nextInt(10);
            int x = rand.nextInt(MAP_WIDTH - w - 2) + 1;
            int y = rand.nextInt(MAP_HEIGHT - h - 2) + 1;

            Room r = new Room(x, y, w, h);
            rooms.add(r);

            roomColors[i][0] = rand.nextFloat() * 0.6f + 0.4f;
            roomColors[i][1] = rand.nextFloat() * 0.6f + 0.4f;
            roomColors[i][2] = rand.nextFloat() * 0.6f + 0.4f;

            for (int xx = x; xx < x + w; xx++)
                for (int yy = y; yy < y + h; yy++) {
                    floor[xx][yy] = true;
                    roomId[xx][yy] = i;
                }

            if (i > 0) connectRooms(rooms.get(i - 1), r);
        }
    }

    private void connectRooms(Room a, Room b) {
        int ax = a.x + a.w / 2;
        int ay = a.y + a.h / 2;
        int bx = b.x + b.w / 2;
        int by = b.y + b.h / 2;

        for (int x = Math.min(ax, bx); x <= Math.max(ax, bx); x++)
            floor[x][ay] = true;

        for (int y = Math.min(ay, by); y <= Math.max(ay, by); y++)
            floor[bx][y] = true;
    }

    // ===== GAME LOOP =====
    private void loop() {
        double last = glfwGetTime();

        while (!glfwWindowShouldClose(window)) {
            double now = glfwGetTime();
            float delta = (float)(now - last);
            last = now;

            update(delta);

            glClear(GL_COLOR_BUFFER_BIT);
            glLoadIdentity();

            applyCamera();

            drawMap();
            drawPlayer();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    // ===== CAMERA =====
    private void applyCamera() {
        glScalef(CAMERA_ZOOM, CAMERA_ZOOM, 1f);

        float camX = playerX + playerSize / 2f - (windowWidth / CAMERA_ZOOM) / 2f;
        float camY = playerY + playerSize / 2f - (windowHeight / CAMERA_ZOOM) / 2f;

        glTranslatef(-camX, -camY, 0);
    }

    // ===== UPDATE =====
    private void update(float dt) {
        float dx = 0, dy = 0;

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) dy += speed * dt;
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) dy -= speed * dt;
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) dx -= speed * dt;
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) dx += speed * dt;

        tryMove(dx, dy);
    }

    private void tryMove(float dx, float dy) {
        if (!collides(playerX + dx, playerY)) playerX += dx;
        if (!collides(playerX, playerY + dy)) playerY += dy;
    }

    private boolean collides(float x, float y) {
        int tx = (int)((x + playerSize / 2) / TILE_SIZE);
        int ty = (int)((y + playerSize / 2) / TILE_SIZE);
        return tx < 0 || ty < 0 || tx >= MAP_WIDTH || ty >= MAP_HEIGHT || !floor[tx][ty];
    }

    // ===== DRAWING =====
    private void drawMap() {
        for (int x = 0; x < MAP_WIDTH; x++)
            for (int y = 0; y < MAP_HEIGHT; y++)
                if (floor[x][y]) {
                    if (roomId[x][y] >= 0) {
                        float[] c = roomColors[roomId[x][y]];
                        glColor3f(c[0], c[1], c[2]);
                    } else glColor3f(0.7f, 0.7f, 0.7f);

                    float px = x * TILE_SIZE;
                    float py = y * TILE_SIZE;

                    glBegin(GL_QUADS);
                    glVertex2f(px, py);
                    glVertex2f(px + TILE_SIZE, py);
                    glVertex2f(px + TILE_SIZE, py + TILE_SIZE);
                    glVertex2f(px, py + TILE_SIZE);
                    glEnd();
                }
    }

    private void drawPlayer() {
        glColor3f(1, 0, 0);
        glBegin(GL_QUADS);
        glVertex2f(playerX, playerY);
        glVertex2f(playerX + playerSize, playerY);
        glVertex2f(playerX + playerSize, playerY + playerSize);
        glVertex2f(playerX, playerY + playerSize);
        glEnd();
    }
}
