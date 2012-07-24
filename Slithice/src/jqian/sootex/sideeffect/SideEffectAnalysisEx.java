package jqian.sootex.sideeffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.location.Location;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.util.SootUtils;
import soot.Scene;
import soot.SootMethod;
import soot.util.Numberer;

/**
 * Side-effect analysis with out-access filter
 * TODO Experiment   (Seems only 1x% side reduction)
 * TODO incorrect implementation, dropped!
 * @deprecated
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class SideEffectAnalysisEx implements ISideEffectAnalysis{
    private Collection _entries;
    private IPtsToQuery _ptsto;    
    HeapAbstraction _heapAbstraction;
    
    private Set<Location>[] _method2ModHeaps; 
    private Set<Location>[] _method2UseHeaps;    
    
    private Set<Location>[] _method2ModGb;
    private Set<Location>[] _method2UseGb;   
    
    private float reduceRate;
    private int reduceCount;
    
	public SideEffectAnalysisEx(IPtsToQuery ptsto,Collection entries, HeapAbstraction heapMemAbstraction){
    	this._entries = entries;
    	this._ptsto = ptsto;
    	this._heapAbstraction = heapMemAbstraction;
    }   
	
	public void build(){ 
		SideEffectAnalysis se = new SideEffectAnalysis(_ptsto, _entries, _heapAbstraction);
		se.build();
		OutsideAccessAnalysis outside = new OutsideAccessAnalysis(_ptsto, _entries, _heapAbstraction);
		outside.build();
		
		 int methodNum = SootUtils.getMethodCount(); 
	     _method2ModHeaps = new Set[methodNum];
	     _method2UseHeaps = new Set[methodNum];
	     _method2ModGb = new Set[methodNum];        
	     _method2UseGb = new Set[methodNum]; 

	     Numberer numberer = Scene.v().getMethodNumberer();
	     for(int i=0; i<methodNum; i++){
	    	 SootMethod m = (SootMethod)numberer.get(i);
	    	 if(m==null) continue;
	    	 
	    	 Set outsideAccessed = outside.getOutsideAccess(i);
	    	 _method2ModHeaps[i] = filter(se.getModHeapLocs(m), outsideAccessed);
		     _method2UseHeaps[i] = filter(se.getUseHeapLocs(m), outsideAccessed);
		     _method2ModGb[i] = filter(se.getModGlobals(m), outsideAccessed);        
		     _method2UseGb[i] = filter(se.getUseGlobals(m), outsideAccessed); 
		     
		     se.clearMethod(i);
		     outside.clearMethod(i);
	     }
	     
	     reduceRate = reduceRate / reduceCount;
	}
	
	private Set<Location> filter(Collection original, Set filter){
		if(original==null)
			return null;
		else if(original.isEmpty()){
			return (Set)original;
		}
		
		ArrayList newone = new ArrayList();
		for(Object o: original){
			if(filter.contains(o)){
				newone.add(o);
			}
		}
		
		reduceRate += (original.size() - newone.size())/original.size();
		reduceCount++;
		
		return SootUtils.toCompactSet(newone);
	}
	

    public Collection<Location> getModGlobals(SootMethod m){
        return _method2ModGb[m.getNumber()];        
    }
    
    public Collection<Location> getUseGlobals(SootMethod m){
        return _method2UseGb[m.getNumber()];  
    }
    
    public Collection<Location> getModHeapLocs(SootMethod m){
        return _method2ModHeaps[m.getNumber()];
    }   
    
    public Collection<Location> getUseHeapLocs(SootMethod m){
        return _method2UseHeaps[m.getNumber()];
    } 
}
