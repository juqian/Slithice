package jqian.sootex.sideeffect;

import java.util.*;

import jqian.sootex.location.*;
import soot.*;

/**
 * Collect the locations a program entity can access
 */
public interface ISideEffectAnalysis {  
    /** Get all locations accessed by a method, including NO globals. */    
    public Collection<Location> getModHeapLocs(SootMethod m);
    public Collection<Location> getUseHeapLocs(SootMethod m); 
    
    public Collection<Location> getModGlobals(SootMethod m);
    public Collection<Location> getUseGlobals(SootMethod m); 
}
