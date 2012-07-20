/** 
 *  Transform a .dot file to an image file
 */

package jqian.util.dot;

import java.io.*;

import jqian.util.ui.PictureViewer;


public class DotViewer {
    private String _filename;
    private String _format;
    private String _graphName;
    private final String _dotpath;
    
    ////////////////////////////////////////////////////
    /** Default to view as jpg file. */
    public DotViewer(String dotpath, String filename){ 	
        this(dotpath, filename, "jpg");
    }
    
    /**
     * @param filename dot file name with extension
     * @param format image file format, could be a choice from {jpg, gif, ps}
     */
    public DotViewer(String dotpath, String filename,String format){ 	
        //FIX Using "./" like path does not work in a plugin
    	this._dotpath = dotpath;
    	this._graphName = filename+"."+format;    	
        this._filename=filename;
        this._format=format;
    }
    
    public void dotIt(){
        runCommand(_dotpath+" -T"+_format+" "+_filename+" -o "+_graphName);           
    }
    
    public void view(){
    	PictureViewer view = new PictureViewer(_graphName,_graphName);
        view.setVisible(true);
    }    
    
    private void runCommand(String cmd){
        try{
            Process process=Runtime.getRuntime().exec(cmd);            
            try{
                process.waitFor();
            }catch(InterruptedException e){
                System.out.println("dot error.");
            }
        }catch(SecurityException e){
            System.err.println("Security error!");
            e.printStackTrace(System.err);
        }catch(NullPointerException e){
            System.err.println("Command is null!");
            e.printStackTrace(System.err);
        }catch(IllegalArgumentException e){
            System.err.println("Command is empty!");
            e.printStackTrace(System.err);
        }catch(IOException e){
            System.err.println("IO error!");
            e.printStackTrace(System.err);
        }
        catch(Exception e){
        	System.err.println("error!");
            e.printStackTrace(System.err);
        }
    }
    

    public static void main(String[] args){
    	if(args.length < 3){
    	     System.out.println("Usage: java DotView <dotpath> <dot_file_name> <image_file_extention>");
    	     System.out.println("    Example: java DotView ./../lib/dot/dot ./output/dot/call_graph.dot jpg");
    	     return;
    	}
    	
        DotViewer dot=new DotViewer(args[0],args[1],args[2]);
        dot.dotIt();
        dot.view();
    }
}
