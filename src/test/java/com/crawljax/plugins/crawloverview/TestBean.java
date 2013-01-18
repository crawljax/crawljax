package com.crawljax.plugins.crawloverview;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.ImmutableList;

public class TestBean {

	private final String someString = "A";
	private final int someInt = 123;
	private final ImmutableList<String> stringList = ImmutableList.of("A", "B");
	private final ImmutableList<AtomicInteger> objectList = ImmutableList
	        .of(new AtomicInteger(42));

	public String getSomeString() {
		return someString;
	}

	public int getSomeInt() {
		return someInt;
	}

	public ImmutableList<String> getStringList() {
		return stringList;
	}

	public ImmutableList<AtomicInteger> getObjectList() {
		return objectList;
	}

}
