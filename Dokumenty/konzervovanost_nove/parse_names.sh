#!/bin/bash


filename="$1"
source_file="compiled_results_edit.txt"


while read -r line
do
    name="$line"

    #file="$name""NEW.txt"
    #grep $name < $source_file | awk -F',' '{print $3}' > $file 
    ./tree.py $name"clustal.fasta" $name"output" $name    



done < "$filename"
