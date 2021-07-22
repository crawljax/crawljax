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

import com.planetj.math.rabinhash.*;

import com.crawljax.stateabstractions.dom.simhash.BitHash;

/**
 *
 * We apply a HashRabin 64 bits to a token.
 */
public class BitHashRabin extends BitHash {

    private RabinHashFunction64 RH64;
    private long REFERENCE_VALUE_HASH = RabinHashFunction64.DEFAULT_HASH_FUNCTION.getP();

    /**
     * 
     * @param shingle String that we want get a hashfunction
     */
    public BitHashRabin(String shingle) {
        super(shingle);
        this.hashfunction();
    }
    
     public BitHashRabin(String shingle,double weight) {
        super(shingle,weight);
        this.hashfunction();
    }

    /**
     * 
     * This is the hash function that we apply, Hashrabin 64.
     */
    protected void hashfunction() {
        RH64 = new RabinHashFunction64(REFERENCE_VALUE_HASH);
        this.baseNumber = RH64.hash(this.getShingle());
        this.applyHash(this.getBaseNumber());
    }
    
    /**
     * Get the number for generate the hash value.
     * 
     * @return Number for generate the hash value.
     */    
    public long getREFERENCE_VALUE_HASH () {
        return REFERENCE_VALUE_HASH;
    }

    /**
    * 
    * @param REFERENCE_VALUE_HASH The value for calculate the Rabinhash funcion
    */
    public void setREFERENCE_VALUE_HASH(long REFERENCE_VALUE_HASH) {
        this.REFERENCE_VALUE_HASH = REFERENCE_VALUE_HASH;
    }

}
