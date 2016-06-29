package cytoscape.customapp.biopax.view;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import cytoscape.customapp.biopax.ServiceController;


public class BiopaxInputCustomDialog extends JDialog implements ActionListener {


        /**
	 * 
	 */
	private static final long serialVersionUID = -1686223848309460501L;
		public static boolean SELECT_DIR = true;
        
	public BiopaxInputCustomDialog() {
		super(ServiceController.getInstance().getCytoscapeDesktopService().getJFrame(), "BioPAX Viz - Select file(s)", true);

		
		inOutDirs = null;
		initControls();
		pack();
		setLocationRelativeTo(ServiceController.getInstance().getCytoscapeDesktopService().getJFrame());

                btnInputFile.requestFocusInWindow();
                btnInputFile.addKeyListener(
                new KeyAdapter() {
                 public void keyPressed(KeyEvent e) {
                   if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                      btnInputFile.doClick();
                   }
                 }
                });
	}



	public void actionPerformed(ActionEvent e) {
		final Object src = e.getSource();
                JRadioButton acquiredButton = null;

                if(src.getClass() == JRadioButton.class){
                    acquiredButton = (JRadioButton)e.getSource();
                }

		if (src == btnInputFile && (SELECT_DIR == false)) {
			final File file = updateSetting(choInputFile);
			if (file != null) {
				if (checkInputFile(file)) {
					txfInputFile.setText(file.getAbsolutePath());
				} else {
					txfInputFile.setText("");
					Utils.showErrorBox(ServiceController.getInstance().getCytoscapeDesktopService(),"Selected input file is not acceptable.", "Please select a '.owl' file.");
				}
				updateStartButton();
			}

		} else if (src == btnInputFile && (SELECT_DIR == true)) {

                        final File file = updateSetting(choInputFile);

                        if (file != null) {
				if (checkInputDir(file)) {

                                    boolean validDir = false;
                                    boolean hasOwlFiles = false;
                                    boolean hasNodeFile = false;

                                    File[] biopaxFiles = file.listFiles();

                                    for (File bpFile : biopaxFiles){

                                        if(hasOwlFiles && hasNodeFile){
                                            validDir = true;
                                            break;
                                        }
                                        else{
                                            String curInputFile = bpFile.getAbsolutePath();

                                            if(curInputFile.endsWith(".nodes")){
                                                hasNodeFile = true;
                                            }
                                            else if(curInputFile.endsWith(".owl")){
                                                hasOwlFiles = true;
                                            }
                                        }
				}

                                if(validDir){
                                    txfInputFile.setText(file.getAbsolutePath());
                                }
                                else {
                                    txfInputFile.setText("");
                                    Utils.showErrorBox(ServiceController.getInstance().getCytoscapeDesktopService(),"Selected input directory is not acceptable.", "Please select a directory containing only '.owl' files and a '.nodes' file.");
                                }

				updateStartButton();
			}
                    }
		} else if (src == btnSelectFile){
                        SELECT_DIR = false;
                        choInputFile.setFileSelectionMode(JFileChooser.FILES_ONLY);

                } else if (src == btnSelectDir){
                        SELECT_DIR = true;
                        choInputFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                } else if (src == btnStart) {
			this.inOutDirs = new File[1];
			inOutDirs[0] = choInputFile.getSelectedFile();
			setVisible(false);
			dispose();
		} else if (src == btnCancel) {
			setVisible(false);
			dispose();
		}
	}

	/**
	 * Gets the settings for input and output directory for batch analysis.
	 *
	 * @return Array of <code>File</code> instances, containing the input and output directory for batch
	 *         analysis.
	 */
	public File[] getInOutDirs() {
		return inOutDirs;
	}



	/**
	 * Creates and lays out the controls inside this dialog.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 */
	private void initControls() {

		// Create the outer box and panel
		Box contentPane = Box.createVerticalBox();
		Utils.setStandardBorder(contentPane);

		JPanel panTitle = new JPanel();
		
                final JPanel panCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		final JPanel panInterpr = new JPanel(new BorderLayout());
		btnSelectDir = new JRadioButton("Select directory", true);
		btnSelectFile = new JRadioButton("Select file");
		

                btnSelectFile.addActionListener(this);
                btnSelectDir.addActionListener(this);

                final ButtonGroup group = new ButtonGroup();
                group.add(btnSelectDir);
		group.add(btnSelectFile);
		

                
                panInterpr.add(btnSelectDir, BorderLayout.WEST);
                panInterpr.add(btnSelectFile, BorderLayout.CENTER);
                panInterpr.setSize(500,100);
                
                panCenter.add(panInterpr);
		contentPane.add(panCenter);
                
		final int BS = Utils.BORDER_SIZE / 2;
		contentPane.add(Box.createVerticalStrut(BS));

		// Create file choosers and pre-load with paths in settings
		choInputFile = new JFileChooser();
		choInputFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// choInputFile.setSelectedFile(new File(inputDir));

                
		// Initialize text fields for displaying currently selected paths
		final int columnsCount = Utils.BORDER_SIZE * 4;
		txfInputFile = new JTextField("", columnsCount);


		// Create buttons for changing the settings
		JButton[] buttons = new JButton[1];
		buttons[0] = btnInputFile = Utils.createButton("Select", null, this);
		Utils.equalizeSize(buttons);


		// Add buttons for selecting paths and files
		JPanel panTop = new JPanel();
		JPanel panSelects = new JPanel(new GridBagLayout());
		GridBagConstraints constr = new GridBagConstraints();
		constr.insets.left = BS;
		constr.insets.right = BS;
		constr.gridx = constr.gridy = 0;

		// Layout the controls in tabular form
		addPathSelector("Input file/directory:", txfInputFile, btnInputFile, panSelects, constr);
		
		panTop.add(panSelects);
		contentPane.add(panTop);
		contentPane.add(Box.createVerticalStrut(Utils.BORDER_SIZE));

                
		// Add info about node attributes
		final JPanel panAttr = new JPanel(new FlowLayout(FlowLayout.CENTER, BS, BS));
		
		// Add Start Analysis and Cancel buttons
		final JPanel panBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		final JPanel panButtons = new JPanel(new GridLayout(1, 2, Utils.BORDER_SIZE, 0));
		panButtons.add(btnCancel = Utils.createButton("Cancel", null, this));
		panButtons.add(btnStart = Utils.createButton("Display pathway", null, this));
		panBottom.add(panButtons);
		updateStartButton();

		contentPane.add(panBottom);
		setContentPane(contentPane);
		setResizable(false);

	}

	/**
	 * Adds a path selector to the dialog.
	 *
	 * @param aMessage
	 *            Label of the path to be selected.
	 * @param aField
	 *            Field for the selected path name.
	 * @param aButton
	 *            Button to be pressed for choosing the path.
	 * @param aPanel
	 *            Panel to add aMessage, aField and aButton.
	 * @param aConstr
	 *            Constraints for visualization.
	 */
	private void addPathSelector(String aMessage, JTextField aField, JButton aButton, JPanel aPanel,
			GridBagConstraints aConstr) {
		aConstr.gridx = 0;
		aConstr.anchor = GridBagConstraints.LINE_END;
		aPanel.add(new JLabel(aMessage), aConstr);

		aConstr.gridx = 1;
		aConstr.anchor = GridBagConstraints.CENTER;
		aField.setEditable(false);
		aPanel.add(aField, aConstr);

		aConstr.gridx = 2;
		aPanel.add(aButton, aConstr);
		aConstr.gridy++;
	}

	/**
	 * Updates the status of the button for starting the visualization processing.
	 */
	private void updateStartButton() {
		if ("".equals(txfInputFile.getText())) {// || "".equals(txfInputDir.getText())) {
			btnStart.setEnabled(false);
		} else {
			btnStart.setEnabled(true);
                        btnStart.requestFocusInWindow();
                        btnStart.addKeyListener(
                          new KeyAdapter() {
                             public void keyPressed(KeyEvent e) {
                               if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                                  btnStart.doClick();
                               }
                             }
                          });
		}
	}

	/**
	 * Updates the chosen (input/output) file.
	 *
	 * @param aChooser
	 *            JFileChooser, from which a new file has to be chosen.
	 * @return Chosen file.
	 */
	private File updateSetting(JFileChooser aChooser) {
		if (aChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			return aChooser.getSelectedFile();
		}
		return null;
	}

	/**
	 * Checks the output directory, i.e. if aFile exists, is a directory, is empty and is writable.
	 *
	 * @param aFile
	 *            File to be checked.
	 * @return <code>true</code> if the output directory fulfills the criterion, and <code>false</code>
	 *         otherwise.
	 */
	public static boolean checkInputDir(File aFile) {
		try {
			if (aFile.exists() && aFile.isDirectory() && aFile.list().length > 0 && aFile.canRead()) {
				return true;
			}
		} catch (Exception ex) {
			// Fall through
		}
		return false;
	}

	/**
	 * Checks the output directory given by aFileName, i.e. if the directory exists, is a directory, is empty
	 * and is writable.
	 *
	 * @param aFileName
	 *            Name of the directory to check.
	 * @return <code>true</code> if the output directory fulfills the criterion, and <code>false</code>
	 *         otherwise.
	 */
	public static boolean checkInputDir(String aFileName) {
		try {
			return checkInputDir(new File(aFileName));
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Checks the input directory, i.e. if aFile exists, is a directory, is not empty and is readable.
	 *
	 * @param aFile
	 *            File to be checked.
	 * @return <code>true</code> if the input directory fulfills the criterion, and <code>false</code>
	 *         otherwise.
	 */
	public static boolean checkInputFile(File aFile) {
		try {
			if (aFile.exists() && aFile.isFile() && aFile.canRead() && aFile.getName().endsWith(".owl")) {
				return true;
			}
		} catch (Exception ex) {
			// Fall through
		}
		return false;
	}

	/**
	 * Checks the input directory given by aFileName, i.e. if the directory exists, is a directory, is empty
	 * and is writable.
	 *
	 * @param aFileName
	 *            Name of the directory to check.
	 * @return <code>true</code> if the input directory fulfills the criterion, and <code>false</code>
	 *         otherwise.
	 */
	public static boolean checkInputFile(String aFileName) {
		try {
			return checkInputFile(new File(aFileName));
		} catch (Exception ex) {
			return false;
		}
	}

	
	/**
	 * &quot;Cancel&quot; button.
	 */
	private JButton btnCancel;

	/**
	 * Button to select input directory (.sif, .gml, .xgmml files)
	 */
	private JButton btnInputFile;

	

	/**
	 * Radio button for applying all possible interpretations.
	 */
	private JRadioButton btnSelectFile;

	/**
	 * Radio button for applying only interpretations that treat the networks as directed.
	 */
	private JRadioButton btnSelectDir;

	
	/**
	 * Start analysis button.
	 */
	private JButton btnStart;

	/**
	 * Directory chooser for the Networks.
	 */
	private JFileChooser choInputFile;

	/**
	 * Two-element array storing the input and output directory as selected by the user.
	 */
	private File[] inOutDirs;

	/**
	 * Text field showing the name of the selected input directory.
	 */
	private JTextField txfInputFile;

	
}
