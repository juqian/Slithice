package jqian.sootex;

import java.util.*;
import jqian.util.*;
import soot.*;

/** 
 * Some already known side-effect free methods.
 * Use this information can save a lot of analysis time, especially for the library methods.
 * TODO Experiment
 */
public class SideEffectFreeMethods {
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
}
