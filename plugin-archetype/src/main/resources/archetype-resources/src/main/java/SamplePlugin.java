package ${package};

import java.io.FileWriter;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.OnNewStatePlugin;

public class SamplePlugin implements OnNewStatePlugin {

	@Override
	public void onNewState(CrawlSession session) {
		try {
			String dom = session.getBrowser().getDom();
			String fileName = session.getCurrentState().getName() + ".html";

			FileWriter fw = new FileWriter(fileName, false);
			fw.write(dom);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
