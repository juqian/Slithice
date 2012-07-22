package jqian.sootex.util;

import java.util.*;

import soot.*;
import soot.toolkits.graph.*;


/**
 * FIX 2008-07-12 An error occurred in handling exceptional flows
 */ 
public class HammockCFG extends UnitGraph{
	protected Collection<Unit> _units; //valid units, may be a part of unitChain
	protected List<Unit> _heads;
	protected List<Unit> _tails;
	
	@SuppressWarnings("unchecked")
	public HammockCFG(Body body) {
		super(body);
		
		int size = unitChain.size();
    	unitToSuccs = new HashMap<Unit,List<Unit>>(size * 2 + 1, 0.7f);
    	unitToPreds = new HashMap<Unit,List<Unit>>(size * 2 + 1, 0.7f);
           
    	//Get the unique entry
    	Unit entryPoint = unitChain.getFirst();
		List<Unit> nonExceptionalEntries = new ArrayList<Unit>(1);
		nonExceptionalEntries.add(entryPoint);
		
		//set entry links
		Unit entry = CFGEntry.v();
		_heads = new ArrayList<Unit>(1);		
		_heads.add(entry);
		unitToPreds.put(entry,Collections.EMPTY_LIST);			
		unitToSuccs.put(entry,nonExceptionalEntries);		
		for (Unit s: nonExceptionalEntries) {		
			List<Unit> preds = new ArrayList<Unit>(1);
			preds.add(entry);	
			unitToPreds.put(s, preds);			
		}
		
		
		//tailor graph
		List<Unit> ends = new LinkedList<Unit>();
		Set<Unit> processed = new HashSet<Unit>();
		Stack<Unit> stack = new Stack<Unit>();
		stack.addAll(nonExceptionalEntries);
		    
		while (!stack.isEmpty()) {
			Unit s = (Unit) stack.pop();

			if (!processed.add(s)) {
				continue;
			}

			//set successors
			List<Unit> succs = findSuccsOf(s);
			unitToSuccs.put(s,succs);	
			
			if(succs.size()==0){
				ends.add(s);
			}
			
			for (Unit next: succs) {
				List<Unit> preds = assurePredsList(next);
				preds.add(s);				

				if (!processed.contains(next)) {
					stack.add(next);
				}
			}
		}
		
		//set exit links	
		Unit exit = CFGExit.v();	
		unitToPreds.put(exit, ends);
		unitToSuccs.put(exit, Collections.EMPTY_LIST);
		for (Unit s: ends) {
			List<Unit> succs = new ArrayList<Unit>(1);
			succs.add(exit);
			unitToSuccs.put(s,succs);
		}
		
		_tails = new ArrayList<Unit>(1);
		_tails.add(exit);	
		_units = unitToSuccs.keySet();		
	}
	
	
	private List<Unit> findSuccsOf(Unit unit) {
		List<Unit> successors = new ArrayList<Unit>();

		if (unit.fallsThrough()) {	
			Unit next = unitChain.getSuccOf(unit);
			if (next != null) {
				successors.add(next);				 
			}
		}

		if (unit.branches()) {
			for (Iterator<UnitBox> targetIt = unit.getUnitBoxes().iterator(); targetIt.hasNext();) {
				Unit target = targetIt.next().getUnit();
				// Arbitrary bytecode can branch to the same
				// target it falls through to, so we screen for duplicates:
				if (!successors.contains(target)) {
					successors.add(target);					 
				}
			}
		}
		return successors;
	}
	
	private List<Unit> assurePredsList(Unit s){
		List<Unit> preds = unitToPreds.get(s);
		if(preds==null){
			preds = new ArrayList<Unit>();
			unitToPreds.put(s,preds);
		}
		return preds;
	}

	/** Return a single entry CFGEntry.v(). */
	public List<Unit> getHeads() {		 
		return _heads;
	}
	
	public List<Unit> getTails() {		 
		return _tails;
	}

	public List<Unit> getPredsOf(Unit s) {
		return unitToPreds.get(s);
	}

	public List<Unit> getSuccsOf(Unit s) {
		return unitToSuccs.get(s);
	}

	public Iterator<Unit> iterator() {
		return _units.iterator();
	}

	public int size() {
		return _units.size();
	}      
		
	//////////////////////////////////////////////////
	public List<Unit> getExtendedBasicBlockPathBetween(Unit from, Unit to){
		return null;
	}	  
	 
	public String toString() {
		StringBuffer buf = new StringBuffer();
		for(Unit u: _units) {
			buf.append("// preds: " + getPredsOf(u) + "\n");
			buf.append(u.toString() + '\n');
			buf.append("// succs " + getSuccsOf(u) + "\n");
		}
		return buf.toString();
	}
	
	public Object clone(){
		return new HammockCFG(this.method.getActiveBody());
	}
}
