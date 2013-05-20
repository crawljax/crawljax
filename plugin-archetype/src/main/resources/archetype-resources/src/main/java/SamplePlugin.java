package ${package};

import java.io.FileWriter;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.state.StateVertex;

public class SamplePlugin implements OnNewStatePlugin {

	@Override
	public void onNewState(CrawlerContext context, StateVertex newState) {
		try {
			String dom = context.getBrowser().getDom();
			String fileName = context.getCurrentState().getName() + ".html";

			FileWriter fw = new FileWriter(fileName, false);
			fw.write(dom);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
