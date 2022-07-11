package net.hironico.common.utils;

import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.lang.reflect.Method;

public class DynamicFileLoader {
	 
	// The methods addFile and associated final Class[] parameters were gratefully copied from
	// anthony_miguel @ http://forum.java.sun.com/thread.jsp?forum=32&thread=300557&tstart=0&trange=15
	private static final Class<?>[] parameters = new Class<?>[]{URL.class};
 
	public static void addFile(String s) throws IOException {
		File f = new File(s);
		addFile(f);
	}//end method
 
	public static void addFile(File f) throws IOException {
		addURL(f.toURI().toURL());
	}//end method
 
	public static void addURL(URL u) throws IOException {
		URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
		Class<URLClassLoader> sysclass = URLClassLoader.class;
		try {
			Method method = sysclass.getDeclaredMethod("addURL",parameters);
			method.setAccessible(true);
			method.invoke(sysloader,new Object[]{ u });
		} catch (Throwable t) {
			t.printStackTrace();
			throw new IOException("Error, could not add URL to system classloader");
		}//end try catch
    }//end method
}