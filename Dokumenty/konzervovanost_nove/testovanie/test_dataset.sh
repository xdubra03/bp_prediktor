#!/bin/bash

test_file=$1
train_file=$2

./svm.py  $test_file
./count_results.py $test_file
./count_frame.py $test_file
