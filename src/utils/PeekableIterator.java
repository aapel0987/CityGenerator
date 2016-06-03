package utils;

import java.util.List;
import java.util.ListIterator;

public class PeekableIterator<T> implements ListIterator<T> {

	private T next;
	private ListIterator<T> iterator;
	
	public PeekableIterator(List<T> list){
		iterator = list.listIterator();
		if(iterator.hasNext()) next = iterator.next();
		else next = null;
	}
	
	public T peek(){
		return next;
	}
	
	@Override
	public void add(T arg0) {
		iterator.add(arg0);
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public boolean hasPrevious() {
		return iterator.hasPrevious();
	}

	@Override
	public T next() {
		T current = next;
		next = null;
		if(iterator.hasNext()) next = iterator.next();
		return current;
	}

	@Override
	public int nextIndex() {
		return iterator.nextIndex();
	}

	@Override
	public T previous() {
		throw new UnsupportedOperationException("previous() is not implemented by PeekableIterator.");
	}

	@Override
	public int previousIndex() {
		throw new UnsupportedOperationException("previousIndex() is not implemented by PeekableIterator.");
	}

	@Override
	public void remove() {
		iterator.previous();
		iterator.previous();
		iterator.remove();
		next();
	}

	@Override
	public void set(T arg0) {
		throw new UnsupportedOperationException("set() is not implemented by PeekableIterator.");
	}

}
