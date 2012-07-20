package test.cases;

import java.lang.reflect.Method;

/** 
 * Test PDG construction. 
 */
public class PDG {
	public static void main(String[] args) {
		test_reflection();

		Node n2 = new Node();
		test_param_passing_callee(1, n2);
		test_param_passing();
	}
	
	static int s;

	static class Node {
		int f;
	}

	static void test_reflection() {
		try {
			int i = 0, j = 1;
			Class<?> c = Class.forName("java.lang.String");
			Method[] methods = c.getMethods();

			System.out.println(methods.toString());
			i = i + j;

			if (i > 0) {
				Method m = c.getDeclaredMethod("toString");
				m.setAccessible(true);

				Object str = c.newInstance();
				m.invoke(str);

				i = i - 1;
			}

			System.out.println(i);
		} catch (Exception e) {
		}
	}
	

	// Test construction of formals and actuals
	static int test_param_passing_callee(int i, Node n) {
		n.f = n.f + 1;

		int[] arr = new int[10];
		arr[1] = arr[0];

		s = s + 2;

		return i;
	}

	static void test_param_passing() {
		Node n1 = new Node();
		test_param_passing_callee(0, n1);
	}
}
