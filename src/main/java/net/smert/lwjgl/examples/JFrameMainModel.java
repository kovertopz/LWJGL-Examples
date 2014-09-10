package net.smert.lwjgl.examples;

import com.jdotsoft.jarloader.JarClassLoader;
import java.io.File;
import java.io.FilenameFilter;
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

    private boolean checkMainMethodExists(String filenameWithoutExtension) {
        boolean result = false;

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
            if (validModifiers && validVoid) {
                result = true;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException | SecurityException e) {
            // Do nothing since not all classes have main methods.
        }

        return result;
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
        List<String> mainClasses = new ArrayList();
        URL classLocation = JFrameMainModel.class.getResource(JFrameMainModel.class.getSimpleName() + ".class");
        String protocol = classLocation.getProtocol();

        switch (protocol) {
            case "file":
                getMainClassesFromFile(mainClasses, classLocation.getPath());
                break;

            case "jar":
                getMainClassesFromJar(mainClasses);
                break;

            default:
                throw new RuntimeException("Unknown protocol for current class: " + protocol);
        }

        return mainClasses;
    }

    private void getMainClassesFromFile(List<String> mainClasses, String pathThisClass) {
        File fileThisClass = new File(pathThisClass);
        String directory = fileThisClass.getParent();

        File dir = new File(directory);

        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".class") && !name.contains("$") && !name.startsWith("Main");
            }
        });

        for (File javaClass : files) {
            String filenameWithoutExtension = javaClass.getName().replace(".class", "");

            if (checkMainMethodExists(filenameWithoutExtension) == true) {
                mainClasses.add(PACKAGE_PREFIX + filenameWithoutExtension);
            }
        }
    }

    private void getMainClassesFromJar(List<String> mainClasses) {
        CodeSource src = Main.class.getProtectionDomain().getCodeSource();

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

                    if (fullpath.startsWith(DIRECTORY_PREFIX) && fullpath.endsWith(".class")) {
                        Path path = Paths.get(fullpath);
                        String filename = path.getFileName().toString();

                        if (filename.contains("$")) {
                            continue;
                        }

                        if (filename.equals(Main.class.getSimpleName() + ".class")) {
                            continue;
                        }

                        String filenameWithoutExtension = getFileNameWithoutExtension(filename);

                        if (checkMainMethodExists(filenameWithoutExtension) == true) {
                            mainClasses.add(PACKAGE_PREFIX + filenameWithoutExtension);
                        }
                    }
                }
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }
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
