package net.smert.lwjgl.examples.nehe;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

/**
 *
 * @author Jason
 */
public class NeheLesson_5to8 {

    private final String WINDOW_TITLE = "NeHe's OpenGL Lesson 5-8 for LWJGL (3D Shapes, Texture Mapping, Texture Filters, Lighting & Keyboard Control, Blending)";
    private boolean configBlending = false;
    private boolean configDebugMode = false;
    private boolean configFullScreen = false;
    private boolean configLighting = false;
    private boolean configRotation = true;
    private boolean configRunning = false;
    private boolean configSmoothLighting = true;
    private boolean configVSync = false;
    private boolean configKeysPressed[] = new boolean[Keyboard.KEYBOARD_SIZE];
    private DisplayMode configDisplayMode = null;
    private float configCameraFieldOfView = 70.0f;
    private float configCameraZClipNear = 0.1f;
    private float configCameraZClipFar = 256.0f;
    private float rotationCube = 0.0f;
    private float rotationPyramid = 0.0f;
    private float rotationSpeedX = 0.0f;
    private float rotationSpeedY = 0.0f;
    private float rotationX = 0.0f;
    private float rotationY = 0.0f;
    private float[] ambient = {0.2f, 0.2f, 0.2f, 1.0f};
    private float[] configClearColor = {0.0f, 0.0f, 0.0f, 0.0f};
    private float[] diffuse = {0.8f, 0.8f, 0.8f, 1.0f};
    private float[] emission = {0.0f, 0.0f, 0.0f, 0.0f};
    private float[] globalAmbientLighting = {0.2f, 0.2f, 0.2f, 1.0f};
    private float[] globalLightAmbient = {0.0f, 0.0f, 0.0f, 1.0f};
    private float[] globalLightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
    private float[] globalLightPosition = {100.0f, 100.0f, 100.0f, 1.0f};       // Different from OpenGL spec
    private float[] globalLightSpecular = {1.0f, 1.0f, 1.0f, 1.0f};
    private float[] globalLightSpotDirection = {0.0f, 0.0f, -1.0f, 0.0f};
    private float[] specular = {0.0f, 0.0f, 0.0f, 1.0f};
    private int configWindowDepth = 32;
    private int configWindowHeight = 768;
    private int configWindowWidth = 1024;
    private int globalLightSpotCutoff = 180;
    private int globalLightSpotExponent = 0;
    private int shininess = 0;
    private int textureFilter = 0;
    private int[] textureCrate = new int[3];
    private int[] textureGlass = new int[3];
    private String[] commandLineArgs;

    public static void main(String[] args) {
        NeheLesson_5to8 main = new NeheLesson_5to8();
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
        loadTextures();
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
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glClearDepth(1.0D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glFrontFace(GL11.GL_CCW);
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

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

    private void loadTextures() {
        try {
            textureCrate = loadTexture("Crate.bmp");
            textureGlass = loadTexture("Glass.bmp");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[] loadTexture(String path) throws IOException {
        BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream(path));

        int imageheight = image.getHeight();
        int imagewidth = image.getWidth();
        int ARGB[] = new int[imageheight * imagewidth];
        byte RGBA[] = new byte[imageheight * imagewidth * 4];

        image.getRGB(0, 0, imagewidth, imageheight, ARGB, 0, imagewidth);

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

        ByteBuffer pixeldata = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
        pixeldata.put(RGBA);
        pixeldata.rewind();

        IntBuffer buf = BufferUtils.createIntBuffer(3);
        GL11.glGenTextures(buf);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, buf.get(0));                     // Create Nearest Filtered Texture
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, imagewidth, imageheight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixeldata);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, buf.get(1));                     // Create Linear Filtered Texture
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, imagewidth, imageheight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixeldata);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, buf.get(2));                     // Create MipMapped Texture
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);
        GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, GL11.GL_RGBA, imagewidth, imageheight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixeldata);

        return new int[]{buf.get(0), buf.get(1), buf.get(2)};
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

        if ((Keyboard.isKeyDown(Keyboard.KEY_DOWN) == true)
                && (configKeysPressed[Keyboard.KEY_DOWN] == false)) {
            configKeysPressed[Keyboard.KEY_DOWN] = true;

            rotationSpeedX -= 0.01f;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) == false) {
            configKeysPressed[Keyboard.KEY_DOWN] = false;
        }
        if ((Keyboard.isKeyDown(Keyboard.KEY_F1) == true)
                && (configKeysPressed[Keyboard.KEY_F1] == false)) {
            configKeysPressed[Keyboard.KEY_F1] = true;

            switchFullScreen();
        } else if (Keyboard.isKeyDown(Keyboard.KEY_F1) == false) {
            configKeysPressed[Keyboard.KEY_F1] = false;
        }
        if ((Keyboard.isKeyDown(Keyboard.KEY_LEFT) == true)
                && (configKeysPressed[Keyboard.KEY_LEFT] == false)) {
            configKeysPressed[Keyboard.KEY_LEFT] = true;

            rotationSpeedY -= 0.01f;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) == false) {
            configKeysPressed[Keyboard.KEY_LEFT] = false;
        }
        if ((Keyboard.isKeyDown(Keyboard.KEY_RIGHT) == true)
                && (configKeysPressed[Keyboard.KEY_RIGHT] == false)) {
            configKeysPressed[Keyboard.KEY_RIGHT] = true;

            rotationSpeedY += 0.01f;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) == false) {
            configKeysPressed[Keyboard.KEY_RIGHT] = false;
        }
        if ((Keyboard.isKeyDown(Keyboard.KEY_UP) == true)
                && (configKeysPressed[Keyboard.KEY_UP] == false)) {
            configKeysPressed[Keyboard.KEY_UP] = true;

            rotationSpeedX += 0.01f;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_UP) == false) {
            configKeysPressed[Keyboard.KEY_UP] = false;
        }
        if ((Keyboard.isKeyDown(Keyboard.KEY_SPACE) == true)
                && (configKeysPressed[Keyboard.KEY_SPACE] == false)) {
            configKeysPressed[Keyboard.KEY_SPACE] = true;

            switchRotation();
        } else if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) == false) {
            configKeysPressed[Keyboard.KEY_SPACE] = false;
        }
        if ((Keyboard.isKeyDown(Keyboard.KEY_B) == true)
                && (configKeysPressed[Keyboard.KEY_B] == false)) {
            configKeysPressed[Keyboard.KEY_B] = true;

            switchBlending();
        } else if (Keyboard.isKeyDown(Keyboard.KEY_B) == false) {
            configKeysPressed[Keyboard.KEY_B] = false;
        }
        if ((Keyboard.isKeyDown(Keyboard.KEY_F) == true)
                && (configKeysPressed[Keyboard.KEY_F] == false)) {
            configKeysPressed[Keyboard.KEY_F] = true;

            switchTextureFiltering();
        } else if (Keyboard.isKeyDown(Keyboard.KEY_F) == false) {
            configKeysPressed[Keyboard.KEY_F] = false;
        }
        if ((Keyboard.isKeyDown(Keyboard.KEY_L) == true)
                && (configKeysPressed[Keyboard.KEY_L] == false)) {
            configKeysPressed[Keyboard.KEY_L] = true;

            switchLighting();
        } else if (Keyboard.isKeyDown(Keyboard.KEY_L) == false) {
            configKeysPressed[Keyboard.KEY_L] = false;
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

    private void switchRotation() {
        configRotation = !configRotation;

        if (configDebugMode == true) {
            if (configBlending == true) {
                System.out.println("Enabling Rotation");
            } else {
                System.out.println("Disabling Rotation");
            }
        }
    }

    private void switchBlending() {
        configBlending = !configBlending;

        if (configDebugMode == true) {
            if (configBlending == true) {
                System.out.println("Enabling Blending");
            } else {
                System.out.println("Disabling Blending");
            }
        }

        if (configBlending == true) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        } else {
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
    }

    private void switchTextureFiltering() {
        textureFilter++;
    }

    private void switchLighting() {
        configLighting = !configLighting;

        if (configDebugMode == true) {
            if (configLighting == true) {
                System.out.println("Enabling Lighting");
            } else {
                System.out.println("Disabling Lighting");
            }
        }

        if (configLighting == true) {
            GL11.glEnable(GL11.GL_LIGHTING);
        } else {
            GL11.glDisable(GL11.GL_LIGHTING);
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

        updateLighting();

        GL11.glDisable(GL11.GL_TEXTURE_2D);

        renderColoredCube();
        renderColoredPyramid();

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        renderTexturedBlendedCube();
        renderTexturedCube();

        if (configRotation == true) {
            rotationCube += 0.05f;
            rotationPyramid -= 0.15f;
            rotationX += rotationSpeedX;
            rotationY += rotationSpeedY;
        }
    }

    private void clearScene() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glLoadIdentity();
    }

    private void updateLighting() {
        FloatBuffer floatbuffer = BufferUtils.createFloatBuffer(4);

        GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, (FloatBuffer) floatbuffer.put(globalAmbientLighting).flip());

        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, (FloatBuffer) floatbuffer.put(globalLightAmbient).flip());
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, (FloatBuffer) floatbuffer.put(globalLightDiffuse).flip());
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, (FloatBuffer) floatbuffer.put(globalLightPosition).flip());
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, (FloatBuffer) floatbuffer.put(globalLightSpecular).flip());
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPOT_DIRECTION, (FloatBuffer) floatbuffer.put(globalLightSpotDirection).flip());
        GL11.glLighti(GL11.GL_LIGHT0, GL11.GL_SPOT_CUTOFF, globalLightSpotCutoff);
        GL11.glLighti(GL11.GL_LIGHT0, GL11.GL_SPOT_EXPONENT, globalLightSpotExponent);
        GL11.glEnable(GL11.GL_LIGHT0);

        GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT, (FloatBuffer) floatbuffer.put(ambient).flip());
        GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE, (FloatBuffer) floatbuffer.put(diffuse).flip());
        GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_EMISSION, (FloatBuffer) floatbuffer.put(emission).flip());
        GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR, (FloatBuffer) floatbuffer.put(specular).flip());
        GL11.glMateriali(GL11.GL_FRONT_AND_BACK, GL11.GL_SHININESS, shininess);
    }

    private void renderColoredCube() {
        GL11.glPushMatrix();

        GL11.glTranslatef(1.5f, -1.5f, -6.0f);
        GL11.glRotatef(rotationCube, 0.0f, 1.0f, 0.0f);

        renderCube();

        GL11.glPopMatrix();
    }

    private void renderColoredPyramid() {
        GL11.glPushMatrix();

        GL11.glTranslatef(-1.5f, -1.5f, -6.0f);
        GL11.glRotatef(rotationPyramid, 0.0f, 1.0f, 0.0f);

        GL11.glBegin(GL11.GL_TRIANGLES);

        // +Z
        GL11.glNormal3f(0.0f, 0.4472136f, 0.8944272f);

        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glColor3f(1.0f, 0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.5f, 0.0f);
        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);

        // +X
        GL11.glNormal3f(0.8944272f, 0.4472136f, 0.0f);

        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glColor3f(1.0f, 0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.5f, 0.0f);
        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);

        // -Z
        GL11.glNormal3f(0.0f, 0.4472136f, -0.8944272f);

        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glColor3f(1.0f, 0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.5f, 0.0f);
        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);

        // -X
        GL11.glNormal3f(-0.8944272f, 0.4472136f, 0.0f);

        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glColor3f(1.0f, 0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.5f, 0.0f);
        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);

        // Bottom
        GL11.glNormal3f(0.0f, -1.0f, 0.0f);

        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);

        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);

        GL11.glEnd();
        GL11.glPopMatrix();
    }

    private void renderCube() {
        GL11.glBegin(GL11.GL_TRIANGLES);

        // Face +Z
        GL11.glNormal3f(0.0f, 0.0f, 1.0f);

        GL11.glColor3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);                                          // Upper left of face is the origin
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);

        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glColor3f(0.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glColor3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);

        // Face +X
        GL11.glNormal3f(1.0f, 0.0f, 0.0f);

        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glColor3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glColor3f(0.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);

        GL11.glColor3f(0.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);

        // Face -Z
        GL11.glNormal3f(0.0f, 0.0f, -1.0f);

        GL11.glColor3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);

        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glColor3f(0.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glColor3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);

        // Face -X
        GL11.glNormal3f(-1.0f, 0.0f, 0.0f);

        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
        GL11.glColor3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
        GL11.glColor3f(0.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);

        GL11.glColor3f(0.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);

        // Face +Y
        GL11.glNormal3f(0.0f, 1.0f, 0.0f);

        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glColor3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);

        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
        GL11.glColor3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);

        // Face -Y
        GL11.glNormal3f(0.0f, -1.0f, 0.0f);

        GL11.glColor3f(0.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glColor3f(0.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);

        GL11.glColor3f(0.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glColor3f(0.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);

        GL11.glEnd();
    }

    private void renderTexturedBlendedCube() {
        GL11.glPushMatrix();

        GL11.glTranslatef(1.5f, 1.5f, -6.0f);
        GL11.glRotatef(-rotationX, 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(-rotationY, 0.0f, 1.0f, 0.0f);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureGlass[textureFilter % 3]);

        renderCube();

        GL11.glPopMatrix();
    }

    private void renderTexturedCube() {
        GL11.glPushMatrix();

        GL11.glTranslatef(-1.5f, 1.5f, -6.0f);
        GL11.glRotatef(rotationX, 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(rotationY, 0.0f, 1.0f, 0.0f);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureCrate[textureFilter % 3]);

        renderCube();

        GL11.glPopMatrix();
    }

    private void shutdown() {
        Display.destroy();
    }

}
