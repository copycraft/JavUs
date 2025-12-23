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

    public static void main(String[] args) {
        new Main().run();
    }

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
            width = w;
            height = h;
            glViewport(0, 0, width, height);
            if (game != null) game.setSize(width, height);
            if (menu != null) menu.setSize(width, height);
        });

        menu = new Menu(window, width, height, () -> gameState = 1);
        game = new Game(width, height); // ✅ FIXED

        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (gameState == 1) game.handleKey(key, action);
            else menu.handleKey(key, action);
        });

        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            // Get mouse position
            double[] mx = new double[1];
            double[] my = new double[1];
            glfwGetCursorPos(window, mx, my);

            if (gameState == 0) {
                menu.handleMouse(button, action);
            } else if (gameState == 1) {
                game.handleMouse((float) mx[0], (float) my[0], button, action);
            }
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

            switch (gameState) {
                case 0 -> menu.drawMenu();
                case 1 -> {
                    game.update(dt);
                    game.render();
                }
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
}
