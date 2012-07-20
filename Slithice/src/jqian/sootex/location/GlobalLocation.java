package jqian.sootex.location;

import soot.SootField;
import soot.Type;

public class GlobalLocation extends Location{
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
    
    protected SootField _field; 
}
