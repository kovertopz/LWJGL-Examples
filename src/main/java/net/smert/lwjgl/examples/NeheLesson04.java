package net.smert.lwjgl.examples;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

/**
 *
 * In lesson 04 we learn how to open an OpenGL window with a size of 800x600 and a 32-bit color depth. We set the
 * background color to gray. Then we draw a triangle and square on the screen. The triangle will have three colors which
 * will blend together and the square will only have one. We will rotate the triangle around the Y axis and the square
 * around the Z axis. Using LWJGL 2.9.1.
 *
 * This code is based on NeHe's tutorials which can be found at: http://nehe.gamedev.net/
 *
 * @author Jason Sorensen
 */
public class NeheLesson04 {

    private final String windowTitle = "NeHe's OpenGL Lesson 4 for LWJGL (Rotation)";
    private boolean configConsoleDebugMode = false;
    private boolean configDisplayFullScreen = false;
    private boolean configDisplayVSync = false;
    private boolean keyDownF1 = false;
    private boolean lessonRunning = false;
    private DisplayMode configDisplayMode = null;
    private float configCameraFieldOfView = 70.0f;
    private float configCameraZClipNear = 0.05f;
    private float configCameraZClipFar = 256.0f;
    private float configSkyColor[] = {0.5f, 0.5f, 0.5f, 0.0f};
    private float rotationQuad = 0.0f;                                          // Angle for the quad (NEW)
    private float rotationTriangle = 0.0f;                                      // Angle for the triangle (NEW)
    private int configDisplayDepth = 32;
    private int configDisplayHeight = 600;
    private int configDisplayWidth = 800;
    private String[] commandLineArgs;

    /**
     * Everything starts and ends here. Optional command line arguments.
     *
     * If debug is specified on the command line then the lesson will show debug messages.
     *
     * If fullscreen is specified on the command line then the lesson will start full screen, otherwise windowed mode
     * will be used.
     *
     * If vsync is specified on the command line then the lesson will start with vsync enabled.
     */
    public static void main(String[] args) {
        NeheLesson04 jlesson04 = new NeheLesson04();
        jlesson04.run(args);
    }

    public void run(String[] args) {
        commandLineArgs = args;

        parseCommandLineOptions();

        try {
            init();

            while (lessonRunning) {
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
            configConsoleDebugMode = true;
        } else if (commandlinearg.equalsIgnoreCase("fullscreen") == true) {
            configDisplayFullScreen = true;
        } else if (commandlinearg.equalsIgnoreCase("vsync") == true) {
            configDisplayVSync = true;
        }
    }

    private void init() throws Exception {
        createWindow();
        initGL();
        startLesson();
    }

    /*
     * First try to get a display mode that is full screen capable, if not then
     * fall back to a windowed mode.
     */
    private void createWindow() throws Exception {
        DisplayMode dms[] = Display.getAvailableDisplayModes();
        for (int i = 0; i < dms.length; i++) {
            if ((dms[i].getWidth() == configDisplayWidth)
                    && (dms[i].getHeight() == configDisplayHeight)
                    && (dms[i].getBitsPerPixel() == configDisplayDepth)) {
                configDisplayMode = dms[i];
                break;
            }
        }

        if (configDisplayMode == null) {
            configDisplayMode = new DisplayMode(configDisplayWidth, configDisplayHeight);
        }

        if (configConsoleDebugMode == true) {
            System.out.println("Display Mode: Width: " + configDisplayMode.getWidth()
                    + "px Height: " + configDisplayMode.getHeight() + "px Depth: "
                    + configDisplayMode.getBitsPerPixel() + "bpp");
        }

        Display.setDisplayMode(configDisplayMode);
        Display.setTitle(windowTitle);
        Display.create();

        if (Display.getDisplayMode().isFullscreenCapable() == true) {
            Display.setFullscreen(configDisplayFullScreen);
        }
        Display.setVSyncEnabled(configDisplayVSync);
    }

    private void initGL() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);                                      // Enable the depth buffer
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glClearDepth(1.0);
        GL11.glShadeModel(GL11.GL_SMOOTH);                                      // Enable computed colors of vertices to be interpolated as the primitive is rasterized

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

        GL11.glClearColor(configSkyColor[0], configSkyColor[1],
                configSkyColor[2], configSkyColor[3]);                          // Set the sky color
    }

    private void startLesson() {
        lessonRunning = true;
    }

    private void mainLoop() {
        input();
        render();

        Display.update();
    }

    private void input() {
        if (Display.isCloseRequested() == true) {                               // Exit if window is closed
            lessonRunning = false;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) == true) {                  // Exit if Escape is pressed
            lessonRunning = false;
        }
        if ((Keyboard.isKeyDown(Keyboard.KEY_F1) == true)
                && (keyDownF1 == false)) {                                      // Is F1 being pressed?
            keyDownF1 = true;                                                   // Tell program F1 is being held

            switchMode();                                                       // Toggle Fullscreen / Windowed Mode
        } else if (Keyboard.isKeyDown(Keyboard.KEY_F1) == false) {              // Has F1 key been released?
            keyDownF1 = false;
        }
    }

    private void switchMode() {
        configDisplayFullScreen = !configDisplayFullScreen;

        try {
            Display.setFullscreen(configDisplayFullScreen);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void render() {
        clearScene();

        GL11.glTranslatef(-1.5f, 0.0f, -6.0f);                                  // Move left 1.5 units And into the screen 6.0
        GL11.glRotatef(rotationTriangle, 0.0f, 1.0f, 0.0f);                     // Rotate the triangle on the Y axis (NEW)

        GL11.glBegin(GL11.GL_TRIANGLES);                                        // Start draw a triangle
        GL11.glColor3f(1.0f, 0.0f, 0.0f);                                       // Set the color to red
        GL11.glVertex3f(0.0f, 1.0f, 0.0f);                                      // Top
        GL11.glColor3f(0.0f, 1.0f, 0.0f);                                       // Set the color to green
        GL11.glVertex3f(-1.0f, -1.0f, 0.0f);                                    // Bottom left
        GL11.glColor3f(0.0f, 0.0f, 1.0f);                                       // Set the color to blue
        GL11.glVertex3f(1.0f, -1.0f, 0.0f);                                     // Bottom right
        GL11.glEnd();                                                           // Finished drawing the triangle

        GL11.glLoadIdentity();                                                  // Reset the current Modelview Matrix (NEW)
        GL11.glTranslatef(1.5f, 0.0f, -6.0f);                                   // Move right 1.5 units and into the screen 6.0 (CHANGED)
        GL11.glRotatef(rotationQuad, 1.0f, 0.0f, 0.0f);                         // Rotate the quad on the X axis (NEW)

        GL11.glColor3f(0.25f, 0.25f, 1.0f);                                     // Set the color to blue for all verticies

        GL11.glBegin(GL11.GL_QUADS);                                            // Start drawing a quad
        GL11.glVertex3f(-1.0f, 1.0f, 0.0f);                                     // Top left
        GL11.glVertex3f(1.0f, 1.0f, 0.0f);                                      // Top tight
        GL11.glVertex3f(1.0f, -1.0f, 0.0f);                                     // Bottom right
        GL11.glVertex3f(-1.0f, -1.0f, 0.0f);                                    // Bottom left
        GL11.glEnd();                                                           // Finished drawing the quad

        rotationQuad += 0.2f;                                                   // Increase the rotation angle for the quad (NEW)
        rotationTriangle -= 0.15f;                                              // Decrease the rotation angle for the triangle (NEW)
    }

    private void clearScene() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glLoadIdentity();
    }

    private void shutdown() {
        Display.destroy();
    }

}
