package cytoscape.customapp.biopax.action;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

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

	public class TreeNetwork {
	 	private CyNetwork cyTreeNetwork;
	    private CyNetworkView cyView;
	    private CyNetworkView cyTreeView;
	    
	    //private ArrayList<CyNodeSuid> nodes_suid_List;
	    
	    private ArrayList<CyNode> treeNodesList;
	    private ArrayList<String> networksTitlesList;
	    private ArrayList<CyNetwork> networksList;
	    private String biopaxDirPath;
	    //private ArrayList<ArrayList<Integer>> nodeAttrsList;
		private Hashtable<Long, CyTreeNodeDetails> cyTreeNodeDetailsHT;
		private String PRESENT_PROTEIN_COLOR = "255,185,15";
	    private String ABSENT_PROTEIN_COLOR = "154,50,205";


	    //private JFrame treeInternalFrame;
	    //private Point treeViewLocationPoint;


	    public static String biopaxInputFilePath = "";
	    
	public TreeNetwork(String biopaxDirPath){
	    	this.biopaxDirPath=biopaxDirPath;
	    	treeNodesList = new ArrayList<CyNode>();
	        //cyTreeNetwork = Cytoscape.createNetwork("Tree", false);
	        cyTreeNetwork = ServiceController.getInstance().getNetworkFactory().createNetwork();
	        cyTreeNetwork.getRow(cyTreeNetwork).set(CyNetwork.NAME, "Tree");
	        //ServiceController.getInstance().getNetMgr().addNetwork(cyTreeNetwork);
	        //cyTreeView= ServiceController.getInstance().getNetworkViewFactory().createNetworkView(cyTreeNetwork);
	        cyTreeNodeDetailsHT=new Hashtable<Long, CyTreeNodeDetails>();
	        try {
				createTree();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void createTree() throws FileNotFoundException, IOException{   
        File biopaxF = new File(biopaxDirPath);
        File[] biopaxFiles = biopaxF.listFiles();
        CyTable nodeAtts, edgeAtts;
        nodeAtts=cyTreeNetwork.getDefaultNodeTable();
        String nodeColour="node.fillColor";
        //String nodeRDFId="node.RDFId";
        nodeAtts.createColumn(nodeColour, String.class, true);
        //nodeAtts.createColumn(nodeRDFId, String.class, true);
        edgeAtts=cyTreeNetwork.getDefaultEdgeTable();

        for (File bpFile : biopaxFiles){

                String curInputFile = bpFile.getAbsolutePath();

                if(curInputFile.endsWith(".nodes")){

                    FileInputStream fstream = new FileInputStream(curInputFile);

                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));

                    String strLine = "";
                    

                    while ((strLine = br.readLine())!=null){
                        
                        StringTokenizer st = new StringTokenizer(strLine,"\t");
                        String tmpNodeStr = st.nextToken();
                        System.out.println("tmpNodeStr: "+tmpNodeStr);

                        CyNode tmpNode = cyTreeNetwork.addNode();
                        cyTreeNetwork.getRow(tmpNode).set(CyNetwork.NAME, tmpNodeStr);
                        treeNodesList.add(tmpNode);                      
                        

                        boolean pathExists = false;

                        for (File bpFileInternal : biopaxFiles){

                            String curInputFileInternalPath = bpFileInternal.getAbsolutePath();
                            String curInputFileInternal = bpFileInternal.getName();
                            StringTokenizer stFileStart = new StringTokenizer(curInputFileInternal,"_");
                            String tmpFileStartStr = stFileStart.nextToken();


                            if(tmpNodeStr.startsWith(tmpFileStartStr)){
                                CustomBiopaxClient bpClient = new CustomBiopaxClient(curInputFileInternalPath);
                                pathExists = bpClient.checkExistenceOfThePath(curInputFileInternalPath);
                                String nodecolor="";
                                if(pathExists){
                                	nodeAtts.getRow(tmpNode.getSUID()).set(nodeColour,PRESENT_PROTEIN_COLOR);
                                    nodecolor="present";
                                } else {
                                    nodeAtts.getRow(tmpNode.getSUID()).set(nodeColour,ABSENT_PROTEIN_COLOR);
                                    nodecolor="absent";
                                } 
                                CyTreeNodeDetails cyTreeNodeDetails=
                                		new CyTreeNodeDetails(tmpNode, tmpNodeStr,nodecolor);
                                cyTreeNodeDetailsHT.put(tmpNode.getSUID(), cyTreeNodeDetails);
                                break;
                            }
                        }

                        //CyAttributes nodeAtts = Cytoscape.getNodeAttributes();

                        /*
                        if(pathExists)
                            nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",PRESENT_PROTEIN_COLOR);
                        else
                            nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",ABSENT_PROTEIN_COLOR);
						*/
                        
                    }
                    in.close();


                    fstream = new FileInputStream(curInputFile);

                    in = new DataInputStream(fstream);
                    br = new BufferedReader(new InputStreamReader(in));

                    strLine = "";


                    while ((strLine = br.readLine())!=null){

                        StringTokenizer st = new StringTokenizer(strLine,"\t");

                        String tmpNodeStrLeft = st.nextToken();
                        CyNode tmpNodeLeft = null;

                        ////////////////////
                        //System.out.println("***********>tmpNodeStrLeft="+tmpNodeStrLeft);
                        for(int nId=0; nId<treeNodesList.size(); nId++){
                        	////////////////////
                        	CyTreeNodeDetails cyTreeNodeDetails=cyTreeNodeDetailsHT.get(treeNodesList.get(nId).getSUID());
                            String tmpNodeName=cyTreeNodeDetails.getName();
                        	//System.out.println("\ttmpNodeName="+tmpNodeName);
                            if(tmpNodeStrLeft.equals(tmpNodeName)){
                                tmpNodeLeft = treeNodesList.get(nId);
                                break;
                            }
                        }

                        
                        String tmpNodeStrRight = st.nextToken();
                        CyNode tmpNodeRight = null;

                        if(tmpNodeStrRight!=null){
                            for(int nId=0; nId<treeNodesList.size(); nId++){
                            	CyTreeNodeDetails cyTreeNodeDetails=cyTreeNodeDetailsHT.get(treeNodesList.get(nId).getSUID());
                                String tmpNodeName=cyTreeNodeDetails.getName();
                            	//System.out.println("\ttmpNodeName="+tmpNodeName);
                                if(tmpNodeStrRight.equals(tmpNodeName)){
                                    tmpNodeRight = treeNodesList.get(nId);
                                    break;
                                }
                            }
                        }

                        if(tmpNodeRight!=null){
                            //CyEdge tmpEdge = Cytoscape.getCyEdge(tmpNodeLeft, tmpNodeRight, Semantics.INTERACTION, "pp", true);
                            //cyTreeNetwork.addEdge(tmpEdge);
                        	CyEdge tmpEdge=cyTreeNetwork.addEdge(tmpNodeLeft, tmpNodeRight, true);
                            
                            CyRow row=edgeAtts.getRow(tmpEdge.getSUID());
                            row.set("INTERACTION", "pp");
                        }

                    }
                }
          }
    }    
	
	public CyNetwork getCyTreeNetwork(){
    	return cyTreeNetwork;
    }
	
	public Hashtable<Long, CyTreeNodeDetails> getCyTreeNodeDetailsHT(){
		return cyTreeNodeDetailsHT;
	}

}
