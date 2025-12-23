package org.example;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTBakedChar;
import java.nio.ByteBuffer;
import java.io.InputStream;
import java.io.IOException;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.glfw.GLFW.*;

public class Menu {
    private long window;
    private int width, height;
    private final Runnable startAction;

    private STBTTBakedChar.Buffer cdata;
    private int fontTex;

    public Menu(long window, int width, int height, Runnable startAction) {
        this.window = window;
        this.width = width;
        this.height = height;
        this.startAction = startAction;

        try {
            loadFontFromResource("/fonts/arial.ttf");
        } catch (IOException e) {
            System.err.println("Failed to load font resource: " + e.getMessage());
            cdata = null;
            fontTex = -1;
        }
    }

    public void setSize(int w, int h) { this.width = w; this.height = h; }

    private void loadFontFromResource(String resourcePath) throws IOException {
        InputStream is = Menu.class.getResourceAsStream(resourcePath);
        if (is == null) throw new IOException("Resource not found: " + resourcePath);
        byte[] bytes = is.readAllBytes();
        is.close();

        ByteBuffer ttf = BufferUtils.createByteBuffer(bytes.length);
        ttf.put(bytes);
        ttf.flip();

        cdata = STBTTBakedChar.malloc(96); // ASCII 32..127
        ByteBuffer bitmap = BufferUtils.createByteBuffer(512 * 512);
        stbtt_BakeFontBitmap(ttf, 32, bitmap, 512, 512, 32, cdata);

        fontTex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, fontTex);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        // GL_ALPHA may be deprecated on some contexts but works for simple examples
        glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, 512, 512, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    }

    public void handleKey(int key, int action) {
        // Could be extended, e.g. Enter to start
        if (action == GLFW_PRESS && key == GLFW_KEY_ENTER) startAction.run();
    }

    public void handleMouse(int button, int action) {
        if (button == 0 && action == GLFW_PRESS) {
            double[] mx = new double[1], my = new double[1];
            org.lwjgl.glfw.GLFW.glfwGetCursorPos(window, mx, my);
            float mouseX = (float) mx[0];
            float mouseY = height - (float) my[0]; // convert to bottom-left origin

            float btnX = width / 2f - 100f, btnY = height / 2f - 20f, btnW = 200f, btnH = 40f;
            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                startAction.run();
            }
        }
    }

    public void drawMenu() {
        // Screen projection
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, 0, height, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        // Draw background button quad
        float btnX = width / 2f - 100f, btnY = height / 2f - 20f, btnW = 200f, btnH = 40f;
        glColor3f(0.25f, 0.45f, 0.75f);
        glBegin(GL_QUADS);
        glVertex2f(btnX, btnY);
        glVertex2f(btnX + btnW, btnY);
        glVertex2f(btnX + btnW, btnY + btnH);
        glVertex2f(btnX, btnY + btnH);
        glEnd();

        // Draw text if font loaded
        if (cdata != null && fontTex != -1) {
            glColor3f(1f, 1f, 1f);
            // draw centered-ish
            float tx = btnX + 20f;
            float ty = btnY + 25f;
            drawText("SinglePlayer", tx, ty, 1.0f);
        } else {
            // fallback: no font -> draw simple white label box
            glColor3f(1f, 1f, 1f);
            glBegin(GL_QUADS);
            glVertex2f(btnX + 20f, btnY + 10f);
            glVertex2f(btnX + 80f, btnY + 10f);
            glVertex2f(btnX + 80f, btnY + 30f);
            glVertex2f(btnX + 20f, btnY + 30f);
            glEnd();
        }
    }

    private void drawText(String text, float x, float y, float scale) {
        if (cdata == null || fontTex == -1) return;

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBindTexture(GL_TEXTURE_2D, fontTex);

        float px = x;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch < 32 || ch > 126) continue;
            STBTTBakedChar g = cdata.get(ch - 32);

            float x0 = px + g.xoff() * scale;
            float y0 = y + g.yoff() * scale; // use + since STB yoff is baseline relative
            float x1 = x0 + (g.x1() - g.x0()) * scale;
            float y1 = y0 + (g.y1() - g.y0()) * scale;

            float s0 = g.x0() / 512f, t0 = g.y0() / 512f;
            float s1 = g.x1() / 512f, t1 = g.y1() / 512f;

            glBegin(GL_QUADS);
            // flip texture t coordinates: 1 - t
            glTexCoord2f(s0, 1f - t0); glVertex2f(x0, y0);
            glTexCoord2f(s1, 1f - t0); glVertex2f(x1, y0);
            glTexCoord2f(s1, 1f - t1); glVertex2f(x1, y1);
            glTexCoord2f(s0, 1f - t1); glVertex2f(x0, y1);
            glEnd();

            px += g.xadvance() * scale;
        }

        glDisable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
    }
}
