package cytoscape.customplugins.biopax.action;

import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;
import ding.view.DGraphView;
import giny.view.NodeView;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class CustomMouseListener implements MouseListener, MouseMotionListener
{

    private CyNetworkView networkView = null;

    public CustomMouseListener(){
            networkView=Cytoscape.getCurrentNetworkView();
            ((DGraphView) networkView).getCanvas().addMouseListener(this);
            ((DGraphView) networkView).getCanvas().addMouseMotionListener(this);
    }


    public void mouseClicked(MouseEvent e){
        FileWriter foutstream = null;
        try {
            foutstream = new FileWriter("/Users/djifos/Desktop/outFile.txt");
            BufferedWriter out = new BufferedWriter(foutstream);
            out.write("lalalalal");
            out.close();
            NodeView nv = ((DGraphView) networkView).getPickedNodeView(e.getPoint());
            if (nv != null) {
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Mouse Clicked!");
            }
        } catch (IOException ex) {
            Logger.getLogger(CustomMouseListener.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                foutstream.close();
            } catch (IOException ex) {
                Logger.getLogger(CustomMouseListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseExited(MouseEvent me) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}