package nl.ru.cmbi.hssp.web;

import nl.ru.cmbi.hssp.job.HsspQueue;
import nl.ru.cmbi.hssp.tools.util.Config;
import nl.ru.cmbi.hssp.web.page.AlignmentPage;
import nl.ru.cmbi.hssp.web.page.HomePage;
import nl.ru.cmbi.hssp.web.rest.JobRestResource;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.request.resource.SharedResourceReference;

/**
 * Application object for your web application.
 * If you want to run this application without deploying, run the Start class.
 * 
 * @see nl.ru.cmbi.hssp.web.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends WebPage> getHomePage()
	{
		return HomePage.class;
	}
	
	private HsspQueue hsspQueue = new HsspQueue(Integer.parseInt(Config.properties.getProperty("hsspthreads")));
	
	public HsspQueue getHsspQueue() {
		
		return hsspQueue;
	}

	/**
	 * @see org.apache.wicket.Application#init()
	 */
	@Override
	public void init()
	{
		super.init();

		// add your configuration here
		
		mountPage("/align", AlignmentPage.class);
		
		mountResource("/rest", new ResourceReference("restReference") {
			
			JobRestResource resource = new JobRestResource(WicketApplication.this);
			
			@Override
			public IResource getResource() {
				return resource;
			}
		});
		
		mountResource("/align.js",new PackageResourceReference(AlignmentPage.class, "align.js"));
		mountResource("/align.css",new PackageResourceReference(AlignmentPage.class, "align.css"));
	}
}
