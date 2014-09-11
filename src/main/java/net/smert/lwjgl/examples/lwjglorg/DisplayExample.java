package net.smert.lwjgl.examples.lwjglorg;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

/**
 * Example from: http://lwjgl.org/wiki/index.php?title=LWJGL_Basics_1_(The_Display)
 *
 * This example will open a window.
 *
 * @author Jason Sorensen <sorensenj@smert.net>
 */
public class DisplayExample {

    public static void main(String[] argv) {
        DisplayExample displayExample = new DisplayExample();
        displayExample.start();
    }

    public void start() {
        try {
            Display.setDisplayMode(new DisplayMode(800, 600));
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        // init OpenGL here
        while (!Display.isCloseRequested()) {

            // render OpenGL here
            Display.update();
        }

        Display.destroy();
    }

}
