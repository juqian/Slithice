package jqian.sootex.ptsto;

import java.util.*;

import soot.ArrayType;
import soot.Immediate;
import soot.RefLikeType;
import soot.RefType;
import soot.SootField;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;

import jqian.sootex.location.AccessPath;
import jqian.sootex.location.ArraySpace;
import jqian.sootex.location.CommonInstObject;
import jqian.sootex.location.HeapField;
import jqian.sootex.location.HeapLocation;
import jqian.sootex.location.InstanceObject;
import jqian.sootex.location.Location;
import jqian.sootex.location.HeapAbstraction;
import jqian.util.CollectionUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class PtsToHelper {
	public static IPtsToQuery createPointsToQuery(PointsToAnalysisType type) {
		IPtsToQuery ptsto = null;
		switch (type) {
		case SPARK:
			ptsto = new SparkPtsToQuery();
			break;
		case TYPE_BASED:
			ptsto = new TypeBasedPtsToQuery(false);
			break;
		case NAIVE:
			ptsto = new NaivePtsToQuery();
			break;
		default:
			throw new RuntimeException("pointer analysis - " + type + " - unsupported.");
		}

		return ptsto;
	}
	   
	static Set<Location> getField(Set<InstanceObject> objects, SootField field) {
		Set<Location> hset = new HashSet<Location>();
		for (InstanceObject o: objects) {			 
			if (!(o instanceof CommonInstObject))
				continue;

			CommonInstObject hobj = (CommonInstObject)o;
			HeapField f = hobj.getField(field);

			// Heap field can be null, if the object is considered as an atomic type
			if (f != null)
				hset.add(f);
		}
		return hset;
	}

	static Set<Location> getArrayElement(Set<InstanceObject> objects) {
		Set<Location> hset = new HashSet<Location>();
		for (InstanceObject o: objects) {			 
			if (!(o instanceof ArraySpace))
				continue;

			ArraySpace hobj = (ArraySpace) o;
			Location p = hobj.getElement();
			hset.add(p);
		}
		return hset;
	}
	
	public static boolean mayAlias(IPtsToQuery ptsto, Location ptr1, Location ptr2){    
		if(ptr1==ptr2){
    		return true;
    	}
		
    	//firstly check the type
    	Type t1 = ptr1.getType();
    	Type t2 = ptr2.getType();
    	
    	if(!(t1 instanceof RefLikeType) || !(t2 instanceof RefLikeType)){
    		return false;
    	}
    	else if(t1 instanceof ArrayType && !(t2 instanceof ArrayType)){
    		return false;
    	}
    	else if(t1 instanceof RefType && !(t2 instanceof RefType)){
    		return false;
    	}
    	
    	//check points-to sets
    	Set pt1 = ptsto.getPointTos(null, null, ptr1);
		Set pt2 = ptsto.getPointTos(null, null, ptr2);		
		return CollectionUtils.hasInterset(pt1, pt2);		 
    } 
	    
    /**Get all abstract locations that may alias with 'ap' in 'stmt'*/
	private static Set<Location> getAliasedLocations(Unit stmt,AccessPath ap,IPtsToQuery query){  
        Set cur = new HashSet(40);
        cur.add(ap.getRoot()); 
        
        for(Object ac: ap.getAccessors()){
        	Set<InstanceObject> heaps;
        	if(cur.size()==1){
        		heaps = query.getPointTos(null, stmt, (Location)cur.iterator().next());
        	}
        	else{
        		heaps = new HashSet<InstanceObject>(100);
        		for(Object p: cur){      
        	        Set s = query.getPointTos(null, stmt,(Location)p);
        	        heaps.addAll(s);
        	    }
        	}    
        
            if(ac instanceof SootField){
                cur = getField(heaps, (SootField)ac);
            }else{
                cur = getArrayElement(heaps);
            }            
        }
        return cur;
    }
 
	//TODO performance
	public static Set<Location> getAccessedLocations(IPtsToQuery ptsto, 
			HeapAbstraction heapAbstraction, Unit stmt, AccessPath ap){
		if(heapAbstraction==HeapAbstraction.FIELD_SENSITIVE){
			Set<Location> locs = PtsToHelper.getAliasedLocations(null, ap, ptsto);
			return locs;
		}
		
		Set<Location> locs = new HashSet<Location>();
		if(heapAbstraction==HeapAbstraction.FIELD_BASED){
			if(ap.withArrayElmtAccess()){
				//locs.add(Location.getUnknownArrayElmt());
				
				// distinguish elements of different arrays
				Location base = ap.getRoot();
				Set<InstanceObject> heaps = ptsto.getPointTos(null, stmt,base);	
				Set<Type> types = new HashSet<Type>();
				for(InstanceObject o: heaps){
					Type t = ((ArraySpace)o).getType();
					types.add(t);
				}
				
				for(Type t: types){
					ArraySpace o = (ArraySpace)InstanceObject.typeToObject(t);
					locs.add(o.getElement());
				}
			}
			else{
				SootField f = ap.getLastAccessedField();
				locs.add(Location.getHeapFieldLocation(f));
			}
		}
		else if(heapAbstraction==HeapAbstraction.TYPE_BASED){
			/*Type rootType;
			if(ap.withArrayElmtAccess()){
				Type elmtType = ap.getDeclareType();
				rootType = elmtType.makeArrayType();
			}
			else{
				SootField f = ap.getLastAccessedField();
				rootType = f.getDeclaringClass().getType();
			}
			
			Set<Type> concreteSubTypes = TypeBasedPointsToAnalysis.v(false).reachingObjects(rootType);
			for(Type t: concreteSubTypes){
				HeapLocation loc = Location.getLocationForType(t);
				out.add(loc);
			}*/		
			
			Location base = ap.getRoot();
			Set<InstanceObject> heaps = ptsto.getPointTos(null, stmt,base);				
			for(InstanceObject o: heaps){
				Type t = null;
				if(o instanceof ArraySpace){
					t = ((ArraySpace)o).getType();
				}
				else if(o instanceof CommonInstObject){
					t = ((CommonInstObject)o).getType();
				}
				else{
					throw new RuntimeException("");
				}
				 
				HeapLocation loc = Location.getLocationForType(t);
				locs.add(loc);
			}
		}
		// naive, no distinguishment
		else{	
			Location loc = null;
			if(ap.withArrayElmtAccess()){
				loc = Location.getUnknownArrayElmt();				
			}
			else{
				loc = Location.getUnknownHeapField();
			}
			locs.add(loc);
		}
		
		return locs;
	}
    
	protected static final Set<Location> getAliasedLocations(Unit stmt,Value ref,IPtsToQuery query){
		if(ref instanceof InstanceFieldRef){
			InstanceFieldRef iref = (InstanceFieldRef)ref;
			Location base = Location.valueToLocation(iref.getBase());
			Set<InstanceObject> heaps = query.getPointTos(null, stmt,base);			
			Set out = getField(heaps, iref.getField());
			return out;
		}
		else if(ref instanceof ArrayRef){
			ArrayRef aref = (ArrayRef)ref;
			Location base = Location.valueToLocation(aref.getBase());
			Set<InstanceObject> heaps = query.getPointTos(null, stmt,base);
			Set out = getArrayElement(heaps);
			return out;
		}
		else if(ref instanceof Immediate){
			Location base = Location.valueToLocation(ref);
			Set out = new HashSet(1);
			out.add(base);
			return out;
		}
		else{
			throw new RuntimeException();
		}
    }
}
