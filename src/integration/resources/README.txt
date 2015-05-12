mvn install:install-file -DpomFile=../pom.xml -Dfile=versor-maven-plugin-0.0.1.2.jar -Dsources=versor-maven-plugin-0.0.1.2-sources.jar -Djavadoc=versor-maven-plugin-0.0.1.2-javadoc.jar -DgroupId=ru.myweek-end.maven -DartifactId=versor-maven-plugin -Dversion=0.0.1.2 -Dpackaging=jar -DlocalRepositoryPath=${project}/target/rep -DcreateChecksum=true
mvn install:install-file -Dfile=../../pom.xml -DgroupId=ru.myweek-end.maven -DartifactId=versor-maven-plugin-root -Dversion=0.0.1.2 -Dpackaging=pom -DlocalRepositoryPath=${project}/target/rep -DcreateChecksum=true

