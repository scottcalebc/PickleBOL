import sys
import os
import re

def sorted_alphanumeric(data):
    convert = lambda text: int(text) if text.isdigit() else text.lower()
    alphanum_key = lambda key: [ convert(c) for c in re.split('([0-9]+)', key) ] 
    return sorted(data, key=alphanum_key)

if len(sys.argv) != 2: 
    print("args: testDir")
    print("runs all the tests in the testDir.")
    print("ex: python3 testRunner.py tests/float")
    sys.exit()

if not os.path.exists(sys.argv[1]):
    print("test directory does not exist")
    sys.exit()

# build pickle
testDir = os.path.abspath(sys.argv[1])
os.chdir("..")
pickleDir = os.path.abspath(os.curdir)
print(pickleDir)
if not os.path.exists(pickleDir):
    print("pickle directory does not exist")
    sys.exit()
res = os.system("javac pickle/*.java")
print("Java Build Result:", res)

# run the tests
for file in sorted_alphanumeric(os.listdir(testDir)):
    if file.endswith(".txt"):
        filepath = os.path.abspath(os.path.join(os.curdir, "documentation",file))
        print("Testing File:", file)
        res = os.system("java pickle.Pickle \"" + filepath + "\"")
        print("\n")

# clean pickle
os.chdir("pickle")
os.system("rm -r *.class")