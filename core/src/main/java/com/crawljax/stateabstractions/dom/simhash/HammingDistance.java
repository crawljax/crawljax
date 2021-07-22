/*
 * 
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
 * 
 */

package com.crawljax.stateabstractions.dom.simhash;


/**
 * The Hamming distance between two strings of equal length is the number of positions 
 * for which the corresponding symbols are different.
 */
public class HammingDistance {
     /**
     * Calculate the Hamming distance betwen two array of bits
     * 
     * @param source Array with the fingerprint values
     * @param target Array with the fingerprint values
     * @return The Hamming distance
     */
    public static int hamming(int[] source, int[] target) {
        int distance=0;
        
        if (source.length!= target.length) return distance;
        for(int i=0;i<source.length;i++) {
            if (source[i]!=target[i]) distance++;
        }
        
        return distance;
    }
      /**
     * Calculate the Hamming distance betwen two String of bits
     * 
     * @param source String with the fingerprint values
     * @param target String with the fingerprint values
     * @return The Hamming distance
     */
      public static int hamming(String source, String target) {
        int distance=0;
        
        if (source.length()!= target.length()) return distance;
        for(int i=0;i<source.length();i++) {
            if (source.charAt(i)!=target.charAt(i)) distance++;
        }
        
        return distance;
    }
}
