Build :

mvn install assembly:assembly process-resources

Launch Command:

java -Xbootclasspath/p:C:/java/git_clones/smocker/bootLibs/javassist.jar;C:/java/git_clones/smocker/smockerAgent1.7/target/smockerAgent1.7-0.0.1-SNAPSHOT.jar -javaagent:C:/java/git_clones/smocker/smockerAgent1.7/target/smockerAgent1.7-0.0.1-SNAPSHOT.jar -Djava.util.logging.SimpleFormatter.format="%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-6s %2$s %5$s%6$s%n" 