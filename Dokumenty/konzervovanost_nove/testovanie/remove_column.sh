#!/bin/bash

filename="train200_150_new1.csv"
awk -F',' '{print $2","$3","$4","$5","$6","$7","$8","$9","$10}' > $filename
