package org.example;

import static org.lwjgl.opengl.GL11.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapGen {
    public static final int TILE_SIZE = 10;
    public static final int MAP_WIDTH = 100;
    public static final int MAP_HEIGHT = 80;
    public static final int MAX_ROOMS = 10;

    private final boolean[][] floor = new boolean[MAP_WIDTH][MAP_HEIGHT];
    private final int[][] roomId = new int[MAP_WIDTH][MAP_HEIGHT];
    private final float[][] roomColors = new float[MAX_ROOMS][3];
    private final List<Room> rooms = new ArrayList<>();
    private final Random rng;

    public static class Room {
        public int x, y, w, h;
        public Room(int x, int y, int w, int h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }
    }

    public MapGen(long seed) {
        rng = new Random(seed);
        generate();
    }

    public List<Room> getRooms() { return rooms; }

    private void generate() {
        // Clear map
        for (int x = 0; x < MAP_WIDTH; x++)
            for (int y = 0; y < MAP_HEIGHT; y++) {
                floor[x][y] = false;
                roomId[x][y] = -1;
            }

        // Generate rooms
        for (int i = 0; i < MAX_ROOMS; i++) {
            int w = 6 + rng.nextInt(10);
            int h = 6 + rng.nextInt(10);
            int x = rng.nextInt(Math.max(1, MAP_WIDTH - w - 2)) + 1;
            int y = rng.nextInt(Math.max(1, MAP_HEIGHT - h - 2)) + 1;

            Room r = new Room(x, y, w, h);
            rooms.add(r);

            roomColors[i][0] = rng.nextFloat() * 0.6f + 0.3f;
            roomColors[i][1] = rng.nextFloat() * 0.6f + 0.3f;
            roomColors[i][2] = rng.nextFloat() * 0.6f + 0.3f;

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

        int thickness = 3; // 👈 hallway thickness (odd number looks best)

        if (rng.nextBoolean()) {
            // Horizontal first
            for (int x = Math.min(ax, bx); x <= Math.max(ax, bx); x++) {
                for (int t = -thickness / 2; t <= thickness / 2; t++) {
                    carve(x, ay + t);
                }
            }
            for (int y = Math.min(ay, by); y <= Math.max(ay, by); y++) {
                for (int t = -thickness / 2; t <= thickness / 2; t++) {
                    carve(bx + t, y);
                }
            }
        } else {
            // Vertical first
            for (int y = Math.min(ay, by); y <= Math.max(ay, by); y++) {
                for (int t = -thickness / 2; t <= thickness / 2; t++) {
                    carve(ax + t, y);
                }
            }
            for (int x = Math.min(ax, bx); x <= Math.max(ax, bx); x++) {
                for (int t = -thickness / 2; t <= thickness / 2; t++) {
                    carve(x, by + t);
                }
            }
        }
    }

    private void carve(int x, int y) {
        if (x < 0 || y < 0 || x >= MAP_WIDTH || y >= MAP_HEIGHT) return;
        floor[x][y] = true;
    }


    public boolean collides(float x, float y) {
        int tx = (int)((x + TILE_SIZE / 2f) / TILE_SIZE);
        int ty = (int)((y + TILE_SIZE / 2f) / TILE_SIZE);
        if (tx < 0 || ty < 0 || tx >= MAP_WIDTH || ty >= MAP_HEIGHT) return true;
        return !floor[tx][ty];
    }

    public void drawMap() {
        for (int x = 0; x < MAP_WIDTH; x++)
            for (int y = 0; y < MAP_HEIGHT; y++) {
                if (floor[x][y]) {
                    float[] c = roomId[x][y] >= 0 ? roomColors[roomId[x][y]] : new float[]{0.7f, 0.7f, 0.7f};
                    glColor3f(c[0], c[1], c[2]);

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
    }

    public void drawMiniMap(float playerX, float playerY, int windowHeight) {
        float scale = 3f, px = 10f, py = windowHeight - MAP_HEIGHT * scale - 10f;

        // Draw map
        for (int x = 0; x < MAP_WIDTH; x++)
            for (int y = 0; y < MAP_HEIGHT; y++) {
                if (floor[x][y]) {
                    float[] c = roomId[x][y] >= 0 ? roomColors[roomId[x][y]] : new float[]{0.7f, 0.7f, 0.7f};
                    glColor3f(c[0], c[1], c[2]);
                    glBegin(GL_QUADS);
                    glVertex2f(px + x * scale, py + y * scale);
                    glVertex2f(px + (x + 1) * scale, py + y * scale);
                    glVertex2f(px + (x + 1) * scale, py + (y + 1) * scale);
                    glVertex2f(px + x * scale, py + (y + 1) * scale);
                    glEnd();
                }
            }

        // Draw player
        glColor3f(1f, 0.2f, 0.2f);
        glBegin(GL_QUADS);
        glVertex2f(px + playerX / TILE_SIZE * scale, py + playerY / TILE_SIZE * scale);
        glVertex2f(px + (playerX + TILE_SIZE) / TILE_SIZE * scale, py + playerY / TILE_SIZE * scale);
        glVertex2f(px + (playerX + TILE_SIZE) / TILE_SIZE * scale, py + (playerY + TILE_SIZE) / TILE_SIZE * scale);
        glVertex2f(px + playerX / TILE_SIZE * scale, py + (playerY + TILE_SIZE) / TILE_SIZE * scale);
        glEnd();
    }
}
