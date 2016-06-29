package cytoscape.customapp.biopax.action;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;

import cytoscape.customapp.biopax.ServiceController;

public class MySelectionListener implements RowsSetListener {
	CyApplicationManager manager;
	int shapeCount;

	public MySelectionListener() {
		manager = ServiceController.getInstance().getCyApplicationManager();
		//shapeCount = 0;
	}

	public void handleEvent(RowsSetEvent event) {
		// First see if this even has anything to do with selections
		if (!event.containsColumn(CyNetwork.SELECTED)) {
			// Nope, we're done
			return;
		}

		// For each selected node, get the view in the current network
		// and change the shape
		CyNetworkView currentNetworkView = manager.getCurrentNetworkView();
		CyNetwork currentNetwork = currentNetworkView.getModel();
		if (event.getSource() != currentNetwork.getDefaultNodeTable())
			return;

		for (RowSetRecord record : event.getColumnRecords(CyNetwork.SELECTED)) {
			Long suid = record.getRow().get(CyIdentifiable.SUID, Long.class);
			Boolean value = (Boolean) record.getValue();
			if (value.equals(Boolean.TRUE)) {
				CyNode node = currentNetwork.getNode(suid);
				
				String nodeName = currentNetwork.getRow(node).get(CyNetwork.NAME, String.class);
				nodeName=nodeName.substring(0, 8);
				ServiceController.getInstance().displayView(nodeName);
				
				//CyRow row=currentNetwork.getRow(node);
				//row.set(CyNetwork.SELECTED, false);
			}
		}
		currentNetworkView.updateView();
	}
}