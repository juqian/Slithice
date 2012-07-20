package jqian.sootex.dependency.slicing;

import java.util.*;

import jqian.sootex.location.AccessPath;
import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.location.Location;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.ptsto.PtsToHelper;
import jqian.sootex.util.SootUtils;
import soot.*;
import soot.jimple.*;


/**
 * Slicing criterion on Java source code level
 */
public class JavaSlicingCriterion implements SlicingCriterion{
	protected Set<String> _varNames = new HashSet<String>();
	protected String _typeOrMethod;
	protected int _line;
	protected boolean _postExecution;
	
	/**
	 * @param varNames   A string identifier for the concern variable.
	 *        The variable can be the name of a local, a full qualified field signature like
	 *        "<java.lang.System: java.io.PrintStream out>", or an access path like
	 *        "local.<java.util.List: int size>".
	 * @param scope     Which could be a method signature or a full qualified type name
	 * @param postExecution   Whether to start slicing from the state that the given line
	 *         has been executed.
	 */
	public JavaSlicingCriterion(Collection<String> varNames,String scope,int line,boolean postExecution){
		this._varNames.addAll(varNames);
		this._typeOrMethod = scope;
		this._line = line;
		this._postExecution = postExecution;
	}
	
	
	public String getEnclosingScope(){
		return _typeOrMethod;
	} 
	
	/**
	 * Turn line into Units in bytecode level, map variables to Location in IR, and map type into SootClass
	 * @param query  A points-to query is demanded when a slicing criterion concerns on access paths.
	 *               This parameter can be null if you are sure the slicing criterion does not concerns
	 *               on any access path.
	 *               The specified points-to query should be the one used to construct dependence graph
	 */
	public Collection<JimpleSlicingCriterion> toJimpleCriterion(IPtsToQuery query, HeapAbstraction heapAbstraction){	
		//locate the method, and the units
		LinkedList<Unit> units = new LinkedList<Unit>();
		SootClass cls = null;	
		SootMethod method = null;
		try{		
			cls = Scene.v().getSootClass(_typeOrMethod);
		}catch(Exception e){}
		
		try{				
			method = Scene.v().getMethod(_typeOrMethod);
		}catch(Exception e){}
		
		if(method==null && cls!=null){
			for(SootMethod m: cls.getMethods()){			 
				unitsInLine(m,_line,units);				
				
				if(units.size()>0){
					method = m;
					break;
				}
			}
		}
		else if(method!=null){
			unitsInLine(method,_line,units);	
		}
		
		// can not map the criterion from source code to bytecode
		if(method==null)
			return null;
		
		//locate the concerned access paths
		Set<AccessPath> aps = new HashSet<AccessPath>();
		for(Local loc: method.getActiveBody().getLocals()){		
			String name = loc.getName();
			if(_varNames.contains(name)){
				Location stackLoc = Location.valueToLocation(loc);
				AccessPath ap = AccessPath.getByRoot(stackLoc);
				aps.add(ap);
			}
		}
		 
		//parse names with dots
		for(String name: _varNames){
			int index = name.indexOf('.');
			if(index>=0){
				//must be a global field
				if(name.charAt(0)=='<'){					
					try{					
						SootField field = Scene.v().getField(name);	
						Location global = Location.getGlobalLocation(field);
						AccessPath ap = AccessPath.getByRoot(global);
						aps.add(ap);
					}
					catch(Exception e){}					
				}
				else{//an instance field
					String local = name.substring(0,index);
					String fieldSignature = name.substring(index+1);
					try{					
						SootField field = Scene.v().getField(fieldSignature);	
						
						//find match locals
						if(local.equals("this")){
							Local loc = method.getActiveBody().getThisLocal();
							Location stackLoc = Location.valueToLocation(loc);
							AccessPath ap = AccessPath.getByRoot(stackLoc);
							ap = ap.appendFieldRef(field);
							aps.add(ap);
						}
						else{
							for(Local loc: method.getActiveBody().getLocals()){		
								if(loc.getName().equals(local)){
									Location stackLoc = Location.valueToLocation(loc);
									AccessPath ap = AccessPath.getByRoot(stackLoc);
									ap = ap.appendFieldRef(field);
									aps.add(ap);
								}
							}
						}
						
					}
					catch(Exception e){}	
				}
			}
		}
		
		
		Collection<JimpleSlicingCriterion> criteria = new HashSet<JimpleSlicingCriterion>();
		if(_postExecution){
			//collect in reverse order, choose slicing criterion on the tail Units first
			while(!units.isEmpty() && !aps.isEmpty()){
				Unit u = units.removeLast();
				collectCriterion(method,query,heapAbstraction,u,aps,criteria);
			}
		}
		else{
			for(Unit u: units){
				if(aps.isEmpty())
					break;
				
				collectCriterion(method,query,heapAbstraction,u,aps,criteria);
			}
		}	
		
		return criteria;
	}
	
	private void collectCriterion(SootMethod method, IPtsToQuery query, HeapAbstraction heapAbstraction, 
			Unit u, Set<AccessPath> aps, Collection<JimpleSlicingCriterion> out){
		Set<AccessPath> occur = occuredAccessPaths(method,u);		
		Collection<AccessPath> tmp  = new LinkedList<AccessPath>();
		tmp.addAll(occur);
		
		occur.retainAll(aps);
		
		if(!occur.isEmpty()){
			Set<Location> locs = getAliasedLocations(occur,u,query, heapAbstraction);
			
			if(_postExecution && u instanceof DefinitionStmt){
				Value left = ((DefinitionStmt)u).getLeftOp();
				AccessPath ap = AccessPath.valueToAccessPath(method, u, left);
				
				//If the postExecution flag is set, then the temporals should be 
				//considered in slicing Java stmts.
				//Here we heuristically assume varibales with name start from '$'
				//are temporals introduced in jimple. 
				if(ap!=null && aps.contains(ap)){
					for(AccessPath x: tmp){
						if(x.length()>0) continue;

						Location loc = x.getRoot();
						String name = loc.toString();
						boolean isTemporal = name.length()>0 && name.charAt(0)=='$';
						if(isTemporal){
							locs.add(loc);							 
						}
					}
				}				
			}
			
			
			JimpleSlicingCriterion criterion = new JimpleSlicingCriterion(method,u,locs,_postExecution);
			out.add(criterion);
		}
		
		aps.removeAll(occur);
	}
	
	private Set<Location> getAliasedLocations(Set<AccessPath> aps,Unit u, 
							IPtsToQuery query, HeapAbstraction heapAbstraction){
		Set<Location> aliased = new HashSet<Location>();
		for(AccessPath ap: aps){
			if(ap.length()>0 && query!=null){
				// TODO 这里要确保堆抽象的一致性
				Set<Location> x = PtsToHelper.getAccessedLocations(query, heapAbstraction, u, ap);
				aliased.addAll(x);
			}
			else{
				aliased.add(ap.getRoot());
			}			
		}
		
		return aliased;
	}
	
	
	protected Set<AccessPath> occuredAccessPaths(SootMethod method,Unit s){
		Set<AccessPath> vars = new HashSet<AccessPath>();
		List<?> boxed = s.getUseAndDefBoxes();
		for(Iterator<?> it=boxed.iterator();it.hasNext();){
			ValueBox box = (ValueBox)it.next();
			Value v = box.getValue();
			
			if(v instanceof Local || v instanceof FieldRef || v instanceof ArrayRef){
				AccessPath ap = AccessPath.valueToAccessPath(method,s,v);
				vars.add(ap);
			}			 
		}
		
		return vars;
	}
	
	protected boolean useVariables(Unit s,Set<String> variables){	 
		for(ValueBox box: s.getUseBoxes()){			 
			if(variables.contains(box.toString()))
				return true;
		}
		
		return false;
	}
	
	public static void unitsInLine(SootMethod m,int line,Collection<Unit> units){		
		if(m.isConcrete()&& !m.hasActiveBody()){
			m.retrieveActiveBody();
		}		
		
		if(m.hasActiveBody()){
			for(Unit s: m.getActiveBody().getUnits()){				
				if(SootUtils.getLine(s)==line){
					units.add(s);					
				}
			}
		}
	}

}
