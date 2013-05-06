echo off
set SCRIPT_DIR=.
java -Xmx1024M -XX:MaxPermSize=256m  -jar "%SCRIPT_DIR%\sbt-launch-0.11.X.jar" %*
echo on