#!/bin/bash
# Produit un rapport JaCoCo à partir du fichier jacoco.exec

java -jar ../../lib/jacococli.jar report jacoco.exec --classfiles ../../classes --sourcefiles ../../src --html html-jacoco 
