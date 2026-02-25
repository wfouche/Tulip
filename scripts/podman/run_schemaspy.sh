jbang run \
    --java 21 \
    --deps org.postgresql:postgresql:RELEASE \
    -m org.schemaspy.Main \
    org.schemaspy:schemaspy:RELEASE \
    -t pgsql11 \
    -db demo \
    -host localhost \
    -port 5432 \
    -u postgres \
    -p mysecretpassword \
    -vizjs \
    -o report
