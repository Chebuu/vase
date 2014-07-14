package nl.ru.cmbi.vase.web;

import nl.ru.cmbi.vase.job.HsspQueue;
import nl.ru.cmbi.vase.tools.util.Config;
import nl.ru.cmbi.vase.web.page.AlignmentPage;
import nl.ru.cmbi.vase.web.page.HomePage;
import nl.ru.cmbi.vase.web.page.InputPage;
import nl.ru.cmbi.vase.web.page.SearchResultsPage;
import nl.ru.cmbi.vase.web.rest.JobRestResource;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.request.resource.SharedResourceReference;

import de.agilecoders.wicket.core.Bootstrap;

/**
 * Application object for your web application.
 * If you want to run this application without deploying, run the Start class.
 * 
 * @see nl.ru.cmbi.vase.web.Start#main(String[])
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

		// Bootstrap
		Bootstrap.install(this);
		
		// add your configuration here
		
		mountPage("/search/${structureID}", SearchResultsPage.class);
		
		// alignmentPage can take either one, or two parameters,
		// depending on the number of chains in the structure
		mountPage("/align", AlignmentPage.class);
		
		mountPage("/input", InputPage.class);
		
		mountResource("/rest", new ResourceReference("restReference") {
			
			JobRestResource resource = new JobRestResource(WicketApplication.this);
			
			@Override
			public IResource getResource() {
				return resource;
			}
		});

		mountResource("/jobs.js",new PackageResourceReference(AlignmentPage.class, "jobs.js"));
		mountResource("/align.js",new PackageResourceReference(AlignmentPage.class, "align.js"));
		mountResource("/align.css",new PackageResourceReference(AlignmentPage.class, "align.css"));
	}
}
