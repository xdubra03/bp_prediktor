#!/usr/bin/python

import numpy as np
import pandas as pd
import random
import sys
import csv
from sklearn.metrics import matthews_corrcoef
data_frame = pd.read_csv(sys.argv[1])

test_res = data_frame[['class']]
train_res = data_frame[['predicted1']]

print(test_res)
print(train_res)

mcc = matthews_corrcoef(test_res, train_res)
print(mcc)
