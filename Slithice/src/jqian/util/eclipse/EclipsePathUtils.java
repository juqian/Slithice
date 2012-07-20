package jqian.util.eclipse;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.jdt.core.*;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.*;


/**
 * @note It may not work for classes in a jar file. 
 * 		 Use Class.getResource() or ClassLoader.getResource() instead. 
 * 
 */	
public class EclipsePathUtils {
	public static String getProjectPath(IJavaProject project){
		String path = null;
		try{
		    path =  project.getCorrespondingResource().getLocation().toString();
		}
		catch(JavaModelException e){			
		}
		
		return path;
	}
	
	public static String getClassPath(IJavaProject project){	
		String classpath="";
		
		try{	
			String prjPath = project.getCorrespondingResource().getLocation().toString();
			String prjName = project.getElementName();
			String bin = project.getOutputLocation().makeRelative().toString();
			bin = bin.substring(prjName.length());
			bin = prjPath+bin;
			
			boolean containsSrc = false;
			IClasspathEntry[] entries = project.getResolvedClasspath(true);	
			int size = entries.length;			
			for (int i = 0; i <size; i++) {
				IClasspathEntry e = entries[i];
				int kind = e.getContentKind();
				if (kind == IPackageFragmentRoot.K_SOURCE){
					containsSrc = true;
				}
				else if(kind == IPackageFragmentRoot.K_BINARY){
					if(!classpath.equals(""))
						classpath += ";";
					
					classpath += e.getPath().toString();					
				}
			}
			
			//add output path
			if(containsSrc){
				if (!classpath.equals(""))
					classpath += ";";

				classpath += bin;
			}
		}
		catch(JavaModelException e){}
		
		return classpath;
	}
	
	public static String getEclipseInstallationPath(){
		String path = "";
		try{
			path = Platform.getInstallLocation().getURL().getFile().toString();	
			File file = new File(path);
			path = file.getCanonicalPath();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return path;
	}
	
	public static String getFilePath(IFile file){
		//String path = ((IFile)o).getLocation().makeAbsolute().toFile().getAbsolutePath();
		return file.getLocation().toString();
	}
	
	public static String getEditorFilePath(IEditorPart editor){
		IEditorInput input = editor.getEditorInput();
		if(input instanceof IFileEditorInput){
			IFile file = ((IFileEditorInput)input).getFile();
			return getFilePath(file);
		}
		else{
			return null;
		}
	}
	
	public static String getWorkspacePath(){
		//ResourcesPlugin.getWorkspace(); 
		String path = "";
		try{
			//Platform.getLocation();
			path = Platform.getInstanceLocation().getURL().getFile().toString();	
			File file = new File(path);
			path = file.getCanonicalPath();
		}catch(Exception e){
			e.printStackTrace();
		}
		return path;
	}
	
	public static String getPluginPath(Class<?> pluginClass)
	{
		//AbstractUIPlugin.getDefault().getStateLocation().makeAbsolute().toFile().getAbsolutePath()
		//Activator.getDefault().getBundle();
		Plugin plugin = null;
		try{
			Method getDefault = pluginClass.getMethod("getDefault");
			plugin = (Plugin)getDefault.invoke(null);
		}
		catch(Exception e){
			return null;
		}
		
		Bundle bundle = plugin.getBundle();
		return getPluginPath(bundle); 
	}
		
	public static String getPluginPath(String pluginId){ 
		Bundle bundle = Platform.getBundle(pluginId);		
		if( bundle == null ){
			throw new RuntimeException("Could not resolve plugin:" + pluginId + "\r\n");
		}
		
		return getPluginPath(bundle);
	}

	public static String getPluginPath(Bundle pluginBundle) {
		String pluginName = pluginBundle.getSymbolicName();

		/* resolve Bundle::getEntry to local URL */
		URL pluginURL = null;
		try {
			pluginURL = FileLocator.resolve(pluginBundle.getEntry("/"));
		} 
		catch (IOException e) {
			throw new RuntimeException("Could not get installation directory of plugin: " + pluginName);
		}

		String pluginInstallDir = pluginURL.getPath().trim();
		if (pluginInstallDir.length() == 0)
			throw new RuntimeException("Could not get installation directory of plugin: " + pluginName);

		// since path returned by URL::getPath starts with a forward slash, that
		// is not suitable to run command lines on Windows-OS, but for Unix-based
		// OSes it is needed. So strip one character for windows. There seems to
		// be no other clean way of doing this.
		if (Platform.getOS().compareTo(Platform.OS_WIN32) == 0)
			pluginInstallDir = pluginInstallDir.substring(1);

		int length = pluginInstallDir.length();
		char tail = pluginInstallDir.charAt(length - 1);
		if (tail == File.separatorChar || tail == '/' || tail == '\\') {
			pluginInstallDir = pluginInstallDir.substring(0, length - 1);
		}
		return pluginInstallDir;
	}
}
