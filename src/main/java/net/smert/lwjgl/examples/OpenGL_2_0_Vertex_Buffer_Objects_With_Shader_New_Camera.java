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
import org.lwjgl.opengl.PixelFormat;

/**
 *
 * @author Jason Sorensen <sorensenj@smert.net>
 */
public class OpenGL_2_0_Vertex_Buffer_Objects_With_Shader_New_Camera {

    private final static float MOUSE_POLL = 1.0f / 125.0f;
    private final static String WINDOW_TITLE = "OpenGL 2.0 Vertex Buffer Objects With Shader New Camera";

    private boolean exampleRunning;
    private boolean fixRoll = false;
    private int fps;
    private int fsId = 0;
    private int pId = 0;
    private int uniformModel;
    private int uniformProjecton;
    private int uniformView;
    private int vboCubeWithQuadsAndDifferentColorsPerVertexColors;
    private int vboCubeWithQuadsAndDifferentColorsPerVertexVertices;
    private int vboCubeWithQuadsColors;
    private int vboCubeWithQuadsVertices;
    private int vboCubeWithTrianglesColors;
    private int vboCubeWithTrianglesVertices;
    private int vsId = 0;
    private long lastSecond;
    private long lastTime;

    private Camera camera;
    private FloatBuffer matrix4FloatBuffer;
    private Transform4f cubeWithQuadsAndDifferentColorsPerVertexVerticesTransform;
    private Transform4f cubeWithQuadsVerticesTransform;
    private Transform4f cubeWithTrianglesVerticesTransform;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        OpenGL_2_0_Vertex_Buffer_Objects_With_Shader_New_Camera example = new OpenGL_2_0_Vertex_Buffer_Objects_With_Shader_New_Camera();
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
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        GL11.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    private void initShader() {
        vsId = this.loadShader("opengl_2_0_cam.vsh", GL20.GL_VERTEX_SHADER);
        fsId = this.loadShader("opengl_2_0_cam.fsh", GL20.GL_FRAGMENT_SHADER);

        pId = GL20.glCreateProgram();
        GL20.glAttachShader(pId, vsId);
        GL20.glAttachShader(pId, fsId);

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
        matrix4FloatBuffer = BufferUtils.createFloatBuffer(16);
        createVBOCubeWithQuads();
        createVBOCubeWithQuadsAndDifferentColorsPerVertex();
        createVBOCubeWithTriangles();
        Mouse.setGrabbed(true);
        exampleRunning = true;

        while (exampleRunning) {
            float delta = getDelta();

            input(delta);
            camera.updateViewMatrix();
            render();
            updateFPS();

            Display.update();
            Display.sync(3000);
        }

        Display.destroy();
    }

    private void createVBOCubeWithTriangles() {
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

        vboCubeWithTrianglesColors = GL15.glGenBuffers();
        vboCubeWithTrianglesVertices = GL15.glGenBuffers();

        setBufferData(vboCubeWithTrianglesColors, colorFloatBuffer);
        setBufferData(vboCubeWithTrianglesVertices, vertexFloatBuffer);

        cubeWithTrianglesVerticesTransform = new Transform4f();
        cubeWithTrianglesVerticesTransform.setPosition(-2.0f, 0.0f, 0.0f);
    }

    private void createVBOCubeWithQuads() {
        FloatBuffer colorFloatBuffer = BufferUtils.createFloatBuffer(192);
        FloatBuffer vertexFloatBuffer = BufferUtils.createFloatBuffer(192);

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

        vboCubeWithQuadsColors = GL15.glGenBuffers();
        vboCubeWithQuadsVertices = GL15.glGenBuffers();

        setBufferData(vboCubeWithQuadsColors, colorFloatBuffer);
        setBufferData(vboCubeWithQuadsVertices, vertexFloatBuffer);

        cubeWithQuadsVerticesTransform = new Transform4f();
        cubeWithQuadsVerticesTransform.setPosition(2.0f, 0.0f, 0.0f);
    }

    private void createVBOCubeWithQuadsAndDifferentColorsPerVertex() {
        FloatBuffer colorFloatBuffer = BufferUtils.createFloatBuffer(192);
        FloatBuffer vertexFloatBuffer = BufferUtils.createFloatBuffer(192);

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

        vboCubeWithQuadsAndDifferentColorsPerVertexColors = GL15.glGenBuffers();
        vboCubeWithQuadsAndDifferentColorsPerVertexVertices = GL15.glGenBuffers();

        setBufferData(vboCubeWithQuadsAndDifferentColorsPerVertexColors, colorFloatBuffer);
        setBufferData(vboCubeWithQuadsAndDifferentColorsPerVertexVertices, vertexFloatBuffer);

        cubeWithQuadsAndDifferentColorsPerVertexVerticesTransform = new Transform4f();
        cubeWithQuadsAndDifferentColorsPerVertexVerticesTransform.setPosition(0.0f, 2.0f, 0.0f);
    }

    public void setBufferData(int vboid, FloatBuffer floatBuffer) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboid);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, floatBuffer, GL15.GL_STATIC_DRAW);
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
        GL11.glLoadIdentity();

        GL20.glUseProgram(pId);

        camera.getProjectionMatrix().toFloatBuffer(matrix4FloatBuffer);
        matrix4FloatBuffer.flip();
        GL20.glUniformMatrix4(uniformProjecton, false, matrix4FloatBuffer);

        camera.getViewMatrix().toFloatBuffer(matrix4FloatBuffer);
        matrix4FloatBuffer.flip();
        GL20.glUniformMatrix4(uniformView, false, matrix4FloatBuffer);

        renderVBOOfTrianglesWithDrawArrays(
                vboCubeWithTrianglesColors, vboCubeWithTrianglesVertices, cubeWithTrianglesVerticesTransform);
        renderVBOOfQuadsWithDrawArrays(vboCubeWithQuadsColors, vboCubeWithQuadsVertices, cubeWithQuadsVerticesTransform);
        renderVBOOfQuadsWithDrawArrays(
                vboCubeWithQuadsAndDifferentColorsPerVertexColors, vboCubeWithQuadsAndDifferentColorsPerVertexVertices,
                cubeWithQuadsAndDifferentColorsPerVertexVerticesTransform);

        GL20.glUseProgram(0);
    }

    private void renderVBOOfQuadsWithDrawArrays(int vboColor, int vboVertex, Transform4f transform) {
        transform.toFloatBuffer(matrix4FloatBuffer);
        matrix4FloatBuffer.flip();
        GL20.glUniformMatrix4(uniformModel, false, matrix4FloatBuffer);

        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColor);
        GL11.glColorPointer(4, GL11.GL_FLOAT, 0, 0);

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertex);
        GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);

        GL11.glDrawArrays(GL11.GL_QUADS, 0, 24);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
    }

    private void renderVBOOfTrianglesWithDrawArrays(int vboColor, int vboVertex, Transform4f transform) {
        transform.toFloatBuffer(matrix4FloatBuffer);
        matrix4FloatBuffer.flip();
        GL20.glUniformMatrix4(uniformModel, false, matrix4FloatBuffer);

        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColor);
        GL11.glColorPointer(4, GL11.GL_FLOAT, 0, 0);

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertex);
        GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 36);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
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
