package net.smert.lwjgl.examples.nehe;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

/**
 *
 * In lesson 06 we learn how to open an OpenGL window with a size of 800x600 and a 32-bit color depth. We set the
 * background color to gray. Then we draw a cube on the screen. The cube will have a texture mapped to it and will
 * rotate around XY axes. Texture filtering can be toggled and lighting can be turned off and on. Using LWJGL 2.9.1.
 *
 * This code is based on NeHe's tutorials which can be found at: http://nehe.gamedev.net/
 *
 * @author Jason Sorensen
 */
public class NeheLesson06 {

    private final String windowTitle = "Jason's OpenGL Lesson 6 for LWJGL (Texture Mapping, Texture Filters, Lighting & Keyboard Control)";
    private boolean configConsoleDebugMode = false;
    private boolean configDisplayFullScreen = false;
    private boolean configDisplayVSync = false;
    private boolean keyDownF1 = false;
    private boolean keyDownF = false;                                           // Is F key pressed (NEW)
    private boolean keyDownL = false;                                           // Is L key pressed (NEW)
    private boolean lightOn = false;                                            // Lighting OFF / ON (NEW)
    private boolean lessonRunning = false;
    private DisplayMode configDisplayMode = null;
    private float configCameraFieldOfView = 70.0f;
    private float configCameraZClipNear = 0.05f;
    private float configCameraZClipFar = 256.0f;
    private float configSkyColor[] = {0.5f, 0.5f, 0.5f, 0.0f};
    private float lightAmbient[] = {0.8f, 0.0f, 0.0f, 1.0f};                    // Ambient Light Values (Red) (NEW)
    private float lightDiffuse[] = {0.0f, 0.8f, 0.0f, 1.0f};                    // Diffuse Light Values (Green) (NEW)
    private float lightPosition[] = {0.0f, 3.0f, -5.0f, 1.0f};                  // Light Position (Top of cube) (NEW)
    private float rotationSpeedX = 0.0f;                                        // Angle for the X axis (NEW)
    private float rotationSpeedY = 0.0f;                                        // Angle for the Y axis (NEW)
    private float rotationX = 0.0f;                                             // Angle for the X axis (NEW)
    private float rotationY = 0.0f;                                             // Angle for the Y axis (NEW)
    private int configDisplayDepth = 32;
    private int configDisplayHeight = 600;
    private int configDisplayWidth = 800;
    private int textureFilter = 0;                                              // Texture filter to use (NEW)
    private int texture[] = new int[3];                                         // Storage for 3 textures (NEW)
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
        NeheLesson06 jlesson06 = new NeheLesson06();
        jlesson06.run(args);
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
        loadTextures();                                                         // Load textures (NEW)
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
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);                                      // Enable Texture Mapping (NEW)
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

        ByteBuffer temp = ByteBuffer.allocateDirect(16);
        temp.order(ByteOrder.nativeOrder());
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, (FloatBuffer) temp.asFloatBuffer().put(lightAmbient).flip());              // Setup The Ambient Light (NEW)
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, (FloatBuffer) temp.asFloatBuffer().put(lightDiffuse).flip());              // Setup The Diffuse Light (NEW)
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, (FloatBuffer) temp.asFloatBuffer().put(lightPosition).flip());            // Position The Light (NEW)
        GL11.glEnable(GL11.GL_LIGHT0);                                          // Enable Light Zero (NEW)
    }

    private void loadTextures() {
        try {
            texture = loadTexture("Crate.bmp");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[] loadTexture(String path) throws IOException {
        BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream(path));

        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();
        int ARGB[] = new int[imageHeight * imageWidth];
        byte RGBA[] = new byte[imageHeight * imageWidth * 4];

        image.getRGB(0, 0, imageWidth, imageHeight, ARGB, 0, imageWidth);

        for (int i = 0; i < ARGB.length; i++) {
            int alpha = ARGB[i] >> 24 & 0xff;
            int red = ARGB[i] >> 16 & 0xff;
            int green = ARGB[i] >> 8 & 0xff;
            int blue = ARGB[i] & 0xff;

            RGBA[i * 4 + 0] = (byte) red;
            RGBA[i * 4 + 1] = (byte) green;
            RGBA[i * 4 + 2] = (byte) blue;
            RGBA[i * 4 + 3] = (byte) alpha;
        }

        ByteBuffer pixelData = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);
        pixelData.put(RGBA);
        pixelData.rewind();

        IntBuffer buf = ByteBuffer.allocateDirect(12).order(ByteOrder.nativeOrder()).asIntBuffer();
        GL11.glGenTextures(buf);                                                // Create Texture In OpenGL

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, buf.get(0));                     // Create Nearest Filtered Texture
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, imageWidth, imageHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixelData);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, buf.get(1));                     // Create Linear Filtered Texture
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, imageWidth, imageHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixelData);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, buf.get(2));                     // Create MipMapped Texture
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);
        GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, GL11.GL_RGBA, imageWidth, imageHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixelData);

        return new int[]{buf.get(0), buf.get(1), buf.get(2)};                   // Return Image Addresses In Memory
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
        if ((Keyboard.isKeyDown(Keyboard.KEY_F) == true)
                && (keyDownF == false)) {                                       // Is F being pressed? (NEW)
            keyDownF = true;                                                    // Tell program F is being held (NEW)

            textureFilter += 1;
        } else if (!Keyboard.isKeyDown(Keyboard.KEY_F)) {                       // Has F key been released? (NEW)
            keyDownF = false;
        }
        if ((Keyboard.isKeyDown(Keyboard.KEY_L) == true)
                && (keyDownL == false)) {                                       // Is L being pressed? (NEW)
            keyDownL = true;                                                    // Tell program L is being held (NEW)

            lightOn = !lightOn;
            if (lightOn == false) {
                GL11.glDisable(GL11.GL_LIGHTING);                               // Disable lighting (NEW)
            } else {
                GL11.glEnable(GL11.GL_LIGHTING);                                // Enable lighting (NEW)
            }
        } else if (!Keyboard.isKeyDown(Keyboard.KEY_L)) {                       // Has L key been released? (NEW)
            keyDownL = false;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {                              // Is Up Arrow being pressed? (NEW)
            rotationSpeedX -= 0.001f;                                           // If so, increase rotationSpeedX (NEW)
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {                            // Is Down Arrow being pressed? (NEW)
            rotationSpeedX += 0.001f;                                           // If so, decrease rotationSpeedX (NEW)
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {                            // Is Left Arrow being pressed? (NEW)
            rotationSpeedY -= 0.001f;                                           // If so, decrease rotationSpeedY (NEW)
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {                           // Is Right Arrow being pressed? (NEW)
            rotationSpeedY += 0.001f;                                           // If so, increase rotationSpeedY (NEW)
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

        GL11.glTranslatef(0.0f, 0.0f, -5.0f);                                   // Move into the screen 5 units (NEW)
        GL11.glRotatef(rotationX, 1.0f, 0.0f, 0.0f);                            // Rotate on the X axis (NEW)
        GL11.glRotatef(rotationY, 0.0f, 1.0f, 0.0f);                            // Rotate on the Y axis (NEW)

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture[textureFilter % 3]);     // Select a texture based on filter (NEW)

        GL11.glBegin(GL11.GL_QUADS);
        // Front Face
        GL11.glNormal3f(0.0f, 0.0f, 1.0f);                                      // Front face normal (NEW)
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(-1.0f, -1.0f, 1.0f);                                    // Bottom left of the texture and quad (NEW)
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f, -1.0f, 1.0f);                                     // Bottom right of the texture and quad (NEW)
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);                                      // Top right of the texture and quad (NEW)
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-1.0f, 1.0f, 1.0f);                                     // Top left of the texture and quad (NEW)
        // Back Face
        GL11.glNormal3f(0.0f, 0.0f, -1.0f);                                     // Back face normal (NEW)
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(-1.0f, -1.0f, -1.0f);                                   // Bottom right of the texture and quad (NEW)
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(-1.0f, 1.0f, -1.0f);                                    // Top right of the texture and quad (NEW)
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(1.0f, 1.0f, -1.0f);                                     // Top left of the texture and quad (NEW)
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(1.0f, -1.0f, -1.0f);                                    // Bottom left of the texture and quad (NEW)
        // Top Face
        GL11.glNormal3f(0.0f, 1.0f, 0.0f);                                      // Top face normal (NEW)
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-1.0f, 1.0f, -1.0f);                                    // Top left of the texture and quad (NEW)
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(-1.0f, 1.0f, 1.0f);                                     // Bottom left of the texture and quad (NEW)
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);                                      // Bottom right of the texture and quad (NEW)
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(1.0f, 1.0f, -1.0f);                                     // Top right of the texture and quad (NEW)
        // Bottom Face
        GL11.glNormal3f(0.0f, -1.0f, 0.0f);                                     // Bottom face normal (NEW)
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(-1.0f, -1.0f, -1.0f);                                   // Top right of the texture and quad (NEW)
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(1.0f, -1.0f, -1.0f);                                    // Top left of the texture and quad (NEW)
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(1.0f, -1.0f, 1.0f);                                     // Bottom left of the texture and quad (NEW)
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(-1.0f, -1.0f, 1.0f);                                    // Bottom right of the texture and quad (NEW)
        // Right face
        GL11.glNormal3f(1.0f, 0.0f, 0.0f);                                      // Right face normal (NEW)
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f, -1.0f, -1.0f);                                    // Bottom right of the texture and quad (NEW)
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(1.0f, 1.0f, -1.0f);                                     // Top right of the texture and quad (NEW)
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);                                      // Top left of the texture and quad (NEW)
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(1.0f, -1.0f, 1.0f);                                     // Bottom left of the texture and quad (NEW)
        // Left Face
        GL11.glNormal3f(-1.0f, 0.0f, 0.0f);                                     // Left face normal (NEW)
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(-1.0f, -1.0f, -1.0f);                                   // Bottom left of the texture and quad (NEW)
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(-1.0f, -1.0f, 1.0f);                                    // Bottom right of the texture and quad (NEW)
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(-1.0f, 1.0f, 1.0f);                                     // Top right of the texture and quad (NEW)
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-1.0f, 1.0f, -1.0f);                                    // Top left of the texture and quad (NEW)
        GL11.glEnd();

        rotationX += rotationSpeedX;                                            // X axis rotation (NEW)
        rotationY += rotationSpeedY;                                            // Y axis rotation (NEW)
    }

    private void clearScene() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glLoadIdentity();
    }

    private void shutdown() {
        Display.destroy();
    }

}
