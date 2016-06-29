package cytoscape.customapp.biopax;

import java.util.Hashtable;
import java.util.Properties;

import org.cytoscape.app.swing.AbstractCySwingApp;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.task.visualize.ApplyVisualStyleTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.swing.DialogTaskManager;

import cytoscape.customapp.biopax.action.BioPaxAction;
import cytoscape.customapp.biopax.action.CyNodeDetails;
import cytoscape.customapp.biopax.action.MySelectionListener;

public class BioPaxVizApp extends AbstractCySwingApp 
{
	public BioPaxVizApp(CySwingAppAdapter adapter)
	{
		super(adapter);
		CySwingApplication cytoscapeDesktopService = adapter.getCySwingApplication();
		CyNetworkManager netMgr = adapter.getCyNetworkManager();
		CyNetworkFactory networkFactory = adapter.getCyNetworkFactory();
		CyTableFactory cyTableFactory = adapter.getCyTableFactory();
		VisualMappingManager vMappingManager = adapter.getVisualMappingManager();
		CyNetworkViewFactory networkViewFactory = adapter.getCyNetworkViewFactory();
		CyNetworkViewManager networkViewManager = adapter.getCyNetworkViewManager();
		VisualStyleFactory visualStyleFactory = adapter.getVisualStyleFactory();
		LoadVizmapFileTaskFactory loadVizmapFileTaskFactory =  adapter.get_LoadVizmapFileTaskFactory();
		ApplyVisualStyleTaskFactory applyVisualStyleTaskFactory = adapter.get_ApplyVisualStyleTaskFactory();
		DialogTaskManager dialogTaskManager = adapter.getDialogTaskManager();
		CyApplicationManager cyApplicationManager=adapter.getCyApplicationManager();
		
		//Init the controller
		ServiceController.getInstance().setAdapter(adapter);
		ServiceController.getInstance().setCytoscapeDesktopService(cytoscapeDesktopService);
		ServiceController.getInstance().setDialogTaskManager(dialogTaskManager);
		ServiceController.getInstance().setNetMgr(netMgr);
		ServiceController.getInstance().setNetworkFactory(networkFactory);
		ServiceController.getInstance().setCyTableFactory(cyTableFactory);
		ServiceController.getInstance().setvMappingManager(vMappingManager);
		ServiceController.getInstance().setVisualStyleFactory(visualStyleFactory);
		ServiceController.getInstance().setNetworkViewFactory(networkViewFactory);
		ServiceController.getInstance().setNetworkViewManager(networkViewManager);
		ServiceController.getInstance().setLoadVizmapFileTaskFactory(loadVizmapFileTaskFactory);
		ServiceController.getInstance().setApplyVisualStyleTaskFactory(applyVisualStyleTaskFactory);
		ServiceController.getInstance().setCyApplicationManager(cyApplicationManager);
		
		MySelectionListener listener=new MySelectionListener();
		adapter.getCyServiceRegistrar().registerService(listener, RowsSetListener.class, new Properties());
		adapter.getCySwingApplication()
                    .addAction(new BioPaxAction());
	}
}