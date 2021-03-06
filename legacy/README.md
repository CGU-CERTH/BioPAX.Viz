BioPAX.Viz
==========

BioPAXViz is a Cytoscape (version 2.x) plugin providing a comprehensive framework for metabolic pathway visualization.


<h2>Install notes</h2>
<h3>Requirements</h3>
* Linux (verified to work with Ubuntu 12.04 lts) or Mac OS (verified to work with Mac OS X Lion 10.7.5)
* Java (TM) SE Development Kit (verified to work with v.1.6.0_37)
* Apache Maven (verified to work with v.3.0.4)
* Cytoscape (verified to work in v.2.8.2)
<br/>
<br/>


<h3>Building the project and creating a JAR file</h3>
Execute the following command:<br/>
<code>mvn package</code>
<br/>
<br/>


<h3>Installing the plugin</h3>
1. Open Cytoscape. 
2. From the menu list select: Plugins -> Manage Plugins 
3. Search for the (core) plugin named 'BioPAX v.***', e.g. 'BioPAX v.0.72'.
4. Select this plugin and click 'Delete'.
5. Quit Cytoscape.
(the series of steps 1-5 is required to be performed only once)
6. Go to the following path: 
../BioPAX_Viz_Project/target/ 
7. Copy the files 'biopax-2.8.4-SNAPSHOT-jar-with-dependencies.jar' and 'biopax-2.8.4-SNAPSHOT.jar' 
to the folder 
'path_to_cytoscape_installation/Cytoscape_v2.8.2/plugins/'.
8. Done!
<br/>
<br/>

<h3>Running the plugin</h3>
1. Open Cytoscape.
2. From the menu list select:
Plugins -> BioPAX Viz
3. From the pop-up window that will come up select a directory that contains a tree file and a list of BioPAX files, that contain information about the presence/absence of the pathway and of the proteins within the pathway.
4. Hit the 'Display pathway' button!
<br/>
<br/>


<h3>Usage notes</h3>
* You can select any node from the tree structure and see the instance of the reference pathway for the selected node/organism. 
* The tree structure is shown in the smaller window at the top left corner of the main window by default and it is resizable and movable at any location in the Cytoscape window. If you dismiss this window in the background you can always bring it back to the front by hitting the 'Space' button in the keyboard.
* The <i>presence</i> property is denoted by the <b>orange</b> color while the <i>absence</i> property is denoted by the <b>purple</b> color!
* In case you need to display a single BioPAX file, when the pop-up window is displayed at first you have to enable the 'Select file' radio-button option and then choose a specific BioPAX file (.owl extension) from the File-chooser window.
<br/>
<br/>

Copyright (c) 2014 CERTH<br/>
Author: Dimitrios Vitsios<br/>
Last edit: 26 April 2014
