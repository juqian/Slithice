package jqian.sootex.du;

import soot.*;

public interface IGlobalDUQuery {
    public IReachingDUQuery getRDQuery(MethodOrMethodContext method);
    public IReachingDUQuery getRUQuery(MethodOrMethodContext method);
    
    public void releaseQuery(MethodOrMethodContext method);
}
