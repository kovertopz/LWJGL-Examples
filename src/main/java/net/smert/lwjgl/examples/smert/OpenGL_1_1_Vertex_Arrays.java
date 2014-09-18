package net.smert.lwjgl.examples.smert;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;

/**
 *
 * @author Jason Sorensen <sorensenj@smert.net>
 */
public class OpenGL_1_1_Vertex_Arrays {

    private final static float MOUSE_POLL = 1.0f / 125.0f;
    private final static float PI_OVER_180 = (float) Math.PI / 180.0f;
    private final static String WINDOW_TITLE = "OpenGL 1.1 Vertex Arrays";

    private boolean exampleRunning;
    private FloatBuffer cubeWithQuadsAndDifferentColorsPerVertexColors;
    private FloatBuffer cubeWithQuadsAndDifferentColorsPerVertexVertices;
    private FloatBuffer cubeWithQuadsColors;
    private FloatBuffer cubeWithQuadsVertices;
    private FloatBuffer cubeWithTrianglesColors;
    private FloatBuffer cubeWithTrianglesVertices;
    private int fps;
    private long lastSecond;
    private long lastTime;

    private Vector3f camPostion;
    private Vector3f camRotation;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        OpenGL_1_1_Vertex_Arrays example = new OpenGL_1_1_Vertex_Arrays();
        example.start();
    }

    public void start() {
        createWindow();
        initCamera();
        initGL();
        initProjectionMatrix();
        initModelViewMatrix();

        System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));

        GL11.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

        initTimer();
        mainLoop();
    }

    private void createWindow() {
        PixelFormat pixelFormat = new PixelFormat()
                .withDepthBits(24)
                .withStencilBits(8);
        ContextAttribs contextAtrributes = new ContextAttribs(3, 2)
                .withProfileCompatibility(true);

        try {
            Display.setDisplayMode(new DisplayMode(852, 480));
            Display.setTitle(WINDOW_TITLE);
            Display.create(pixelFormat, contextAtrributes);
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void initCamera() {
        camPostion = new Vector3f(0.0f, 0.0f, 5.0f);
        camRotation = new Vector3f();
    }

    private void initGL() {
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glDepthMask(true);
        GL11.glClearDepth(1.0);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    private void initProjectionMatrix() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();

        GLU.gluPerspective(
                70.0f, // FOV
                (float) Display.getWidth() / (float) Display.getHeight(), // Aspect ratio
                0.05f, // Near
                256.0f); // Far

        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
    }

    private void initModelViewMatrix() {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }

    private void initTimer() {
        lastSecond = getTime();
        lastTime = getTime();
    }

    private long getTime() {
        return System.nanoTime();
    }

    private long getTimerResolution() {
        return 1000000000L;
    }

    private void mainLoop() {
        cubeWithQuadsAndDifferentColorsPerVertexColors = BufferUtils.createFloatBuffer(192);
        cubeWithQuadsAndDifferentColorsPerVertexVertices = BufferUtils.createFloatBuffer(192);
        cubeWithQuadsColors = BufferUtils.createFloatBuffer(192);
        cubeWithQuadsVertices = BufferUtils.createFloatBuffer(192);
        cubeWithTrianglesColors = BufferUtils.createFloatBuffer(192);
        cubeWithTrianglesVertices = BufferUtils.createFloatBuffer(192);

        createVertexArrayCubeWithQuads(cubeWithQuadsColors, cubeWithQuadsVertices);
        createVertexArrayCubeWithQuadsAndDifferentColorsPerVertex(
                cubeWithQuadsAndDifferentColorsPerVertexColors, cubeWithQuadsAndDifferentColorsPerVertexVertices);
        createVertexArrayCubeWithTriangles(cubeWithTrianglesColors, cubeWithTrianglesVertices);
        Mouse.setGrabbed(true);
        exampleRunning = true;

        while (exampleRunning) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glLoadIdentity();

            float delta = getDelta();

            input(delta);
            updateCamera();
            renderVertexArrayOfTrianglesWithDrawArrays(
                    cubeWithTrianglesColors, cubeWithTrianglesVertices, -2.0f, 0.0f, 0.0f);
            renderVertexArrayOfQuadsWithDrawArrays(cubeWithQuadsColors, cubeWithQuadsVertices, 2.0f, 0.0f, 0.0f);
            renderVertexArrayOfQuadsWithDrawArrays(
                    cubeWithQuadsAndDifferentColorsPerVertexColors, cubeWithQuadsAndDifferentColorsPerVertexVertices,
                    0.0f, 2.0f, 0.0f);
            updateFPS();

            Display.update();
            Display.sync(3000);
        }

        Mouse.setGrabbed(false);
        Display.destroy();
    }

    private void createVertexArrayCubeWithTriangles(FloatBuffer colorFloatBuffer, FloatBuffer vertexFloatBuffer) {
        // Front
        Vector4f frontColor1 = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f);

        frontColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        frontColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        frontColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        Vector4f frontColor2 = new Vector4f(0.5f, 0.0f, 0.0f, 1.0f);

        frontColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        frontColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        frontColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        // Back
        Vector4f backColor1 = new Vector4f(0.0f, 1.0f, 1.0f, 1.0f);

        backColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        backColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        backColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);

        Vector4f backColor2 = new Vector4f(0.0f, 0.5f, 0.5f, 1.0f);

        backColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        backColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        backColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);

        // Left
        Vector4f leftColor1 = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f);

        leftColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        leftColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        leftColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);

        Vector4f leftColor2 = new Vector4f(0.0f, 0.5f, 0.0f, 1.0f);

        leftColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        leftColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        leftColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        // Right
        Vector4f rightColor1 = new Vector4f(1.0f, 0.0f, 1.0f, 1.0f);

        rightColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        rightColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        rightColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        Vector4f rightColor2 = new Vector4f(0.5f, 0.0f, 0.5f, 1.0f);

        rightColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        rightColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        rightColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);

        // Top
        Vector4f topColor1 = new Vector4f(0.0f, 0.0f, 1.0f, 1.0f);

        topColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        topColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        topColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        Vector4f topColor2 = new Vector4f(0.0f, 0.0f, 0.5f, 1.0f);

        topColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        topColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        topColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        // Bottom
        Vector4f bottomColor1 = new Vector4f(1.0f, 1.0f, 0.0f, 1.0f);

        bottomColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        bottomColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        bottomColor1.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        Vector4f bottomColor2 = new Vector4f(0.5f, 0.5f, 0.0f, 1.0f);

        bottomColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        bottomColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        bottomColor2.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        colorFloatBuffer.flip();
        vertexFloatBuffer.flip();
    }

    private void createVertexArrayCubeWithQuads(FloatBuffer colorFloatBuffer, FloatBuffer vertexFloatBuffer) {
        // Front
        Vector4f frontColor = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f);

        frontColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        frontColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        frontColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        frontColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        // Back
        Vector4f backColor = new Vector4f(0.0f, 1.0f, 1.0f, 1.0f);

        backColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        backColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        backColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        backColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);

        // Left
        Vector4f leftColor = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f);

        leftColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        leftColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        leftColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        leftColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        // Right
        Vector4f rightColor = new Vector4f(1.0f, 0.0f, 1.0f, 1.0f);

        rightColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        rightColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        rightColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        rightColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);

        // Top
        Vector4f topColor = new Vector4f(0.0f, 0.0f, 1.0f, 1.0f);

        topColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        topColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        topColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        topColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        // Bottom
        Vector4f bottomColor = new Vector4f(1.0f, 1.0f, 0.0f, 1.0f);

        bottomColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        bottomColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        bottomColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        bottomColor.toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        colorFloatBuffer.flip();
        vertexFloatBuffer.flip();
    }

    private void createVertexArrayCubeWithQuadsAndDifferentColorsPerVertex(FloatBuffer colorFloatBuffer, FloatBuffer vertexFloatBuffer) {
        // Front
        new Vector4f(1.0f, 0.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(0.0f, 1.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(0.0f, 1.0f, 1.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(1.0f, 0.0f, 1.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        // Back
        new Vector4f(1.0f, 0.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(0.0f, 1.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(0.0f, 1.0f, 1.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(1.0f, 0.0f, 1.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);

        // Left
        new Vector4f(0.0f, 1.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(1.0f, 0.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(1.0f, 0.0f, 1.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(0.0f, 1.0f, 1.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        // Right
        new Vector4f(0.0f, 1.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(1.0f, 0.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(1.0f, 0.0f, 1.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(0.0f, 1.0f, 1.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);

        // Top
        new Vector4f(0.0f, 1.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(1.0f, 0.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(0.0f, 1.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(1.0f, 0.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        // Bottom
        new Vector4f(1.0f, 0.0f, 1.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(0.0f, 1.0f, 1.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(1.0f, 0.0f, 1.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(0.0f, 1.0f, 1.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        colorFloatBuffer.flip();
        vertexFloatBuffer.flip();
    }

    private float getDelta() {
        long now = getTime();
        float delta = (float) (now - lastTime) / (float) getTimerResolution();
        lastTime = now;
        return delta;
    }

    private void input(float delta) {
        if (Display.isCloseRequested()) {
            exampleRunning = false;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) == true) {
            exampleRunning = false;
        }

        float xPositionDelta = 0.0f;
        float yPositionDelta = 0.0f;
        float zPositionDelta = 0.0f;

        if (Keyboard.isKeyDown(Keyboard.KEY_S) == true) {
            xPositionDelta += Math.sin(camRotation.getY() * PI_OVER_180);
            zPositionDelta += Math.cos(camRotation.getY() * PI_OVER_180);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_W) == true) {
            xPositionDelta -= Math.sin(camRotation.getY() * PI_OVER_180);
            zPositionDelta -= Math.cos(camRotation.getY() * PI_OVER_180);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A) == true) {
            xPositionDelta += Math.sin((camRotation.getY() - 90) * PI_OVER_180);
            zPositionDelta += Math.cos((camRotation.getY() - 90) * PI_OVER_180);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D) == true) {
            xPositionDelta += Math.sin((camRotation.getY() + 90) * PI_OVER_180);
            zPositionDelta += Math.cos((camRotation.getY() + 90) * PI_OVER_180);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) == true) {
            yPositionDelta += 0.5f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) == true) {
            yPositionDelta -= 0.5f;
        }

        float moveSpeed = 9.0f;

        camPostion.setX(camPostion.getX() + xPositionDelta * delta * moveSpeed);
        camPostion.setY(camPostion.getY() + yPositionDelta * delta * moveSpeed);
        camPostion.setZ(camPostion.getZ() + zPositionDelta * delta * moveSpeed);

        float xRotationDelta = 0.0f;
        float yRotationDelta = 0.0f;

        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) == true) {
            xRotationDelta -= 5.0f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_UP) == true) {
            xRotationDelta += 5.0f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) == true) {
            yRotationDelta -= 5.0f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) == true) {
            yRotationDelta += 5.0f;
        }

        xRotationDelta += Mouse.getDY();
        yRotationDelta += Mouse.getDX();

        float lookSpeed = 10.0f;

        camRotation.setX(camRotation.getX() - xRotationDelta * MOUSE_POLL * lookSpeed);
        camRotation.setY(camRotation.getY() - yRotationDelta * MOUSE_POLL * lookSpeed);

        correctHeadingPitchAndRoll();
    }

    public void correctHeadingPitchAndRoll() {
        if (camRotation.getX() > 90.0f) {
            camRotation.setX(90.0f);
        } else if (camRotation.getX() < -90.0f) {
            camRotation.setX(-90.0f);
        }

        if ((camRotation.getY() >= 360.0f) || (camRotation.getY() <= -360.0f)) {
            camRotation.setY(camRotation.getY() % 360.0f);
        }
        if (camRotation.getY() < 0) {
            camRotation.setY(camRotation.getY() + 360.0f);
        }
    }

    private void updateCamera() {
        GL11.glRotatef(camRotation.getX(), 1.0f, 0, 0);
        GL11.glRotatef(360.0f - camRotation.getY(), 0, 1.0f, 0);
        GL11.glRotatef(camRotation.getZ(), 0, 0, 1.0f);
        GL11.glTranslatef(-camPostion.getX(), camPostion.getY(), -camPostion.getZ());
    }

    private void renderVertexArrayOfQuadsWithDrawArrays(FloatBuffer colorFloatBuffer, FloatBuffer vertexFloatBuffer, float x, float y, float z) {
        GL11.glPushMatrix();

        GL11.glTranslatef(x, y, z);

        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);

        GL11.glColorPointer(4, 0, colorFloatBuffer);
        GL11.glVertexPointer(3, 0, vertexFloatBuffer);

        GL11.glDrawArrays(GL11.GL_QUADS, 0, 24);

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);

        GL11.glPopMatrix();
    }

    private void renderVertexArrayOfTrianglesWithDrawArrays(FloatBuffer colorFloatBuffer, FloatBuffer vertexFloatBuffer, float x, float y, float z) {
        GL11.glPushMatrix();

        GL11.glTranslatef(x, y, z);

        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);

        GL11.glColorPointer(4, 0, colorFloatBuffer);
        GL11.glVertexPointer(3, 0, vertexFloatBuffer);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 36);

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);

        GL11.glPopMatrix();
    }

    private void updateFPS() {
        if (getTime() - lastSecond > getTimerResolution()) {
            Display.setTitle(WINDOW_TITLE + " FPS: " + fps);
            fps = 0;
            lastSecond += getTimerResolution();
        }
        fps++;
    }

}
