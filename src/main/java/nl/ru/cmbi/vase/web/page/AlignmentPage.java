package nl.ru.cmbi.vase.web.page;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.ru.cmbi.vase.analysis.Calculator;
import nl.ru.cmbi.vase.analysis.MutationDataObject;
import nl.ru.cmbi.vase.data.Alignment;
import nl.ru.cmbi.vase.data.AlignmentSet;
import nl.ru.cmbi.vase.data.ResidueInfo;
import nl.ru.cmbi.vase.data.ResidueInfoSet;
import nl.ru.cmbi.vase.data.VASEDataObject;
import nl.ru.cmbi.vase.data.VASEDataObject.PlotDescription;
import nl.ru.cmbi.vase.parse.StockholmParser;
import nl.ru.cmbi.vase.web.panel.align.AlignmentPanel;
import nl.ru.cmbi.vase.web.panel.align.AlignmentLinkedPlotPanel;
import nl.ru.cmbi.vase.web.panel.align.EntropyTablePanel;
import nl.ru.cmbi.vase.web.panel.align.StructurePanel;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.wicket.core.util.string.JavaScriptUtils;

public class AlignmentPage extends WebPage {
	
	private static final Logger log = LoggerFactory.getLogger(AlignmentPage.class);
	
	private String PDBID;
	private Character chainID=null;
	
	private static Calculator calculator = new Calculator();

	public AlignmentPage(final PageParameters parameters) {
		
		StringValue	pdbIDString		= parameters.get(0),
					chainIDString	= parameters.get(1);
		
		Map<Character,VASEDataObject> dataPerChain;
		
		if(pdbIDString==null || pdbIDString.isNull() || pdbIDString.isEmpty()) {
					
			throw new RestartResponseAtInterceptPageException(new ErrorPage("pdb id missing"));
			
		} else {
		
			PDBID = pdbIDString.toString().toLowerCase();
	
			try {
				
				URL stockholmURL = new URL(String.format("ftp://ftp.cmbi.ru.nl/pub/molbio/data/hssp3/%s.hssp.bz2",PDBID)),
					pdbURL = new URL(String.format("http://www.rcsb.org/pdb/files/%s.pdb",PDBID));
				
				dataPerChain  = StockholmParser.parseStockHolm ( new BZip2CompressorInputStream(stockholmURL.openStream()), pdbURL);
				
			} catch (Exception e) {
							
				log.error("Error getting alignments for " + pdbIDString + " : " + e.getMessage(),e);
				
				throw new RestartResponseAtInterceptPageException(new ErrorPage("Error getting alignments for " + pdbIDString + " : " + e.toString()));
			}
			
			if(chainIDString!=null && !chainIDString.isNull() && !chainIDString.isEmpty()) {
				
				chainID = chainIDString.toChar();
			}
			else if (dataPerChain.size()==1) {
				
				List<Character> chainIDs = new ArrayList<Character>();
				chainIDs.addAll(dataPerChain.keySet());
				
				chainID = chainIDs.get(0);
			}
			else if(chainID==null) {
				
				String chains = "Select one of the following chains: ";
				for(char chain : dataPerChain.keySet()) {
					chains += " "+chain;
				}
				
				throw new RestartResponseAtInterceptPageException(new ErrorPage(chains));
			}
			else if(!dataPerChain.containsKey(chainID)) {
				
				throw new RestartResponseAtInterceptPageException(new ErrorPage("No such chain: "+chainID));
			}
		}
		

//		####################################################################
		
		String title=String.format("Alignment of %s chain %c", PDBID,chainID);
		
		add(new Label("page-title",title));
		add(new Label("page-header",title));
		
		final VASEDataObject data = dataPerChain.get(chainID);
		
		final AlignmentPanel alignmentPanel = new AlignmentPanel("alignment",data);
		add(alignmentPanel);
		
		add(new JSDefinitions("js-definitions",
				alignmentPanel));
		
		addToTabs( "entropy-table", "Entropy Table", 
				new EntropyTablePanel("entropy-table",alignmentPanel,data));
		
		add(new ListView<PlotDescription>("plots",data.getPlots()){
			
			private int plotCount=0;

			@Override
			protected void populateItem(ListItem<PlotDescription> item) {
				
				PlotDescription pd = item.getModelObject();
				plotCount++;
				
				Component plot = new AlignmentLinkedPlotPanel("plot",alignmentPanel,pd,data.getTable());
				
				String id="plot"+plotCount;
				
				addToTabs( id, pd.getPlotTitle(), plot);
				
				item.add(plot);
			}
		});
		
		add(new ListView<String>("tabs", this.tabidsModel){

			@Override
			protected void populateItem(ListItem<String> item) {
				
				String tabid = item.getModelObject();
				
				WebMarkupContainer link = new WebMarkupContainer("tab-link");
				
				link.add(new AttributeModifier("onclick",String.format("switchTabVisibility('%s');", tabid)));
				link.add(new Label("tab-title",tabTitles.get(tabid)));
				
				item.add(link);
			}
		});
		
		String structurePath =
			RequestCycle.get().urlFor(HomePage.class, new PageParameters()).toString()
			+ "/rest/structure/" + PDBID;
		
		if(data.getPdbURL()!=null) {
			
			structurePath = data.getPdbURL().toString();
		}
		
		add(new StructurePanel("structure",structurePath));
	}
	
	private Map<String,String> tabTitles = new LinkedHashMap<String,String>();
	
	private IModel<List<String>> tabidsModel = new LoadableDetachableModel<List<String>>() {

		@Override
		protected List<String> load() {
			
			return new ArrayList<String>(tabTitles.keySet());
		}
	};
	
	private void addToTabs(String id, String tabTitle, Component component) {

		String display="none";
		if(tabTitles.size()==0)
			display="block";
		
		tabTitles.put(id,tabTitle);
		component.add(new AttributeModifier("id",id));
		component.add(new AttributeModifier("style","display:"+display+";"));
		
		add(component);
	}
	
	private class JSDefinitions extends Component {
		
		private AlignmentPanel alignmentPanel;
		
		public JSDefinitions(String id, AlignmentPanel a) {
			super(id);
			
			alignmentPanel = a;
		}

		@Override
		protected void onRender() {
			
			JavaScriptUtils.writeOpenTag(getResponse());
			
			//////////////////////////////////////////////////
			
			getResponse().write("var tabids=[");
			
			for(String tabid : AlignmentPage.this.tabTitles.keySet()) {
				
				getResponse().write(String.format("\'%s\',", tabid));
			}
			getResponse().write("];\n");

			String urlString = RequestCycle.get().urlFor(HomePage.class, new PageParameters()).toString();
			getResponse().write(String.format("var baseURL='%s';\n", urlString));
			
			getResponse().write("var pPDBresclass=/"+AlignmentPanel.pdbResiduePrefix+"([^\\s]+)/;\n");
			
			getResponse().write("var palignmentposclass=/"+AlignmentPanel.alignmentPositionPrefix+"([0-9]+)/;\n");

			getResponse().write("var alignment_columnheader_classname='"+AlignmentPanel.columnHeaderClassname+"';\n");
			
			JavaScriptUtils.writeCloseTag(getResponse());
		}
		
	}
}
