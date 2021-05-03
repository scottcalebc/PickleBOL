import sys
import os

if len(sys.argv) != 3: 
    print("args: pathToTestDirectory textFileToCreate")
    sys.exit()


if not os.path.exists(sys.argv[1]):
    print("test directory does not exist")
    sys.exit()

f = open(os.path.join("./tests/", sys.argv[2]), 'w')

for file in os.listdir(sys.argv[1]):
    if file.endswith(".txt"):
        f2 = open(os.path.join(sys.argv[1], file))
        f.write("// " + file)
        for line in f2.readlines():
            f.write(line)
        f.write("\n\n")
