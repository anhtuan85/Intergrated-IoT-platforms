rm -rf ./bin
#create .class files
mkdir ./bin
#Compile java files
javac -cp '/home/ubuntu/workspace/platform/iotivity222/swig/iotivity-lite-java/libs/iotivity-lite.jar:./lib/*' -sourcepath ./src -d ./bin ./src/com/kmk/app/*.java 
#create jar file
jar -cfv ocf_device.jar -C ./bin .
