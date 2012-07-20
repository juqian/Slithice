package jqian.sootex.ptsto;

import java.util.*;

import jqian.sootex.location.*;
import soot.SootMethod;
import soot.Unit;

/**
 * A standard interface for query points-to information.
 */
public interface IPtsToQuery {
    public Set<InstanceObject> getPointTos(SootMethod m, Unit stmt, Location ptr);
}
