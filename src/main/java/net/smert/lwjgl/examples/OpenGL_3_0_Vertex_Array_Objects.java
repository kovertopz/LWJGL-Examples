package net.smert.lwjgl.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.opengl.Util;

/**
 *
 * @author Jason Sorensen <sorensenj@smert.net>
 */
public class OpenGL_3_0_Vertex_Array_Objects {

    private final static float MOUSE_POLL = 1.0f / 125.0f;
    private final static String WINDOW_TITLE = "OpenGL 3.0 Vertex Array Objects";

    private boolean exampleRunning;
    private boolean fixRoll = false;
    private int fps;
    private int fsId = 0;
    private int pId = 0;
    private int uniformModel;
    private int uniformProjecton;
    private int uniformView;
    private int vaoCubeWithTriangles;
    private int vaoCubeWithTriangles2;
    private int vaoCubeWithQuadsAndDifferentColorsPerVertex;
    private int vboCubeWithTrianglesColors;
    private int vboCubeWithTrianglesVertices;
    private int vboCubeWithTriangles2Colors;
    private int vboCubeWithTriangles2Vertices;
    private int vboCubeWithQuadsAndDifferentColorsPerVertexColors;
    private int vboCubeWithQuadsAndDifferentColorsPerVertexVertices;
    private int vsId = 0;
    private long lastSecond;
    private long lastTime;

    private Camera camera;
    private FloatBuffer matrix4FloatBuffer;
    private Transform4f cubeWithTrianglesAndDifferentColorsPerVertexVerticesTransform;
    private Transform4f cubeWithTriangles2VerticesTransform;
    private Transform4f cubeWithTrianglesVerticesTransform;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        OpenGL_3_0_Vertex_Array_Objects example = new OpenGL_3_0_Vertex_Array_Objects();
        example.start();
    }

    public void start() {
        createWindow();
        initCamera();
        initGL();
        initShader();

        System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));

        initTimer();
        mainLoop();
    }

    private void createWindow() {
        PixelFormat pixelFormat = new PixelFormat()
                .withDepthBits(24)
                .withStencilBits(8);
        ContextAttribs contextAtrributes = new ContextAttribs(3, 3)
                .withProfileCore(true);

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
        camera = new Camera();

        camera.setPerspectiveProjection(
                70.0f, // FOV
                (float) Display.getWidth() / (float) Display.getHeight(), // Aspect ratio
                0.05f, // Near
                256.0f); // Far

        camera.lookAt(new Vector3f(0.0f, 0.0f, 5.0f), new Vector3f(0.0f, 0.0f, 0.0f), Vector3f.WORLD_Y_AXIS);
    }

    private void initGL() {
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glDepthMask(true);
        GL11.glClearDepth(1.0);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    private void initShader() {
        vsId = this.loadShader("opengl_3_3.vsh", GL20.GL_VERTEX_SHADER);
        fsId = this.loadShader("opengl_3_3.fsh", GL20.GL_FRAGMENT_SHADER);

        pId = GL20.glCreateProgram();
        GL20.glAttachShader(pId, vsId);
        GL20.glAttachShader(pId, fsId);

        GL20.glBindAttribLocation(pId, 0, "in_Color");
        GL20.glBindAttribLocation(pId, 1, "in_Position");

        GL20.glLinkProgram(pId);

        if (GL20.glGetProgrami(pId, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            System.err.println(GL20.glGetShaderInfoLog(pId, 8192));
            throw new RuntimeException("Shader had linking errors!");
        }

        GL20.glValidateProgram(pId);

        if (GL20.glGetProgrami(pId, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
            System.err.println(GL20.glGetShaderInfoLog(pId, 8192));
            throw new RuntimeException("Shader had validate errors!");
        }

        uniformModel = GL20.glGetUniformLocation(pId, "uModel");
        uniformProjecton = GL20.glGetUniformLocation(pId, "uProjection");
        uniformView = GL20.glGetUniformLocation(pId, "uView");
    }

    public int loadShader(String filename, int type) {
        StringBuilder shaderSource = new StringBuilder();

        try {
            InputStream is = this.getClass().getResourceAsStream(filename);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
            br.close();
        } catch (IOException | NullPointerException e) {
            System.err.println("Could not read file: " + filename);
            e.printStackTrace();
            System.exit(-1);
        }

        int shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);

        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.err.println(GL20.glGetShaderInfoLog(shaderID, 8192));
            throw new RuntimeException("Shader \"" + filename + "\" had compile errors!");
        }

        return shaderID;
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
        cubeWithTrianglesVerticesTransform = new Transform4f();
        cubeWithTrianglesVerticesTransform.setPosition(-2.0f, 0.0f, 0.0f);
        cubeWithTriangles2VerticesTransform = new Transform4f();
        cubeWithTriangles2VerticesTransform.setPosition(2.0f, 0.0f, 0.0f);
        cubeWithTrianglesAndDifferentColorsPerVertexVerticesTransform = new Transform4f();
        cubeWithTrianglesAndDifferentColorsPerVertexVerticesTransform.setPosition(0.0f, 2.0f, 0.0f);

        int[] vaoResult = new int[3];
        createVAOCubeWithTriangles(vaoResult);
        vaoCubeWithTriangles = vaoResult[0];
        vboCubeWithTrianglesColors = vaoResult[1];
        vboCubeWithTrianglesVertices = vaoResult[2];
        createVAOCubeWithTriangles(vaoResult);
        vaoCubeWithTriangles2 = vaoResult[0];
        vboCubeWithTriangles2Colors = vaoResult[1];
        vboCubeWithTriangles2Vertices = vaoResult[2];
        createVAOCubeWithTrianglesAndDifferentColorsPerVertex(vaoResult);
        vaoCubeWithQuadsAndDifferentColorsPerVertex = vaoResult[0];
        vboCubeWithQuadsAndDifferentColorsPerVertexColors = vaoResult[1];
        vboCubeWithQuadsAndDifferentColorsPerVertexVertices = vaoResult[2];

        Mouse.setGrabbed(true);
        exampleRunning = true;
        matrix4FloatBuffer = BufferUtils.createFloatBuffer(16);

        while (exampleRunning) {
            float delta = getDelta();

            input(delta);
            camera.updateViewMatrix();
            render();
            updateFPS();

            Util.checkGLError();

            Display.update();
            Display.sync(3000);
        }

        Mouse.setGrabbed(false);
        Display.destroy();
    }

    private void createVAOCubeWithTriangles(int[] result) {
        FloatBuffer colorFloatBuffer = BufferUtils.createFloatBuffer(192);
        FloatBuffer vertexFloatBuffer = BufferUtils.createFloatBuffer(192);

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

        createVAO(result, colorFloatBuffer, vertexFloatBuffer);
    }

    private void createVAOCubeWithTrianglesAndDifferentColorsPerVertex(int[] result) {
        FloatBuffer colorFloatBuffer = BufferUtils.createFloatBuffer(192);
        FloatBuffer vertexFloatBuffer = BufferUtils.createFloatBuffer(192);

        // Front
        new Vector4f(1.0f, 0.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(0.0f, 1.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(0.0f, 1.0f, 1.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        new Vector4f(1.0f, 0.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
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

        new Vector4f(1.0f, 0.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
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

        new Vector4f(0.0f, 1.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, 0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
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

        new Vector4f(0.0f, 1.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
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

        new Vector4f(0.0f, 1.0f, 0.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, 0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
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

        new Vector4f(1.0f, 0.0f, 1.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, -0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(1.0f, 0.0f, 1.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);
        new Vector4f(0.0f, 1.0f, 1.0f, 1.0f).toFloatBuffer(colorFloatBuffer);
        new Vector3f(-0.5f, -0.5f, 0.5f).toFloatBuffer(vertexFloatBuffer);

        colorFloatBuffer.flip();
        vertexFloatBuffer.flip();

        createVAO(result, colorFloatBuffer, vertexFloatBuffer);
    }

    public void createVAO(int[] result, FloatBuffer colorFloatBuffer, FloatBuffer vertexFloatBuffer) {
        result[0] = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(result[0]);

        result[1] = GL15.glGenBuffers();
        result[2] = GL15.glGenBuffers();

        setBufferData(result[1], colorFloatBuffer, 0, 4);
        setBufferData(result[2], vertexFloatBuffer, 1, 3);

        GL30.glBindVertexArray(0);
    }

    public void setBufferData(int vboid, FloatBuffer floatBuffer, int index, int size) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboid);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, floatBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(index, size, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
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

        Vector3f positionDelta = new Vector3f();

        if (Keyboard.isKeyDown(Keyboard.KEY_S) == true) {
            positionDelta.setZ(-1.0f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_W) == true) {
            positionDelta.setZ(1.0f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A) == true) {
            positionDelta.setX(-1.0f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D) == true) {
            positionDelta.setX(1.0f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) == true) {
            positionDelta.setY(-1.0f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) == true) {
            positionDelta.setY(1.0f);
        }

        if (positionDelta.magnitudeSquared() > 0) {
            float moveSpeed = 9.0f;

            positionDelta.normalize();
            positionDelta.multiply(delta * moveSpeed);

            camera.moveForward(positionDelta.getX(), 0, positionDelta.getZ());
            camera.move(0, positionDelta.getY(), 0);
        }

        Vector3f rotationDelta = new Vector3f();

        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) == true) {
            rotationDelta.addX(-1.0f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_UP) == true) {
            rotationDelta.addX(1.0f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) == true) {
            rotationDelta.addY(1.0f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) == true) {
            rotationDelta.addY(-1.0f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_Q) == true) {
            rotationDelta.addZ(1.0f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_E) == true) {
            rotationDelta.addZ(-1.0f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_C) == true) {
            fixRoll = true;
        }

        if (fixRoll) {
            float roll = camera.getRoll();

            if ((roll >= -0.05f) && (roll <= 0.05f)) {
                fixRoll = false;
            } else if (roll > 0.0f) {
                rotationDelta.addZ(-1.0f);
            } else {
                rotationDelta.addZ(1.0f);
            }
        }

        rotationDelta.addX(Mouse.getDY());
        rotationDelta.addY(-Mouse.getDX());

        if (rotationDelta.magnitudeSquared() > 0) {
            float lookSpeed = 10.0f;

            rotationDelta.multiply(MOUSE_POLL * lookSpeed);

            camera.rotate(rotationDelta.getX(), rotationDelta.getY(), rotationDelta.getZ());
        }
    }

    private void render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL20.glUseProgram(pId);

        camera.getProjectionMatrix().toFloatBuffer(matrix4FloatBuffer);
        matrix4FloatBuffer.flip();
        GL20.glUniformMatrix4(uniformProjecton, false, matrix4FloatBuffer);

        camera.getViewMatrix().toFloatBuffer(matrix4FloatBuffer);
        matrix4FloatBuffer.flip();
        GL20.glUniformMatrix4(uniformView, false, matrix4FloatBuffer);

        renderVBOOfTrianglesWithDrawArrays(vaoCubeWithTriangles, cubeWithTrianglesVerticesTransform);
        renderVBOOfTrianglesWithDrawArrays(vaoCubeWithTriangles2, cubeWithTriangles2VerticesTransform);
        renderVBOOfTrianglesWithDrawArrays(
                vaoCubeWithQuadsAndDifferentColorsPerVertex, cubeWithTrianglesAndDifferentColorsPerVertexVerticesTransform);

        GL20.glUseProgram(0);
    }

    private void renderVBOOfTrianglesWithDrawArrays(int vaoID, Transform4f transform) {
        transform.toFloatBuffer(matrix4FloatBuffer);
        matrix4FloatBuffer.flip();
        GL20.glUniformMatrix4(uniformModel, false, matrix4FloatBuffer);

        GL30.glBindVertexArray(vaoID);

        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 36);

        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(0);

        GL30.glBindVertexArray(0);
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
