#!/bin/bash
jbang cache clear
jbang --fresh run asciidoctorj-cli@wfouche \
    -r asciidoctor-revealjs \
    -r asciidoctor-diagram \
    presentation.adoc \
    -b revealjs
