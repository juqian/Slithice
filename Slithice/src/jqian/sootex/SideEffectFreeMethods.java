package jqian.sootex;

import java.io.*;
import java.util.*;

import jqian.Global;
import jqian.util.*;
//import jqian.Options;

import soot.*;


public class SideEffectFreeMethods {
	private static SideEffectFreeMethods _instance;
	private SideEffectFreeMethods(){
		loadSpecials();
	}	
	
	public static SideEffectFreeMethods v(){
		if(_instance==null){
			_instance = new SideEffectFreeMethods();
		}
		return _instance;
	}
	
	///////////////////////////////////////////////////////
    private Set<SootMethod> _sideEffectFreeMethods;     
    private Set<SootClass> _sideEffectFreeClasses;   
    private Set<String> _sideEffectFreePkgs; 
    
    
    /**Check whether the method is a side effect free library method.*/
    public boolean isSideEffectFreeMethod(SootMethod m){
        if(_sideEffectFreeMethods.contains(m)){
            return true;
        }
        
        SootClass c=m.getDeclaringClass();  
        String pkgName = c.getPackageName();
        if(_sideEffectFreeClasses.contains(c) || _sideEffectFreePkgs.contains(pkgName)){
            return true;
        } 
        
        return false;
    } 
   
    public String toString(){
        String s = "Side effect free library methods\n";
        s += CollectionUtils.toString(_sideEffectFreeMethods.iterator(),"\n"); 
        
        s += "\nSide effect free library classes\n";
        s += CollectionUtils.toString(_sideEffectFreeClasses.iterator(),"\n"); 
        
        s += "\nSide effect free library packages\n";
        s += CollectionUtils.toString(_sideEffectFreePkgs.iterator(),"\n"); 
        
        return s;
    } 
    
    /** This method only works in global analysis mode. */
	private void loadSpecials(){
		Scene scene = Scene.v();
		List<String> tmp = new LinkedList<String>();
    	
    	try{    	
    		InputStream stream = PathUtils.getFileByName(getClass(),"heuristics.txt");
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
	
			// side effect free methods			
			readSection(in, tmp);
			_sideEffectFreeMethods = new HashSet<SootMethod>();
			for (String name: tmp) {				
				SootMethod m = getMethod(name);
				if(m!=null){//For local analysis, this method may be not resolved yet
					_sideEffectFreeMethods.add(m);
				}
			}
			tmp.clear();			
			
			
			// side effect free classes			
			readSection(in, tmp);  //a section of misc classes			
			_sideEffectFreeClasses = new HashSet<SootClass>();
			for (String name: tmp) {
				SootClass c = getClass(name);
				if(c!=null)	_sideEffectFreeClasses.add(c);
			}
			tmp.clear();
			//_sideEffectFreeClasses.addAll(throwables);	
	        
			//side effect free packages
			_sideEffectFreePkgs = new HashSet<String>();
			readSection(in, _sideEffectFreePkgs); 
			 
			//atomic types
			int typeCount = Scene.v().getTypeNumberer().size();
		  
			readSection(in, tmp);
			
			
			
			
			tmp.clear();
			
			in.close();
    	}
    	catch(Exception e){
    		throw new Error("Fail to load java names: "+e);
    	}
	}
	
	
	private static void printErrInGlobalMode(Exception e){
		if(soot.options.Options.v().whole_program()){
			Global.v().out.println(e.getMessage());
		}	
	}
	
	
	private void getAllSubClasses(SootClass cls,Collection<SootClass> out){
		try{
			FastHierarchy hierarchy = Scene.v().getFastHierarchy();	
			Collection<SootClass> subs = hierarchy.getSubclassesOf(cls);
			out.addAll(subs);
			
			for(SootClass c: subs){
				getAllSubClasses(c,out);
			}	
		}
		catch(Exception e){
			printErrInGlobalMode(e);
		}	
	}
	
	private static SootMethod getMethod(String name){
		try{
			return Scene.v().getMethod(name);
		}
		catch(Exception e){
			printErrInGlobalMode(e);		
		}
		
		return null;
	}
	
	private SootClass getClass(String name){
		try{
			return Scene.v().getSootClass(name);
		}
		catch(Exception e){
			printErrInGlobalMode(e);		
		}
		
		return null;
	} 
     
    private static void readSection(BufferedReader in,Collection<String> names) throws IOException{    	
    	 String line;
    	 
    	 //skill empty lines
    	 while(true) {
    		 line = in.readLine();      		 
    	     if(line==null)  return;
    	     line = line.trim();
    	     if(!line.equals(""))
        		 break;
    	 }    	 
    	 
         for(; line!=null && !line.equals(""); line = in.readLine()) {
        	 if(line.charAt(0)=='#')
        		 continue;
        	 
        	 names.add(line);
         }
    }

    
    
    
       
 
}
