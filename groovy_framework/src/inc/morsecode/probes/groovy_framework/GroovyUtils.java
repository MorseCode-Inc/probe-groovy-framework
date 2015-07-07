package inc.morsecode.probes.groovy_framework;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.codehaus.groovy.ant.Groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyObject;


public class GroovyUtils {

	private static GroovyClassLoader loader= new GroovyClassLoader(); 
	
	public GroovyUtils() {
		
	}
	
	public static GroovyObject loadResource(String path) throws IOException, InstantiationException, IllegalAccessException {
		return loadResource(GroovyUtils.loader, path);
	}
	
	public static GroovyObject loadResource(GroovyClassLoader loader, String path) throws IOException, InstantiationException, IllegalAccessException {
		URL url= loader.getResource(path);
		
		GroovyCodeSource code= new GroovyCodeSource(url);
		
		return load(loader, code);
		
	}
	

	public static GroovyObject getInstance(File file) throws IOException, InstantiationException, IllegalAccessException {
		if (!file.exists() || !file.canRead()) {
			throw new FileNotFoundException(file +" does not exist or permission denied for read.");
		}
		
		GroovyCodeSource code= new GroovyCodeSource(file);
		
		return load(loader, code);
	}

	public static GroovyObject load(GroovyCodeSource code) throws InstantiationException, IllegalAccessException {
		return load(GroovyUtils.loader, code);
	}
	
	public static GroovyObject load(GroovyClassLoader loader, GroovyCodeSource code) throws InstantiationException, IllegalAccessException {
		Class groovy= loader.parseClass(code);
		
		GroovyObject object= (GroovyObject) groovy.newInstance();
		return object;
	}
	
	

}
