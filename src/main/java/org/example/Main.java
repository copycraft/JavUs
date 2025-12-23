package org.example;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Main {

    private long window;

    // Player position
    private float playerX = 0f;
    private float playerY = 0f;
    private final float speed = 0.02f;

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        init();
        loop();

        // Cleanup
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void init() {
        // Disable libdecor plugin warning
        System.setProperty("org.lwjgl.glfw.libdecor", "false");

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Create window
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(800, 600, "LWJGL Among Us Prototype", 0, 0);
        if (window == 0) throw new RuntimeException("Failed to create window");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // VSync
        glfwShowWindow(window);

        GL.createCapabilities();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClearColor(0.1f, 0.1f, 0.15f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            handleInput();
            renderPlayer();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void handleInput() {
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) playerY += speed;
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) playerY -= speed;
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) playerX -= speed;
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) playerX += speed;

        // Keep player inside -1..1 OpenGL coordinates
        if (playerX > 0.9f) playerX = 0.9f;
        if (playerX < -0.9f) playerX = -0.9f;
        if (playerY > 0.9f) playerY = 0.9f;
        if (playerY < -0.9f) playerY = -0.9f;
    }

    private void renderPlayer() {
        glColor3f(1f, 0f, 0f); // Red player

        float size = 0.1f;

        glBegin(GL_QUADS);
        glVertex2f(playerX - size / 2, playerY - size / 2);
        glVertex2f(playerX + size / 2, playerY - size / 2);
        glVertex2f(playerX + size / 2, playerY + size / 2);
        glVertex2f(playerX - size / 2, playerY + size / 2);
        glEnd();
    }
}
