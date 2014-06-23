package nl.ru.cmbi.hssp.web;


import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import nl.ru.cmbi.hssp.web.WicketApplication;
import nl.ru.cmbi.hssp.web.page.HomePage;
import nl.ru.cmbi.hssp.web.rest.JobRestResource;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.rest.utils.test.BufferedMockRequest;

import com.google.gson.Gson;

/**
 * Simple test using the WicketTester
 */
public class TestRestful
{
	private static final Logger log = LoggerFactory.getLogger(TestRestful.class);
	
	private WicketTester tester;

	@Before
	public void setUp()
	{		
		tester = new WicketTester(new WicketApplication());
	}
    
    @Test
    public void testCustom() throws IOException, InterruptedException {
    	
    	URL url = new URL("http://www.rcsb.org/pdb/files/101M.pdb");
    	StringWriter writer = new StringWriter();
    	IOUtils.copy(url.openStream(), writer);
    	String pdb = writer.toString();
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(
			tester.getApplication(), tester.getHttpSession(),
			tester.getServletContext());
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("pdbfile", pdb);
		mockRequest.setParameter("chain", "A");

		tester.setRequest(mockRequest);
		tester.executeUrl("rest/custom");

		String	status = "",
				id = tester.getLastResponseAsString().replaceAll("^\"", "").replaceAll("\"$", "");
		
		assertTrue( id!=null && !id.isEmpty() );
		
		List<String> expectedStati = Arrays.asList(new String[]{"QUEUED","RUNNING","FINISHED"});
		
		while ( true ) {
			
			tester.getRequest().setMethod("GET");
			tester.executeUrl("rest/status/"+id);
			
			status = tester.getLastResponseAsString().replaceAll("^\"", "").replaceAll("\"$", "");
			
			log.info("status=\""+status+"\"");
			
			assertTrue( expectedStati.contains(status) );
			
			if(status.equals("FINISHED")) {
				break;
			}
			
			Thread.sleep(60000);
		} 
    }
}
