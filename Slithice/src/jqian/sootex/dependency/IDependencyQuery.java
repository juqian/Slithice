package jqian.sootex.dependency;

import java.util.Collection;

import soot.SootMethod;
import soot.Unit;

public interface IDependencyQuery {
	public Collection<Unit> getCtrlDependences(SootMethod m, Unit u);
	
	/** Get Write->Read dependences. */
	public Collection<Unit> getWRDependences(SootMethod m, Unit u);
	
	/** Get Read->Write dependences. */
	public Collection<Unit> getRWDependences(SootMethod m, Unit u);
	
	/** Get Write->Write dependences. */
	public Collection<Unit> getWWDependences(SootMethod m, Unit u);
	
	public void release(SootMethod m);
}
