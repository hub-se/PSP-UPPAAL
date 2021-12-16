To install the tool:
mvn install

To run the tool:
mvn exec:java -Dexec.mainClass="view.Main" -Dexec.args="[your properties file]"

e.g. mvn exec:java -Dexec.mainClass="view.Main" -Dexec.args="models/simpleTest.properties"

Known issues:
By default, in the .xml files made with UPPAAL there is a .dtd mentioned that is outdated (http://www.it.uu.se/research/group/darts/uppaal/flat-1_2.dtd). As JDOM will not load .xml files with this .dtd anymore, you have to change it to some other .dtd file, e.g. "./models/flat-1_1.dtd". 