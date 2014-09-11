package net.smert.lwjgl.examples.nehe;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.StringTokenizer;
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
public class NeheLesson_10 {

    private final String WINDOW_TITLE = "NeHe's OpenGL Lesson 10 for LWJGL (Loading And Moving Through A 3D World)";
    private boolean configBlending = false;
    private boolean configDebugMode = false;
    private boolean configFullScreen = false;
    private boolean configLighting = false;
    private boolean configRunning = false;
    private boolean configSmoothLighting = true;
    private boolean configVSync = false;
    private boolean configKeysPressed[] = new boolean[Keyboard.KEYBOARD_SIZE];
    private DisplayMode configDisplayMode = null;
    private float configCameraFieldOfView = 70.0f;
    private float configCameraZClipNear = 0.1f;
    private float configCameraZClipFar = 256.0f;
    private float heading = 0.0f;
    private float pitch = 0.0f;
    private float xpos = 0.0f;
    private float zpos = 0.0f;
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
    private int[] texture = new int[3];
    private Sector sector;
    private String[] commandLineArgs;

    public static void main(String[] args) {
        NeheLesson_10 main = new NeheLesson_10();
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
        setupWorld();
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
        //GL11.glEnable(GL11.GL_CULL_FACE);                                     // Must be disabled since our walls are only one polygon thick
        //GL11.glCullFace(GL11.GL_BACK);
        GL11.glClearDepth(1.0D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glFrontFace(GL11.GL_CCW);
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

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
            texture = loadTexture("mud.bmp");
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
        pixeldata.flip();

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

    private void setupWorld() {
        try {
            BufferedReader worldData = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("world.txt")));
            int numTriangles;
            String line;

            while ((line = worldData.readLine()) != null) {
                if (line.trim().length() == 0 || line.trim().startsWith("//")) {
                    continue;
                }

                if (line.startsWith("NUMPOLLIES")) {
                    numTriangles = Integer.parseInt(line.substring(line.indexOf("NUMPOLLIES") + "NUMPOLLIES".length() + 1));
                    sector = new Sector(numTriangles);

                    break;
                }
            }

            for (int i = 0; i < sector.numTriangles; i++) {
                for (int vert = 0; vert < 3; vert++) {

                    while ((line = worldData.readLine()) != null) {
                        if (line.trim().length() == 0 || line.trim().startsWith("//")) {
                            continue;
                        }

                        break;
                    }

                    if (line != null) {
                        StringTokenizer st = new StringTokenizer(line, " ");

                        sector.triangle[i].vertex[vert].x = Float.valueOf(st.nextToken()).floatValue();
                        sector.triangle[i].vertex[vert].y = Float.valueOf(st.nextToken()).floatValue();
                        sector.triangle[i].vertex[vert].z = Float.valueOf(st.nextToken()).floatValue();
                        sector.triangle[i].vertex[vert].u = Float.valueOf(st.nextToken()).floatValue();
                        sector.triangle[i].vertex[vert].v = Float.valueOf(st.nextToken()).floatValue();
                    }
                }
            }

            worldData.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
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

        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) == true) {
            xpos += (float) Math.sin(heading * (Math.PI / 180)) * 0.01f;
            zpos += (float) Math.cos(heading * (Math.PI / 180)) * 0.01f;
        }
        if ((Keyboard.isKeyDown(Keyboard.KEY_F1) == true)
                && (configKeysPressed[Keyboard.KEY_F1] == false)) {
            configKeysPressed[Keyboard.KEY_F1] = true;

            switchFullScreen();
        } else if (Keyboard.isKeyDown(Keyboard.KEY_F1) == false) {
            configKeysPressed[Keyboard.KEY_F1] = false;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) == true) {
            heading += 2.0f * 0.05f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_NEXT)) {
            pitch += 1.0f * 0.05f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) == true) {
            heading -= 2.0f * 0.05f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_PRIOR)) {
            pitch -= 1.0f * 0.05f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_UP) == true) {
            xpos -= (float) Math.sin(heading * (Math.PI / 180)) * 0.01f;
            zpos -= (float) Math.cos(heading * (Math.PI / 180)) * 0.01f;
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
            //GL11.glEnable(GL11.GL_CULL_FACE);
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

        float x_m, y_m, z_m, u_m, v_m;
        int numTriangles;

        GL11.glRotatef(pitch, 1.0f, 0, 0);
        GL11.glRotatef(360.0f - heading, 0, 1.0f, 0);

        GL11.glTranslatef(-xpos, -0.5f, -zpos);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture[textureFilter % 3]);

        numTriangles = sector.numTriangles;

        // Process Each Triangle
        for (int loop_m = 0; loop_m < numTriangles; loop_m++) {
            GL11.glBegin(GL11.GL_TRIANGLES);
            GL11.glNormal3f(0.0f, 0.0f, 1.0f);
            x_m = sector.triangle[loop_m].vertex[0].x;
            y_m = sector.triangle[loop_m].vertex[0].y;
            z_m = sector.triangle[loop_m].vertex[0].z;
            u_m = sector.triangle[loop_m].vertex[0].u;
            v_m = sector.triangle[loop_m].vertex[0].v;
            GL11.glTexCoord2f(u_m, v_m);
            GL11.glVertex3f(x_m, y_m, z_m);

            x_m = sector.triangle[loop_m].vertex[1].x;
            y_m = sector.triangle[loop_m].vertex[1].y;
            z_m = sector.triangle[loop_m].vertex[1].z;
            u_m = sector.triangle[loop_m].vertex[1].u;
            v_m = sector.triangle[loop_m].vertex[1].v;
            GL11.glTexCoord2f(u_m, v_m);
            GL11.glVertex3f(x_m, y_m, z_m);

            x_m = sector.triangle[loop_m].vertex[2].x;
            y_m = sector.triangle[loop_m].vertex[2].y;
            z_m = sector.triangle[loop_m].vertex[2].z;
            u_m = sector.triangle[loop_m].vertex[2].u;
            v_m = sector.triangle[loop_m].vertex[2].v;
            GL11.glTexCoord2f(u_m, v_m);
            GL11.glVertex3f(x_m, y_m, z_m);
            GL11.glEnd();
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

    private void shutdown() {
        Display.destroy();
    }

}

class Sector {

    public int numTriangles;
    public Triangle triangle[];

    public Sector(int num) {
        numTriangles = num;

        triangle = new Triangle[numTriangles];

        for (int i = 0; i < numTriangles; i++) {
            triangle[i] = new Triangle();
        }
    }

}

class Triangle {

    public InternalVertex vertex[];

    public Triangle() {
        vertex = new InternalVertex[3];

        for (int i = 0; i < 3; i++) {
            vertex[i] = new InternalVertex();
        }
    }

}

class InternalVertex {

    public float x, y, z;
    public float u, v;

}
