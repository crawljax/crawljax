/*
Copyright [2009] [UOC]
This program is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the Free
Software Foundation; either version 2 of the License, or (at your option)
any later version.
This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
for more details, currently published at
http://www.gnu.org/copyleft/gpl.html or in the gpl.txt in the wiki2html of
this distribution.
You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc., 51
Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
Authors: Albert Juhé, Universidad Oberta de Catalunya; 
You may contact the author at [ajuhe@omaonk.com]
And the copyright holder at [jrivera@uoc.es] [Av. Tibidabo 39-43 - 08035 Barcelona]
 */
package com.crawljax.stateabstractions.dom.simhash;

/**
 *
 * Apply a hash function to a String.
 */
abstract public class BitHash {

    protected Long baseNumber = new Long(0);
    protected String bitString = "";
    protected int LONGSIZE = 64;
    protected int[] bitNumber;
    protected String shingle;
    //This is the weight of the term, is the value that increments or decrements de vector
    private double weight = 1; 

    /**
     * 
     * @param shingle String that we want get a hashfunction
     */
    public BitHash(String shingle) {
        this.shingle = shingle;
    }

    public BitHash(String shingle, double weight) {
        this.weight = weight;
        this.shingle = shingle;
    }

    /**
     * After getting a hash value, we convert this value in a bit representation
     * and we store it in a String.
     * 
     * @param hashValue 
     * @return String of the bit representation of the hash value
     */
    protected String applyHash(Long hashValue) {
        this.baseNumber = hashValue;
        this.bitString = Long.toBinaryString(getBaseNumber());
        for (int i = this.getBitString().length(); i < this.LONGSIZE; i++) {
            this.bitString = "0" + this.getBitString();
        }
        this.convertToArraybits();

        return this.getBitString();
    }

    /**
     * 
     * Converts the String bit representation to a array integer bit representation.
     */
    private void convertToArraybits() {
        bitNumber = new int[this.LONGSIZE];
        for (int i = 0; i < this.LONGSIZE; i++) {
            this.bitNumber[(this.LONGSIZE - 1) - i] = Integer.parseInt(this.getBitString().substring(i, i + 1));
        }
    }

    /**
     * 
     * @return String bit representation of the hash value
     */
    public String toString() {
        String vectorBits = "";

        for (int i = 0; i < this.LONGSIZE; i++) {
            if (this.bitNumber[i] < 0) {
                vectorBits = " " + this.bitNumber[i] + vectorBits;
            } else {
                vectorBits = "  " + this.bitNumber[i] + vectorBits;
            }
        }

        return vectorBits;
    }

    /** This is the hash function that we apply to the String (shingle) */
    abstract void hashfunction();

    /**
     * 
     * @param bitnumber Position in the array int representation
     * @return Return the ith bit in the array int bit representation
     */
    public int getbitNumber(int bitnumber) {
        return this.bitNumber[bitnumber];
    }

    public Long getBaseNumber() {
        return baseNumber;
    }

    public String getBitString() {
        return bitString;
    }

    public int[] getBitNumber() {
        return bitNumber;
    }

    public String getShingle() {
        return shingle;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
