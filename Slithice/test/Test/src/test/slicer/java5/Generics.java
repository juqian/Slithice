package test.slicer.java5;

public class Generics {
	//definition of generic type	
	static class Node<DataType> {
		int index;
		DataType data;
	}

	//test generics
	static void generic() {
		Node<Integer> n = new Node<Integer>();
		int y = 0;
		n.data = new Integer(1);

		int x = n.data;
		x = y + 1;
		x++;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		generic();
	}

}
