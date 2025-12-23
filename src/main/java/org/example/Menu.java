package org.example;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTBakedChar;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;

public class Menu {

    private final long window;
    private int width, height;
    private final Runnable startAction;

    private STBTTBakedChar.Buffer cdata;
    private int fontTex = -1;

    public Menu(long window, int width, int height, Runnable startAction) {
        this.window = window;
        this.width = width;
        this.height = height;
        this.startAction = startAction;

        try {
            loadFont("/fonts/arial.ttf");
        } catch (IOException e) {
            System.err.println("FONT LOAD FAILED: " + e.getMessage());
        }
    }

    public void setSize(int w, int h) {
        this.width = w;
        this.height = h;
    }

    /* ================= FONT ================= */

    private void loadFont(String path) throws IOException {
        InputStream is = Menu.class.getResourceAsStream(path);
        if (is == null) throw new IOException("Missing font: " + path);

        byte[] data = is.readAllBytes();
        is.close();

        ByteBuffer ttf = BufferUtils.createByteBuffer(data.length);
        ttf.put(data).flip();

        cdata = STBTTBakedChar.malloc(96);
        ByteBuffer bitmap = BufferUtils.createByteBuffer(512 * 512);

        stbtt_BakeFontBitmap(ttf, 32, bitmap, 512, 512, 32, cdata);

        fontTex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, fontTex);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, 512, 512, 0,
                GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    }

    /* ================= INPUT ================= */

    public void handleKey(int key, int action) {
        if (key == GLFW_KEY_ENTER && action == GLFW_PRESS) {
            startAction.run();
        }
    }

    public void handleMouse(int button, int action) {
        if (button != GLFW_MOUSE_BUTTON_1 || action != GLFW_PRESS) return;

        double[] mx = new double[1];
        double[] my = new double[1];
        glfwGetCursorPos(window, mx, my);

        float mouseX = (float) mx[0];
        float mouseY = height - (float) my[0];

        float bx = width / 2f - 150;
        float by = height / 2f - 40;
        float bw = 300;
        float bh = 80;

        if (mouseX >= bx && mouseX <= bx + bw &&
                mouseY >= by && mouseY <= by + bh) {
            startAction.run();
        }
    }

    /* ================= DRAW ================= */

    public void drawMenu() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, 0, height, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glClearColor(0.08f, 0.08f, 0.12f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);

        // Title
        glColor3f(1f, 1f, 1f);
        drawText("JavUs", width / 2f - 80, height - 120, 2.5f);

        // Button
        float bx = width / 2f - 150;
        float by = height / 2f - 40;
        float bw = 300;
        float bh = 80;

        glColor3f(0.25f, 0.55f, 0.9f);
        glBegin(GL_QUADS);
        glVertex2f(bx, by);
        glVertex2f(bx + bw, by);
        glVertex2f(bx + bw, by + bh);
        glVertex2f(bx, by + bh);
        glEnd();

        glColor3f(1f, 1f, 1f);
        drawText("SinglePlayer", bx + 50, by + 50, 1.4f);
    }

    /* ================= TEXT ================= */

    private void drawText(String text, float x, float y, float scale) {
        if (cdata == null || fontTex == -1) return;

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBindTexture(GL_TEXTURE_2D, fontTex);

        float px = x;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < 32 || c > 126) continue;

            STBTTBakedChar g = cdata.get(c - 32);

            float x0 = px + g.xoff() * scale;
            float y0 = y + g.yoff() * scale;
            float x1 = x0 + (g.x1() - g.x0()) * scale;
            float y1 = y0 + (g.y1() - g.y0()) * scale;

            float s0 = g.x0() / 512f;
            float t0 = g.y0() / 512f;
            float s1 = g.x1() / 512f;
            float t1 = g.y1() / 512f;

            glBegin(GL_QUADS);
            glTexCoord2f(s0, 1 - t0); glVertex2f(x0, y0);
            glTexCoord2f(s1, 1 - t0); glVertex2f(x1, y0);
            glTexCoord2f(s1, 1 - t1); glVertex2f(x1, y1);
            glTexCoord2f(s0, 1 - t1); glVertex2f(x0, y1);
            glEnd();

            px += g.xadvance() * scale;
        }

        glDisable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
    }
}
