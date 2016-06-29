package cytoscape.customapp.biopax.action;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.Xref;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import cytoscape.customapp.biopax.ServiceController;
import cytoscape.customapp.biopax.util.CustomBiopaxClient;

public class BioPaxNetwork {
	//private String BIOPAX_DIR_PATH;
	private ArrayList<ArrayList<String>> protToProtList;
    private CyNetwork cyNetwork;
    private ArrayList<CyNode> nodesList;    
    private ArrayList<CyEdge> edgesList;
    private ArrayList<String[]> protIdsToNodeNames;
    public  String biopaxInputFilePath = "";
    private String networkDisplayName="";
    public  boolean hasChosenBioPAXFile = false;
    private  String customCommentStr = "$$custom comment$$";
    private  String PRESENT_PROTEIN_COLOR = "255,185,15";
    private  String ABSENT_PROTEIN_COLOR = "154,50,205";
    private  String NOFLAG_PROTEIN_COLOR = "0,0,0";
    private Hashtable<Long,CyNodeDetails> cyNodeDetailsHT;
	private Hashtable<Long, CyTreeNodeDetails> cyTreeNodeDetailsHT;
	
	public BioPaxNetwork(String biopaxDirPath, String networkDisplayName){
		this.biopaxInputFilePath=biopaxDirPath;
		this.networkDisplayName=networkDisplayName;
		this.protToProtList = new ArrayList<ArrayList<String>>();
		this.nodesList= new ArrayList<CyNode>() ;    
		this.edgesList=new ArrayList<CyEdge>();
		this.protIdsToNodeNames=new ArrayList<String[]>();
	    this.cyNodeDetailsHT=new Hashtable<Long,CyNodeDetails>();
	    this.cyTreeNodeDetailsHT=new Hashtable<Long, CyTreeNodeDetails>();
	    
	    this.cyNetwork=createCyNetworkFromSingleBioPAX();
	}
	
	public CyNetwork createCyNetworkFromSingleBioPAX(){

        ArrayList<ArrayList<String>> protToProtList = new ArrayList<ArrayList<String>>();
        CyNetwork cyNetwork;
        cyNetwork=ServiceController.getInstance().getNetworkFactory().createNetwork();
        cyNetwork.getRow(cyNetwork).set(CyNetwork.NAME, this.networkDisplayName);
        
        CyTable nodeAtts, edgeAtts;
        nodeAtts=cyNetwork.getDefaultNodeTable();
        String nodeColour="node.fillColor";
        String nodeRDFId="node.RDFId";
        nodeAtts.createColumn(nodeColour, String.class, true);
        nodeAtts.createColumn(nodeRDFId, String.class, true);
        edgeAtts=cyNetwork.getDefaultEdgeTable();
        

        int edgeCnt = 0;

        try {
            CustomBiopaxClient bpClient = new CustomBiopaxClient(this.biopaxInputFilePath);
       
            boolean hasSequences = bpClient.getPathAndOrgName(this.biopaxInputFilePath);
            protToProtList = bpClient.getGeneNetworkFromBiopax(this.biopaxInputFilePath);

            
            bpClient.getPathAndOrgName(this.biopaxInputFilePath);
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
            Logger.getLogger(BioPaxAction.class.getName()).log(Level.SEVERE, null, ex);
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
 
	 public CyNetwork getCyNetwork(){
		 return this.cyNetwork;
	 }
     
	 public Hashtable<Long,CyNodeDetails> getCyNodeDetailsHT(){
		 return this.cyNodeDetailsHT;
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

