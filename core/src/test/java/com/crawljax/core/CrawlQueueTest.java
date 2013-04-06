package com.crawljax.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CrawlQueue.java
 * @author Jae-Hwan Jung
 * @since 2013, April 
 */
public class CrawlQueueTest {
	
	CrawlQueue crawlQueue;
	
	/**
	 * Concurrency support class to run multiple threads at the same time.
	 * Default value is 4. When the 4th threads calls CyclicBarrier.await(), it starts all awaiting threads.
	 */
	CyclicBarrier cb;
	
	MyRunnable runnable1;
	MyRunnable runnable2;
	MyRunnable runnable3;
	
	/**
	 * Use to store values from other threads
	 */
	Boolean isExceptionThrown;
	Boolean isUnexpectedExceptionThrown;
	int counter;
	
	@Before
	public void setUp() throws Exception {
		crawlQueue = new CrawlQueue(3);
		cb = new CyclicBarrier(4);
		runnable1 = new MyRunnable("Runnable1");
		runnable2 = new MyRunnable("Runnable2");
		runnable3 = new MyRunnable("Runnable3");
		isExceptionThrown=false;
		isUnexpectedExceptionThrown = false;
		counter = 0;
	}

	/**
	 * Starts four threads at the same time to run CrawlQueue.add().
	 * Tests CrawlQueue.peek() & element() additionally.
	 * Only three elements are expected to added.
	 * IllegalStateException is expected to be thrown.
	 */
	@Test 
	public void testAddAndElementAndPeek() throws InterruptedException, BrokenBarrierException {
		
		Thread t1 = new Thread(new AddRunnable(runnable1));
		Thread t2 = new Thread(new AddRunnable(runnable2));		
		Thread t3 = new Thread(new AddRunnable(runnable3));
		Thread t4 = new Thread(new AddRunnable(new MyRunnable("Runnable4")));
		
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
		t4.start();
		
		t1.join();
		t2.join();
		t3.join();
		t4.join();
		
		assertTrue(crawlQueue.size()==3 && isExceptionThrown==true &&
				isUnexpectedExceptionThrown==false);
	}
	
	/**
	 * Starts four threads at the same time to run CrawlQueue.poll().
	 * The size should never be negative.
	 * No exception is expected to be thrown.
	 */
	@Test
	public void testPoll() {
		
		assertTrue(crawlQueue.poll()==null);		
		addRunnablesToQueue();
		
		try {
			Thread t1 = new Thread(new PollRunnable(0));
			Thread t2 = new Thread(new PollRunnable(0));
			Thread t3 = new Thread(new PollRunnable(0));
			Thread t4 = new Thread(new PollRunnable(0));
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
			assertTrue(isExceptionThrown==false);
		} catch (Exception ex) {
			fail("Exception not expected");
		}
		
	}
	
	/**
	 * Starts four threads at the same time to run CrawlQueue.remove().
	 * CrawlQueue.isEmpty() is tested additionally.
	 * The size should never be negative.
	 * No exception is expected to be thrown.
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
		assertTrue(isExceptionThrown==false && isUnexpectedExceptionThrown==false);
	}
	
	/**
	 * Just a simple test for crawlQueue.clear()
	 */
	@Test
	public void testClear() {
		
		Thread t1 = new Thread(new AddRunnable(runnable1));
		Thread t2 = new Thread(new AddRunnable(runnable2));		
		Thread t3 = new Thread(new AddRunnable(runnable3));		
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
		assertTrue(isExceptionThrown==false && isUnexpectedExceptionThrown==false);
	}
	
	/**
	 * Simple test for CrawlQueue.removeAll()
	 */
	@Test
	public void testRemoveAll() {
		addRunnablesToQueue();
		CrawlQueue queue = new CrawlQueue();
		queue.add(runnable3);
		queue.add(runnable1);
		crawlQueue.removeAll(queue);
		assertTrue(crawlQueue.size()==1 && crawlQueue.peek()==runnable2);
		assertTrue(isExceptionThrown==false && isUnexpectedExceptionThrown==false);
	}
	
	/**
	 * Simple test for CrawlQueue.retainAll()
	 */
	@Test
	public void testRetainAll() {
		addRunnablesToQueue();
		CrawlQueue queue = new CrawlQueue();
		queue.add(runnable3);
		queue.add(runnable1);
		crawlQueue.retainAll(queue);
		assertTrue(crawlQueue.size()==2 && 
				!crawlQueue.contains(runnable2));
		assertTrue(isExceptionThrown==false && isUnexpectedExceptionThrown==false);
	}
	
	/**
	 * Simple test for CrawlQueue.toArray() and toArray(T[])
	 */
	@Test
	public void testToArray() {
		addRunnablesToQueue();
		Object[] objArray = crawlQueue.toArray();
		assertTrue(objArray.length == crawlQueue.size());
		Boolean exists;
		for(Runnable r: crawlQueue) {
			exists = false;
			for (int i=0; i<objArray.length; i++) {
				if (objArray[i]==r)
					exists = true;
			}
			assertTrue(exists==true);
		}
		
		Object[] runnableArray = crawlQueue.toArray(new Runnable[crawlQueue.size()]);
		assertTrue(runnableArray.length == crawlQueue.size());
		for(Runnable r: crawlQueue) {
			exists = false;
			for (int i=0; i<runnableArray.length; i++) {
				if (runnableArray[i]==r)
					exists = true;
			}
			assertTrue(exists==true);
		}
		assertTrue(isExceptionThrown==false && isUnexpectedExceptionThrown==false);
	}
	
	/**
	 * Starts four threads at the same time to run CrawlQueue.drainTo() on 4 different lists.
	 * Only one of the lists should contain 3 elements and the rest should be empty.
	 * No exception is expected to be thrown.
	 */
	@Test
	public void testDrainTo() {
		addRunnablesToQueue();
		final List<Runnable> list1 = new ArrayList<Runnable>();
		final List<Runnable> list2 = new ArrayList<Runnable>();
		final List<Runnable> list3 = new ArrayList<Runnable>();
		final List<Runnable> list4 = new ArrayList<Runnable>();
		
		Thread t1 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					cb.await();
					crawlQueue.drainTo(list1);
				} catch (Exception e) {
					isUnexpectedExceptionThrown = true;
				}
			}			
		});
		
		Thread t2 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					cb.await();
					crawlQueue.drainTo(list2);
				} catch (Exception e) {
					isUnexpectedExceptionThrown = true;
				}
			}			
		});
		
		Thread t3 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					cb.await();
					crawlQueue.drainTo(list3);
				} catch (Exception e) {
					isUnexpectedExceptionThrown = true;
				}
			}			
		});
		
		Thread t4 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					cb.await();
					crawlQueue.drainTo(list4);
				} catch (Exception e) {
					isUnexpectedExceptionThrown = true;
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
		
		assertTrue(crawlQueue.size()==0 && 
				((list1.size()==3 && list2.size()==0 && list3.size()==0 && list4.size()==0) ||
						(list2.size()==3 && list1.size()==0 && list3.size()==0 && list4.size()==0) ||
						(list3.size()==3 && list1.size()==0 && list2.size()==0 && list4.size()==0) ||
						(list4.size()==3 && list1.size()==0 && list2.size()==0 && list3.size()==0)));
		assertTrue(isExceptionThrown==false && isUnexpectedExceptionThrown==false);
	}
	
	/**
	 * Starts four threads at the same time to run CrawlQueue.offer().
	 * Only three of the threads should successfully execute offer().
	 * No exception is expected to be thrown.
	 */
	@Test
	public void testOffer() {
		 
		Thread t1 = new Thread(new OfferRunnable(runnable1));		
		Thread t2 = new Thread(new OfferRunnable(runnable1));		
		Thread t3 = new Thread(new OfferRunnable(runnable1));		
		Thread t4 = new Thread(new OfferRunnable(new MyRunnable("Runnable4")));
			
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
		}
		
		assertTrue(crawlQueue.size()==3 && counter==3);
		assertTrue(isExceptionThrown==false && isUnexpectedExceptionThrown==false);
	}
	
	/**
	 * Starts four threads at the same time to run CrawlQueue.offer() with timeout.
	 * Only one thread should time out.
	 * No exception is expected to be thrown.
	 */
	@Test
	public void testOfferWithTimeoutFail() {
		
		Thread t1 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					cb.await();
					if (crawlQueue.offer(runnable1, 1000, TimeUnit.MILLISECONDS))
						counter++;
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
					if (crawlQueue.offer(runnable2, 1000, TimeUnit.MILLISECONDS))
						counter++;
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
					if (crawlQueue.offer(runnable3, 1000, TimeUnit.MILLISECONDS))
						counter++;
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
					if (crawlQueue.offer(new MyRunnable("Runnable4"), 1000, TimeUnit.MILLISECONDS))
						counter++;
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
		}
		
		assertTrue(crawlQueue.size()==3 && counter==3);
		assertTrue(isExceptionThrown==false && isUnexpectedExceptionThrown==false);
	}
	
	/**
	 * Starts four threads at the same time to run CrawlQueue.offer() with timeout.
	 * All threads should successfully execute before timeout.
	 * No exception is expected to be thrown.
	 */
	@Test
	public void testOfferWithTimeoutSuccess() {
		
		Thread t1 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					cb.await();
					if (crawlQueue.offer(runnable1, 1000, TimeUnit.MILLISECONDS))
						counter++;
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
					if (crawlQueue.offer(runnable2, 1000, TimeUnit.MILLISECONDS))
						counter++;
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
					if (crawlQueue.offer(runnable3, 1000, TimeUnit.MILLISECONDS))
						counter++;
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
					if (crawlQueue.offer(new MyRunnable("Runnable4"), 1000, TimeUnit.MILLISECONDS))
						counter++;
				} catch (BrokenBarrierException | InterruptedException e) {
					isUnexpectedExceptionThrown = true;
					e.printStackTrace();
				}
			}			
		});
		
		Thread t5 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					isUnexpectedExceptionThrown = true;
					e.printStackTrace();
				}
				crawlQueue.remove();
			}			
		});
			
		t1.start();
		t2.start();
		t3.start();		
		t4.start();		
		t5.start();
		
		try {
			t1.join();
			t2.join();
			t3.join();
			t4.join();
			t5.join();
		} catch (InterruptedException e) {
			fail("Exception not expected");
			e.printStackTrace();
		}
		
		assertTrue(crawlQueue.size()==3 && counter==4);
		assertTrue(isExceptionThrown==false && isUnexpectedExceptionThrown==false);
	}
	
	/**
	 * Starts four threads at the same time to run CrawlQueue.drainTo() with
	 * maximum number of elements parameter on 3 different lists.
	 * For the default setting, only one list should contain 2 elements,
	 * and one contain 1 element, and the rest should be empty.
	 * No exception is expected to be thrown.
	 */
	@Test
	public void testDrainMaxElementsTo() {
		addRunnablesToQueue();
		final List<Runnable> list1 = new ArrayList<Runnable>();
		final List<Runnable> list2 = new ArrayList<Runnable>();
		final List<Runnable> list3 = new ArrayList<Runnable>();
		
		Thread t1 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					cb.await();
					crawlQueue.drainTo(list1, 2);
				} catch (BrokenBarrierException | InterruptedException e) {
					isUnexpectedExceptionThrown = true;
				}
			}			
		});
		
		Thread t2 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					cb.await();
					crawlQueue.drainTo(list2, 2);
				} catch (BrokenBarrierException | InterruptedException e) {
					isUnexpectedExceptionThrown = true;
				}
			}			
		});
		
		Thread t3 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					cb.await();
					crawlQueue.drainTo(list3, 2);
				} catch (BrokenBarrierException | InterruptedException e) {
					isUnexpectedExceptionThrown = true;
				}
			}			
		});
							
		t1.start();
		t2.start();
		t3.start();		
		
		try {
			cb.await();
			t1.join();
			t2.join();
			t3.join();
		} catch (InterruptedException | BrokenBarrierException e) {
			fail("Exception not expected");
			e.printStackTrace();
		}
		
		assertTrue(crawlQueue.size()==0 && 
				((list1.size()==2 && list2.size()==1 && list3.size()==0) ||
						(list1.size()==2 && list2.size()==0 && list3.size()==1) ||
						(list2.size()==2 && list1.size()==1 && list3.size()==0) ||
						(list2.size()==2 && list1.size()==0 && list3.size()==1) ||
						(list3.size()==2 && list1.size()==1 && list2.size()==0) ||
						(list3.size()==2 && list1.size()==0 && list2.size()==1)));
		assertTrue(isExceptionThrown==false && isUnexpectedExceptionThrown==false);
	}
	
	/**
	 * Test for CrawlQueue.poll()
	 * 4 threads are run and only one of them should time out.
	 */
	@Test
	public void testPollTimeoutFail() {
		
		assertTrue(crawlQueue.poll()==null);	
		
		try {
			Thread t1 = new Thread(new PollRunnable(1000));
			Thread t2 = new Thread(new PollRunnable(1000));
			Thread t3 = new Thread(new PollRunnable(1000));
			Thread t4 = new Thread(new PollRunnable(1000));
			t1.start();
			t2.start();
			t3.start();
			t4.start();
			addRunnablesToQueue();

			t1.join();
			t2.join();
			t3.join();
			t4.join();
			
			assertTrue(crawlQueue.size()==0 && counter==1);
			assertTrue(isExceptionThrown==false && isUnexpectedExceptionThrown==false);
		} catch (Exception ex) {
			fail("Exception not expected");
		}
		
	}
	
	/**
	 * Test for CrawlQueue.poll()
	 * 4 threads are run for poll() and all of them should pass before timeout eventually.
	 */
	@Test
	public void testPollTimeoutSuccess() {
		
		assertTrue(crawlQueue.poll()==null);	
		
		try {
			Thread t1 = new Thread(new PollRunnable(1000));
			Thread t2 = new Thread(new PollRunnable(1000));
			Thread t3 = new Thread(new PollRunnable(1000));
			Thread t4 = new Thread(new PollRunnable(1000));
			Thread t5 = new Thread(new Runnable(){
				@Override
				public void run() {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					}
					crawlQueue.add(new MyRunnable("Runnable4"));
				}			
			});
			t1.start();
			t2.start();
			t3.start();
			t4.start();
			
			addRunnablesToQueue();
			t5.start();
			
			t1.join();
			t2.join();
			t3.join();
			t4.join();
			t5.join();
			
			assertTrue(crawlQueue.size()==0 && counter==0);
			assertTrue(isExceptionThrown==false && isUnexpectedExceptionThrown==false);
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception not expected");
			
		}
		
	}
	
	/**
	 * Test for CrawlQueue.put() and take()
	 * 3 threads for put() and 3 threads for take() are run.
	 * Eventually, all threads should eventually succeed and 
	 * there should be no remaining element in the queue.
	 */
	@Test
	public void testPutTake() {

		assertTrue(crawlQueue.poll()==null);	
		
		try {
			addRunnablesToQueue();
			Thread t1 = new Thread(new PutRunnable());
			Thread t2 = new Thread(new PutRunnable());
			Thread t3 = new Thread(new PutRunnable());
			Thread t4 = new Thread(new TakeRunnable());
			Thread t5 = new Thread(new TakeRunnable());
			Thread t6 = new Thread(new TakeRunnable());
			
			t1.start();
			t2.start();
			t3.start();
			t4.start();
			t5.start();
			t6.start();
			cb.await();

			t1.join();
			t2.join();
			t3.join();
			t4.join();
			t5.join();
			t6.join();
			
			assertTrue(crawlQueue.remainingCapacity()==0);
			assertTrue(isExceptionThrown==false && isUnexpectedExceptionThrown==false);
		} catch (Exception ex) {
			fail("Exception not expected");
		}
		
	}
	
	private void addRunnablesToQueue() {
		crawlQueue.add(runnable1);
		crawlQueue.add(runnable2);
		crawlQueue.add(runnable3);
	}
	
	/**
	 * Custom Runnable to execute CrawlQueue.remove()
	 */
	private class RemoveRunnable implements Runnable {

		@Override
		public void run() {
			try {
				cb.await();
				crawlQueue.remove();
			} catch (NoSuchElementException e) {
				isExceptionThrown = true;
			} catch (Exception e) {
				isUnexpectedExceptionThrown = true;
			} 
		}
		
	}
	
	/**
	 * Custom Runnable to execute CrawlQueue.take()
	 */
	private class TakeRunnable implements Runnable {
		
		@Override
		public void run() {
			try {
				crawlQueue.take();
			} catch (Exception e) {
				isUnexpectedExceptionThrown = true;
			}
		}
		
	}
	
	/**
	 * Custom Runnable to execute CrawlQueue.put()
	 */
	private class PutRunnable implements Runnable {
		
		@Override
		public void run() {
			try {
				cb.await();
				crawlQueue.put(new MyRunnable("dummy"));
			} catch (Exception e) {
				isUnexpectedExceptionThrown = true;
			}
		}
		
	}
	
	/**
	 * Custom Runnable to execute CrawlQueue.poll()
	 */
	private class PollRunnable implements Runnable {

		int timeout;
		
		public PollRunnable(int t) {
			timeout = t;
		}
		
		@Override
		public void run() {
			try {
				cb.await();
				if (crawlQueue.poll(timeout, TimeUnit.MILLISECONDS)==null)
					counter++;
			} catch (Exception e) {
				isUnexpectedExceptionThrown = true;
			}
		}
		
	}
	
	/**
	 * Custom Runnable to execute CrawlQueue.add()
	 */
	private class AddRunnable implements Runnable {

		Runnable runnable;
		
		public AddRunnable(Runnable r) {
			runnable = r;
		}
		
		@Override
		public void run() {
			try {
				cb.await();
				crawlQueue.add(runnable);
			} catch (IllegalStateException e) {
				isExceptionThrown = true;
			} catch (Exception e) {
				isUnexpectedExceptionThrown = true;
			}
		}
		
	}
	
	/**
	 * Custom Runnable to execute CrawlQueue.offer()
	 */
	private class OfferRunnable implements Runnable {

		Runnable runnable;
		
		public OfferRunnable(Runnable r) {
			runnable = r;
		}
		
		@Override
		public void run() {
			try {
				cb.await();
				if (crawlQueue.offer(runnable))
					counter++;
			} catch (Exception e) {
				isUnexpectedExceptionThrown = true;
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
