package jqian.sootex.dependency.slicing;

import java.util.*;

import jqian.sootex.dependency.pdg.*;
import jqian.sootex.util.SootUtils;
import soot.*;

/**
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ProgramSlice {
    private Collection<SlicingCriterion> _criteria;
    private Map<MethodOrMethodContext,Collection> _mc2stmts; //covered statement in each method context
    private Map<MethodOrMethodContext,Collection> _mc2params;   //relevant parameter indexes
    
	public ProgramSlice(Collection criteria,Collection<DependenceNode> depNodes){
    	this._criteria = criteria;
    	_mc2stmts = new HashMap<MethodOrMethodContext,Collection>();
    	_mc2params = new HashMap<MethodOrMethodContext,Collection>();
    	
    	for(DependenceNode n: depNodes){    	 	
    		MethodOrMethodContext mc = n.getMethodOrMethodContext();
    		
    		if(n instanceof JimpleStmtNode){
    			Collection stmts = openMappedCollection(_mc2stmts,mc);
    			stmts.add(n.getBinding());
    		}
    		else if(n instanceof FormalIn){
    			FormalIn fm = (FormalIn)n;
    			int paramIdx = fm.getParamIndex();
    			if(paramIdx>=0){
    				Collection params = openMappedCollection(_mc2params,mc);
    				params.add(new Integer(paramIdx));
    			}
    		}
    	}
    }
    
    public Collection<SlicingCriterion> criterion(){
    	return _criteria;
    }    
    
    public Collection<MethodOrMethodContext> getMethodOrMethodContexts(){
    	return _mc2stmts.keySet();
    }
    
    public Collection<Unit> getRelevantStmts(SootClass cls){
    	//TODO Here we do not actually handle context-sensitive analysis
    	Collection<Unit> units = new LinkedList<Unit>();
    	for(Iterator<?> it=cls.methodIterator();it.hasNext();){
    		SootMethod m = (SootMethod)it.next();    		
    		Collection<Unit> relevants = getRelevantStmts(m);
    		if(relevants!=null)
    			units.addAll(relevants);
    	}
    	
    	return units;
    }
    
	public Collection<Unit> getRelevantStmts(MethodOrMethodContext mc){
    	return _mc2stmts.get(mc);
    }
    
    /** Return indexes of relevant parameters. */
	public Collection<Integer> getRelevantParams(MethodOrMethodContext mc){
    	return _mc2params.get(mc);
    }
    
    public Collection<Integer> getRelevantLines(MethodOrMethodContext mc){
    	Collection<Unit> stmts = getRelevantStmts(mc);
    	if(stmts==null)
    		return Collections.emptyList();
    	
    	return toJavaLines(stmts);
    }
    
    public Collection<Integer> getRelevantLines(SootClass cls){
    	Collection<Unit> stmts = getRelevantStmts(cls);
    	if(stmts==null)
    		return new LinkedList<Integer>();
    	
    	return toJavaLines(stmts);
    }
    
	private Collection openMappedCollection(Map map,MethodOrMethodContext mc){
    	Collection out = (Collection)map.get(mc);
    	if(out==null){
    		out = new LinkedList();
    		map.put(mc,out);
    	}
    	return out;
    }
    
    private static Collection<Integer> toJavaLines(Collection<Unit> in){
    	Collection<Integer> lines = new HashSet<Integer>();
    	for(Unit s: in){    	 	
    		int line = SootUtils.getLine(s);
    		lines.add(line);
    	}
    	return lines;
    }
}
