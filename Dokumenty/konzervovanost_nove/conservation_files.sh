#!/bin/bash


filename="sequence_names.txt"

while read -r line
do
    name="$line"

    #file="$name""NEW.txt"
    #grep $name < $source_file | awk -F',' '{print $3}' > $file
    ./correlation.py $name"clustal.fasta" $name"correlation.txt" $name



done < "$filename"
