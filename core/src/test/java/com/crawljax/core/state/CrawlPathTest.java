package com.crawljax.core.state;

import com.crawljax.core.Crawler;
import org.junit.Test;

public class CrawlPathTest {

    @Test
    public void test() {
        CrawlPath path = new CrawlPath(0);
        Eventable a = new Eventable();
        a.setId(1);
        Eventable b = new Eventable();
        b.setId(2);
        path.add(a);
        Crawler.printCrawlPath(path, true);
        path.add(b);
        Crawler.printCrawlPath(path, true);
        path.remove(a);
        path.set(0, a);
        Crawler.printCrawlPath(path, true);
    }
}
