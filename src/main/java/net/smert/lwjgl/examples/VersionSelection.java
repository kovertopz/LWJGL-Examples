package net.smert.lwjgl.examples;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

/**
 * Example from: http://lwjgl.org/wiki/index.php?title=Version_selection
 *
 * This example will create a window with a OpenGL 3.2 context by default and display the OpengGL version in the
 * console.
 *
 * @author Jason Sorensen <sorensenj@smert.net>
 */
public class VersionSelection {

    public static void main(String[] args) {
        new VersionSelection();
    }

    public VersionSelection() {
        boolean select = true;

        if (select) {
            this.specifiedCreate();
        } else {
            this.normalCreate();
        }

        System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));

        while (!Display.isCloseRequested()) {
            Display.sync(60);
            Display.update();
        }

        Display.destroy();
    }

    // Let Display manage our OpenGL version
    private void normalCreate() {
        try {
            Display.setDisplayMode(new DisplayMode(320, 240));
            Display.setTitle("Version selection");
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    // Use a specified version of OpenGL - namely version 3.2
    private void specifiedCreate() {
        PixelFormat pixelFormat = new PixelFormat();
        ContextAttribs contextAtrributes = new ContextAttribs(3, 2)
                .withForwardCompatible(true)
                .withProfileCore(true);

        try {
            Display.setDisplayMode(new DisplayMode(320, 240));
            Display.setTitle("Version selection");
            Display.create(pixelFormat, contextAtrributes);
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
