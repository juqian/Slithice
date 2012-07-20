package test.slicer.java4;

public class WithNonStaticNestedClass {
	public interface IAdd{
		public int add(int x, int y);
	}	
	
	int CONST = 1;
	
	//use final environment value
	int add(final int x,final int y){
		IAdd adder = new IAdd(){
			public int add(int m,int n){
				int z = x + y;
				return z;
			}			
		};
		
		int m = adder.add(x, y);
		return m;
	}
	
	//named non static nested, use super class field
	class Incr{
		int increment(int z){
			return add(z, CONST);
		}
	}
	
	//more than one nested level
	public class B{
		public class C{
			int A(int x, int y){
			    x = add(x, y);
			    
			    Incr inc = new Incr();
			    y = inc.increment(y);
			    y = y + CONST;
			    
			    return x;
			}
		}
		
		int test1(){		 
			C c = new C();
			int sum = 0;
			int i = 1;
			while(i < 11){
				sum = c.A(sum, i);
				i = i + 1;
			}
			
			return sum + i;
		}
	}	
		 
	void start(){
		B b = new B();
		b.test1();
	}
	
	
	public static void main(String[] args) {
		WithNonStaticNestedClass cls = new WithNonStaticNestedClass();
		cls.start();
	}
	
	
}
