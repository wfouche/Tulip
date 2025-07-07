call jbang cache clear
call jbang --fresh run asciidoctorj-cli@wfouche ^
    -r asciidoctor-revealjs ^
    -r asciidoctor-diagram ^
    presentation.adoc ^
    -b revealjs