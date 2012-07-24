package jqian.sootex.location;

import soot.SootField;
import soot.Type;

/** Model a static class field */
public class GlobalLocation extends Location{
    protected final SootField _field; 
    
    GlobalLocation(SootField field){
        this._field=field;
    }
    
    public SootField getSootField(){
        return _field;
    }
    
    public String toString(){
        return _field.getDeclaringClass().getShortName()+"."+_field.getName();
    } 
    
    public Type getType(){
        return _field.getType();
    }
}
