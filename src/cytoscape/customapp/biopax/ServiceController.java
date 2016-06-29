package cytoscape.customapp.biopax;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.task.visualize.ApplyVisualStyleTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import cytoscape.customapp.biopax.action.CyNodeDetails;
import cytoscape.customapp.biopax.action.CyTreeNodeDetails;

public class ServiceController {

	private CySwingAppAdapter adapter;
	private DialogTaskManager dialogTaskManager;
	private CyApplicationManager cyApplicationManager;
	private CyNetworkManager netMgr;
	private CyNetworkFactory networkFactory;
	private CySwingApplication cytoscapeDesktopService;
	private CyTableFactory cyTableFactory;
	private VisualMappingManager vMappingManager;
	private VisualStyleFactory visualStyleFactory;
	private CyNetworkViewFactory networkViewFactory;
	private CyNetworkViewManager networkViewManager;
	private LoadVizmapFileTaskFactory loadVizmapFileTaskFactory;
	private ApplyVisualStyleTaskFactory applyVisualStyleTaskFactory;
	private ArrayList<CyNetwork> networksList;
    private ArrayList<String> networksTitlesList;
    private ArrayList<Hashtable<Long, CyNodeDetails>> cyNodeDetailsHTList;
	
	private static ServiceController instance = new ServiceController();
	
	public ServiceController(){
		networksList= new ArrayList<CyNetwork>();
		networksTitlesList= new ArrayList<String>();
		cyNodeDetailsHTList=new ArrayList<Hashtable<Long, CyNodeDetails>>();
	}
	
	/**
	 * Get the current instance
	 * @return ServiceController
	 */
	public static ServiceController getInstance() {
		return instance;
	}
	
	public LoadVizmapFileTaskFactory getLoadVizmapFileTaskFactory() {
		return loadVizmapFileTaskFactory;
	}

	public void setLoadVizmapFileTaskFactory(
			LoadVizmapFileTaskFactory loadVizmapFileTaskFactory) {
		this.loadVizmapFileTaskFactory = loadVizmapFileTaskFactory;
	}

	public ApplyVisualStyleTaskFactory getApplyVisualStyleTaskFactory() {
		return applyVisualStyleTaskFactory;
	}

	public void setApplyVisualStyleTaskFactory(
			ApplyVisualStyleTaskFactory applyVisualStyleTaskFactory) {
		this.applyVisualStyleTaskFactory = applyVisualStyleTaskFactory;
	}

	public CyNetworkViewFactory getNetworkViewFactory() {
		return networkViewFactory;
	}

	public void setNetworkViewFactory(CyNetworkViewFactory networkViewFactory) {
		this.networkViewFactory = networkViewFactory;
	}

	public CyNetworkViewManager getNetworkViewManager() {
		return networkViewManager;
	}

	public void setNetworkViewManager(CyNetworkViewManager networkViewManager) {
		this.networkViewManager = networkViewManager;
	}

	public CyTableFactory getCyTableFactory() {
		return cyTableFactory;
	}

	public void setCyTableFactory(CyTableFactory cyTableFactory) {
		this.cyTableFactory = cyTableFactory;
	}

	public VisualMappingManager getvMappingManager() {
		return vMappingManager;
	}

	public void setvMappingManager(VisualMappingManager vMappingManager) {
		this.vMappingManager = vMappingManager;
	}

	public VisualStyleFactory getVisualStyleFactory() {
		return visualStyleFactory;
	}

	public void setVisualStyleFactory(VisualStyleFactory visualStyleFactory) {
		this.visualStyleFactory = visualStyleFactory;
	}

	public CySwingApplication getCytoscapeDesktopService() {
		return cytoscapeDesktopService;
	}

	public void setCytoscapeDesktopService(
			CySwingApplication cytoscapeDesktopService) {
		this.cytoscapeDesktopService = cytoscapeDesktopService;
	}

	public CyNetworkFactory getNetworkFactory() {
		return networkFactory;
	}

	public void setNetworkFactory(CyNetworkFactory networkFactory) {
		this.networkFactory = networkFactory;
	}
	
	public CyNetworkManager getNetMgr() {
		return netMgr;
	}

	public void setNetMgr(CyNetworkManager netMgr) {
		this.netMgr = netMgr;
	}

	public CyNetwork getCurrentNetwork(){
		return cyApplicationManager.getCurrentNetwork();
	}
	
	public CySwingAppAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(CySwingAppAdapter adapter) {
		this.adapter = adapter;
	}

	public DialogTaskManager getDialogTaskManager() {
		return dialogTaskManager;
	}

	public void setDialogTaskManager(DialogTaskManager dialogTaskManager) {
		this.dialogTaskManager = dialogTaskManager;
	}

	public CyApplicationManager getCyApplicationManager() {
		return cyApplicationManager;
	}

	public void setCyApplicationManager(CyApplicationManager cyApplicationManager) {
		this.cyApplicationManager = cyApplicationManager;
	}
	
	/*
	 private ;
    private  ;
    private  ;
	 */
	public void resetAllLists(){
		networksList = new ArrayList<CyNetwork>();
		networksTitlesList = new ArrayList<String>();
		cyNodeDetailsHTList=new ArrayList<Hashtable<Long, CyNodeDetails>>();
	}
	
	public ArrayList<CyNetwork> getNetworksList(){
		return this.networksList;
	}
	
	public void addToNetworksList(CyNetwork cyNetwork){
		this.networksList.add(cyNetwork);
	}
	
	public ArrayList<String> getNetworksTitlesList(){
		return this.networksTitlesList;
	}
	
	public void addToNetworksTitlesList(String networkTitle){
		this.networksTitlesList.add(networkTitle);
	}
	
	public void addToCyNodeDetailsHTList(Hashtable<Long, CyNodeDetails> cyNodeDetailsHT){
		this.cyNodeDetailsHTList.add(cyNodeDetailsHT);
	}
	
	public int searchNetworkTitle(String networkName){
		System.out.println("Searching for: "+networkName+", length="+networkName.length());
		int found=-1;
		for(String s:networksTitlesList){
			found++;
			System.out.println("Network title="+s);
			if(s.startsWith(networkName)){
				break;
			}
		}
		return found;
	}
	
	
	
	public void displayView(String networkName){
		CyNetwork viewNetwork=null;
		int pos=-1;
		for(CyNetwork cyNetwork:this.networksList){
			pos++;
        	String cyNetworkName = cyNetwork.getRow(cyNetwork).get(CyNetwork.NAME, String.class);
        	if(cyNetworkName.startsWith(networkName)){
        		viewNetwork=cyNetwork;
        		break;
        	}
		}
		if(pos!=-1&&viewNetwork!=null){
			Hashtable<Long, CyNodeDetails> cyNodeDetailsHT=cyNodeDetailsHTList.get(pos);
			CyNetworkView cyView= ServiceController.getInstance().getNetworkViewFactory().createNetworkView(viewNetwork);

			updateNodeColors(cyView,cyNodeDetailsHT);
	        cyView.fitContent();
	        CyLayoutAlgorithmManager alMan =ServiceController.getInstance().getAdapter().getCyLayoutAlgorithmManager();
	        CyLayoutAlgorithm algo =alMan.getLayout("force-directed");
	        TaskIterator itr = algo.createTaskIterator(cyView,
	                                             algo.createLayoutContext(),
	                                             CyLayoutAlgorithm.ALL_NODE_VIEWS,
	                                            null);
	        ServiceController.getInstance().getAdapter().getTaskManager().execute(itr);
	        ServiceController.getInstance().getNetworkViewManager().addNetworkView(cyView);
	        
	        
	        cyView.updateView();
		}
		
		
	}
	
	public void updateNodeColors(CyNetworkView cyView, Hashtable<Long,CyNodeDetails> cyNodeDetailsHT){
    	Enumeration<Long> cyNodeKeys = cyNodeDetailsHT.keys();
                            
        //Crash of program was happening here
        /*..........*/
        while(cyNodeKeys.hasMoreElements()) {
            long suid= (Long) cyNodeKeys.nextElement();
            CyNodeDetails cyNodeDetails=cyNodeDetailsHT.get(suid);
            View<CyNode> nodeView=cyView.getNodeView(cyNodeDetails.getCyNode());
            //System.out.println("Testing "+(count++)+".......");
            if(nodeView!=null){	                        
                String color=cyNodeDetails.getColor();
                String nodeLabel=cyNodeDetails.getName();
                
                Color nodeColor;
                if(color.equals("present"))
                	nodeColor=new Color(255, 185, 15);
                else if(color.equals("absent"))
                	nodeColor=new Color(154, 50, 205);
                else
                	nodeColor=new Color(0, 0, 0);
                nodeView.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, nodeColor);                        
                nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL, nodeLabel);
            }
         }
    }
	
}
