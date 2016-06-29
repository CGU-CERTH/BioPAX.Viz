BioPAX.Viz
==========

BioPAXViz is a Cytoscape (version 3.x) app providing a comprehensive framework for metabolic pathway visualization.


<h2>Install notes</h2>
<h3>Requirements</>
* Cytoscape: _verified to work in v3.4.0)_
* Java (TM) SE Development Kit (required for building the project from source): _verified to work with v.1.8.0_45_
Compatible with all major Operating Systems: _verified to work with Linux Ubuntu 14.04 LTS and Windows 10_
<br/>
<br/>


<h3>Building the project and creating a JAR file</h3>
The easiest way to build the application and create the jar file, is through the use of an IDE such as NetBeans or Eclipse.
You can also build the project through a command line interface with the following commands (slight changes may exist depending on OS):
* Build the project from within the source:<br/>
<code>javac -cp ".:/path/2/library/jar/files" *.java</code>
* Create the .jar file:<br/>
<code>jar cfm BioPaxViz.jar app-manifest *.class</code>
<br/>
<br/>


<h3>Installing the App</h3>
1. Open Cytoscape. 
2. From the menu list select: Apps -> App Manager 
3. Click on the 'Install from file' button
4. Search for the BioPaxViz.jar file and add it.
5. The App should be now visible and available though the menu list: Apps -> BioPaxViz (1.0)
6. Done!
<br/>
<br/>

<h3>Running the App</h3>
1. Open Cytoscape.
2. From the menu list select:
Apps -> BioPaxViz (1.0)
3. From the pop-up window that will come up select a directory that contains a tree file and a list of BioPAX files, that contain information about the presence/absence of the pathway and of the proteins within the pathway.
4. Hit the 'Display pathway' button!
<br/>
<br/>

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
