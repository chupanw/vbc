package model.java.net;

import edu.cmu.cs.vbc.VBCClassLoader;

import java.io.InputStream;
import java.net.URL;
import java.net.URLStreamHandlerFactory;

/**
 * Our own URLClassLoader so that classes loaded from URLs can be lifted
 *
 * @// TODO: 11/13/17 for now we only implement just enough so that Jetty works
 * @author chupanw
 */
public class URLClassLoader extends java.net.URLClassLoader {

    public URLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public URLClassLoader(URL[] urls) {
        super(urls);
    }

    public URLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        InputStream in = getResourceAsStream(name.replace('.', '/') + ".class");
        VBCClassLoader vbcClassLoader = getVBCClassLoader(this.getParent());
        if (in != null) {
            System.out.println("Lifting " + name + " with our own URLClassLoader");
            return vbcClassLoader.liftClass(name, vbcClassLoader.loader().loadClass(in));
        }
        else
            return super.findClass(name);
    }

    private VBCClassLoader getVBCClassLoader(ClassLoader currentLoader) {
        if (currentLoader instanceof VBCClassLoader) {
            return (VBCClassLoader) currentLoader;
        } else if (currentLoader.getParent() != null) {
            return getVBCClassLoader(currentLoader.getParent());
        } else {
            throw new RuntimeException("Could not find any VBCClassLoader in current class loading hierarchy");
        }
    }
}
