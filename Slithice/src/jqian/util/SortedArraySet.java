package jqian.util;

import java.util.*;

/**
 * @author bruteforce
 * A compact set representation. Currently this set is read only.
 */
public class SortedArraySet<E> extends AbstractSet<E> {
	private static final int DEFAULT_SIZE = 8;
	
	private Object[] _elements;
	private Comparator<Object> _comparator;

	@SuppressWarnings("unchecked")
	public SortedArraySet(Collection<E> numberables,Comparator<E> comparator) {
		_elements = numberables.toArray();
		_comparator = (Comparator<Object>)comparator;
		Arrays.sort(_elements,_comparator);
	}

	@SuppressWarnings("unchecked")
	public SortedArraySet(int size, Comparator<E> comparator){
	    _elements = new Object[size];
	    _comparator = (Comparator<Object>)comparator;
	}

	public SortedArraySet(Comparator<E> comparator){
	    this(DEFAULT_SIZE,comparator);
	}
	  	    
	public boolean isEmpty() {
		return _elements.length == 0;
	}

	public boolean contains(Object o) {
		int id = Arrays.binarySearch(_elements, o,_comparator);
		return id >= 0;
	}

	public boolean containsAll(Collection<?> c) {
		for (Iterator<?> it = c.iterator(); it.hasNext();) {
			Object o = it.next();
			if (!contains(o)) {
				return false;
			}
		}

		return true;
	}

	public int size() {
		return _elements.length;
	}

	public Object[] toArray() {
		Object[] array = new Object[_elements.length];
		System.arraycopy(_elements, 0, array, 0, _elements.length);
		return array;
	}


	public boolean equals(Object o) {
		if (o.getClass() != this.getClass())
			return false;

		SortedArraySet<?> that = (SortedArraySet<?>) o;
		if (that._elements.length != this._elements.length)
			return false;

		int length = _elements.length;
		for (int i = 0; i < length; i++) {
			if (!this._elements[i].equals(that._elements[i]))
				return false;
		}

		return true;
	}

	private class ArrayIterator implements Iterator<E> {
		public boolean hasNext() {
			return _index < _elements.length;
		}

		@SuppressWarnings("unchecked")
		public E next() {
			if(_index >= _elements.length)
	            throw new NoSuchElementException();
			  
			E o = (E)_elements[_index];
			_index++;
			return o;
		}

		public void remove() {
			throw new RuntimeException("Not implemented");
		}

		int _index;
	}

	public Iterator<E> iterator() {
		return new ArrayIterator();
	}

	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append('[');
		
		int size = _elements.length;
		for(int i=0;i<size;i++){
			Object elmt = _elements[i];
			buf.append(elmt.toString());
			
			if(i<size-1){
				buf.append(", ");
			}
		}	
		
		buf.append(']');
		return buf.toString();
	}
	

	public void clear() {
		for(int i=0; i<_elements.length; i++){
			_elements[i] = null;
		}		
	}

	public boolean remove(Object o) {
		throw new RuntimeException("Method not implemented");
	} 

    public boolean add(E e){
        throw new RuntimeException("Do not support element by element add, this would be slow");
    } 

    protected void doubleCapacity(){
        int newSize = _elements.length * 2;
        Object[] newElements = new Object[newSize];
        System.arraycopy(_elements, 0, newElements, 0, _elements.length);
        _elements = newElements;
    }
}


