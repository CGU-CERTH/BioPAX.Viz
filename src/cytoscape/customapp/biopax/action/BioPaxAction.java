package cytoscape.customapp.biopax.action;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;


//import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskIterator;

import cytoscape.customapp.biopax.ServiceController;
import cytoscape.customapp.biopax.view.BiopaxInputCustomDialog;
//import org.biopax.paxtools.io.jena.JenaIOHandler;
//import org.apache.log4j.Level;
//import org.biopax.paxtools.io.BioPAXIOHandler;
////import org.biopax.paxtools.io.SimpleIOHandler;
//import org.apache.log4j.Logger;


public class BioPaxAction extends AbstractCyAction {
    CySwingAppAdapter adapter;
	boolean hasSequences=true;
	
    private CyNetwork cyTreeNetwork;
    //private CyNetworkView cyView;
    private CyNetworkView cyTreeView;
    
    //private ArrayList<CyNodeSuid> nodes_suid_List;
    
    private ArrayList<CyNode> treeNodesList;
    private ArrayList<String> networksTitlesList;
    private ArrayList<CyNetwork> networksList;
    private ArrayList<CyNetworkView> cyViewsList;
    //private ArrayList<ArrayList<Integer>> nodeAttrsList;

    //private JFrame treeInternalFrame;
    //private Point treeViewLocationPoint;


    public static String biopaxInputFilePath = "";
    public static boolean hasChosenBioPAXFile = false;
    private static String PRESENT_PROTEIN_COLOR = "255,185,15";
    private static String ABSENT_PROTEIN_COLOR = "154,50,205";
   

    
    //private Hashtable<Long,CyNodeDetails> cyNodeDetailsHT;
	private Hashtable<Long, CyTreeNodeDetails> cyTreeNodeDetailsHT;

		
	private static final long serialVersionUID = 5873179381580327065L;
		
	public BioPaxAction() {
    	super("BioPaxVizVer1.0 (simple app)");
    	setPreferredMenu("Apps.Samples");
        setPreferredMenu("Apps");     
        cyTreeNodeDetailsHT=new Hashtable<Long, CyTreeNodeDetails>();
    }
	
	
	
    public void actionPerformed(ActionEvent e) {
        System.out.println("BioPaxViz Hello Application");
        
        Set<CyNetwork> networkSet=ServiceController.getInstance().getNetMgr().getNetworkSet();
        Set<CyNetworkView> networkViewSet=ServiceController.getInstance().getNetworkViewManager().getNetworkViewSet();
        BiopaxInputCustomDialog.SELECT_DIR = true;
        BiopaxInputCustomDialog d1 = new BiopaxInputCustomDialog();
        d1.setVisible(true);
        final File[] inOutDirs = d1.getInOutDirs();
        //Only file is chosen
        if(BiopaxInputCustomDialog.SELECT_DIR == false){
                biopaxInputFilePath = inOutDirs[0].getAbsolutePath();
                String networkDisplayName = inOutDirs[0].getName();

                
                //Set<CyNetwork> networkSet=networkManager.getNetworkSet();
                if(biopaxInputFilePath!=null){
                    Iterator networkSetIter = networkSet.iterator();
                    while(networkSetIter.hasNext()){
                    	ServiceController.getInstance().getNetMgr().destroyNetwork((CyNetwork)networkSetIter.next());
                    }
                    
                    Iterator networkViewSetIter = networkViewSet.iterator();
                    while(networkViewSetIter.hasNext()){
                    	ServiceController.getInstance().getNetworkViewManager().destroyNetworkView((CyNetworkView)networkViewSetIter.next());
                    }

                    BioPaxNetwork bpNetwork=new BioPaxNetwork(biopaxInputFilePath, networkDisplayName);
                    CyNetwork cyNetwork=bpNetwork.getCyNetwork();
                    Hashtable<Long,CyNodeDetails> cyNodeDetailsHT=bpNetwork.getCyNodeDetailsHT();
                    ServiceController.getInstance().getNetMgr().addNetwork(cyNetwork);
                    
                    CyNetworkView cyView= ServiceController.getInstance().getNetworkViewFactory().createNetworkView(cyNetwork);
                    updateNodeColors(cyView,cyNodeDetailsHT);
                    cyView.fitContent();
                    /*..........*/
                    CyLayoutAlgorithmManager alMan =ServiceController.getInstance().getAdapter().getCyLayoutAlgorithmManager();
                    CyLayoutAlgorithm algo =alMan.getLayout("force-directed");
                    TaskIterator itr = algo.createTaskIterator(cyView,
                                                         algo.createLayoutContext(),
                                                         CyLayoutAlgorithm.ALL_NODE_VIEWS,
                                                        null);
                    ServiceController.getInstance().getAdapter().getTaskManager().execute(itr);
                    ServiceController.getInstance().getNetworkViewManager().addNetworkView(cyView);
                    
                    
                    cyView.updateView();
                    //ArrayList<CyNode> nodes = (ArrayList<CyNode>) CyTableUtil.getNodesInState(ServiceController.getInstance().getCyApplicationManager().getCurrentNetwork(),"selected",true);
            		
            		//JOptionPane.showMessageDialog(null, "Number of selected nodes are "+nodes.size());
                }
            } else{


                String biopaxDirPath = inOutDirs[0].getAbsolutePath();
                if(biopaxDirPath!=null){

                	Iterator networkSetIter = networkSet.iterator();
                    while(networkSetIter.hasNext()){
                    	ServiceController.getInstance().getNetMgr().destroyNetwork((CyNetwork)networkSetIter.next());
                    }
                    
                    Iterator networkViewSetIter = networkViewSet.iterator();
                    while(networkViewSetIter.hasNext()){
                    	ServiceController.getInstance().getNetworkViewManager().destroyNetworkView((CyNetworkView)networkViewSetIter.next());
                    }
                    
                    //Update done on 20.06.2015
                    TreeNetwork treeNetwork=new TreeNetwork(biopaxDirPath);
                    cyTreeNetwork=treeNetwork.getCyTreeNetwork();
                    cyTreeNodeDetailsHT=treeNetwork.getCyTreeNodeDetailsHT();
                    createTreeView();
                    //End Update done on 20.06.2015
                    
                    File biopaxF = new File(biopaxDirPath);
                    File[] biopaxFiles = biopaxF.listFiles();


                    //---->Changes16June2015
                    ServiceController.getInstance().resetAllLists();
                    for (File tmpBpFile : biopaxFiles){

                        String tmpCurBiopaxInputFile = tmpBpFile.getAbsolutePath();
                        String tmpNetworkDisplayName = tmpBpFile.getName();
                        System.out.println("\tNetwork Display name="+tmpNetworkDisplayName);

                        if(tmpCurBiopaxInputFile.endsWith(".owl")){
                        	System.out.println("************Testing 1..............");
                        	System.out.println("Processing file "+tmpCurBiopaxInputFile);
                        	BioPaxNetwork bpNetwork=new BioPaxNetwork(tmpCurBiopaxInputFile, tmpNetworkDisplayName);
                            CyNetwork cyNetwork=bpNetwork.getCyNetwork();
                            Hashtable<Long,CyNodeDetails> cyNodeDetailsHT=bpNetwork.getCyNodeDetailsHT();
                            ServiceController.getInstance().getNetMgr().addNetwork(cyNetwork);
                            
                            ServiceController.getInstance().addToNetworksList(cyNetwork);
                            ServiceController.getInstance().addToNetworksTitlesList(tmpNetworkDisplayName);
                            //ServiceController.getInstance().addToViewsList(cyView);
                            ServiceController.getInstance().addToCyNodeDetailsHTList(cyNodeDetailsHT);
                        	
                        	System.out.println("************Testing 2..............");
                        }
                    }
                    this.networksTitlesList=ServiceController.getInstance().getNetworksTitlesList();
                    this.networksList=ServiceController.getInstance().getNetworksList();
                   
                    
                    System.out.println("Networks Title List:");
                    /*for(String s:networksTitlesList)
                    	System.out.println("\t"+s);
                   	*/
                    for(CyNetwork cyNetwork:networksList){
                    	String s = cyNetwork.getRow(cyNetwork).get(CyNetwork.NAME, String.class);
                    	System.out.println("\t"+s);
                    }
                    
                }

            }
    }
    
    public void createTreeView(){
        Set<CyNetwork> networkSet=ServiceController.getInstance().getNetMgr().getNetworkSet();
        Set<CyNetworkView> networkViewSet=ServiceController.getInstance().getNetworkViewManager().getNetworkViewSet();
        Iterator networkSetIter = networkSet.iterator();
        while(networkSetIter.hasNext()){
        	ServiceController.getInstance().getNetMgr().destroyNetwork((CyNetwork)networkSetIter.next());
        }
        
        Iterator networkViewSetIter = networkViewSet.iterator();
        while(networkViewSetIter.hasNext()){
        	ServiceController.getInstance().getNetworkViewManager().destroyNetworkView((CyNetworkView)networkViewSetIter.next());
        }
        
        
    	ServiceController.getInstance().getNetMgr().addNetwork(cyTreeNetwork);
        cyTreeView= ServiceController.getInstance().getNetworkViewFactory().createNetworkView(cyTreeNetwork);
        Enumeration<Long> cyTreeNodeKeys = cyTreeNodeDetailsHT.keys();
        
        while(cyTreeNodeKeys.hasMoreElements()) {
            long suid= (Long) cyTreeNodeKeys.nextElement();
            CyTreeNodeDetails cyTreeNodeDetails=cyTreeNodeDetailsHT.get(suid);
            View<CyNode> nodeView=cyTreeView.getNodeView(cyTreeNodeDetails.getCyNode());
            //System.out.println("Testing "+(count++)+".......");
            if(nodeView!=null){	                        
                String color=cyTreeNodeDetails.getColor();
                String nodeLabel=cyTreeNodeDetails.getName();
                
                Color nodeColor;
                if(color.equals("present"))
                	nodeColor=new Color(255, 185, 15);
                else if(color.equals("absent"))
                	nodeColor=new Color(154, 50, 205);
                else
                	nodeColor=new Color(0, 0, 0);
                nodeView.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, nodeColor);                        
                nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL, nodeLabel);
                
                nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL_WIDTH, 20.0);
            
                /////////////////
                Font font = nodeView.getVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_FACE);
            }
         }
        cyTreeView.fitContent();

        CyLayoutAlgorithmManager alMan =ServiceController.getInstance().getAdapter().getCyLayoutAlgorithmManager();
        CyLayoutAlgorithm algo =alMan.getLayout("hierarchical");
        TaskIterator itr = algo.createTaskIterator(cyTreeView,
                                             algo.createLayoutContext(),
                                             CyLayoutAlgorithm.ALL_NODE_VIEWS,
                                            null);
        ServiceController.getInstance().getAdapter().getTaskManager().execute(itr);
        ServiceController.getInstance().getNetworkViewManager().addNetworkView(cyTreeView);
        cyTreeView.updateView();
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
