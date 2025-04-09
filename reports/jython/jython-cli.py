from __future__ import print_function
import os
import sys

text = """

import org.python.util.jython;

public class __CLASSNAME__ {

    public static void main(String... args) {
        jython.main(args);
    }
}

"""

def main():
    scriptFilename = sys.argv[1]
    javaClassname = "_" + os.path.basename(scriptFilename)[:-3]
    javaFilename = javaClassname + ".java"
    deps = []
    version = "2.7.4"
    with open(scriptFilename) as f:
        lines = f.readlines()
        tag1 = "##DEPS"
        tag2 = "##JYTHON"
        for line in lines:
            if len(line) > len(tag1):
                if line[:len(tag1)] == tag1:
                    list = line.split()
                    dep = list[1]
                    deps.append(dep)
            if len(line) > len(tag2):
                if line[:len(tag2)] == tag2:
                    version = line.split()[1]
    dep = "org.python:jython-standalone:" + version
    deps.append(dep)

    jf = open(javaFilename,"w+")
    jf.write('///usr/bin/env jbang "$0" "$@" ; exit $?' + "\n\n")
    for dep in deps:
        jf.write("//DEPS " + dep + "\n")
        #print(dep)
    jf.write(text.replace("__CLASSNAME__",javaClassname))
    jf.close()
    #print(sys.argv[1:])
    params = ""
    for e in sys.argv[1:]:
        if len(params) > 0:
            params += " "
        params += e
    os.system("jbang run " + javaFilename + " " + params)

main()
