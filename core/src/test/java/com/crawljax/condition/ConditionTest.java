package com.crawljax.condition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.crawljax.core.state.Identification;

/**
 * Since many of the AbstractConditions override equals and hashcode,
 * we provide some test cases for them.
 */

@RunWith(Parameterized.class)
public class ConditionTest {
    
    Condition self;
    Condition equivalent;
    Condition different;
    
    public ConditionTest(Condition _self, Condition _equivalent, Condition _different) {
        this.self = _self;
        this.equivalent = _equivalent;
        this.different = _different;
    }
    
    @Parameters(name = "c{index}, {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { 
                new RegexCondition("rx0"),
                new RegexCondition("rx0"),
                new RegexCondition("rx1")
            },
            { 
                new NotRegexCondition("nrx0"),
                new NotRegexCondition("nrx0"),
                new NotRegexCondition("nrx1")
            },
            { 
                new UrlCondition("u0"),
                new UrlCondition("u0"),
                new UrlCondition("u1")
            },
            { 
                new NotUrlCondition("u0"),
                new NotUrlCondition("u0"),
                new NotUrlCondition("u1")
            },
            { 
                new XPathCondition("x0"),
                new XPathCondition("x0"),
                new XPathCondition("x1")
            },
            { 
                new NotXPathCondition("x0"),
                new NotXPathCondition("x0"),
                new NotXPathCondition("x1")
            },
            {
                new VisibleCondition(new Identification(Identification.How.xpath, "xp")),
                new VisibleCondition(new Identification(Identification.How.xpath, "xp")),
                new VisibleCondition(new Identification(Identification.How.tag, "xp"))
            },
            {
                new NotVisibleCondition(new Identification(Identification.How.xpath, "xp")),
                new NotVisibleCondition(new Identification(Identification.How.xpath, "xp")),
                new NotVisibleCondition(new Identification(Identification.How.tag, "xp"))
            },
            { 
                new JavaScriptCondition("0"),
                new JavaScriptCondition("0"),
                new JavaScriptCondition("1")
            },

         });
    }
       
    @Test
    public void testEquality() {
        assertEquals(self, equivalent);
        assertEquals(equivalent, self);
        assertNotEquals(self, different);
        assertNotEquals(different, self);
        assertNotEquals(self, null);
        assertEquals(self, self);
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
