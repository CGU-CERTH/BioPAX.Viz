package cytoscape.customplugins.biopax;


import cytoscape.Cytoscape;
import cytoscape.customplugins.biopax.action.CustomPathwayVisualizerAction;
import cytoscape.logger.CyLogger;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;


/**
 * BioPAX Visualizer PlugIn.
 *
 * @author Dimitrios Vitsios
 */
public class BiopaxPlugin extends CytoscapePlugin {
	
	protected static final CyLogger log = CyLogger.getLogger(BiopaxPlugin.class);
	
    /**
     * Name of Plugin.
     */
    public static final String PLUGIN_NAME = "BioPAX Viz Plugin";

	/**
	 * Attribute Name for BioPAX Utility Class.
	 */
	public static final String BP_UTIL = "BIO_PAX_UTIL";

	/**
	 * Proxy Host Property Name
	 */
	public static final String PROXY_HOST_PROPERTY = "dataservice.proxy_host";

	/**
	 * Proxy Port Property Name
	 */
	public static final String PROXY_PORT_PROPERTY = "dataservice.proxy_port";

	/**
	 * Constructor.
	 * This method is called by the main Cytoscape Application upon startup.
	 */
	public BiopaxPlugin() {
            
                CustomPathwayVisualizerAction menuAction = new CustomPathwayVisualizerAction(this);
                Cytoscape.getDesktop().getCyMenus().addCytoscapeAction((CytoscapeAction) menuAction);
	}

}
