package com.crawljax.core;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CrawlQueue.java
 * @author Jae-Hwan Jung
 *
 */
public class CrawlQueueTest {
	
	CrawlQueue crawlQueue;
	CyclicBarrier cb;
	MyRunnable runnable1;
	MyRunnable runnable2;
	MyRunnable runnable3;
	Boolean isExceptionThrown;
	Boolean isUnexpectedExceptionThrown;
	
	@Before
	public void setUp() throws Exception {
		crawlQueue = new CrawlQueue(3);
		cb = new CyclicBarrier(4);
		runnable1 = new MyRunnable("Runnable1");
		runnable2 = new MyRunnable("Runnable2");
		runnable3 = new MyRunnable("Runnable3");
		isExceptionThrown=false;
		isUnexpectedExceptionThrown = false;
	}

	@Test (expected = IllegalStateException.class)
	public void testAddAndElementAndPeek() throws InterruptedException, BrokenBarrierException {
		
		Thread t1 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					cb.await();
					crawlQueue.add(runnable1);
				} catch (BrokenBarrierException | InterruptedException e) {
					isUnexpectedExceptionThrown = true;
					e.printStackTrace();
				}
			}			
		});
		
		Thread t2 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					cb.await();
					crawlQueue.add(runnable2);
				} catch (BrokenBarrierException | InterruptedException e) {
					isUnexpectedExceptionThrown = true;
					e.printStackTrace();
				}
			}			
		});
		
		Thread t3 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					cb.await();
					crawlQueue.add(runnable3);
				} catch (BrokenBarrierException | InterruptedException e) {
					isUnexpectedExceptionThrown = true;
					e.printStackTrace();
				}
			}			
		});
		
		Runnable r = crawlQueue.peek();
		assertTrue(r==null);
		try {
			r = crawlQueue.element();
			fail("Exception expected");
		} catch (NoSuchElementException e) {
			assertTrue(isUnexpectedExceptionThrown == false);
			assertTrue(r == null);
		}
		
		
		t1.start();
		t2.start();
		t3.start();		
		cb.await();
		
		t1.join();
		t2.join();
		t3.join();
		
		assertTrue(crawlQueue.size()==3);
		crawlQueue.add(new MyRunnable("Runnable4"));
		
	}
	
	@Test
	public void testPoll() {
		
		assertTrue(crawlQueue.poll()==null);		
		addRunnablesToQueue();
		
		try {
			Thread t1 = new Thread(new PollRunnable());
			Thread t2 = new Thread(new PollRunnable());
			Thread t3 = new Thread(new PollRunnable());
			Thread t4 = new Thread(new PollRunnable());
			t1.start();
			t2.start();
			t3.start();
			t4.start();
			t1.join();
			t2.join();
			t3.join();
			t4.join();
			assertTrue(crawlQueue.size()==0);
			assertTrue(isUnexpectedExceptionThrown==false);
		} catch (Exception ex) {
			fail("Exception not expected");
		}
		
	}
	
	/**
	 * isEmpty()
	 */
	@Test
	public void testRemove() {
	
		try {
			crawlQueue.remove();
			fail("Exception expected");
		} catch (NoSuchElementException e) {
			assertTrue(true);
		}
		
		addRunnablesToQueue();
		
		assertFalse(crawlQueue.isEmpty());
		
		Thread t1 = new Thread(new RemoveRunnable());
		Thread t2 = new Thread(new RemoveRunnable());
		Thread t3 = new Thread(new RemoveRunnable());
		Thread t4 = new Thread(new RemoveRunnable());
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		try {
			t1.join();
			t2.join();
			t3.join();
			t4.join();
		} catch (InterruptedException e) {
			fail("exception not expected");
			e.printStackTrace();
		}
		assertTrue(crawlQueue.size()==0);
		assertTrue(crawlQueue.isEmpty());
		assertTrue(isExceptionThrown==true);
		assertTrue(isUnexpectedExceptionThrown==false);
	}

	/**
	 * Just a simple test to test basic functionality of 
	 * addAll(), containsAll(), contain() and iterator()
	 */
	@Test
	public void testAddAllAndContainsAll() {
		
		addRunnablesToQueue();

		CrawlQueue queue = new CrawlQueue();
		queue.addAll(crawlQueue);
		
		for (Iterator<Runnable> i=crawlQueue.iterator(); i.hasNext(); ) {
			crawlQueue.contains(i.next());
		}
		
		assertTrue(queue.containsAll(crawlQueue));
	}
	
	/**
	 * Just a simple test for crawlQueue.clear()
	 */
	@Test
	public void testClear() {
		
		Thread t1 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					cb.await();
					crawlQueue.add(runnable1);
				} catch (BrokenBarrierException | InterruptedException e) {
					isUnexpectedExceptionThrown = true;
					e.printStackTrace();
				}
			}			
		});
		
		Thread t2 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					cb.await();
					crawlQueue.add(runnable2);
				} catch (BrokenBarrierException | InterruptedException e) {
					isUnexpectedExceptionThrown = true;
					e.printStackTrace();
				}
			}			
		});
		
		Thread t3 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					cb.await();
					crawlQueue.add(runnable3);
				} catch (BrokenBarrierException | InterruptedException e) {
					isUnexpectedExceptionThrown = true;
					e.printStackTrace();
				}
			}			
		});
		
		Thread t4 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					cb.await();
					crawlQueue.clear();
				} catch (BrokenBarrierException | InterruptedException e) {
					isUnexpectedExceptionThrown = true;
					e.printStackTrace();
				}
			}			
		});
			
		t1.start();
		t2.start();
		t3.start();		
		t4.start();		
		
		try {
			t1.join();
			t2.join();
			t3.join();
			t4.join();
		} catch (InterruptedException e) {
			fail("Exception not expected");
			e.printStackTrace();
		}
		
		assertTrue((crawlQueue.size()==0 && crawlQueue.peek()==null)
				|| (crawlQueue.size()!=0 && crawlQueue.peek()!=null));
	}
	
	@Test
	public void testRemoveAll() {
		addRunnablesToQueue();
		CrawlQueue queue = new CrawlQueue();
		queue.add(runnable3);
		queue.add(runnable1);
		crawlQueue.removeAll(queue);
		assertTrue(crawlQueue.size()==1 && crawlQueue.peek()==runnable2);
	}
	
	@Test
	public void testRetainAll() {
		addRunnablesToQueue();
		CrawlQueue queue = new CrawlQueue();
		queue.add(runnable3);
		queue.add(runnable1);
		crawlQueue.retainAll(queue);
		assertTrue(crawlQueue.size()==2 && 
				!crawlQueue.contains(runnable2));
	}
	
	//TODO
	
	private void addRunnablesToQueue() {
		crawlQueue.add(runnable1);
		crawlQueue.add(runnable2);
		crawlQueue.add(runnable3);
	}
	
	/**
	 * Custom Runnable to execute CrawlQueue.removew()
	 */
	private class RemoveRunnable implements Runnable {

		@Override
		public void run() {
			try {
				cb.await();
				crawlQueue.remove();
			} catch (NoSuchElementException e) {
				isExceptionThrown = true;
			} catch (BrokenBarrierException | InterruptedException e) {
				isUnexpectedExceptionThrown = true;
				e.printStackTrace();
			} 
		}
		
	}
	
	/**
	 * Custom Runnable to execute CrawlQueue.poll()
	 */
	private class PollRunnable implements Runnable {

		@Override
		public void run() {
			try {
				cb.await();
				crawlQueue.poll();
			} catch (BrokenBarrierException | InterruptedException e) {
				isUnexpectedExceptionThrown = true;
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Custom Runnable to be used for CrawlQueue
	 */
	private class MyRunnable implements Runnable {

		private String name;

		public MyRunnable(String n) {
			name = n;
		}

		@Override
		public void run() {
			// Do Nothing
		}

		@Override
		public String toString() {
			return name;
		}

	}
}
