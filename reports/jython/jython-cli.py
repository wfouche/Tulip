from __future__ import print_function
import os
import sys
import base64

text = """
// import org.python.util.jython;
import org.python.util.PythonInterpreter;
import java.util.Base64;

public class __CLASSNAME__ {

    public static String mainScriptTextBase64 = "__MAIN_SCRIPT__";
    
    public static void main(String... args) {
        String mainScriptFilename = "__MAIN_SCRIPT_FILENAME__";
        String mainScript = "";
        String jythonArgsScript = ""; 
        for (String arg: args) {
            //System.out.println("Java: " + arg);
            if (jythonArgsScript.length() == 0) {
                if (!arg.equals(mainScriptFilename)) {
                    jythonArgsScript += "'" + mainScriptFilename + "', ";
                }
            } else {
                jythonArgsScript += ", ";
            }
            jythonArgsScript += "'" + arg + "'";
        }
        if (jythonArgsScript.length() == 0) {
            jythonArgsScript = "'" + mainScriptFilename + "'";
        }
        jythonArgsScript = "import sys; sys.argv = [" + jythonArgsScript + "]";
        {
            byte[] decodedBytes = Base64.getDecoder().decode(mainScriptTextBase64);
            String text = new String(decodedBytes);
            //System.out.println("===");
            //System.out.println(text);
            //System.out.println("===");
            mainScript = text;
        }
        //System.out.println("args --> " + jythonArgsScript);
        {
            // run script
            PythonInterpreter pyInterp = new PythonInterpreter();
            // initialize args
            pyInterp.exec(jythonArgsScript);
            // run script
            //pyInterp.exec("__name__=\"\"");
            pyInterp.exec(mainScript);
        }
        //jython.main(args);
    }
}

"""

def main():
    scriptFilename = sys.argv[1]
    javaClassname = os.path.basename(scriptFilename)[:-3] + "_py"
    javaFilename = scriptFilename.replace(".","_") + ".java"
    deps = []
    jythonVersion = "2.7.4"
    javaVersion = "21"
    with open(scriptFilename) as f:
        lines = f.readlines()
        tag1 = "##DEPS"
        tag2 = "##JYTHON"
        tag3 = "##JAVA"
        for line in lines:
            if len(line) > len(tag1):
                if line[:len(tag1)] == tag1:
                    list = line.split()
                    dep = list[1]
                    deps.append(dep)
            if len(line) > len(tag2):
                if line[:len(tag2)] == tag2:
                    jythonVersion = line.split()[1]
            if len(line) > len(tag3):
                if line[:len(tag3)] == tag3:
                    javaVersion = line.split()[1]

    dep = "org.python:jython-standalone:" + jythonVersion
    deps.append(dep)

    data = open(scriptFilename,'rb').read()
    scriptFileTextB64 = base64.b64encode(data).decode('utf-8')

    jf = open(javaFilename,"w+")
    jf.write('///usr/bin/env jbang "$0" "$@" ; exit $?' + "\n\n")
    jf.write('// spotless:off\n')
    for dep in deps:
        jf.write("//DEPS " + dep + "\n")
        #print(dep)
    jf.write("//JAVA " + javaVersion + "\n")
    jf.write('// spotless:on\n')

    jtext = text.replace("__CLASSNAME__",javaClassname)
    jtext = jtext.replace("__MAIN_SCRIPT__", scriptFileTextB64)
    jtext = jtext.replace("__MAIN_SCRIPT_FILENAME__", scriptFilename)
    jf.write(jtext)
    jf.close()
    #print(sys.argv[1:])
    params = ""
    for e in sys.argv[1:]:
        if len(params) > 0:
            params += " "
        params += e
    os.system("jbang run " + javaFilename + " " + params)

main()
