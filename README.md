mvn --version
Apache Maven 3.9.8 (36645f6c9b5079805ea5009217e36f2cffd34256)
Maven home: D:\Tooling\Application\apache-maven-3.9.8
Java version: 1.8.0_241, vendor: Oracle Corporation, runtime: C:\Program Files\Java\jdk1.8.0_241\jre
Default locale: en_US, platform encoding: GBK
OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"

1. mvn package
2. modify the dns setting at the line 32 App.java and line line 70 FQDN
3. set the hosts file, format as below:
   target\hosts.txt:
   test01 test 123@abc!@# Root r00t%%%!
   test02 test 123!fwefwe Root rfjiwnw
4.  java -jar target\app-1.0-SNAPSHOT-jar-with-dependencies.jar
5.  check the report file target\connection_report.html
