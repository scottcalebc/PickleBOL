#!/bin/bash
python3 testRunner.py tests/Bool &> output/Bool_Output.txt
python3 testRunner.py tests/Date &> output/Date_Output.txt
python3 testRunner.py tests/Errors &> output/Errors_Output.txt
python3 testRunner.py tests/Expressions &> output/Expressions_Output.txt
python3 testRunner.py tests/Float &> output/Float_Output.txt
python3 testRunner.py tests/Flow &> output/Flow_Output.txt
python3 testRunner.py tests/Functions &> output/Functions_Output.txt
python3 testRunner.py tests/Int &> output/Int_Output.txt
python3 testRunner.py tests/String &> output/String_Output.txt