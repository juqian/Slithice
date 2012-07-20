package jqian.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** Holding small number of elements. Compact representation, no delete support. */
class ArraySet extends AbstractSet<Object>{
	static int increase;
	
    private int numElements;	   
    private Object[] elements;

    public ArraySet( int size ){	 
        elements = new Object[size];
        numElements = 0;
    }

    final public void clear(){
        numElements = 0;
    }

    final public boolean contains(Object obj){
        for(int i = 0; i < numElements; i++)
            if(elements[i].equals(obj))
                return true;

        return false;
    }

    final public boolean add(Object e){	        
        if(contains(e))
            return false;
         
        if(numElements == elements.length)
           incrCapacity();
        
        elements[numElements++] = e;
        return true;	        
    }

	final public boolean addAll(Collection<?> s) {
        boolean ret = false;
        if( !(s instanceof ArraySet) ) 
        	return super.addAll(s);
        
        ArraySet as = (ArraySet) s;
        int size = as.numElements;
        Object[] asElmts = as.elements;
        for(int i=0; i<size; i++){
        	ret = add(asElmts[i]) | ret;
        }
			
        return ret;
    }

    final public int size(){
        return numElements;
    }

    final public Iterator<Object> iterator(){
        return new ArrayIterator<Object>();
    }

    private class ArrayIterator<V> implements Iterator<V>{
        int nextIndex;

        final public boolean hasNext(){
            return nextIndex < numElements;
        }
     
		@SuppressWarnings("unchecked")
		final public V next() throws NoSuchElementException{
            if(!(nextIndex < numElements))
                throw new NoSuchElementException();

            return (V) elements[nextIndex++];
        }

        final public void remove() throws NoSuchElementException{
           throw new RuntimeException();
        }
    }

    final private void incrCapacity(){
    	increase++;
    	
        int newSize = elements.length + 5;
        Object[] newElements = new Object[newSize];
        System.arraycopy(elements, 0, newElements, 0, numElements);
        elements = newElements;	         
    }

    final public Object[] toArray(){
        Object[] array = new Object[numElements];

        System.arraycopy(elements, 0, array, 0, numElements);
        return array;
    }

    @SuppressWarnings("unchecked")
	final public Object[] toArray(Object[] array ){
        System.arraycopy(elements, 0, array, 0, numElements);
        return array;
    }
}