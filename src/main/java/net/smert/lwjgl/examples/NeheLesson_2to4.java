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
public class NeheLesson_2to4 {

    private final String WINDOW_TITLE = "NeHe's OpenGL Lesson 2-4 for LWJGL (Your First Polygon, Adding Color, Rotation)";
    private boolean configDebugMode = false;
    private boolean configFullScreen = false;
    private boolean configRunning = false;
    private boolean configSmoothLighting = true;
    private boolean configVSync = false;
    private boolean configKeysPressed[] = new boolean[Keyboard.KEYBOARD_SIZE];
    private DisplayMode configDisplayMode = null;
    private float configCameraFieldOfView = 70.0f;
    private float configCameraZClipNear = 0.1f;
    private float configCameraZClipFar = 256.0f;
    private float rotationQuad = 0.0f;
    private float rotationTriangle = 0.0f;
    private float configClearColor[] = {0.5f, 1.0f, 0.5f, 0.0f};
    private int configWindowDepth = 32;
    private int configWindowHeight = 768;
    private int configWindowWidth = 1024;
    private String[] commandLineArgs;

    public static void main(String[] args) {
        NeheLesson_2to4 main = new NeheLesson_2to4();
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
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glClearDepth(1.0);
        GL11.glShadeModel(GL11.GL_SMOOTH);

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
        if ((Keyboard.isKeyDown(Keyboard.KEY_S) == true)
                && (configKeysPressed[Keyboard.KEY_S] == false)) {
            configKeysPressed[Keyboard.KEY_S] = true;

            switchSmoothLighting();
        } else if (Keyboard.isKeyDown(Keyboard.KEY_S) == false) {
            configKeysPressed[Keyboard.KEY_S] = false;
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

    private void switchSmoothLighting() {
        configSmoothLighting = !configSmoothLighting;

        if (configDebugMode == true) {
            if (configSmoothLighting == true) {
                System.out.println("Enabling Smooth Lighting");
            } else {
                System.out.println("Disabling Smooth Lighting");
            }
        }

        if (configSmoothLighting == true) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        } else {
            GL11.glShadeModel(GL11.GL_FLAT);
        }
    }

    private void render() {
        clearScene();

        renderQuad();
        renderTriangle();
    }

    private void clearScene() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glLoadIdentity();
    }

    private void renderQuad() {
        GL11.glPushMatrix();

        GL11.glTranslatef(1.5f, 0.0f, -6.0f);
        GL11.glRotatef(rotationQuad, 1.0f, 0.0f, 0.0f);

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glColor3f(0.3f, 0.3f, 1.0f);
        GL11.glVertex3f(-1.0f, 1.0f, 0.0f);
        GL11.glVertex3f(1.0f, 1.0f, 0.0f);
        GL11.glColor3f(1.0f, 0.3f, 0.3f);
        GL11.glVertex3f(1.0f, -1.0f, 0.0f);
        GL11.glVertex3f(-1.0f, -1.0f, 0.0f);

        GL11.glEnd();
        GL11.glPopMatrix();

        rotationQuad += 0.2f;
    }

    private void renderTriangle() {
        GL11.glPushMatrix();

        GL11.glTranslatef(-1.5f, 0.0f, -6.0f);
        GL11.glRotatef(rotationTriangle, 0.0f, 1.0f, 0.0f);

        GL11.glBegin(GL11.GL_TRIANGLES);

        GL11.glColor3f(1.0f, 0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 1.0f, 0.0f);
        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glVertex3f(-1.0f, -1.0f, 0.0f);
        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glVertex3f(1.0f, -1.0f, 0.0f);

        GL11.glEnd();
        GL11.glPopMatrix();

        rotationTriangle -= 0.15f;
    }

    private void shutdown() {
        Display.destroy();
    }

}
