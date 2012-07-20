package jqian.slicer.util;

//import jqian.util.Path;
import jqian.slicer.plugin.*;
import java.io.*;
import java.net.URL;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.*;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.*;


public class PathUtil {
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
				if (!classpath.equals("")){
					classpath = bin + ";" + classpath;
				}
				else{
					classpath = bin;
				}
			}
		}
		catch(JavaModelException e){}
		
		return classpath;
	}
	
	
	public static String getPyxisConfigurationPath(){
		String path = "";
		try{
			path = Platform.getInstallLocation().getURL().getFile().toString();	
			File file = new File(path);
			path = file.getCanonicalPath();
			
			path += File.separator + "configuration";
			path += File.separator + ID.PLUGIN_NAME;			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return path;
	}
	
	public static String getPluginPath()
	{
		/*String path = "";
		try{
			path = Path.getFullPathRelateClass("../../../../",PathUtil.class);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return path;*/
		
		/* get bundle with the specified id */
		String pluginId = ID.PLUGIN_NAME;
		Bundle bundle = Platform.getBundle(pluginId);
		if( bundle == null ){
			throw new RuntimeException("Could not resolve plugin: " + pluginId + "\r\n");
		}
		
		/* resolve Bundle::getEntry to local URL */
		URL pluginURL = null;
		try {
			pluginURL = FileLocator.resolve(bundle.getEntry("/"));
		} catch (IOException e) {
			throw new RuntimeException("Could not get installation directory of the plugin: " + pluginId);
		}
		
		String pluginInstallDir = pluginURL.getPath().trim();
		if( pluginInstallDir.length() == 0 )
			throw new RuntimeException("Could not get installation directory of the plugin: " + pluginId);
		
		/* since path returned by URL::getPath starts with a forward slash, that
		 * is not suitable to run commandlines on Windows-OS, but for Unix-based
		 * OSes it is needed. So strip one character for windows. There seems
		 * to be no other clean way of doing this. */
		if( Platform.getOS().compareTo(Platform.OS_WIN32) == 0 )
			pluginInstallDir = pluginInstallDir.substring(1);
		
		int length = pluginInstallDir.length();
		char tail = pluginInstallDir.charAt(length-1);
		if(tail==File.separatorChar || tail=='/' || tail=='\\'){
			pluginInstallDir = pluginInstallDir.substring(0,length-1);
		}
		return pluginInstallDir;
	}

		
}
