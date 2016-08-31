BioPAX.Viz
==========

BioPAXViz is a Cytoscape (version 3.x) app providing a comprehensive framework for metabolic pathway visualization.


<h2>Install notes</h2>
<h3>Requirements</h3>
* Cytoscape: <i>verified to work in v3.4.0)</i>
* Java (TM) SE Development Kit (required only for building the project from source): <i>verified to work with v.1.8.0_45_</i>
Compatible with all major Operating Systems: <i>verified to work with Linux Ubuntu 14.04 LTS and Windows 10_</i>
<br/>
<br/>


<h4>(<i>optional</i>) Building the project and creating a JAR file</h4>
This is only recommended for expert users that have some programming experience. Otherwise you should directly use the provided pre-compiled jar file, located in the <code>target</code> directory.

The easiest way to build the application and create the jar file, is through the use of an IDE such as NetBeans or Eclipse.
You can also build the project through a command line interface with the following commands (slight changes may exist depending on OS):
* Build the project from within the source:<br/>
<code>javac -cp ".:/path/2/library/jar/files" *.java</code>
* Create the .jar file:<br/>
<code>jar cfm BioPaxViz.jar app-manifest *.class</code>
<br/>
<br/>


<h3>Installing the App</h3>
1. Download the pre-compiled jar file from the <code>target</code> directory
2. Open Cytoscape. 
3. From the menu list select: Apps -> App Manager 
4. Click on the 'Install from file' button
5. Search for the BioPaxViz.jar file you downloaded in Step 1 and add it.
6. The App should be now visible and available though the menu list: Apps -> BioPaxViz (1.0)
7. Done!
<br/>
<br/>

<h3>Running the App on the sample input</h3>
1. Download the <code>target</code> directory (it should contain 39 BioPAX files and a tree file). The BioPAX files already contain information about the presence/absence of the pathway and of the proteins within the pathway.
2. Open Cytoscape.
3. From the menu list select:
Apps -> BioPaxViz (1.0)
4. From the pop-up window that will come up select the directory you downloaded in Step 1.
5. Hit the 'Display pathway' button!
<br/>
<br/>

<h4>Workflow for preparing your own input</h4>
1. Download the pathway files in BioPAX format (BioCyc, MetaCyc, etc).
2. Run the BioPAXClient software in order to introduce the presence/absence information for each of the BioPAX files; however, the means to obtain the information is independent to the plugin (user decision).
3. Generate the tree file. The tree file format is historical; the first column is node name, the second is parent name, and leafs of the tree are marked with the third column with ‘undef’ value in it. The root node does not have a parent and there should obviously be only one root. As an example, see the tree structure below:

<code>
node80
node63		node80
FNUC-ATC	node63	undef
XFAS-9A5	node63	undef
MPUL-UAB	node80	undef
</code>


<h3>Uninstalling the App</h3>
1. Open Cytoscape. 
2. From the menu list select: Apps -> App Manager 
3. Click on the 'Currently Installed' tab
4. Select the 'BioPaxViz' app and click unistall
5. The App will be removed from Cytoscape after the next restart
<br/>
<br/>


<h2>Usage notes</h2>
* You can select any node from the tree structure and see the instance of the reference pathway for the selected node/organism. 
* The tree structure is shown in the smaller window at the top left corner of the main window by default and it is resizable and movable at any location in the Cytoscape window. If you dismiss this window in the background you can always bring it back to the front by hitting the 'Space' button in the keyboard.
* The <i>presence</i> property is denoted by the <b>orange</b> color while the <i>absence</i> property is denoted by the <b>purple</b> color!
* In case you need to display a single BioPAX file, when the pop-up window is displayed at first you have to enable the 'Select file' radio-button option and then choose a specific BioPAX file (.owl extension) from the File-chooser window.
<br/>
<br/>

Copyright (c) 2016 CERTH<br/>
Author: Fotis E. Psomopoulos<br/>
Last edit: 29 June 2016
