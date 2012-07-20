package jqian.sootex.dependency.slicing;

import java.util.*;
import jqian.sootex.location.Location;
import jqian.sootex.util.SootUtils;
import jqian.util.CollectionUtils;
import soot.*;

public class JimpleSlicingCriterion implements SlicingCriterion {
	/**
	 * 
	 * @param mc            Currently, this parameter can only be a SootMethod
	 * @param stmt
	 * @param variables      The variable set can be empty or null. In that case, only the <code>stmt</code> is used
	 *                       as slicing criteria.
	 * @param postExecution  Whether to start slicing after the given statement has been executed
	 */
	public JimpleSlicingCriterion(MethodOrMethodContext mc, Unit stmt, Set<Location> variables,boolean postExecution) {
		this._mc = mc;
		this._stmt = stmt;
		this._variables = variables;
		this._postExecution = postExecution;
	}

	public Unit statement() {
		return _stmt;
	}
	
	public MethodOrMethodContext context(){
		return _mc;
	}
	
	public boolean startFromPostExecution(){
		return _postExecution;
	}

/*	public Collection toStmtCriteria(IReachingDUQuery rd) {
		Collection criteria = new HashSet();
		//get the definitely defined location and used locations
	

		//Set useLoc = new HashSet();

		for (Iterator it = _variables.iterator(); it.hasNext();) {
			Location loc = (Location) it.next();

		}
		
		/*Collection criteria = new HashSet();
		//get the definitely defined location and used locations
		AccessPath def = UnitInfo.v().getDefAccessPath(_stmt);
		AccessPath use = UnitInfo.v().getCopiedAccessPath(_stmt);
		Location assigned = null;
		if (def != null && def.length() == 0)
			assigned = def.getRoot();

		//Set useLoc = new HashSet();
		if (use != null) {

		}

		for (Iterator it = _variables.iterator(); it.hasNext();) {
			Location loc = (Location) it.next();
			if (loc == assigned) {

			}

		}
	

		
		//TODO
		return criteria;
	}
*/
	
	public Set<Location> variables() {
		return _variables;
	}

	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("<");
		str.append(SootUtils.toShortString(_stmt.toString()));
		str.append(",{");
		str.append(CollectionUtils.toString(_variables.iterator(), ","));
		str.append("}>");
		return str.toString();
	}

	protected MethodOrMethodContext _mc;
	protected Unit _stmt;
	protected Set<Location> _variables;
	private boolean _postExecution;
}
