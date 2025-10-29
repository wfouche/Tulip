///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS org.python:jython-standalone:2.7.4
//DEPS com.google.code.gson:gson:2.11.0
//DEPS org.hdrhistogram:HdrHistogram:2.2.2
//DEPS io.github.wfouche.tulip:tulip-runtime:2.1.12-dev

import org.python.util.jython;

public class Jython {

    public static void main(String... args) {
        jython.main(args);
    }

}
