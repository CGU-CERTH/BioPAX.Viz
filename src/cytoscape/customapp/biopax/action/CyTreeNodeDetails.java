package cytoscape.customapp.biopax.action;

import org.cytoscape.model.CyNode;

public class CyTreeNodeDetails {
	private CyNode cyNode;
	private String name;
	private String color;
	
	public CyTreeNodeDetails(CyNode node, String nm, String colr){
		cyNode=node;
		name=nm;
		color=colr;
	}
	
	public CyNode getCyNode(){
		return cyNode;
	}
	
	public String getName(){
		return name;
	}
	
	public String getColor(){
		return color;
	}
	
	public String toString(){
		String s="";
		s="suid="+cyNode.getSUID()+
				"\tname="+name+
				"\tcolor="+color;
		return s;
	}
}
