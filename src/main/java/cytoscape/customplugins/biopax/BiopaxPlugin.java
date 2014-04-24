/** BioPAX Viz Plugin for Cytoscape
 **
 ** Copyright (c) 2013 Center for Research and Technology Hellas (CERTH)
 **
 ** This is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** both UofT and MSKCC have no obligations to provide maintenance, 
 ** support, updates, enhancements or modifications.  In no event shall
 ** UofT or MSKCC be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** UofT or MSKCC have been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this software; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA;
 ** or find it at http://www.fsf.org/ or http://www.gnu.org.
 **/
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
