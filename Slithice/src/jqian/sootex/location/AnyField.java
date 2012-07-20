package jqian.sootex.location;

import jqian.Global;
import soot.*;

public class AnyField extends SootField {
    private static AnyField _instance=null;
    protected static void reset(){
    	_instance = null;
    }
    
    public static AnyField v(){
        if(_instance==null){
            _instance=new AnyField();
            Global.v().regesiterResetableGlobals(AnyField.class);
        }
        return _instance;
    }
    
    private AnyField(){
        super("*",Scene.v().getSootClass("java.lang.Object").getType());
    }
    
    /** Added as a field of java.lang.Object. */
    public SootClass getDeclaringClass(){
        return Scene.v().getSootClass("java.lang.Object");
    }
    
    public boolean isStatic(){
    	return false;
    }
     
    public boolean isDeclared(){
       return false;
    }
              
    public String  toString(){
        return "*";
    }  
}
