Steps to connect with external API

1. Start the python server
$ py -m flask --app server run

2. For Java application, Server port and URI are defined in the application.properties file under resources.
Please change the port if needed.

3. Start the Java application

4. Once #3 is done openAPI UI can be accessed via below

http://localhost:8080/webjars/swagger-ui/index.html

5. To run the Junit tests the server uri is defined in application-test.properties under test/resources as a placeholder