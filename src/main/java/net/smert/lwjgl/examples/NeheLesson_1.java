package net.smert.lwjgl.examples;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

/**
 *
 * @author Jason
 */
public class NeheLesson_1 {

    private final String WINDOW_TITLE = "NeHe's OpenGL Lesson 01 for LWJGL (Setting Up An OpenGL Window)";
    private boolean configDebugMode = false;
    private boolean configFullScreen = false;
    private boolean configRunning = false;
    private boolean configVSync = false;
    private boolean configKeysPressed[] = new boolean[Keyboard.KEYBOARD_SIZE];
    private DisplayMode configDisplayMode = null;
    private float configCameraFieldOfView = 70.0f;
    private float configCameraZClipNear = 0.1f;
    private float configCameraZClipFar = 256.0f;
    private float configClearColor[] = {0.5f, 1.0f, 0.5f, 0.0f};
    private int configWindowDepth = 32;
    private int configWindowHeight = 768;
    private int configWindowWidth = 1024;
    private String[] commandLineArgs;

    public static void main(String[] args) {
        NeheLesson_1 main = new NeheLesson_1();
        main.run(args);
    }

    public void run(String[] args) {
        commandLineArgs = args;

        parseCommandLineOptions();

        try {
            init();

            while (configRunning) {
                mainLoop();
            }

            shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void parseCommandLineOptions() {
        for (String commandlinearg : commandLineArgs) {
            parseCommandLineOption(commandlinearg);
        }
    }

    private void parseCommandLineOption(String commandlinearg) {
        if (commandlinearg.equalsIgnoreCase("debug") == true) {
            configDebugMode = true;
        } else if (commandlinearg.equalsIgnoreCase("fullscreen") == true) {
            configFullScreen = true;
        } else if (commandlinearg.equalsIgnoreCase("vsync") == true) {
            configVSync = true;
        }
    }

    private void init() throws Exception {
        createWindow();
        initGL();
        startLesson();
    }

    private void createWindow() {
        try {
            DisplayMode displaymodes[] = Display.getAvailableDisplayModes();

            for (int i = 0; i < displaymodes.length; i++) {
                if ((displaymodes[i].getWidth() == configWindowWidth)
                        && (displaymodes[i].getHeight() == configWindowHeight)
                        && (displaymodes[i].getBitsPerPixel() == configWindowDepth)) {
                    configDisplayMode = displaymodes[i];
                }

                if (configDebugMode == true) {
                    System.out.println("Found Mode: Width: " + displaymodes[i].getWidth()
                            + "px Height: " + displaymodes[i].getHeight() + "px Depth: "
                            + displaymodes[i].getBitsPerPixel() + "bpp");
                }
            }

            if (configDisplayMode == null) {
                configDisplayMode = Display.getDesktopDisplayMode();
            }

            if (configDebugMode == true) {
                System.out.println("Using Display Mode: Width: " + configDisplayMode.getWidth()
                        + "px Height: " + configDisplayMode.getHeight() + "px Depth: "
                        + configDisplayMode.getBitsPerPixel() + "bpp");
            }

            Display.setDisplayMode(configDisplayMode);
            Display.setTitle(WINDOW_TITLE);
            Display.create();

            if ((Display.getDisplayMode().isFullscreenCapable() == true) && (configFullScreen == true)) {
                Display.setFullscreen(configFullScreen);
            }

            Display.setVSyncEnabled(configVSync);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initGL() {
        initProjectionMatrix();
        initModelViewMatrix();
    }

    private void initProjectionMatrix() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();

        GLU.gluPerspective(
                configCameraFieldOfView,
                (float) configDisplayMode.getWidth() / (float) configDisplayMode.getHeight(),
                configCameraZClipNear,
                configCameraZClipFar);
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
    }

    private void initModelViewMatrix() {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        GL11.glClearColor(configClearColor[0], configClearColor[1], configClearColor[2], configClearColor[3]);
    }

    private void startLesson() {
        configRunning = true;
    }

    private void mainLoop() {
        input();
        render();

        Display.update();
    }

    private void input() {
        if (Display.isCloseRequested() == true) {
            configRunning = false;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) == true) {
            configRunning = false;
        }

        if ((Keyboard.isKeyDown(Keyboard.KEY_F1) == true)
                && (configKeysPressed[Keyboard.KEY_F1] == false)) {
            configKeysPressed[Keyboard.KEY_F1] = true;

            switchFullScreen();
        } else if (Keyboard.isKeyDown(Keyboard.KEY_F1) == false) {
            configKeysPressed[Keyboard.KEY_F1] = false;
        }
    }

    private void switchFullScreen() {
        configFullScreen = !configFullScreen;

        if (configDebugMode == true) {
            if (configFullScreen == true) {
                System.out.println("Entering Full Screen Mode");
            } else {
                System.out.println("Exiting Full Screen Mode");
            }
        }

        try {
            Display.setFullscreen(configFullScreen);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void render() {
        clearScene();
    }

    private void clearScene() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL11.glLoadIdentity();
    }

    private void shutdown() {
        Display.destroy();
    }

}
