package com.crawljax.condition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Since many of the AbstractConditions override equals and hashcode,
 * we provide some test cases for them.
 */

public class ConditionTest {
    
    Condition self;
    Condition equivalent;
    Condition different;
    
    @Before 
    public void setup() {
        self = new RegexCondition("rx0");
        equivalent = new RegexCondition("rx0");
        different = new RegexCondition("rx1");
    }
    
    @Test
    public void testEquality() {
        assertTrue(self.equals(equivalent));
        assertTrue(equivalent.equals(self));
        assertFalse(self.equals(different));
        assertFalse(different.equals(self));
        assertFalse(self.equals(null));
        assertTrue(self.equals(self));
    }
    
    @Test
    public void testToString() {
        assertEquals(self.toString(), equivalent.toString());
        assertNotEquals(self.toString(), different.toString());
    }
    
    @Test
    public void testHashCode() {
        assertEquals(self.hashCode(), equivalent.hashCode());
        assertNotEquals(self.hashCode(), different.hashCode());
    }
}
