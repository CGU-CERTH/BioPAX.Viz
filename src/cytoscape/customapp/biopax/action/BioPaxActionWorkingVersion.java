package cytoscape.customapp.biopax.action;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JFrame;

//import org.apache.log4j.Level;
//import org.biopax.paxtools.io.BioPAXIOHandler;
//import org.biopax.paxtools.io.SimpleIOHandler;
//import org.apache.log4j.Logger;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.Xref;
//import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskIterator;

import cytoscape.customapp.biopax.ServiceController;
import cytoscape.customapp.biopax.util.CustomBiopaxClient;
import cytoscape.customapp.biopax.view.BiopaxInputCustomDialog;
//import org.biopax.paxtools.io.jena.JenaIOHandler;


public class BioPaxActionWorkingVersion extends AbstractCyAction {
    /**
	 * 
	 */
	private ArrayList<ArrayList<String>> protToProtList = new ArrayList<ArrayList<String>>();
	CySwingAppAdapter adapter;
	private BioPAXIOHandler biopaxIO;
	
	boolean hasSequences=true;
	
    private CyNetwork cyNetworkModel = null;
    private CyNetwork cyTreeNetwork;
    private CyNetworkView cyView;
    private CyNetworkView cyTreeView;
    
    private ArrayList<CyNode> nodesList;
    //private ArrayList<CyNodeSuid> nodes_suid_List;
    
    private ArrayList<CyEdge> edgesList;
    private ArrayList<CyNode> treeNodesList;
    private ArrayList<CyNetwork> networksList;
    private ArrayList<CyNetworkView> viewsList;
    private ArrayList<String> networksTitlesList;
    private ArrayList<String[]> protIdsToNodeNames;
    private ArrayList<ArrayList<Integer>> nodeAttrsList;

    private JFrame treeInternalFrame;
    private Point treeViewLocationPoint;


    public static String biopaxInputFilePath = "";
    public static boolean hasChosenBioPAXFile = false;
    private static String customCommentStr = "$$custom comment$$";
    
    private static String PRESENT_PROTEIN_COLOR = "255,185,15";
    private static String ABSENT_PROTEIN_COLOR = "154,50,205";
   

    
    private static String NOFLAG_PROTEIN_COLOR = "0,0,0";
    private CyNetworkView activeNetworkView = null;
    private String BIOPAX_DIR_PATH = null;
	private Hashtable<Long,CyNodeDetails> cyNodeDetailsHT;

		
	private static final long serialVersionUID = 5873179381580327065L;
		
	public BioPaxActionWorkingVersion() {
    	super("BioPaxVizVer1.0 (simple app)");
    	setPreferredMenu("Apps.Samples");
        setPreferredMenu("Apps");     
        cyNodeDetailsHT=new Hashtable<Long,CyNodeDetails>();
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

                    //nodes_suid_List=new ArrayList<CyNodeSuid>();
                    nodesList = new ArrayList<CyNode>();
                    
                    edgesList = new ArrayList<CyEdge>();
                    protIdsToNodeNames = new ArrayList<String[]>();
                    nodeAttrsList = new ArrayList<ArrayList<Integer>>();
                    networksList = new ArrayList<CyNetwork>();
                    networksTitlesList = new ArrayList<String>();
                    viewsList = new ArrayList<CyNetworkView>();
                    
                    CyNetwork cyNetwork= createCyNetworkFromSingleBioPAX(biopaxInputFilePath, networkDisplayName, true);
                    ServiceController.getInstance().getNetMgr().addNetwork(cyNetwork);
                    
                    cyTreeView= ServiceController.getInstance().getNetworkViewFactory().createNetworkView(cyNetwork);
                    
                    Enumeration<Long> cyNodeKeys = cyNodeDetailsHT.keys();
                                        
                    //Crash of program was happening here
                    /*..........*/
                    while(cyNodeKeys.hasMoreElements()) {
                        long suid= (Long) cyNodeKeys.nextElement();
                        CyNodeDetails cyNodeDetails=cyNodeDetailsHT.get(suid);
                        View<CyNode> nodeView=cyTreeView.getNodeView(cyNodeDetails.getCyNode());
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
                    cyTreeView.fitContent();
                    /*..........*/
                    CyLayoutAlgorithmManager alMan =ServiceController.getInstance().getAdapter().getCyLayoutAlgorithmManager();
                    CyLayoutAlgorithm algo =alMan.getLayout("force-directed");
                    TaskIterator itr = algo.createTaskIterator(cyTreeView,
                                                         algo.createLayoutContext(),
                                                         CyLayoutAlgorithm.ALL_NODE_VIEWS,
                                                        null);
                    ServiceController.getInstance().getAdapter().getTaskManager().execute(itr);
                    ServiceController.getInstance().getNetworkViewManager().addNetworkView(cyTreeView);
                    cyTreeView.updateView();
                }
            } /*else
            {
            	
            }*/
    }
    
    
    public CyNetwork createCyNetworkFromSingleBioPAX(String biopaxInputFilePath, String networkDisplayName, boolean displayView){

        ArrayList<ArrayList<String>> protToProtList = new ArrayList<ArrayList<String>>();
        CyNetwork cyNetwork;
        cyNetwork=ServiceController.getInstance().getNetworkFactory().createNetwork();
        cyNetwork.getRow(cyNetwork).set(CyNetwork.NAME, networkDisplayName);
        
        CyTable nodeAtts, edgeAtts;
        nodeAtts=cyNetwork.getDefaultNodeTable();
        String nodeColour="node.fillColor";
        String nodeRDFId="node.RDFId";
        nodeAtts.createColumn(nodeColour, String.class, true);
        nodeAtts.createColumn(nodeRDFId, String.class, true);
        edgeAtts=cyNetwork.getDefaultEdgeTable();
        

        int edgeCnt = 0;

        try {
            CustomBiopaxClient bpClient = new CustomBiopaxClient(biopaxInputFilePath);
       
            boolean hasSequences = bpClient.getPathAndOrgName(biopaxInputFilePath);
            protToProtList = bpClient.getGeneNetworkFromBiopax(biopaxInputFilePath);
            for(int i=0;i<protToProtList.size();i++){
            	System.out.println("+++++++++++++++++++++++++++++++++++++");
            	for(int j=0;j<protToProtList.get(i).size();j++){
            		System.out.println("\t"+protToProtList.get(i).get(j));
            	}
            }

            
            bpClient.getPathAndOrgName(biopaxInputFilePath);
            Set<ProteinReference> allProtRefs = bpClient.myModel.getObjects(ProteinReference.class);
            Iterator<ProteinReference> allProtRefsIter = allProtRefs.iterator();
            Integer nodeCnt=0;

            while(allProtRefsIter.hasNext()){
                ProteinReference tmpProtRef = (ProteinReference)allProtRefsIter.next();
                
                if(hasSequences){
                    String[] tmpStrArr = new String[2];
                    tmpStrArr[0] = tmpProtRef.getRDFId();
                    String tmpProteinName = null;
                    if(tmpProtRef.getDisplayName()!=null)
                        tmpProteinName = tmpProtRef.getDisplayName();
                    else
                        tmpProteinName = tmpProtRef.getStandardName();

                    tmpStrArr[1] = tmpProteinName+" - ("+(++nodeCnt).toString()+")";
                    
                    System.out.println("\ttmpStrArr="+tmpStrArr[0]+"\t"+tmpStrArr[1]);
                    
                    protIdsToNodeNames.add(tmpStrArr);
                    CyNode tmpNode=cyNetwork.addNode();
                    cyNetwork.getRow(tmpNode).set(CyNetwork.NAME, tmpStrArr[1]);
                    nodeAtts.getRow(tmpNode.getSUID()).set(nodeRDFId,tmpStrArr[0]);
                    
                    nodesList.add(tmpNode);
                    //nodes_suid_List.add(new CyNodeSuid(tmpNode.getSUID(),tmpStrArr[1],tmpNode));

                    Set<String> coms = tmpProtRef.getComment();
                    Iterator itCom = coms.iterator();

                    boolean proteinIsPresent = false;
                    boolean proteinIsAbsent = false;
                    boolean foundExistenceFlag = false;

                    while(itCom.hasNext()){

                        String tmpCom = (String)itCom.next();

                        if(tmpCom.equals(customCommentStr+":present")){
                            System.out.println("\n[1]: The protein is present!\n");
                            foundExistenceFlag = true;
                            proteinIsPresent = true;
                            break;
                        }
                        else if(tmpCom.equals(customCommentStr+":absent")){
                            System.out.println("\n[0]: The protein is absent!\n");
                            foundExistenceFlag = true;
                            proteinIsAbsent = true;
                            break;
                        }
                    }
                   
                    String nodecolor="";
                    if(proteinIsPresent){
                        nodeAtts.getRow(tmpNode.getSUID()).set(nodeColour,PRESENT_PROTEIN_COLOR);
                        nodecolor="present";
                    } else if(proteinIsAbsent){
                        nodeAtts.getRow(tmpNode.getSUID()).set(nodeColour,ABSENT_PROTEIN_COLOR);
                        nodecolor="absent";
                    } else if(!foundExistenceFlag){
                        nodeAtts.getRow(tmpNode.getSUID()).set(nodeColour,NOFLAG_PROTEIN_COLOR);
                        nodecolor="noflag";
                    }
                    CyNodeDetails cyNodeDetails=
                    		new CyNodeDetails(tmpNode,tmpStrArr[1],tmpStrArr[0],nodecolor);
                    cyNodeDetailsHT.put(tmpNode.getSUID(), cyNodeDetails);
                     
                }
                else{

                    String nodeProteinName = "";

                    Set<Xref> xrefs = tmpProtRef.getXref();
                    Iterator xrefIter = xrefs.iterator();

                    while(xrefIter.hasNext()){

                        Xref xref = (Xref)xrefIter.next();
                        String rdfId = xref.getRDFId();
                        
                        if(rdfId.contains("kegg.genes")){

                            nodeProteinName = xref.getId();
                            CyNode tmpNode = cyNetwork.addNode();
                            cyNetwork.getRow(tmpNode).set(CyNetwork.NAME, nodeProteinName);
                            nodeAtts.getRow(tmpNode.getSUID()).set(nodeRDFId,xref.getRDFId());
                            
                            nodesList.add(tmpNode);
                            //nodes_suid_List.add(new CyNodeSuid(tmpNode.getSUID(),nodeProteinName,tmpNode));
                            
                            System.out.println("Node "+(nodeCnt)+": "+tmpNode.getSUID());
                            

                            //nodesList.add(tmpNode);

                            Set<String> coms = tmpProtRef.getComment();
                            Iterator itCom = coms.iterator();

                            boolean proteinIsPresent = false;
                            boolean proteinIsAbsent = false;
                            boolean foundExistenceFlag = false;

                            while(itCom.hasNext()){

                                String tmpCom = (String)itCom.next();

                                if(tmpCom.equals(customCommentStr+":present")){
                                    System.out.println("\n[1]: The protein is present!\n");
                                    foundExistenceFlag = true;
                                    proteinIsPresent = true;
                                    break;
                                }
                                else if(tmpCom.equals(customCommentStr+":absent")){
                                    System.out.println("\n[0]: The protein is absent!\n");
                                    foundExistenceFlag = true;
                                    proteinIsAbsent = true;
                                    break;
                                }
                            }
                            
                            String nodecolor="";
                            if(proteinIsPresent){
                                nodeAtts.getRow(tmpNode.getSUID()).set(nodeColour,PRESENT_PROTEIN_COLOR);
                                nodecolor="present";
                            } else if(proteinIsAbsent){
                                nodeAtts.getRow(tmpNode.getSUID()).set(nodeColour,ABSENT_PROTEIN_COLOR);
                                nodecolor="absent";
                            } else if(!foundExistenceFlag){
                                nodeAtts.getRow(tmpNode.getSUID()).set(nodeColour,NOFLAG_PROTEIN_COLOR);
                                nodecolor="noflag";
                            }
                            CyNodeDetails cyNodeDetails=
                            		new CyNodeDetails(tmpNode,nodeProteinName,rdfId,nodecolor);
                            cyNodeDetailsHT.put(tmpNode.getSUID(), cyNodeDetails);
                         }
                    }

                }
                
            }
            
         //executed only for biopax files with sequences
         for(int protId=0; protId<protToProtList.size(); protId++){
                System.out.println("*******In loop for protToProtList*************protId="+protId);

                CyNode nodeLeft = null;

                String curLeftNodeStr = protToProtList.get(protId).get(0);
                System.out.println(">>>Left node: "+curLeftNodeStr);

                //find left node
                System.out.println("\tprotToProtList.get(protId).get(0)="+curLeftNodeStr);
                nodeLeft = getNodeByRdfId(curLeftNodeStr);


                if(protToProtList.get(protId).size()>1){

                    //find right connected nodes
                    for(int rightId=1; rightId<protToProtList.get(protId).size(); rightId++){

                       CyNode nodeRight = null;

                       boolean breakOuterLoop = false;

                       String curRightNodeStr = protToProtList.get(protId).get(rightId);
                       System.out.println(">>>Right node: "+curRightNodeStr);

                       nodeRight = getNodeByRdfId(curRightNodeStr);

                       if(nodeLeft==null)
                           System.out.println("null node left!!!");

                       if(nodeRight==null)
                           System.out.println("null node right!!!");


                        System.out.println("\nnodeLeft: "+nodeLeft.getSUID()+" nodeRight: "+nodeRight.getSUID());
                        System.out.println("edge no.: "+(++edgeCnt));
                        CyEdge tmpEdge=cyNetwork.addEdge(nodeLeft, nodeRight, true);
                        
                        CyRow row=edgeAtts.getRow(tmpEdge.getSUID());
                        row.set("INTERACTION", "pp");
                        System.out.println("edge was created.");

                        edgesList.add(tmpEdge);
                     }
               }
            }
        } catch (IOException ex) {
        	System.out.println("Error!");
        }
        return cyNetwork;
 }         

    
    
    public CyNode getNodeByRdfId(String curProteinId){
    	//System.out.println("\n\n********In getNodeByRdfId, Must search for:\n\tcurProteinId="+curProteinId);
        CyNode returnNode = null;
        String nodeProteinName = null;

        //System.out.println("In protIdsToNodeNames, there are: ");
        for(int prIds=0; prIds<protIdsToNodeNames.size(); prIds++){

            String[] tmpArr = protIdsToNodeNames.get(prIds);
            //System.out.println("\ttmpArr="+tmpArr[0]+"\t"+tmpArr[1]);
            if(curProteinId.equals(tmpArr[0])){
                nodeProteinName = tmpArr[1];
                break;
            }
        }
        

        for(int fNodeId=0; fNodeId<nodesList.size(); fNodeId++){
                    CyNode tmpNode = nodesList.get(fNodeId);
                    //String tmpNodeName = cyNetwork.getRow(tmpNode).get(CyNetwork.NAME, String.class);
                    CyNodeDetails cyNodeDetails=cyNodeDetailsHT.get(tmpNode.getSUID());
                    String tmpNodeName=cyNodeDetails.getName();
                    //String tmpNodeName=cyNodeDetails.getNodeRDFId();
                    if(tmpNodeName.equals(nodeProteinName)){
                        returnNode = nodesList.get(fNodeId);
                        System.out.println("Found node!!!");
                        break;
                    }
        }
        /*
        for(int fNodeId=0; fNodeId<nodesList.size(); fNodeId++){

                        CyNode tmpNode = nodesList.get(fNodeId);

                        if(tmpNode.getSUID().equals(nodeProteinName)){
                            returnNode = nodesList.get(fNodeId);
                            System.out.println("Found node!!!");
                            break;
                        }
            }
			*/

        return returnNode;

    }
    
    
    
    
    public  static void output(Model model) throws IOException {
		BioPAXIOHandler simpleExporter = new SimpleIOHandler();
		OutputStream out = new ByteArrayOutputStream();
		simpleExporter.convertToOWL(model, out);
		System.out.println(out + "\n");
	}
    
    public Model readModelL3(String inputFileStr) throws FileNotFoundException{
        BioPAXIOHandler handler = new SimpleIOHandler(); //new JenaIOHandler(BioPAXLevel.L3);
        InputStream inputStreamFromFile = new FileInputStream(inputFileStr);
        Model model = handler.convertFromOWL(inputStreamFromFile);

        return model;
    }
    
    public void displayprotIdsToNodeNames(){
    	for(int i=0;i<protIdsToNodeNames.size();i++)
    	{
    		for(int j=0;j<protIdsToNodeNames.get(i).length;j++)
    			System.out.println("\t"+protIdsToNodeNames.get(i)[j]);
    	}
    }
}
