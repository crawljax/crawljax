package com.crawljax.core.state;

/**
 * This class represents an Edge.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @author mesbah
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: Edge.java 6372 2009-12-29 09:25:31Z danny $
 */
public class Edge {

	private long id;
	private StateVertix fromStateVertix;
	private StateVertix toStateVertix;

	/**
	 * Create a new Edge based on a from and to Vertix.
	 * 
	 * @param fromStateVertix
	 *            the Vertix orriginating from
	 * @param toStateVertix
	 *            the Vertix as destination
	 */
	public Edge(StateVertix fromStateVertix, StateVertix toStateVertix) {
		this.fromStateVertix = fromStateVertix;
		this.toStateVertix = toStateVertix;
	}

	/**
	 * Return the id.
	 * 
	 * @return the id of the Edge
	 */
	public long getId() {
		return id;
	}

	/**
	 * Set the new id of this edge.
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the fromStateVertix
	 */
	public StateVertix getFromStateVertix() {
		return fromStateVertix;
	}

	/**
	 * @param fromStateVertix
	 *            the fromStateVertix to set
	 */
	public void setFromStateVertix(StateVertix fromStateVertix) {
		this.fromStateVertix = fromStateVertix;
	}

	/**
	 * @return the toStateVertix
	 */
	public StateVertix getToStateVertix() {
		return toStateVertix;
	}

	/**
	 * @param toStateVertix
	 *            the toStateVertix to set
	 */
	public void setToStateVertix(StateVertix toStateVertix) {
		this.toStateVertix = toStateVertix;
	}

}
