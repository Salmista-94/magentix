
@echo off

set MAGENTIX_JAR=..\lib\magentix2-%VERSION%-jar-with-dependencies.zip
set LIBS=%LIBS%;%MAGENTIX_JAR%
set LIBS=%LIBS%;..\lib\MagentixExamples.jar


java -cp "%LIBS%" SingleAgent_Example.Run

echo off
pause
