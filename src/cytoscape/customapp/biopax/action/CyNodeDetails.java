package cytoscape.customapp.biopax.action;

import org.cytoscape.model.CyNode;

public class CyNodeDetails {
	private CyNode cyNode;
	private String name;
	private String nodeRDFId;
	private String color;
	
	public CyNodeDetails(CyNode node, String nm, String RDFId, String colr){
		cyNode=node;
		name=nm;
		nodeRDFId=RDFId;
		color=colr;
	}
	
	public CyNode getCyNode(){
		return cyNode;
	}
	
	public String getName(){
		return name;
	}
	
	public String getNodeRDFId(){
		return nodeRDFId;
	}
	
	public String getColor(){
		return color;
	}
	
	public String toString(){
		String s="";
		s="suid="+cyNode.getSUID()+
				"\tname="+name+
				"\tRDFId="+nodeRDFId+
				"\tcolor="+color;
		return s;
	}
}
