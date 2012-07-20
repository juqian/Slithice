package test;

import java.io.PrintStream;
import java.util.*;

import jqian.sootex.Cache;
import jqian.sootex.location.AccessPath;
import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.location.InstanceObject;
import jqian.sootex.location.Location;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.ptsto.PtsToHelper;
import soot.*;


/**
 * 
 */
public class PtsToTester {
	public static void testPtsToQuery(PrintStream out,SootMethod m,IPtsToQuery pt2Query){      
		System.out.println("Test local's points-to");
		Body body = m.getActiveBody();
        for (Local local: body.getLocals()) {
            Type type=local.getType();
            if(!(type instanceof RefLikeType)) 
            	continue;
            
            Location ptr = Location.valueToLocation(local);
            out.print("\n"+ptr+" -> ");
            Set<InstanceObject> pt2Set = pt2Query.getPointTos(m, null,ptr);
            out.print(pt2Set.toString());
            
            if(true){//test access path representation
                if (type instanceof RefType) {
                    SootClass cls = ((RefType) type).getSootClass();
                    Collection<SootField> fields = Cache.v().getAllInstanceFields(cls);
                    for (SootField f: fields) {
                        if (f.getType() instanceof RefLikeType) {                            
                            AccessPath ap = AccessPath.getByRoot(ptr);
                            ap = ap.appendFieldRef(f);
                            out.print("\n   " + ap + " : ");
                            Set<?> locs = PtsToHelper.getAccessedLocations(pt2Query, HeapAbstraction.FIELD_SENSITIVE, null, ap);
                            Test.printCollection(locs.iterator(),",");
                            
                            testFieldPtsTo(out,locs,pt2Query);                            
                        }
                    }
                } else if (type instanceof ArrayType) {
                    //Type elmtType = ((ArrayType) type).getArrayElementType();
                    if (type instanceof RefLikeType) {
                        AccessPath ap = AccessPath.getByRoot(ptr);
                        ap = ap.appendArrayRef();
                        out.print("\n   " + ap + ":");
                        Set<?> locs = PtsToHelper.getAccessedLocations(pt2Query, HeapAbstraction.FIELD_SENSITIVE, null, ap);
                        Test.printCollection(locs.iterator(),",");
                        
                        testFieldPtsTo(out,locs,pt2Query);
                    }
                }
            }
        }
    }
    
    private static void testFieldPtsTo(PrintStream out,Collection<?> locs,IPtsToQuery query){
        for(Iterator<?> it=locs.iterator();it.hasNext();){
            Object obj=it.next();
            if(obj instanceof Location){
            	Location ptr = (Location)obj;
                if(ptr.isPointer()){                	
                	Set<InstanceObject> pt2Set = query.getPointTos(null, null,ptr);
                    out.print("\n"+ptr+" -> "+pt2Set);
                }
                
            }
        }
    }
}
