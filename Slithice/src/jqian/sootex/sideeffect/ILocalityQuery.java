package jqian.sootex.sideeffect;

import soot.Local;
import soot.SootMethod;

public interface ILocalityQuery {
	public boolean isRefTgtLocal(SootMethod m, Local v);
	public boolean isRefTgtFresh(SootMethod m, Local v);
}
