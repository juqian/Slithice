package jqian.sootex.dependency;

import java.util.Collection;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.ReachableMethods;

/**
 *
 */
public class CombinedDependencyQuery implements IDependencyQuery {
	private IDependencyQuery _left;
	private IDependencyQuery _right;
	private ReachableMethods _forLeft;
	
	public CombinedDependencyQuery(IDependencyQuery left, IDependencyQuery right, ReachableMethods forLeft){
		this._left = left;
		this._right = right;
		this._forLeft = forLeft;
    }

	public Collection<Unit> getCtrlDependences(SootMethod m, Unit u) {
		if(_forLeft.contains(m)){
			return _left.getCtrlDependences(m, u);
		}
		else{
			return _right.getCtrlDependences(m, u);
		}
	}

	public Collection<Unit> getWRDependences(SootMethod m, Unit u) {
		if(_forLeft.contains(m)){
			return _left.getWRDependences(m, u);
		}
		else{
			return _right.getWRDependences(m, u);
		}
	}

	public Collection<Unit> getRWDependences(SootMethod m, Unit u) {
		if(_forLeft.contains(m)){
			return _left.getRWDependences(m, u);
		}
		else{
			return _right.getRWDependences(m, u);
		}
	}

	public Collection<Unit> getWWDependences(SootMethod m, Unit u) {
		if(_forLeft.contains(m)){
			return _left.getWWDependences(m, u);
		}
		else{
			return _right.getWWDependences(m, u);
		}
	}

	public void release(SootMethod m) {
		_left.release(m);
		_right.release(m);
	}

}
