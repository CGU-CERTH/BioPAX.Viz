package cytoscape.customplugins.biopax.action;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.customplugins.biopax.BiopaxPlugin;
import cytoscape.customplugins.biopax.util.CustomBiopaxClient;
import cytoscape.customplugins.biopax.view.BiopaxInputCustomDialog;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.CyLayouts;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;
import ding.view.DGraphView;
import giny.view.NodeView;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.Xref;


public class CustomPathwayVisualizerAction extends CytoscapeAction {

    private CyNetwork cyNetwork;
    private CyNetwork cyNetworkModel = null;
    private CyNetwork cyTreeNetwork;
    private CyNetworkView cyView;
    private CyNetworkView cyTreeView;
    private ArrayList<CyNode> nodesList;
    private ArrayList<CyEdge> edgesList;
    private ArrayList<CyNode> treeNodesList;
    private ArrayList<CyNetwork> networksList;
    private ArrayList<CyNetworkView> viewsList;
    private ArrayList<String> networksTitlesList;
    private ArrayList<String[]> protIdsToNodeNames;
    private ArrayList<ArrayList<Integer>> nodeAttrsList;

    private JInternalFrame treeInternalFrame;
    private Point treeViewLocationPoint;


    public static String biopaxInputFilePath = "";
    public static boolean hasChosenBioPAXFile = false;
    private static String customCommentStr = "$$custom comment$$";
    
    private static String PRESENT_PROTEIN_COLOR = "255,185,15";
    private static String ABSENT_PROTEIN_COLOR = "154,50,205";
   

    
    private static String NOFLAG_PROTEIN_COLOR = "0,0,0";
    private CyNetworkView activeNetworkView = null;
    private String BIOPAX_DIR_PATH = null;
    
        public CustomPathwayVisualizerAction(BiopaxPlugin myPlugin) {

                /*
                 * Define the name of the plugin as it will be displayed on the 'Plugins' Menu.
                 */
                super("BioPAX Viz");

                // The default menu where the plugin will be displayed on.
                setPreferredMenu("Plugins");
                treeViewLocationPoint = new Point(0,0);
                
        }
        

        public void createTree(String biopaxDirPath) throws FileNotFoundException, IOException{

            treeNodesList = new ArrayList<CyNode>();
            cyTreeNetwork = Cytoscape.createNetwork("Tree", false);

            File biopaxF = new File(biopaxDirPath);
            File[] biopaxFiles = biopaxF.listFiles();

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

                            CyNode tmpNode = Cytoscape.getCyNode(tmpNodeStr, true);
                            cyTreeNetwork.addNode(tmpNode);
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
                                    break;
                                }
                            }

                            CyAttributes nodeAtts = Cytoscape.getNodeAttributes();

                            if(pathExists)
                                nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",PRESENT_PROTEIN_COLOR);
                            else
                                nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",ABSENT_PROTEIN_COLOR);

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

                            for(int nId=0; nId<treeNodesList.size(); nId++){
                                if(tmpNodeStrLeft.equals(treeNodesList.get(nId).getIdentifier())){
                                    tmpNodeLeft = treeNodesList.get(nId);
                                    break;
                                }
                            }

                            
                            String tmpNodeStrRight = st.nextToken();
                            CyNode tmpNodeRight = null;

                            if(tmpNodeStrRight!=null){
                                for(int nId=0; nId<treeNodesList.size(); nId++){
                                    if(tmpNodeStrRight.equals(treeNodesList.get(nId).getIdentifier())){
                                        tmpNodeRight = treeNodesList.get(nId);
                                        break;
                                    }
                                }
                            }

                            if(tmpNodeRight!=null){
                                CyEdge tmpEdge = Cytoscape.getCyEdge(tmpNodeLeft, tmpNodeRight, Semantics.INTERACTION, "pp", true);
                                cyTreeNetwork.addEdge(tmpEdge);
                            }

                        }

                        cyTreeView = Cytoscape.createNetworkView(cyTreeNetwork, "Tree");
                        CyLayoutAlgorithm algo = CyLayouts.getLayout("hierarchical");
                        cyTreeView.applyLayout(algo);


                        CustomMouseListener mouse = new CustomMouseListener();
                        cyTreeView.getComponent().addMouseListener(mouse);
                        

                          
                        treeInternalFrame =
                        Cytoscape.getDesktop().getNetworkViewManager().getInternalFrame(cyTreeView);
                        try {
                           treeInternalFrame.setLocation(0,0);
                           treeInternalFrame.moveToFront();
                           treeInternalFrame.setSelected(true);
                           treeInternalFrame.setMaximizable(false);
                           treeInternalFrame.setVisible(true);

                        }
                        catch (Exception ex)
                        {}
                        
                        break;
                    }
            }

        }
        
        public void actionPerformed(ActionEvent e) {
        
            
            BiopaxInputCustomDialog.SELECT_DIR = true;

            final CytoscapeDesktop desktop = Cytoscape.getDesktop();
            BiopaxInputCustomDialog d1 = new BiopaxInputCustomDialog(desktop);
            d1.setVisible(true);
            final File[] inOutDirs = d1.getInOutDirs();


            if(BiopaxInputCustomDialog.SELECT_DIR == false){
                biopaxInputFilePath = inOutDirs[0].getAbsolutePath();
                String networkDisplayName = inOutDirs[0].getName();

                if(biopaxInputFilePath!=null){
                    
                    Set<CyNetwork> networkSet = Cytoscape.getNetworkSet();
                    Iterator networkSetIter = networkSet.iterator();

                    while(networkSetIter.hasNext()){
                        Cytoscape.destroyNetwork((CyNetwork)networkSetIter.next());
                    }

                    nodesList = new ArrayList<CyNode>();
                    edgesList = new ArrayList<CyEdge>();
                    protIdsToNodeNames = new ArrayList<String[]>();
                    nodeAttrsList = new ArrayList<ArrayList<Integer>>();
                    networksList = new ArrayList<CyNetwork>();
                    networksTitlesList = new ArrayList<String>();
                    viewsList = new ArrayList<CyNetworkView>();

                    createCyNetworkFromSingleBioPAX(biopaxInputFilePath, networkDisplayName, true);
                }
            } else{


                String biopaxDirPath = inOutDirs[0].getAbsolutePath();
                BIOPAX_DIR_PATH = biopaxDirPath;

                if(biopaxDirPath!=null){

                    Set<CyNetwork> networkSet = Cytoscape.getNetworkSet();
                    Iterator networkSetIter = networkSet.iterator();

                    while(networkSetIter.hasNext()){
                        Cytoscape.destroyNetwork((CyNetwork)networkSetIter.next());
                    }


                    nodesList = new ArrayList<CyNode>();
                    edgesList = new ArrayList<CyEdge>();
                    protIdsToNodeNames = new ArrayList<String[]>();
                    nodeAttrsList = new ArrayList<ArrayList<Integer>>();
                    networksList = new ArrayList<CyNetwork>();
                    networksTitlesList = new ArrayList<String>();
                    viewsList = new ArrayList<CyNetworkView>();

                    
                    try {
                        createTree(biopaxDirPath);
                    } catch (Exception ex) {
                        Logger.getLogger(CustomPathwayVisualizerAction.class.getName()).log(Level.SEVERE, null, ex);
                    }



                    File biopaxF = new File(biopaxDirPath);
                    File[] biopaxFiles = biopaxF.listFiles();


                    for (File tmpBpFile : biopaxFiles){

                        String tmpCurBiopaxInputFile = tmpBpFile.getAbsolutePath();
                        String tmpNetworkDisplayName = tmpBpFile.getName();

                        if(tmpCurBiopaxInputFile.endsWith(".owl")){
                            createCyNetworkModelFromBioPAX(tmpCurBiopaxInputFile, tmpNetworkDisplayName);
                            break;
                        }
                    }


                    for (File bpFile : biopaxFiles){

                        String curBiopaxInputFile = bpFile.getAbsolutePath();
                        String networkDisplayName = bpFile.getName();

                        if(curBiopaxInputFile.endsWith(".owl")){
                            CyNetwork tmpCyNetwork = createCyNetworkFromModelAndBioPAX(curBiopaxInputFile, networkDisplayName);
                            networksTitlesList.add(networkDisplayName);
                            networksList.add(tmpCyNetwork);


                        }
                    }

                }

            }
            
            

        }


        public void createCyNetworkModelFromBioPAX(String biopaxInputFilePath, String networkDisplayName){


            ArrayList<ArrayList<String>> protToProtList = new ArrayList<ArrayList<String>>();

            int edgeCnt = 0;

            try {

                CustomBiopaxClient bpClient = new CustomBiopaxClient(biopaxInputFilePath);
                boolean hasSequences = bpClient.getPathAndOrgName(biopaxInputFilePath);
                protToProtList = bpClient.getGeneNetworkFromBiopax(biopaxInputFilePath);

                bpClient.getPathAndOrgName(biopaxInputFilePath);

               
                Set<ProteinReference> allProtRefs = bpClient.myModel.getObjects(ProteinReference.class);
                Iterator allProtRefsIter = allProtRefs.iterator();

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

                        protIdsToNodeNames.add(tmpStrArr);

                        CyNode tmpNode = Cytoscape.getCyNode(tmpStrArr[1], true);
                      
                        nodesList.add(tmpNode);
                      
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


                                CyNode tmpNode = Cytoscape.getCyNode(nodeProteinName, true);

                                nodesList.add(tmpNode);

                            }
                        }

                    }

                }


                //executed only for biopax files with sequences
                for(int protId=0; protId<protToProtList.size(); protId++){

                    CyNode nodeLeft = null;

                    String curLeftNodeStr = protToProtList.get(protId).get(0);
                  
                    //find left node
                    nodeLeft = getNodeByRdfId(curLeftNodeStr);


                    if(protToProtList.get(protId).size()>1){

                        //find right connected nodes
                        for(int rightId=1; rightId<protToProtList.get(protId).size(); rightId++){

                           CyNode nodeRight = null;

                           boolean breakOuterLoop = false;

                           String curRightNodeStr = protToProtList.get(protId).get(rightId);


                           nodeRight = getNodeByRdfId(curRightNodeStr);

                           if(nodeLeft==null)
                               System.out.println("null node left!!!");

                           if(nodeRight==null)
                               System.out.println("null node right!!!");


                            CyEdge tmpEdge = Cytoscape.getCyEdge(nodeLeft, nodeRight, Semantics.INTERACTION, "pp", true);

                            edgesList.add(tmpEdge);

                        }
                   }
                }

            } catch (IOException ex) {
                Logger.getLogger(CustomPathwayVisualizerAction.class.getName()).log(Level.SEVERE, null, ex);
            }



        }


        public CyNetwork createCyNetworkFromModelAndBioPAX(String biopaxInputFilePath, String networkDisplayName){

            int curNodesCounter = 0;
            ArrayList<Integer> tmpNodeAttrsList = new ArrayList<Integer>();

            ArrayList<ArrayList<String>> protToProtList = new ArrayList<ArrayList<String>>();
            
            //copy cyNetworkModel
            CyNetwork cyNetwork = Cytoscape.createNetwork(networkDisplayName, false);

            for(int nId=0; nId<nodesList.size(); nId++){
                CyNode tmpNode = nodesList.get(nId);
                cyNetwork.addNode(tmpNode);
            }
            for(int eId=0; eId<edgesList.size(); eId++){
                CyEdge tmpEdge = edgesList.get(eId);
                cyNetwork.addEdge(tmpEdge);
            }


            int edgeCnt = 0;

            try {

                CustomBiopaxClient bpClient = new CustomBiopaxClient(biopaxInputFilePath);
                boolean hasSequences = bpClient.getPathAndOrgName(biopaxInputFilePath);
                protToProtList = bpClient.getGeneNetworkFromBiopax(biopaxInputFilePath);

                bpClient.getPathAndOrgName(biopaxInputFilePath);


                Set<ProteinReference> allProtRefs = bpClient.myModel.getObjects(ProteinReference.class);
                Iterator allProtRefsIter = allProtRefs.iterator();

                Integer nodeCnt=0;

                while(allProtRefsIter.hasNext()){                    

                    ProteinReference tmpProtRef = (ProteinReference)allProtRefsIter.next();

                    String tmpProteinName = null;
                    if(tmpProtRef.getDisplayName()!=null)
                        tmpProteinName = tmpProtRef.getDisplayName();
                    else
                        tmpProteinName = tmpProtRef.getStandardName();

                    int tmpNodeId = -1;
                    
                    for(Integer nlId=0; nlId<nodesList.size(); nlId++){
                        
                        int tmpNlId = nlId+1;

                        
                        if((tmpProteinName+" - ("+tmpNlId+")").equals(nodesList.get(nlId).getIdentifier())){
                            //System.out.println("tmpProteinName-nodesList.get(): EQUAL!!!");
                            tmpNodeId = nlId;
                            break;
                        }
                    }

                    CyNode tmpNode = nodesList.get(tmpNodeId);
                    curNodesCounter++;
                    

                    if(hasSequences){
                        
                        Set<String> coms = tmpProtRef.getComment();
                        Iterator itCom = coms.iterator();

                        boolean proteinIsPresent = false;
                        boolean proteinIsAbsent = false;
                        boolean foundExistenceFlag = false;

                        while(itCom.hasNext()){

                            String tmpCom = (String)itCom.next();

                            if(tmpCom.equals(customCommentStr+":present")){
                                foundExistenceFlag = true;
                                proteinIsPresent = true;
                                break;
                            }
                            else if(tmpCom.equals(customCommentStr+":absent")){
                                foundExistenceFlag = true;
                                proteinIsAbsent = true;
                                break;
                            }
                        }


                        if(proteinIsPresent){
                            tmpNodeAttrsList.add(1);
                        } else if(proteinIsAbsent){
                            tmpNodeAttrsList.add(0);
                        } else if(!foundExistenceFlag){
                            tmpNodeAttrsList.add(-1);
                        }

                        
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

                                Set<String> coms = tmpProtRef.getComment();
                                Iterator itCom = coms.iterator();

                                boolean proteinIsPresent = false;
                                boolean proteinIsAbsent = false;
                                boolean foundExistenceFlag = false;

                                while(itCom.hasNext()){

                                    String tmpCom = (String)itCom.next();

                                    if(tmpCom.equals(customCommentStr+":present")){
                                        foundExistenceFlag = true;
                                        proteinIsPresent = true;
                                        break;
                                    }
                                    else if(tmpCom.equals(customCommentStr+":absent")){
                                        foundExistenceFlag = true;
                                        proteinIsAbsent = true;
                                        break;
                                    }
                                }

                                if(proteinIsPresent){
                                    CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
                                    nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",PRESENT_PROTEIN_COLOR);
                                } else if(proteinIsAbsent){
                                    CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
                                    nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",ABSENT_PROTEIN_COLOR);
                                } else if(!foundExistenceFlag){
                                    CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
                                    nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",NOFLAG_PROTEIN_COLOR);
                                }

                            }
                        }

                    }

                }


            } catch (IOException ex) {
                Logger.getLogger(CustomPathwayVisualizerAction.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println("$$$ curNodesCounter: "+curNodesCounter);

            nodeAttrsList.add(tmpNodeAttrsList);

            return cyNetwork;

        }


        public CyNetwork updateNodesColorAtANetwork(String biopaxInputFilePath, String networkDisplayName){

            CyNetwork cyNetwork = Cytoscape.createNetwork(networkDisplayName, false);
            
            int curNodesCounter = 0;

            ArrayList<ArrayList<String>> protToProtList = new ArrayList<ArrayList<String>>();


            int edgeCnt = 0;

            try {

                CustomBiopaxClient bpClient = new CustomBiopaxClient(biopaxInputFilePath);
                boolean hasSequences = bpClient.getPathAndOrgName(biopaxInputFilePath);
                protToProtList = bpClient.getGeneNetworkFromBiopax(biopaxInputFilePath);

                bpClient.getPathAndOrgName(biopaxInputFilePath);


                Set<ProteinReference> allProtRefs = bpClient.myModel.getObjects(ProteinReference.class);
                Iterator allProtRefsIter = allProtRefs.iterator();

                Integer nodeCnt=0;

                while(allProtRefsIter.hasNext()){

                    ProteinReference tmpProtRef = (ProteinReference)allProtRefsIter.next();

                    String tmpProteinName = null;
                    if(tmpProtRef.getDisplayName()!=null)
                        tmpProteinName = tmpProtRef.getDisplayName();
                    else
                        tmpProteinName = tmpProtRef.getStandardName();

                    int tmpNodeId = -1;

                    for(Integer nlId=0; nlId<nodesList.size(); nlId++){

                        int tmpNlId = nlId+1;

                        System.out.println("tmpProteinName: "+tmpProteinName+" - ("+tmpNlId+")");
                        System.out.println("nodesList.get("+tmpNlId+"): "+nodesList.get(nlId).getIdentifier());

                        if((tmpProteinName+" - ("+tmpNlId+")").equals(nodesList.get(nlId).getIdentifier())){
                            System.out.println("tmpProteinName-nodesList.get(): EQUAL!!!");
                            tmpNodeId = nlId;
                            break;
                        }
                    }

                    CyNode tmpNode = nodesList.get(tmpNodeId);
                    cyNetwork.addNode(tmpNode);

                    curNodesCounter++;
                    

                    if(hasSequences){

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

                        if(proteinIsPresent){
                            CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
                            nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",PRESENT_PROTEIN_COLOR);
                        } else if(proteinIsAbsent){
                            CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
                            nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",ABSENT_PROTEIN_COLOR);
                        } else if(!foundExistenceFlag){
                            CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
                            nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",NOFLAG_PROTEIN_COLOR);
                        }
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

                                if(proteinIsPresent){
                                    CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
                                    nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",PRESENT_PROTEIN_COLOR);
                                } else if(proteinIsAbsent){
                                    CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
                                    nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",ABSENT_PROTEIN_COLOR);
                                } else if(!foundExistenceFlag){
                                    CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
                                    nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",NOFLAG_PROTEIN_COLOR);
                                }

                            }
                        }

                    }

                }


                
                //executed only for biopax files with sequences
                for(int protId=0; protId<protToProtList.size(); protId++){

                    CyNode nodeLeft = null;

                    String curLeftNodeStr = protToProtList.get(protId).get(0);
                    System.out.println(">>>Left node: "+curLeftNodeStr);

                    //find left node
                    nodeLeft = getNodeByRdfId(curLeftNodeStr);


                    if(protToProtList.get(protId).size()>1){

                        //find right connected nodes
                        for(int rightId=1; rightId<protToProtList.get(protId).size(); rightId++){

                           CyNode nodeRight = null;

                           boolean breakOuterLoop = false;

                           String curRightNodeStr = protToProtList.get(protId).get(rightId);


                           nodeRight = getNodeByRdfId(curRightNodeStr);

                           if(nodeLeft==null)
                               System.out.println("null node left!!!");

                           if(nodeRight==null)
                               System.out.println("null node right!!!");


                           System.out.println("\nnodeLeft: "+nodeLeft.getIdentifier()+" nodeRight: "+nodeRight.getIdentifier());
                           System.out.println("edge no.: "+(++edgeCnt));
                            CyEdge tmpEdge = Cytoscape.getCyEdge(nodeLeft, nodeRight, Semantics.INTERACTION, "pp", true);
                            System.out.println("edge was created.");


                            cyNetwork.addEdge(tmpEdge);
                        }
                   }
                }


            } catch (IOException ex) {
                Logger.getLogger(CustomPathwayVisualizerAction.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println("$$$ curNodesCounter: "+curNodesCounter);

            return cyNetwork;

        }



        public CyNetwork createCyNetworkFromSingleBioPAX(String biopaxInputFilePath, String networkDisplayName, boolean displayView){

            ArrayList<ArrayList<String>> protToProtList = new ArrayList<ArrayList<String>>();

            int edgeCnt = 0;

            try {



                CustomBiopaxClient bpClient = new CustomBiopaxClient(biopaxInputFilePath);
                boolean hasSequences = bpClient.getPathAndOrgName(biopaxInputFilePath);
                protToProtList = bpClient.getGeneNetworkFromBiopax(biopaxInputFilePath);

                bpClient.getPathAndOrgName(biopaxInputFilePath);

                cyNetwork = Cytoscape.createNetwork(networkDisplayName, false);



                Set<ProteinReference> allProtRefs = bpClient.myModel.getObjects(ProteinReference.class);
                Iterator allProtRefsIter = allProtRefs.iterator();

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

                        protIdsToNodeNames.add(tmpStrArr);

                        CyNode tmpNode = Cytoscape.getCyNode(tmpStrArr[1], true);
                   

                        nodesList.add(tmpNode);

                        cyNetwork.addNode(tmpNode);

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

                        if(proteinIsPresent){
                            CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
                            nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",PRESENT_PROTEIN_COLOR);
                        } else if(proteinIsAbsent){
                            CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
                            nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",ABSENT_PROTEIN_COLOR);
                        } else if(!foundExistenceFlag){
                            CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
                            nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",NOFLAG_PROTEIN_COLOR);
                        }
                         
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


                                CyNode tmpNode = Cytoscape.getCyNode(nodeProteinName, true);
                                System.out.println("Node "+(nodeCnt)+": "+tmpNode.getIdentifier());
                    

                                nodesList.add(tmpNode);

                                cyNetwork.addNode(tmpNode);

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

                                if(proteinIsPresent){
                                    CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
                                    nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",PRESENT_PROTEIN_COLOR);
                                } else if(proteinIsAbsent){
                                    CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
                                    nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",ABSENT_PROTEIN_COLOR);
                                } else if(!foundExistenceFlag){
                                    CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
                                    nodeAtts.setAttribute(tmpNode.getIdentifier(),"node.fillColor",NOFLAG_PROTEIN_COLOR);
                                }

                            }
                        }

                    }

                }


                //executed only for biopax files with sequences
                for(int protId=0; protId<protToProtList.size(); protId++){

                    CyNode nodeLeft = null;

                    String curLeftNodeStr = protToProtList.get(protId).get(0);
                    System.out.println(">>>Left node: "+curLeftNodeStr);

                    //find left node
                    nodeLeft = getNodeByRdfId(curLeftNodeStr);


                    if(protToProtList.get(protId).size()>1){

                        //find right connected nodes
                        for(int rightId=1; rightId<protToProtList.get(protId).size(); rightId++){

                           CyNode nodeRight = null;

                           boolean breakOuterLoop = false;

                           String curRightNodeStr = protToProtList.get(protId).get(rightId);


                           nodeRight = getNodeByRdfId(curRightNodeStr);

                           if(nodeLeft==null)
                               System.out.println("null node left!!!");

                           if(nodeRight==null)
                               System.out.println("null node right!!!");


                           System.out.println("\nnodeLeft: "+nodeLeft.getIdentifier()+" nodeRight: "+nodeRight.getIdentifier());
                           System.out.println("edge no.: "+(++edgeCnt));
                            CyEdge tmpEdge = Cytoscape.getCyEdge(nodeLeft, nodeRight, Semantics.INTERACTION, "pp", true);
                            System.out.println("edge was created.");

                            edgesList.add(tmpEdge);
                          

                            cyNetwork.addEdge(tmpEdge);
                          
                        }
                   }
                }


                if(displayView){

                    if(hasSequences){
                        cyView = Cytoscape.createNetworkView(cyNetwork, networkDisplayName);
                        CyLayoutAlgorithm algo = CyLayouts.getLayout("force-directed");
                        cyView = Cytoscape.getCurrentNetworkView();
                        cyView.applyLayout(algo);
                    }
                    else{
                        cyView = Cytoscape.createNetworkView(cyNetwork, networkDisplayName);
                        CyLayoutAlgorithm algo = CyLayouts.getLayout("force-directed");
                        cyView = Cytoscape.getCurrentNetworkView();
                        cyView.applyLayout(algo);
                    }

                    javax.swing.JInternalFrame f =
                    Cytoscape.getDesktop().getNetworkViewManager().getInternalFrame(cyView);
                    try {
                       f.setMaximum(true);
                    }
                    catch (Exception ex)
                    {}

                }
                else {

                    /*
                    do_nothing...
                     */

                }


            } catch (IOException ex) {
                Logger.getLogger(CustomPathwayVisualizerAction.class.getName()).log(Level.SEVERE, null, ex);
            }

            return cyNetwork;

        }



        public static String getGeneNameFromKegg(String geneId) throws IOException {

            String keggRequest = "http://rest.kegg.jp/get/"+geneId;
            String geneName = "";

            HttpClient client = new HttpClient();
            GetMethod method = new GetMethod(keggRequest);

            // Send GET request
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
              System.err.println("Method failed: " + method.getStatusLine());
            }
            InputStream rstream = null;

            // Get the response body
            rstream = method.getResponseBodyAsStream();

            // Process the response

            BufferedReader br = new BufferedReader(new InputStreamReader(rstream));
            String line;


            while ((line = br.readLine()) != null) {

                if(line.contains("NAME")){
                    StringTokenizer st = new StringTokenizer(line," ");
                    st.nextToken();
                    geneName += st.nextToken()+", ";
                }
                else if(line.contains("DEFINITION")){
                    geneName += line.substring(12, line.length());
                    break;
                }
            }

            br.close();

            return geneName;
       }

        public CyNode getNodeByRdfId(String curProteinId){

            CyNode returnNode = null;
            String nodeProteinName = null;

            for(int prIds=0; prIds<protIdsToNodeNames.size(); prIds++){

                String[] tmpArr = protIdsToNodeNames.get(prIds);
                if(curProteinId.equals(tmpArr[0])){
                    nodeProteinName = tmpArr[1];
                    break;
                }
            }


            for(int fNodeId=0; fNodeId<nodesList.size(); fNodeId++){

                        CyNode tmpNode = nodesList.get(fNodeId);

                        if(tmpNode.getIdentifier().equals(nodeProteinName)){
                            returnNode = nodesList.get(fNodeId);
                            System.out.println("Found node!!!");
                            break;
                        }
            }

            return returnNode;

        }


        public class CustomKeyListener implements KeyListener{

            //@Override
            public void keyTyped(KeyEvent ke) {
                System.out.println("keyTyped");
               if(treeInternalFrame!=null){
                   System.out.println("not null");
                        if(ke.getKeyChar() == 32){
                            System.out.println("space was pressed");
                            treeInternalFrame.moveToFront();
                            try {
                                treeInternalFrame.setSelected(true);
                            } catch (PropertyVetoException ex) {
                                Logger.getLogger(CustomPathwayVisualizerAction.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            treeInternalFrame.setVisible(true);
                        }
                }
            }

            //@Override
            public void keyPressed(KeyEvent ke) {
                System.out.println("keyPressed");
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            //@Override
            public void keyReleased(KeyEvent ke) {
                System.out.println("keyReleased");
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            

        }


        public class CustomMouseListener implements MouseListener
        {

            public CustomMouseListener ()
            {
                //CyNetworkView view=Cytoscape.getCurrentNetworkView();
                //((DGraphView) view).getCanvas().addMouseListener(this);
            }

            //@Override
            public void mouseClicked(MouseEvent e)
            {

                try {

                    treeViewLocationPoint = treeInternalFrame.getLocation();

                    
                    CyNetworkView view=Cytoscape.getCurrentNetworkView();
                    NodeView nv =((DGraphView) view).getPickedNodeView(e.getPoint());

                    if(nv != null) {

                        
                        CyNode node =(CyNode) nv.getNode();

                        String curNodeStr = node.getIdentifier();

                        

                        int networkId = -1;

                        for(int netId=0; netId<networksTitlesList.size(); netId++){

                            StringTokenizer st = new StringTokenizer(networksTitlesList.get(netId),"_");
                            String netwTitleStart = st.nextToken();
                        

                            if(curNodeStr.startsWith(netwTitleStart)){
                        
                                if(activeNetworkView!=null){
                                    Cytoscape.destroyNetworkView(activeNetworkView);
                                }

                                
                                CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
                                for(int tmpNId=0; tmpNId<nodeAttrsList.get(netId).size(); tmpNId++){

                                    if(nodeAttrsList.get(netId).get(tmpNId)==1)
                                        nodeAtts.setAttribute(nodesList.get(tmpNId).getIdentifier(),"node.fillColor",PRESENT_PROTEIN_COLOR);
                                    else if(nodeAttrsList.get(netId).get(tmpNId)==0)
                                        nodeAtts.setAttribute(nodesList.get(tmpNId).getIdentifier(),"node.fillColor",ABSENT_PROTEIN_COLOR);
                                    else if(nodeAttrsList.get(netId).get(tmpNId)==-1)
                                        nodeAtts.setAttribute(nodesList.get(tmpNId).getIdentifier(),"node.fillColor",NOFLAG_PROTEIN_COLOR);
                                }
                                
                                
                                CyNetworkView curCyView = Cytoscape.createNetworkView(networksList.get(netId), networksTitlesList.get(netId));
                                CyLayoutAlgorithm algo = CyLayouts.getLayout("force-directed");
                                curCyView.applyLayout(algo);
                                

                                
                                JInternalFrame f =
                                Cytoscape.getDesktop().getNetworkViewManager().getInternalFrame(curCyView.getView());
                                try {
                                   f.setMaximum(true);
                                }
                                catch (Exception ex)
                                {}
                                
                                activeNetworkView = Cytoscape.createNetworkView(networksList.get(netId), networksTitlesList.get(netId));;
                                CustomKeyListener keyL = new CustomKeyListener();
                                activeNetworkView.getComponent().addKeyListener(keyL);
                                
                                
                                try {
                                   treeInternalFrame.setLocation(treeViewLocationPoint.x, treeViewLocationPoint.y);
                                   treeInternalFrame.moveToFront();
                                   treeInternalFrame.setSelected(true);
                                }
                                catch (Exception ex)
                                {}

                                
                                break;
                            }
                        }


                    }
                    
                } catch (Exception ex) {
                    Logger.getLogger(CustomPathwayVisualizerAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void mousePressed(MouseEvent me) {
                //System.out.println("mousePressed");
                //mouseClicked(me);
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                //System.out.println("mouseReleased");
            }

            @Override
            public void mouseEntered(MouseEvent me) {
                //System.out.println("mouseEntered");
                //mouseClicked(me);
            }

            @Override
            public void mouseExited(MouseEvent me) {
                //System.out.println("mouseExited");
            }
        }
}
