REM Use this script to create the ZIP file to be uploaded to Maven Central
REM https://central.sonatype.com/
REM
set tver=2.0.0

REM Remove staging folder
rd/q/s .\tulip-runtime\build\staging-deploy

REM Publish JARs to staging folder
call .\gradlew.bat publish

REM Sign the artifacts
pushd .\tulip-runtime\build\staging-deploy\io\github\wfouche\tulip\tulip-runtime\%tver%
gpg -ab tulip-runtime-%tver%-javadoc.jar
gpg -ab tulip-runtime-%tver%-sources.jar
gpg -ab tulip-runtime-%tver%.jar
gpg -ab tulip-runtime-%tver%.module
gpg -ab tulip-runtime-%tver%.pom
popd

REM ZIP the files to be uploaded to Maven Central
pushd .\tulip-runtime\build\staging-deploy
zip -r tulip-runtime-%tver%.zip io
popd

REM Display artifact files and ZIP file
dir /s .\tulip-runtime\build\staging-deploy\io\github\wfouche\tulip\tulip-runtime\%tver%
dir   .\tulip-runtime\build\staging-deploy\*.zip
