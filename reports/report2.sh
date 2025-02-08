java -Dpython.path=./deps/HdrHistogram-2.2.2.jar:./deps/gson-2.11.0.jar -jar ./deps/jython-standalone-2.7.4.jar  report2.py report2_config.jsonc
jbang run asciidoc@wfouche report2_config.adoc
