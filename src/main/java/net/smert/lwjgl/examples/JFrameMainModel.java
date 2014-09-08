package net.smert.lwjgl.examples;

import com.jdotsoft.jarloader.JarClassLoader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;

/**
 *
 * @author Jason Sorensen <sorensenj@smert.net>
 */
public class JFrameMainModel {

    private final static String DIRECTORY_PREFIX = "net/smert/lwjgl/examples/";
    private final static String PACKAGE_PREFIX = "net.smert.lwjgl.examples.";

    private final DefaultListModel listModelMainClasses;
    private final JarClassLoader jarClassLoader;

    public JFrameMainModel() {
        jarClassLoader = new JarClassLoader();
        listModelMainClasses = new DefaultListModel();
    }

    private String getFileExtension(String filename) {
        int index = filename.lastIndexOf('.');
        String extension = "";

        if (index > 0) {
            extension = filename.substring(index + 1);
        }

        return extension;
    }

    private String getFileNameWithoutExtension(String filename) {
        int index = filename.lastIndexOf('.');
        String name = "";

        if (index > 0) {
            name = filename.substring(0, index);
        }

        return name;
    }

    private List<String> getMainClasses() {
        CodeSource src = Main.class.getProtectionDomain().getCodeSource();
        List<String> mainClasses = new ArrayList();

        if (src != null) {
            URL jar = src.getLocation();
            ZipInputStream zip;

            try {
                zip = new ZipInputStream(jar.openStream());

                while (true) {
                    ZipEntry e = zip.getNextEntry();

                    if (e == null) {
                        break;
                    }

                    String fullpath = e.getName();

                    if (fullpath.startsWith(DIRECTORY_PREFIX)) {
                        String extension = getFileExtension(fullpath);

                        if (!extension.startsWith("class")) {
                            continue;
                        }

                        Path path = Paths.get(fullpath);
                        String filename = path.getFileName().toString();

                        if (filename.contains("$")) {
                            continue;
                        }

                        String filenameWithoutExtension = getFileNameWithoutExtension(filename);

                        if (filenameWithoutExtension.equals(Main.class.getSimpleName())) {
                            continue;
                        }

                        try {
                            Class clazz = jarClassLoader.loadClass(PACKAGE_PREFIX + filenameWithoutExtension);
                            Method method = clazz.getMethod("main", new Class[]{String[].class});

                            boolean validModifiers = false;
                            boolean validVoid = false;

                            if (method != null) {
                                method.setAccessible(true);
                                int nModifiers = method.getModifiers();
                                validModifiers = Modifier.isPublic(nModifiers) && Modifier.isStatic(nModifiers);
                                Class<?> clazzRet = method.getReturnType();
                                validVoid = (clazzRet == void.class);
                            }
                            if (method == null || !validModifiers || !validVoid) {
                                continue;
                            }

                            mainClasses.add(PACKAGE_PREFIX + filenameWithoutExtension);
                        } catch (ClassNotFoundException cnfe) {
                            cnfe.printStackTrace();
                        } catch (NoSuchMethodException | SecurityException ex) {
                            // Do nothing since not all classes have main methods.
                        }
                    }
                }
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }

        return mainClasses;
    }

    public AbstractListModel populateListModel() {
        List<String> mainClasses = getMainClasses();

        for (String clazz : mainClasses) {
            listModelMainClasses.addElement(clazz);
        }

        return listModelMainClasses;
    }

    public void runDemo(String mainClass, String[] args) {
        try {
            jarClassLoader.invokeMain(mainClass, args);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
