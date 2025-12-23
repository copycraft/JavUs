package org.example;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL;

public class Main {
    private long window;
    private int width = 800, height = 600;

    private int gameState = 0; // 0=menu, 1=playing
    private Menu menu;
    private Game game;

    public static void main(String[] args) { new Main().run(); }

    public void run() {
        init();
        loop();
        glfwTerminate();
    }

    private void init() {
        if (!glfwInit()) throw new IllegalStateException("GLFW init failed");
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(width, height, "JavUs", 0, 0);
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        GL.createCapabilities();

        glfwSetFramebufferSizeCallback(window, (win, w, h) -> {
            width = w; height = h;
            glViewport(0, 0, width, height);
            if (game != null) game.setSize(width, height);
            if (menu != null) menu.setSize(width, height);
        });

        menu = new Menu(window, width, height, () -> gameState = 1);
        game = new Game(window, width, height);

        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (gameState == 1) game.handleKey(key, action);
            else menu.handleKey(key, action);
        });

        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (gameState == 0) menu.handleMouse(button, action);
        });
    }

    private void loop() {
        double last = glfwGetTime();
        while (!glfwWindowShouldClose(window)) {
            double now = glfwGetTime();
            float dt = (float) (now - last);
            last = now;

            glClearColor(0.1f, 0.1f, 0.15f, 1f);
            glClear(GL_COLOR_BUFFER_BIT);
            glLoadIdentity();

            switch (gameState) {
                case 0 -> menu.drawMenu();
                case 1 -> {
                    game.update(dt);
                    game.drawGame();
                }
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
}
