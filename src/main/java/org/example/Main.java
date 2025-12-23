package org.example;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL;

public class Main {
    private long window;
    private int width = 800, height = 600;

    // 0 = menu, 1 = playing, 2 = keybinds (not used yet)
    private int state = 0;

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

        // Create subsystems
        menu = new Menu(window, width, height, () -> state = 1);
        game = new Game(window, width, height);

        // Resize callback: update sizes in subsystems
        glfwSetFramebufferSizeCallback(window, (win, w, h) -> {
            if (w > 0 && h > 0) {
                width = w;
                height = h;
                glViewport(0, 0, width, height);
                menu.setSize(width, height);
                game.setSize(width, height);
            }
        });

        // Forward key / mouse events where appropriate
        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (state == 0) menu.handleKey(key, action);
            else if (state == 1) {
                // Allow toggling menu with ESC
                if (action == GLFW_PRESS && key == GLFW_KEY_ESCAPE) state = 0;
                else game.handleKey(key, action);
            }
        });

        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (state == 0) menu.handleMouse(button, action);
            else if (state == 1) game.handleMouse(button, action);
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

            if (state == 0) {
                menu.drawMenu();
            } else if (state == 1) {
                game.update(dt);
                game.drawGame();
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
}
