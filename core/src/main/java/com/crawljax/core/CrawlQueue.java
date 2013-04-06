/**
 * 
 */
package com.crawljax.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class implements a BlockingQueue with a LinkedList as its data abstraction rep
 * instead of extending Stack, making it easy to change implementation between FIFO and FILO.
 * Since there's no random access involved, speed is the same as using Stack. This class is used 
 * in the ThreadPoolExecutor and processes elements in FILO (first-in-last-out). This is now also thread-safe. 
 * Lastly, this class can be either bounded or unbounded(default).
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl> (original implementation)</br>
 * @author Jae-Hwan Jung <jaehwan.jeff.jung@gmail.com> (Major Overhaul on March 23, 2013)
 * @since March 23, 2013  
 */
public class CrawlQueue implements BlockingQueue<Runnable> {
	
	/**
	 * Data Abstraction Rep
	 */
	LinkedList<Runnable> runnables; 
	
	/**
	 * Capacity of this BlockingQueue.	
	 */
	private final int capacity;
	
	/**
	 * Number of items in the Queue
	 */
	private AtomicInteger count = new AtomicInteger();
	
	/**
	 * Simple reentrant lock
	 */
	private final ReentrantLock lock = new ReentrantLock();
	
	/**
	 * Condition for put()
	 */
	private final Condition isNotFull = lock.newCondition() ;
	
	/**
	 * Condition for take()
	 */
	private final Condition isNotEmpty = lock.newCondition();
	
	/**
	 * Default constructor making this BlockingQueue unbounded
	 */
	public CrawlQueue() {
		super();
		this.capacity = Integer.MAX_VALUE;
		runnables = new LinkedList<Runnable>();
	}
	
	/**
	 * Constructor making this BlockingQueue bounded
	 * @param capacity The maximum number of elements.
	 */
	public CrawlQueue(int capacity) {
		super();
		this.capacity = capacity;
		runnables = new LinkedList<Runnable>();
	}

	@Override
	public Runnable element() {
		this.lock.lock();
		try {
			if (count.get() <= 0)
				throw new NoSuchElementException();
			else 
				return runnables.get(count.get()-1);
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public Runnable peek() {
		this.lock.lock();
		try {
			if (count.get() <= 0)
				return null;
			else
				return runnables.get(count.get()-1);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Runnable poll() {
		this.lock.lock();
		try {
			if (count.get() <= 0)
				return null;
			else 
				return removeAt(count.get()-1);
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public Runnable remove() {		
		this.lock.lock();
		try {
			if (count.get() <= 0)
				throw new NoSuchElementException();
			else
				return removeAt(count.get()-1);
		} finally {
			this.lock.unlock();
		}
	}
	
	private Runnable removeAt(int index) {
		Runnable result = runnables.remove(index);
		count.decrementAndGet();
		isNotFull.signal();
		return result;
	}

	@Override
	public boolean addAll(Collection<? extends Runnable> c) {
		this.lock.lock();
		try {			
			if (runnables.addAll(c)) {
				count.set(runnables.size());
				isNotEmpty.signal();
				return true;
			}
			else
				return false;
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public void clear() {
		this.lock.lock();
		try {
			runnables.clear();
			count.set(0);
			isNotFull.signal();
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		this.lock.lock();
		try {
			return runnables.containsAll(c);
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public boolean isEmpty() {		
		this.lock.lock();
		try {
			return count.get()<=0 ? true : false;
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public Iterator<Runnable> iterator() {
		this.lock.lock();
		try {
			return runnables.iterator();
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		this.lock.lock();
		try {
			if (runnables.removeAll(c)) {
				count.set(runnables.size());
				isNotFull.signal();
				return true;
			}
			else
				return false;
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		this.lock.lock();
		try {
			if (runnables.retainAll(c)) {
				count.set(runnables.size());
				isNotFull.signal();
				return true;
			}
			else
				return false;
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public int size() {
		return count.get();
	}

	@Override
	public Object[] toArray() {
		this.lock.lock();
		try {
			return runnables.toArray();
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public <T> T[] toArray(T[] a) {
		this.lock.lock();
		try {
			return runnables.toArray(a);
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public boolean add(Runnable e) {
		this.lock.lock();
		try {
			if (count.get() < capacity)
				return addOneItem(e);
			else
				throw new IllegalStateException();
		} finally {
			this.lock.unlock();
		}
	}

	private boolean addOneItem(Runnable e) {
		if (runnables.add(e)) {
			count.incrementAndGet();
			isNotEmpty.signal();
			return true;
		}
		else
			return false;
	}
	
	@Override
	public boolean contains(Object o) {
		this.lock.lock();
		try {
			return runnables.contains(o);
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public int drainTo(Collection<? super Runnable> c) {
		this.lock.lock();
		try {
			c.addAll(runnables);
			runnables.clear();
			int cnt =count.get();
			count.set(0);
			isNotFull.signal();
			return cnt;
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public int drainTo(Collection<? super Runnable> c, int maxElements) {
		this.lock.lock();
		try {
			int cnt;			
			if (maxElements > capacity)
				cnt = capacity;
			else if (maxElements > count.get())
				cnt = count.get();
			else 
				cnt = maxElements;
			
			for (int i=0; i<cnt; i++) {
				c.add(runnables.remove(count.get()-1));
				count.decrementAndGet();				
			}
			isNotFull.signal();
			return cnt;			
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public boolean offer(Runnable e) {
		this.lock.lock();
		try {
			if (count.get() < capacity)
				return addOneItem(e);
			else
				return false;
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public boolean offer(Runnable e, long timeout, TimeUnit unit)
			throws InterruptedException {
		this.lock.lock();
		try {
			if (count.get() < capacity) {				
				return addOneItem(e);
			}
			else {
				if (timeout <= 0)
					return false;
				try {
					if (isNotFull.awaitNanos(unit.toNanos(timeout)) <=0 ) {
						isNotFull.signal();
						return false;
					}
					else 
						return addOneItem(e);
				} catch (InterruptedException ie) {
					isNotFull.signal();
					throw ie;
				}
			}
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public Runnable poll(long timeout, TimeUnit unit)
			throws InterruptedException {
		this.lock.lock();
		try {
			if (count.get() > 0) 				
				return removeAt(count.get()-1);
			else {
				if (timeout <= 0)
					return null;
				try {
					if (isNotEmpty.awaitNanos(unit.toNanos(timeout))<= 0) {
						isNotEmpty.signal();
						return null;
					}
					else
						return removeAt(count.get()-1);	
				}  catch (InterruptedException ie) {
					isNotEmpty.signal();
					throw ie;
				}
			} 
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public void put(Runnable e) throws InterruptedException {
		this.lock.lock();
		try {
			if (count.get() < capacity) {
				addOneItem(e);
			}
			else {
				try {
					while (!(count.get() < capacity))
						isNotFull.await();
					addOneItem(e);
				} catch (InterruptedException ie) {
					isNotFull.signal();
					throw ie;
				}
			}
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public int remainingCapacity() {
		this.lock.lock();
		try {
			return capacity - count.get();
		} finally {
			this.lock.unlock();
		}		
	}

	@Override
	public boolean remove(Object o) {		
		this.lock.lock();
		try {
			if (count.get() == 0) 
				return false;
			else {
				Boolean result = runnables.remove(o);
				count.decrementAndGet();
				isNotFull.signal();
				return result;
			}
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public Runnable take() throws InterruptedException {
		this.lock.lock();
		try {
			if (count.get() > 0)
				return removeAt(count.get()-1);				
			else {
				try {
					while (!(count.get() > 0))
						isNotEmpty.await();
					return removeAt(count.get()-1);					
				}  catch (InterruptedException ie) {	
					isNotEmpty.signal();
					throw ie;
				}
			} 
		} finally {
			this.lock.unlock();
		}
	}
}
