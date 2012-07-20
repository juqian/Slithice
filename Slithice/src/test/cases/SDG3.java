package test.cases;

/**
 * A test case interact with various classes
 * Mainly for the testing of SDG construction.
 */
public class SDG3{
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
		
		void start(){		 
			C c = new C();
			int sum = 0;
			int i = 1;
			while(i < 11){
				sum = c.A(sum, i);
				i = i + 1;
			}
			
			int x = sum + i;
			x++;
		}
	}	
		 
	void test1(){
		B b = new B();
		b.start();
	}
	
	
	public static void main(String[] args) {
		SDG3 cls = new SDG3();
		cls.test1();
	}
}
