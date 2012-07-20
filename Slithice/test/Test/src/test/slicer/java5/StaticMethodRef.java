package test.slicer.java5;

import static java.lang.Math.random;

public class StaticMethodRef {
    static double staticMethodRef(){
        double x = random();
        return x;
    }
    
	public static void main(String[] args){
		staticMethodRef();
	}
		
}
