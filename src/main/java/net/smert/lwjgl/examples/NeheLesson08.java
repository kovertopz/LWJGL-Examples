package net.smert.lwjgl.examples;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

/**
 *
 * In lesson 08 we learn how to open an OpenGL window with a size of 800x600 and a 32-bit color depth. We set the
 * background color to gray. Then we draw four cubes on the screen. The cube will have a texture mapped to it. Texture
 * filtering can be toggled and lighting can be turned off and on. Blending can also be turned on. The camera can be
 * controlled with WSAD keys. We enable back face culling in OpenGL. Using LWJGL 2.9.1.
 *
 * This code is based on NeHe's tutorials which can be found at: http://nehe.gamedev.net/
 *
 * @author Jason Sorensen
 */
public class NeheLesson08 {

    private final static float PI_OVER_180 = 0.0174532925f;
    private final String windowTitle = "Jason's OpenGL Lesson 8 for LWJGL (Camera & Culling)";
    private boolean blendOn = false;                                            // Blending OFF / ON
    private boolean configConsoleDebugMode = false;
    private boolean configDisplayFullScreen = false;
    private boolean configDisplayVSync = false;
    private boolean keyDownB = false;                                           // Is B key pressed
    private boolean keyDownF1 = false;
    private boolean keyDownF = false;                                           // Is F key pressed
    private boolean keyDownL = false;                                           // Is L key pressed
    private boolean lightOn = false;                                            // Lighting OFF / ON
    private boolean lessonRunning = false;
    private DisplayMode configDisplayMode = null;
    private float cameraHeading = 0.0f;                                         // Camera heading (NEW)
    private float cameraPitch = 0.0f;                                           // Camera pitch (NEW)
    private float cameraRoll = 0.0f;                                            // Camera roll (NEW)
    private float cameraSpeedLimit = 0.01f;                                     // Camera speed (NEW)
    private float cameraXPosition = 0.0f;                                       // Camera X position (NEW)
    private float cameraYPosition = 1.7f;                                       // Camera Y position (NEW)
    private float cameraZPosition = 10.0f;                                      // Camera Z position (NEW)
    private float configCameraFieldOfView = 70.0f;
    private float configCameraZClipNear = 0.05f;
    private float configCameraZClipFar = 256.0f;
    private float configSkyColor[] = {0.0f, 0.0f, 0.0f, 0.0f};                  // Sky color
    private float lightAmbient[] = {0.8f, 0.0f, 0.0f, 1.0f};                    // Ambient Light Values (Red)
    private float lightDiffuse[] = {0.0f, 0.8f, 0.0f, 1.0f};                    // Diffuse Light Values (Green)
    private float lightPosition[] = {1.0f, 3.0f, 0.5f, 1.0f};                   // Light Position (Top of cubes) (CHANGED)
    private int configDisplayDepth = 32;
    private int configDisplayHeight = 600;
    private int configDisplayWidth = 800;
    private int textureFilter = 0;                                              // Texture filter to use
    private int texture[] = new int[3];                                         // Storage for 3 textures
    private long currentTime = 0;                                               // Current time (NEW)
    private long deltaTime = 0;                                                 // Delta time (NEW)
    private long lastTime = 0;                                                  // Last time (NEW)
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
        NeheLesson08 jlesson08 = new NeheLesson08();
        jlesson08.run(args);
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
        loadTextures();                                                         // Load textures
        startLesson();

        Mouse.setGrabbed(true);
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
        GL11.glDisable(GL11.GL_BLEND);                                          // Disable blending
        GL11.glEnable(GL11.GL_CULL_FACE);                                       // Enable culling (NEW)
        GL11.glCullFace(GL11.GL_BACK);                                          // Cull back facing polygons with normals pointing away from you (NEW)
        GL11.glEnable(GL11.GL_DEPTH_TEST);                                      // Enable the depth buffer
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glClearDepth(1.0);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);                                      // Enable Texture Mapping
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
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, (FloatBuffer) temp.asFloatBuffer().put(lightAmbient).flip());              // Setup The Ambient Light
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, (FloatBuffer) temp.asFloatBuffer().put(lightDiffuse).flip());              // Setup The Diffuse Light
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, (FloatBuffer) temp.asFloatBuffer().put(lightPosition).flip());            // Position The Light
        GL11.glEnable(GL11.GL_LIGHT0);                                          // Enable Light Zero

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);                                 // Full brightness, 50% alpha
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);                       // Blending function for translucency based on source alpha value
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
        if ((Keyboard.isKeyDown(Keyboard.KEY_B) == true)
                && (keyDownB == false)) {                                       // Is B being pressed?
            keyDownB = true;                                                    // Tell program B is being held

            blendOn = !blendOn;
            if (blendOn == false) {
                GL11.glDisable(GL11.GL_BLEND);                                  // Turn blending off
                GL11.glEnable(GL11.GL_DEPTH_TEST);                              // Turn depth testing on
            } else {
                GL11.glEnable(GL11.GL_BLEND);                                   // Turn blending on
                GL11.glDisable(GL11.GL_DEPTH_TEST);                             // Turn depth testing off
            }
        } else if (!Keyboard.isKeyDown(Keyboard.KEY_B)) {                       // Has B key been released?
            keyDownB = false;
        }
        if ((Keyboard.isKeyDown(Keyboard.KEY_F1) == true)
                && (keyDownF1 == false)) {                                      // Is F1 being pressed?
            keyDownF1 = true;                                                   // Tell program F1 is being held

            switchMode();                                                       // Toggle Fullscreen / Windowed Mode
        } else if (Keyboard.isKeyDown(Keyboard.KEY_F1) == false) {              // Has F1 key been released?
            keyDownF1 = false;
        }
        if ((Keyboard.isKeyDown(Keyboard.KEY_F) == true)
                && (keyDownF == false)) {                                       // Is F being pressed?
            keyDownF = true;                                                    // Tell program F is being held

            textureFilter += 1;
        } else if (!Keyboard.isKeyDown(Keyboard.KEY_F)) {                       // Has F key been released?
            keyDownF = false;
        }
        if ((Keyboard.isKeyDown(Keyboard.KEY_L) == true)
                && (keyDownL == false)) {                                       // Is L being pressed?
            keyDownL = true;                                                    // Tell program L is being held

            lightOn = !lightOn;
            if (lightOn == false) {
                GL11.glDisable(GL11.GL_LIGHTING);                               // Disable lighting
            } else {
                GL11.glEnable(GL11.GL_LIGHTING);                                // Enable lighting
            }
        } else if (!Keyboard.isKeyDown(Keyboard.KEY_L)) {                       // Has L key been released?
            keyDownL = false;
        }

        cameraUpdateMovementTime();                                             // Update the time between the camera movements (NEW)

        if (Keyboard.isKeyDown(Keyboard.KEY_S) == true) {
            cameraMoveBackpedal();                                              // Move backwards (NEW)
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_W) == true) {
            cameraMoveForward();                                                // Move forward (NEW)
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A) == true) {
            cameraMoveStrafeLeft();                                             // Strafe left (NEW)
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D) == true) {
            cameraMoveStrafeRight();                                            // Strafe right (NEW)
        }

        cameraMoveLookMouse(Mouse.getDX(), Mouse.getDY());                      // Move the pitch and heading based on mouse movements (NEW)

        cameraCorrectHeadingPitchAndRoll();                                     // Prevent from looking more/less than 90 (NEW)
    }

    private void switchMode() {
        configDisplayFullScreen = !configDisplayFullScreen;

        try {
            Display.setFullscreen(configDisplayFullScreen);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cameraUpdateMovementTime() {
        currentTime = System.currentTimeMillis();
        deltaTime = currentTime - lastTime;
        lastTime = currentTime;

        if (deltaTime > 100) {
            deltaTime = 100l;
        }
    }

    private void cameraMoveBackpedal() {
        cameraXPosition += Math.sin(cameraHeading * PI_OVER_180) * deltaTime * cameraSpeedLimit;
        cameraZPosition += Math.cos(cameraHeading * PI_OVER_180) * deltaTime * cameraSpeedLimit;
    }

    private void cameraMoveForward() {
        cameraXPosition -= Math.sin(cameraHeading * PI_OVER_180) * deltaTime * cameraSpeedLimit;
        cameraZPosition -= Math.cos(cameraHeading * PI_OVER_180) * deltaTime * cameraSpeedLimit;
    }

    private void cameraMoveStrafeLeft() {
        cameraXPosition += Math.sin((cameraHeading - 90) * PI_OVER_180) * deltaTime * cameraSpeedLimit;
        cameraZPosition += Math.cos((cameraHeading - 90) * PI_OVER_180) * deltaTime * cameraSpeedLimit;
    }

    private void cameraMoveStrafeRight() {
        cameraXPosition += Math.sin((cameraHeading + 90) * PI_OVER_180) * deltaTime * cameraSpeedLimit;
        cameraZPosition += Math.cos((cameraHeading + 90) * PI_OVER_180) * deltaTime * cameraSpeedLimit;
    }

    private void cameraMoveLookMouse(int x, int y) {
        cameraHeading -= x * cameraSpeedLimit * 10;
        cameraPitch -= y * cameraSpeedLimit * 10;
    }

    private void cameraCorrectHeadingPitchAndRoll() {
        if (cameraHeading >= 360) {
            cameraHeading %= 360;
        } else if (cameraHeading <= -360) {
            cameraHeading %= 360;

            if (cameraHeading < 0) {
                cameraHeading = cameraHeading + 360;
            }
        } else if (cameraHeading < 0) {
            cameraHeading = cameraHeading + 360;
        }

        if (cameraPitch > 90) {
            cameraPitch = 90;
        } else if (cameraPitch < -90) {
            cameraPitch = -90;
        }
    }

    private void render() {
        clearScene();

        GL11.glRotatef(cameraPitch, 1.0f, 0, 0);
        GL11.glRotatef(360.0f - cameraHeading, 0, 1.0f, 0);
        GL11.glRotatef(cameraRoll, 0, 0, 1.0f);
        GL11.glTranslatef((float) -cameraXPosition, (float) -cameraYPosition, (float) -cameraZPosition);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture[textureFilter % 3]);     // Select a texture based on filter

        renderCube(0, 0, 0);                                                    // Render cube
        renderCube(1, 0, 0);                                                    // Render cube
        renderCube(0, 1, 0);                                                    // Render cube
        renderCube(1, 1, 0);                                                    // Render cube
    }

    private void renderCube(float x, float y, float z) {
        GL11.glPushMatrix();

        GL11.glTranslatef((float) x, (float) y, (float) z);

        GL11.glBegin(GL11.GL_QUADS);

        //front face
        GL11.glNormal3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(1.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(0.0f, 0.0f, 0.0f);

        //back face
        GL11.glNormal3f(0.0f, 0.0f, -1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(1.0f, 0.0f, 1.0f);

        //top face
        GL11.glNormal3f(0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(0.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(1.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(0.0f, 1.0f, 0.0f);

        //bottom face
        GL11.glNormal3f(0.0f, -1.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(1.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(0.0f, 0.0f, 1.0f);

        //left face
        GL11.glNormal3f(-1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(1.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(1.0f, 0.0f, 0.0f);

        //right face
        GL11.glNormal3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(0.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(0.0f, 0.0f, 1.0f);

        GL11.glEnd();

        GL11.glPopMatrix();
    }

    private void clearScene() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glLoadIdentity();
    }

    private void shutdown() {
        Display.destroy();
    }

}
