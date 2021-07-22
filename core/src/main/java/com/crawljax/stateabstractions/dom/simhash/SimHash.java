/**
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

 @Author: Albert Juhé, Universidad Oberta de Catalunya; 
 You may contact the author at [ajuhe@omaonk.com]
 And the copyright holder at [jrivera@uoc.es] [Av. Tibidabo 39-43 - 08035 Barcelona]
*/
package com.crawljax.stateabstractions.dom.simhash;

import com.crawljax.stateabstractions.dom.simhash.BitHash;

/**
 *
 *
 * <p>For each token that we add, we regenerate de vector fingerprint.
 * Analizyng the sentence detecting web: tokens = (detecting,web) </p>
 * <p>Sample</p>
 * <p>token: <i>detecting</i> with <b>Rabin hash 64</b></p>
 * <table>
 * <tr>
 * <td>Source bithash1</td><td>0</td><td>1</td><td>1</td><td>0</td><td>1</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>1</td><td>1</td><td>1</td><td>1</td><td>1</td><td>0</td><td>1</td>
 * </tr>
 * <tr>
 * <td>Vector</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td>
 * </tr>
 * <tr>
 * <td>Final vector</td><td>-1</td><td>1</td><td>1</td><td>-1</td><td>1</td><td>-1</td><td>-1</td><td>-1</td><td>-1</td><td>-1</td><td>-1</td><td>-1</td><td>-1</td><td>-1</td><td>-1</td><td>1</td><td>1</td><td>1</td><td>1</td><td>1</td><td>-1</td><td>1</td>
 * </tr>
 * </table> 
 *            
 */
public class SimHash {
    
    /** The size that we use for generate a fingerprint vector */
    private int LONGSIZE = 64;
    private double[] vector;
    private int[] fingerprint;
         
    public SimHash() {
        this.vector = new double[this.LONGSIZE];
        this.fingerprint = new int[this.LONGSIZE];
    }
    
    /**
     * 
     * @param bh 
     */
    public void add(BitHash bh) {
        /*
        System.out.println(bh.shingle + " HasValue:" + bh.baseNumber);        
        System.out.println("Bits  :" + bh.toString());
        System.out.println("Vector:"+this.vectorToString());
         */
        for (int i = 0; i < this.LONGSIZE; i++) {
            //if bh ith bit is 1 we increase the vector else we decrease the vector
            if (bh.getbitNumber(i) == 1) {
                this.vector[i]+=bh.getWeight();
            } else {
                this.vector[i]-=bh.getWeight();
            }
            if (this.vector[i] > 0) {
                this.fingerprint[i] = 1;
            } else {
                this.fingerprint[i] = 0;
            }
        }
    //System.out.println(this.toString());

    }

    public String vectorToString() {
        String vectorBits = "";

        for (int i = 0; i < this.LONGSIZE; i++) {
            if (this.vector[i] < 0) {
                vectorBits = " " + this.vector[i] + vectorBits;
            } else {
                vectorBits = "  " + this.vector[i] + vectorBits;
            }
        }

        return vectorBits;
    }

    /*
     * We can see the bithash and the fingerprint.
     */
    public String toString() {
        String vectorBits = "";
        String fingerprint = "";

        for (int i = 0; i < this.LONGSIZE; i++) {
            if (this.vector[i] < 0) {
                vectorBits = " " + this.vector[i] + vectorBits;
            } else {
                vectorBits = "  " + this.vector[i] + vectorBits;
            }
            fingerprint = " " + this.getFingerprint()[i] + fingerprint;
        }

        return "Vector:" + vectorBits + "\nFingerprint:" + fingerprint;
    }

    public String getStringFingerprint() {
        String fingerprint = "";
        for (int i = 0; i < this.LONGSIZE; i++) {
            fingerprint = " " + this.getFingerprint()[i] + fingerprint;
        }

        return fingerprint;
    }

    public int[] getFingerprint() {
        return fingerprint;
    }
}
