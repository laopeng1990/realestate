#加入so库
mvn install:install-file -DgroupId=com.wpf.homelink -DartifactId=homelinkndk -Dversion=v1 -Dfile=libs/homelinkndk -Dpackaging=so -DgeneratePom=true -Dclassifier=homelink