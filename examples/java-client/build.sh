rm -f *.class *.jar
javac Tulip_Java_Client.java
jar cfm Tulip_Java_Client.jar Manifest.txt  Tulip_Java_Client.class
rm -f ../../libs/Tulip_Java_Client.jar
cp *.jar ../../libs
