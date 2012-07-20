package test.slicer.java4;

import java.lang.reflect.*;

/** To test whether Pyxis Slicer can process reflections. */
public class WithReflection {
	public static void main(String args[]){
		try{
			int i = 0, j = 1;
			Class c = Class.forName("java.lang.String");
			Method[] methods = c.getMethods();
			
			System.out.println(methods.toString());
			i = i + j;			
			
			if(i>0){
				Method m = c.getDeclaredMethod("toString");		
				m.setAccessible(true);
			
				Object str = c.newInstance();			
				m.invoke(str);
				
				i = i - 1;
			}
			
			System.out.println(i);
		}
		catch(Exception e){
			
		}
	}
}
