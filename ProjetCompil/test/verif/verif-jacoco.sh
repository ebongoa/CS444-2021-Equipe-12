#!/bin/bash
# Test de la passe 2 avec JaCoCo

rm -f jacoco.exec

for fich in *.cas
do
    echo "---------------------------------------------------------------------"
    echo "Fichier : $fich"
    echo "---------------------------------------------------------------------"
    cd ../../classes ; java -javaagent:../lib/jacocoagent.jar=destfile=../test/verif/jacoco.exec -cp .:../lib/java-cup-11a-runtime.jar fr.esisar.compilation.verif/TestVerif ../test/verif/$fich ; cd ../test/verif
done

