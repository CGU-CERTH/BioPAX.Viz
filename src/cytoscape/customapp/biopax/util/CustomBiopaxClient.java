package cytoscape.customapp.biopax.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.BiochemicalPathwayStep;
import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PathwayStep;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.Xref;


public class CustomBiopaxClient {

    public Model myModel;
    //private EditorMapAdapter emAd;
    private String biopaxFilename;
    private ArrayList<String[]> customGeneIdsToNamesList;
    public String pathAndOrgName = "";
    private static String customCommentStr = "$$custom comment$$";
    
    public CustomBiopaxClient(String biopaxFilename) throws FileNotFoundException{
        this.biopaxFilename = biopaxFilename;
        myModel = readModelL3(biopaxFilename);
    }


    public boolean checkExistenceOfThePath(String inputPath){


        Set<Pathway> pwSet = myModel.getObjects(Pathway.class);
        Iterator itPath = pwSet.iterator();
        Pathway pw = (Pathway)itPath.next();
        

        Set<String> pwComs = pw.getComment();
        Iterator itPwComs = pwComs.iterator();

        boolean pathwayExists = false;

        int cnt = 0;
        while(itPwComs.hasNext()){

            String tmpCom = (String)itPwComs.next();

            if(tmpCom.equals(customCommentStr+":present")){
                System.out.println("\n[1]: The pathway is present!\n");
                pathwayExists = true;
                break;
            }
            else if(tmpCom.equals(customCommentStr+":absent")){
                System.out.println("\n[0]: The pathway is absent!\n");
                pathwayExists = false;
                break;
            }
        }

        if(!pathwayExists)
            System.out.println("\nNo existence property (presence/absence) is set for the specified pathway.");

        return pathwayExists;
    }


    public ArrayList<ArrayList<String>> getGeneNetworkFromBiopax(String input) throws FileNotFoundException, IOException{

        
        //return protToProtList: each row 'i' contains the genes which the i-th gene is con
        ArrayList<ArrayList<String>> protToProtList = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> protNamesList = new ArrayList<ArrayList<String>>();



        Set<BiochemicalPathwayStep> pathSteps = myModel.getObjects(BiochemicalPathwayStep.class);
        //JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "BiochemicalPathwaySteps - size: "+pathSteps.size());

        Iterator stepsIter = pathSteps.iterator();

        
        int biochPathStepCnt = 0;

        while(stepsIter.hasNext()){

            ArrayList<String> tmpProtToProtList = new ArrayList<String>();
            ArrayList<String> tmpProtNamesList = new ArrayList<String>();

            //look inside a BiochemicalPathwayStep
            BiochemicalPathwayStep curStep = (BiochemicalPathwayStep) stepsIter.next();

            System.out.println("BiochemicalPathwayStep "+(++biochPathStepCnt)+": "+curStep.getRDFId());

            Set<org.biopax.paxtools.model.level3.Process> processSet = curStep.getStepProcess();
            //JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Processes - size: "+processSet.size());

            Iterator processIter = processSet.iterator();

            while(processIter.hasNext()){

                org.biopax.paxtools.model.level3.Process curProcess = (org.biopax.paxtools.model.level3.Process) processIter.next();
                //JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Process: "+curProcess.getRDFId());

                String curRdfId = curProcess.getRDFId();


                //get only Catalysis processes among all Control related
                if(curRdfId.contains("Catalysis")){

                   
                    Catalysis curCatalysis = (Catalysis) myModel.getByID(curRdfId);
                    Set<Entity> catalController = curCatalysis.getParticipant();
                    Iterator catalControlter = catalController.iterator();

                    while(catalControlter.hasNext()){

                        Entity curController = (Entity) catalControlter.next();

                        //get complex or protein Entities
                        if(curController.getRDFId().contains("Protein")){

                            Protein curProt = (Protein) myModel.getByID(curController.getRDFId());
                            ProteinReference curProtRef = (ProteinReference) curProt.getEntityReference();
                            
                            tmpProtToProtList.add(curProtRef.getRDFId());
                            if(curProtRef.getDisplayName()!=null)
                                tmpProtNamesList.add(curProtRef.getDisplayName());
                            else
                                tmpProtNamesList.add(curProtRef.getStandardName());

                        } else if(curController.getRDFId().contains("Complex")){
                            Complex curComplex = (Complex) myModel.getByID(curController.getRDFId());
                            Set<PhysicalEntity> entitiesFromCurComplex = curComplex.getComponent();
                            Iterator entitiesFromCurComplexIter = entitiesFromCurComplex.iterator();

                            while(entitiesFromCurComplexIter.hasNext()){

                                Protein curProteinFromComplex = (Protein) entitiesFromCurComplexIter.next();
                                ProteinReference curProtRef = (ProteinReference) curProteinFromComplex.getEntityReference();
                                
                                tmpProtToProtList.add(curProtRef.getRDFId());
                                if(curProtRef.getDisplayName()!=null)
                                    tmpProtNamesList.add(curProtRef.getDisplayName());
                                else
                                    tmpProtNamesList.add(curProtRef.getStandardName());
                            }
                        }
                    }


                }
            }

           

            System.out.println("\ttmpProtNamesList so far:\n");
            for(int tmpPr=0; tmpPr<tmpProtNamesList.size(); tmpPr++)
                System.out.println(tmpProtNamesList.get(tmpPr));

            

            Set<PathwayStep> nextStepsSet = curStep.getNextStep();
            Iterator nextStepsIter = nextStepsSet.iterator();

            int nextStepCnt = 0;
            while(nextStepsIter.hasNext()){
                BiochemicalPathwayStep curNextStep = (BiochemicalPathwayStep) nextStepsIter.next();

                System.out.println("\nNext step "+(++nextStepCnt)+": "+curNextStep.getRDFId()+"\n");

                System.out.println("Next pathway step: "+curNextStep.getRDFId());


                Set<org.biopax.paxtools.model.level3.Process> nextProcessSet = curNextStep.getStepProcess();
                //JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Processes - size: "+nextProcessSet.size());

                Iterator nextProcessIter = nextProcessSet.iterator();

                while(nextProcessIter.hasNext()){

                    org.biopax.paxtools.model.level3.Process curProcess = (org.biopax.paxtools.model.level3.Process) nextProcessIter.next();
                    //JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Process: "+curProcess.getRDFId());

                    String curRdfId = curProcess.getRDFId();


                    //get only Catalysis processes among all Control related
                    if(curRdfId.contains("Catalysis")){

                        System.out.println("Current Catalysis rdfId: "+curRdfId);

                        Catalysis curCatalysis = (Catalysis) myModel.getByID(curRdfId);
                        Set<Entity> catalController = curCatalysis.getParticipant();
                        Iterator catalControlter = catalController.iterator();

                        while(catalControlter.hasNext()){

                            Entity curController = (Entity) catalControlter.next();

                            //get complex or protein Entities
                            if(curController.getRDFId().contains("Protein")){

                                Protein curProt = (Protein) myModel.getByID(curController.getRDFId());
                                ProteinReference curProtRef = (ProteinReference) curProt.getEntityReference();
                               
                                tmpProtToProtList.add(curProtRef.getRDFId());
                                if(curProtRef.getDisplayName()!=null)
                                    tmpProtNamesList.add(curProtRef.getDisplayName());
                                else
                                    tmpProtNamesList.add(curProtRef.getStandardName());

                            } else if(curController.getRDFId().contains("Complex")){
                                Complex curComplex = (Complex) myModel.getByID(curController.getRDFId());
                                Set<PhysicalEntity> entitiesFromCurComplex = curComplex.getComponent();
                                Iterator entitiesFromCurComplexIter = entitiesFromCurComplex.iterator();

                                while(entitiesFromCurComplexIter.hasNext()){

                                    Protein curProteinFromComplex = (Protein) entitiesFromCurComplexIter.next();
                                    ProteinReference curProtRef = (ProteinReference) curProteinFromComplex.getEntityReference();
                                    tmpProtToProtList.add(curProtRef.getRDFId());
                                    if(curProtRef.getDisplayName()!=null)
                                        tmpProtNamesList.add(curProtRef.getDisplayName());
                                    else
                                        tmpProtNamesList.add(curProtRef.getStandardName());
                                }
                            }
                        }


                    }
                }
            }

           
            System.out.println("\nFinally tmpProtNamesList:\n");
            for(int tmpPr=0; tmpPr<tmpProtNamesList.size(); tmpPr++)
                System.out.println(tmpProtNamesList.get(tmpPr));

            
            protToProtList.add(tmpProtToProtList);
            protNamesList.add(tmpProtNamesList);

        }

           
        return protToProtList;
    }


    public boolean getPathAndOrgName(String input) throws FileNotFoundException{

        String pathwayName = "";

        pathAndOrgName = "";
        boolean hasSequences = false;


        Set<Pathway> path = myModel.getObjects(Pathway.class);

        if(path.size()>1){

            int slashIndex = 0;
            if(input.contains("/"))
                slashIndex = input.lastIndexOf('/');

            pathAndOrgName = input.substring(slashIndex+1, input.length()-4);

            Iterator pathIter = path.iterator();
            while(pathIter.hasNext()){
                Pathway myPath = (Pathway)pathIter.next();
                if(myPath.getOrganism() == null)
                    continue;
                else{
                    pathwayName = myPath.getStandardName();
                }
            }

            hasSequences = false;

        } else if(path.size() == 1){
            Iterator pathIter = path.iterator();
            Pathway myPath = (Pathway)pathIter.next();
            pathwayName = myPath.getStandardName();

            Set<Xref> xrefs = myPath.getXref();
            Iterator xrefsIter = xrefs.iterator();

            while(xrefsIter.hasNext()){
                Xref xref = (Xref)xrefsIter.next();
                String rdfId = xref.getRDFId();
                if(rdfId.contains("Unification")){

                    pathAndOrgName += xref.getId()+"_"+xref.getDb();
                    break;
                }
            }

            hasSequences = true;
        }

        return hasSequences;
    }




    public Model readModelL3(String inputFileStr) throws FileNotFoundException{

            BioPAXIOHandler handler = new SimpleIOHandler(); //new JenaIOHandler(BioPAXLevel.L3);
            InputStream inputStreamFromFile = new FileInputStream(inputFileStr);
            Model model = handler.convertFromOWL(inputStreamFromFile);

            return model;
    }

}