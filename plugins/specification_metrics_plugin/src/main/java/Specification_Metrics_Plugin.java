import java.util.List;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.DomChangeNotifierPlugin;
import com.crawljax.core.plugin.OnFireEventFailedPlugin;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.plugin.PreStateCrawlingPlugin;
import com.crawljax.core.state.Eventable;


public class Specification_Metrics_Plugin
        implements DomChangeNotifierPlugin, OnFireEventFailedPlugin, PreStateCrawlingPlugin {

	private list
	
	@Override
	public void onFireEventFailed(Eventable eventable, List<Eventable> pathToFailure) {
		eventable.getEventType().

	}

	@Override
	public boolean isDomChanged(String domBefore, Eventable e, String domAfter,
	        EmbeddedBrowser browser) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
    public void preStateCrawling(CrawlSession session, List<CandidateElement> candidateElements) {
	    // TODO Auto-generated method stub
	    session.getCurrentState().
    }
	

}
