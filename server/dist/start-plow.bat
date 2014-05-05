set PORT=8081
set JAVA="%JAVA_HOME%\bin\java.exe"
echo Using Java: %JAVA%
%JAVA% -Dplow.cfg.path=%CD%\resources\plow.properties -jar winstone.jar --webappsDir=webapps --httpPort=%PORT%
