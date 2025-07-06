///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS dev.jbang:jash:0.0.3
//DEPS org.tomlj:tomlj:1.1.1
//DEPS org.python:jython-slim:2.7.4
//DEPS org.graalvm.python:jbang:24.2.1
//JAVA 21
//NOINTEGRATIONS

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import dev.jbang.jash.Jash;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import org.python.util.jython;

public class python_jvm_tulip {

    private static final String appName = "python-jvm";
    private static final String appVersion = "__JBANG_SNAPSHOT_ID__/__JBANG_SNAPSHOT_TIMESTAMP__";

    private static void displayAppInfo() {
        String version = appVersion;
        if (appVersion.contains("JBANG_SNAPSHOT_ID")) {
            version = "0/2025-05-02T17:58:14";
        }
        System.out.println(appName + "/" + version);
    }

    private static final String textJythonApp = """
            import org.python.util.PythonInterpreter;
            import java.util.Base64;
            
            public class __CLASSNAME__ {
            
                public static String mainScriptTextBase64 = "__MAIN_SCRIPT__";
 
                public static void main(String... args) {
                    String mainScriptFilename = "__MAIN_SCRIPT_FILENAME__";
                    String mainScript = "";
                    String pythonArgsScript = "";
                    for (String arg: args) {
                        if (pythonArgsScript.length() == 0) {
                            if (!arg.equals(mainScriptFilename)) {
                                pythonArgsScript += "'" + mainScriptFilename + "', ";
                            }
                        } else {
                            pythonArgsScript += ", ";
                        }
                        pythonArgsScript += "'" + arg + "'";
                    }
                    if (pythonArgsScript.length() == 0) {
                        pythonArgsScript = "'" + mainScriptFilename + "'";
                    }
                    pythonArgsScript = "import sys; sys.argv = [" + pythonArgsScript + "]";
                    {
                        byte[] decodedBytes = Base64.getDecoder().decode(mainScriptTextBase64);
                        String text = new String(decodedBytes);
                        mainScript = text;
                    }
                    {
                        // create Python interpreter object
                        PythonInterpreter pyInterp = new PythonInterpreter();
                        // initialize command-line args
                        pyInterp.exec(pythonArgsScript);
                        // run script
                        pyInterp.exec(mainScript);
                    }
                }
            }            
            """;

    public static final String textGraalpyApp = """
            import org.graalvm.polyglot.*;
            import java.util.Base64;
            
            public class __CLASSNAME__ {
            
                public static String mainScriptTextBase64 = "__MAIN_SCRIPT__";
            
                public static void main(String... args) {
                    String mainScriptFilename = "__MAIN_SCRIPT_FILENAME__";
                    String mainScript = "";
                    String pythonArgsScript = "";
                    for (String arg: args) {
                        if (pythonArgsScript.length() == 0) {
                            if (!arg.equals(mainScriptFilename)) {
                                pythonArgsScript += "'" + mainScriptFilename + "', ";
                            }
                        } else {
                            pythonArgsScript += ", ";
                        }
                        pythonArgsScript += "'" + arg + "'";
                    }
                    if (pythonArgsScript.length() == 0) {
                        pythonArgsScript = "'" + mainScriptFilename + "'";
                    }
                    pythonArgsScript = "import sys; sys.argv = [" + pythonArgsScript + "]";
                    {
                        byte[] decodedBytes = Base64.getDecoder().decode(mainScriptTextBase64);
                        String text = new String(decodedBytes);
                        mainScript = text;
                    }
                    {
                        try (var context = Context.newBuilder().option("python.EmulateJython", "__EMJ__").allowAllAccess(__AAA__).build()) {
                            Source sourceArgs = Source.create("python", pythonArgsScript);
                            Value result = context.eval(sourceArgs);
                            Source sourceMain = Source.create("python", mainScript);
                            result = context.eval(sourceMain);
                        }
                     }
                }
            }
            """;

    public static void main(String[] args) throws IOException {
        List<String> deps = new ArrayList<>();
        String jythonVersion = "2.7.4";
        String graalpyVersion = "";
        String graalpyAllowAllAccess = "true";
        String graalpyEmulateJython = "false";
        String javaVersion = "21";
        String javaRuntimeOptions = "";
        String jbangIntegrations = "true";
        String ls = "\n"; // System.lineSeparator();
        boolean debug = false;
        boolean keepJava = false;

        displayAppInfo();

        // --keep-java <script-name>.py arg1 arg2, arg3 ...
        if (args.length > 1 && args[0].equals("--keep-java")) {
            keepJava = true;
            String[] newArgs = new String[args.length - 1];
            for (int i = 1; i < args.length; i++) {
                newArgs[i-1] = args[i];
            }
            args = newArgs;
        }

        // Invoke the Jython interpreter if no Python script file is specified, or Jython command-line options are specified
        if (args.length == 0 || (args.length > 0 && args[0].substring(0,1).equals("-"))) {
            jython.main(args);
            System.exit(0);
        }

        String scriptFilename = args[0];
        String javaClassname = new File(scriptFilename).getName().replace(".", "_");
        String javaFilename = javaClassname + ".java";

        // Parse PEP 723 text block
        {
            StringBuffer tomlText = new StringBuffer("");
            {
                List<String> lines = Files.readAllLines(Paths.get(scriptFilename));
                boolean found = false;
                for (String line: lines) {
                    if (line.startsWith("# /// jbang")) {
                        found = true;
                    }
                    else if (line.startsWith("# ///")) {
                        found = false;
                        break;
                    } else if (line.startsWith("# ")) {
                        if (found) {
                            if (tomlText.length() == 0) {
                                tomlText.append(line.substring(2));
                            } else {
                                tomlText.append(ls + line.substring(2));
                            }
                        }
                    }
                }
            }
            TomlParseResult tpr = Toml.parse(tomlText.toString());
            // [python-jvm]
            TomlTable pythonjvmTable = tpr.getTable("python-jvm");
            if (pythonjvmTable != null) {
                if (pythonjvmTable.isBoolean("debug")) {
                    debug = pythonjvmTable.getBoolean("debug");
                }
            }
            if (debug) {
                System.out.println("[debug] python-jvm init " + javaFilename + " from " + scriptFilename);
            }
            if (debug) {
                System.out.println("");
                System.out.println("[ -----------------jbang-config-begin-------------------- ]");
                System.out.println("");
                System.out.println(tpr.toToml());
                System.out.println("[ -----------------jbang-config-end---------------------- ]");
                System.out.println("");
            }
            if (tpr.isString("requires-jython")) {
                jythonVersion = tpr.getString("requires-jython").substring(2);
            }
            if (tpr.isString("requires-graalpy")) {
                graalpyVersion = tpr.getString("requires-graalpy").substring(2);
                // [graalpy]
                TomlTable graalpyTable = tpr.getTable("graalpy");
                if (graalpyTable != null) {
                    Boolean allowAllAccess = graalpyTable.getBoolean("allowAllAccess");
                    if (allowAllAccess != null && allowAllAccess.equals(Boolean.TRUE) ) {
                        graalpyAllowAllAccess = "true";
                    }
                    Boolean emulateJython = graalpyTable.getBoolean("emulateJython");
                    if (emulateJython != null && emulateJython.equals(Boolean.TRUE) ) {
                        graalpyEmulateJython = "true";
                    }
                }
            }
            if (tpr.isString("requires-java")) {
                javaVersion = tpr.getString("requires-java").substring(2);
            }
            // dependencies
            for (Object e : tpr.getArrayOrEmpty("dependencies").toList()) {
                String dep = (String) e;
                deps.add(dep);
            }
            // [java]
            TomlTable javaTable = tpr.getTable("java");
            if (javaTable != null) {
                String runtimeOptions = javaTable.getString("runtime-options");
                if (runtimeOptions != null) {
                    javaRuntimeOptions = runtimeOptions;
                }
            }
            // [jbang]
            TomlTable jbangTable = tpr.getTable("jbang");
            if (jbangTable != null) {
                Boolean integrations = jbangTable.getBoolean("integrations");
                if (integrations != null && integrations.equals(Boolean.FALSE)) {
                    jbangIntegrations = "false";
                }
            }
        }

        String dep = "org.python:jython-slim:" + jythonVersion;
        if (graalpyVersion.length() > 0) {
            dep = "org.graalvm.python:jbang:" + graalpyVersion;
        }
        deps.add(dep);

        byte[] data = Files.readAllBytes(Paths.get(scriptFilename));
        String scriptFileTextB64 = Base64.getEncoder().encodeToString(data);

        try (BufferedWriter jf = new BufferedWriter(new FileWriter(javaFilename))) {
            jf.write("///usr/bin/env jbang \"$0\" \"$@\" ; exit $?" + ls + ls);
            jf.write("// spotless:off" + ls);
            for (String dependency : deps) {
                jf.write("//DEPS " + dependency + ls);
            }
            jf.write("//JAVA " + javaVersion + ls);
            if (javaRuntimeOptions.length() > 0) {
                jf.write("//RUNTIME_OPTIONS " + javaRuntimeOptions + ls);
            }
            if (jbangIntegrations.equals("false")) {
                jf.write("//NOINTEGRATIONS" + ls);
            }
            jf.write("// spotless:on" + ls + ls);
            String text = textJythonApp;
            if (graalpyVersion.length() > 0) {
                text = textGraalpyApp;
                text = text.replace("__EMJ__", graalpyEmulateJython);
                text = text.replace("__AAA__", graalpyAllowAllAccess);
            }
            String jtext = text.replace("__CLASSNAME__", javaClassname)
                               .replace("__MAIN_SCRIPT__", scriptFileTextB64)
                               .replace("__MAIN_SCRIPT_FILENAME__", scriptFilename);
            jf.write(jtext);
        }

        // register javaFilename to be deleted when the JVM exits
        if (!keepJava) {
            new File(javaFilename).deleteOnExit();
        }

        // jbang run <script>_py.java param1 param2 ...
        {
            StringBuffer params = new StringBuffer("run");

            params.append(" " + javaFilename);
            for (int i = 1; i < args.length; i++) {
                params.append(" " + args[i]);
            }
            //if (debug) System.out.println("[debug] jbang " + params.toString());
            String ext = System.getProperty("os.name").toLowerCase().startsWith("win") ? ".cmd" : "";
            var jargs = params.toString().split("\\s+");
            try (Stream<String> ps = Jash.start("jbang" + ext, jargs).stream()) {
                    ps.forEach(System.out::println);
            }
        }
    }
}
