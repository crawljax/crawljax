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
	}
	
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
					crawlQueue.drainTo(list2);
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
					crawlQueue.drainTo(list3);
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
					crawlQueue.drainTo(list4);
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
		
		assertTrue(crawlQueue.size()==0 && 
				((list1.size()==3 && list2.size()==0 && list3.size()==0 && list4.size()==0) ||
						(list2.size()==3 && list1.size()==0 && list3.size()==0 && list4.size()==0) ||
						(list3.size()==3 && list1.size()==0 && list2.size()==0 && list4.size()==0) ||
						(list4.size()==3 && list1.size()==0 && list2.size()==0 && list3.size()==0)));
	}
	
	@Test
	public void testOffer() {
		 
		Thread t1 = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					cb.await();
					if (crawlQueue.offer(runnable1))
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
					if (crawlQueue.offer(runnable2))
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
					if (crawlQueue.offer(runnable3))
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
					if (crawlQueue.offer(new MyRunnable("Runnable4")))
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
			e.printStackTrace();
		}
		
		assertTrue(crawlQueue.size()==3 && counter==3);
	}
	
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
			e.printStackTrace();
		}
		
		assertTrue(crawlQueue.size()==3 && counter==3);
	}
	
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
	}
	
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
					e.printStackTrace();
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
					e.printStackTrace();
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
					e.printStackTrace();
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
	}
	
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
			assertTrue(isUnexpectedExceptionThrown==false);
		} catch (Exception ex) {
			fail("Exception not expected");
		}
		
	}
	
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
			assertTrue(isUnexpectedExceptionThrown==false);
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception not expected");
			
		}
		
	}
	
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
			assertTrue(isUnexpectedExceptionThrown==false);
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
	 * Custom Runnable to execute CrawlQueue.put()
	 */
	private class TakeRunnable implements Runnable {
		
		@Override
		public void run() {
			try {
				crawlQueue.take();
			} catch (InterruptedException e) {
				isUnexpectedExceptionThrown = true;
				e.printStackTrace();
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
