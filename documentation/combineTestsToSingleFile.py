import sys
import os
import re

def sorted_alphanumeric(data):
    convert = lambda text: int(text) if text.isdigit() else text.lower()
    alphanum_key = lambda key: [ convert(c) for c in re.split('([0-9]+)', key) ] 
    return sorted(data, key=alphanum_key)

if len(sys.argv) != 3: 
    print("args: pathToTestDirectory textFileToCreate")
    sys.exit()


if not os.path.exists(sys.argv[1]):
    print("test directory does not exist")
    sys.exit()

f = open(os.path.join("./tests/", sys.argv[2]), 'w')

for file in sorted_alphanumeric(os.listdir(sys.argv[1])):
    if file.endswith(".txt"):
        f2 = open(os.path.join(sys.argv[1], file))
        f.write("// " + file)
        f.write("\n")
        for line in f2.readlines():
            f.write(line)
        f.write("\n\n")