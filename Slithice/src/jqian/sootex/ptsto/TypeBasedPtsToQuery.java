package jqian.sootex.ptsto;

import java.util.*;

import jqian.sootex.location.InstanceObject;
import jqian.sootex.location.Location;
import soot.*;

/**
 * A type-based points-to relation query
 */
public class TypeBasedPtsToQuery implements IPtsToQuery{
    private final boolean _allReachable; 
	
    public TypeBasedPtsToQuery(boolean allReachable){  
    	TypeBasedPointsToAnalysis.v(allReachable);
    	_allReachable = allReachable;
    } 

    public void getPointTos(Type type, Set<InstanceObject> pt2Set){
    	Set<Type> set = TypeBasedPointsToAnalysis.v(_allReachable).reachingObjects(type);
        for(Type t: set){ 
        	InstanceObject o = InstanceObject.typeToObject(t);
        	pt2Set.add(o);
        }
    }    
    
	public Set<InstanceObject> getPointTos(SootMethod m, Unit stmt, Location ptr){
    	if(!ptr.isPointer())
    		return Collections.emptySet();

    	Set<InstanceObject> pt2Set = new HashSet<InstanceObject>();
    	getPointTos(ptr.getType(),pt2Set);
    	return pt2Set;
    }
}