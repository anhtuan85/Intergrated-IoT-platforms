rm -rf ./bin

#create .class files
mkdir ./bin

#Compile java files
javac -cp './libraries/*' -sourcepath ./src -d ./bin  $(find src -name '*.java')

#create jar file
jar -cfv ncube_device.jar -C ./bin .

