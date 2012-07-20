package test.cases;


/**
 * Test strong update for heap locations
 */
public class StrongUpdate {
	public static void main(String[] args) {
		test_kill_2();
		test_kill_3();
		test_kill_4();
		test_kill_5();
		test_kill_6();
		test_kill_7();
		test_kill_8();
		test_interproc_kill_1();
		test_interproc_kill_2();
		test_interproc_kill_3();
		test_interproc_kill_4();
	}

	public static class Node {
		public int _index;
		public Node _next;

		public void setNode() {
			_index = 0;
		}

		public void kill(Node p, Node q, Node s) {
			p._next = null;
			q._index = 0;
			s._next._index = 2;
		}
	}

	static class SpecialNode extends Node {
		public void setNode() {
			_next = null;
		}

		public void kill(Node p, Node q, Node s) {
			p._next = null;
			s._index = 2;
		}
	}

	private static void test_kill_2() {
		Node i, j, k;
		i = new Node();
		i._index = 0;
		i._index = 2;
		int id = i._index;
	}

	private static void test_kill_3() {
		Node i, j, k;
		i = new Node();
		i._index = 0;
		j = i;

		i = null;

		j._index = 2;
		int id = j._index;
	}

	private static void test_kill_4() {
		Node[] array = new Node[10];
		for (int i = 0; i < 10; i++) {
			array[i] = new Node();
		}

		for (int i = 0; i < array.length; i++) {
			Node cur = array[i];
			cur._index = 1;
			cur._index = 2;
			int k = cur._index;
		}
	}

	private static void test_kill_5() {
		Node head = new Node();
		head._next = null;
		for (int i = 0; i < 10; i++) {
			Node tmp = new Node();
			tmp._next = head;
			head = tmp;
		}

		while (head != null) {
			head._index = 1;
			if (head != null) {
				head._index = 2;
				int i = head._index;
			}
		}
	}

	private static void test_kill_6() {
		int x = 0, y = 1;
		Node p = null;
		if (x > y) {
			p = new Node();
			p._index = -1;
		} else {
			p = new Node();
			p._index = 0;
		}

		p._index = x + y;
		x = p._index;
		Node q = p;
		p = null;
		if (x > 0) {
			q._index = q._index + 1;
			y = q._index;
		}
		Node t = p;
	}
	
	private static void test_kill_7() {
		Node[] array = new Node[10];
		Node s = null;
		for (int i = 0; i < array.length; i++) {
			s = new Node();
			s._index = i;
			array[i] = s;
		}
		s._index = 0;
		int x = s._index;
	}
	
	private static void test_kill_8() {
		Node head = new Node();
		head._next = null;
		for (int i = 0; i < 10; i++) {
			Node tmp = new Node();
			tmp._next = head;
			head = tmp;
		}

		int j = 0;
		while (j < 10) {
			head._next = new Node();
			j++;
		}

		head._next = null;
		head = head._next;
	} 

    public static void test_kill_9(){
     	Node head=new Node();
     	head._next=null;
     	for(int i=0;i<10;i++){
     		Node tmp=new Node();
     		tmp._next=head;
     		head=tmp;
     	}
      
         while(head!=null){
            head._next = null;
            if(head!=null){
               head._next = null;     //a strong update to 163
               Node i=head._next;
            }
         }     
    }
    
	private static void clear(Node p) {
		p._next = null;
	} 

	private static void test_interproc_kill_1() {
		Node p = new Node();
		p._next = p;
		clear(p);
		Node q = p._next;
	}

	// kill with virtual invoke
	private static void test_interproc_kill_2() {
		Node p = new Node(), q = p;
		p._next = p;
		if (p != null) {
			p = ret_clear(p);
			q = p._next;
		}
		q = p;
	}

	private static Node ret_clear(Node p) {
		p._next = null;
		return new Node();
	}

	// kill in the presence of complex control structure
	private static void test_interproc_kill_3() {
		Node n1 = new Node();
		Node n2 = new Node();
		Node n3 = new Node();
		n3._next = n3;

		n1._next = n1;
		n2._index = 0;
		n3._index = 5;
		reset(n1, n2, n3);
		Node q = n1._next;
		int x = n2._index;
		int y = n3._index;
	}

	private static void reset(Node n1, Node n2, Node n3) {
		if (n1 != null) {
			n1._next = null;
			n2._index = 1;
			n3._next._index = 2;
		} else {
			n1._next = n2;
			n3._index = 3;
		}
	}

	// kill in the presence of virtual method call
	private static void test_interproc_kill_4() {
		Node receiver = null;
		if (receiver == null) {
			receiver = new Node();
		} else {
			receiver = new SpecialNode();
		}

		Node n1 = new Node();
		Node n2 = new Node();
		Node n3 = new Node();
		n3._next = n3;

		n1._next = n1;
		n2._index = 0;
		n3._index = 5;
		receiver.kill(n1, n2, n3);
		Node q = n1._next;
		int x = n2._index;
		int y = n3._index;
	}
}
