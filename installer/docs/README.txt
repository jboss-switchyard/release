SwitchYard JBoss EAP Standalone Installer
=========================================

Installing Runtime
------------------
This package contains resources for installing SwitchYard into a 
JBoss Enterprise Application Platform installation.

Prerequisites:
    1.  Java Runtime
    2.  Fully installed Ant available on the command line execution PATH.
    3.  A JBoss EAP installation.  SwitchYard has been tested against EAP 6.4.0-Beta and 6.4.0.Final.

Instructions:
    1.  Open a terminal command prompt.
    2.  Change directory into the root of the bundle.
    3.  Execute command "ant".

This script will ask you for the path to the JBoss EAP distribution and will 
install all the necessary files required to run SwitchYard applications on 
JBoss EAP including the set of quickstart example applications.

Installing BPEL Console
-----------------------
Instructions:
    1. Execute command "ant install-bpel-console"

The script will ask for the location of your EAP install where the console server and webapp will be deployed.


Release Note
-----------------------
On EAP 6.4.0, deploying quickstarts using the mvn install -Pdeploy instructions may fail due to (https://bugzilla.redhat.com/show_bug.cgi?id=1193926).  You may need to manually deploy the quickstart JAR to the server.

