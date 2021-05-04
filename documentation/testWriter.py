import sys
import os

if len(sys.argv) != 3: 
    print("args: textFileWithTests outputDirName")
    sys.exit()

f = open(sys.argv[1], 'r')
lines = f.readlines()

if not os.path.exists(os.path.join("./tests/", sys.argv[2])):
    os.makedirs(os.path.join("./tests/", sys.argv[2]))

for line in lines:
    if line.startswith("//"):
        if f: f.close()
        filename = line[3:-1] + '.txt'
        f = open(os.path.join("./tests/", sys.argv[2], filename), 'w+')
        #print(os.path.join(os.getcwd(), sys.argv[2], filename))
    else:
        if line != "\n":
            f.write(line)