# product-dss

---

|  Branch | Build Status |
| :------------ |:-------------
| master      | [![Build Status](https://wso2.org/jenkins/job/product-dss/badge/icon)](https://wso2.org/jenkins/job/product-dss) |


---

How to enable data service tests?
=================================
1.pom.xml
- Goto maven-surefire-plugin declaration.
- Change <skip>true</skip> to <skip>false</skip>

- Drop the JDBC driver jar (mysql-connector-java-5.1.5-bin.jar in this case) to src/test/resources/lib folder
- Uncomment & change the <!-- JDBC Driver classes --> section


2.Create MySQL database using supplied script 

## How to Contribute
* Please report issues at [DSS JIRA] (https://wso2.org/jira/browse/DS).
* Send your bug fixes pull requests to [master branch] (https://github.com/wso2/product-dss/tree/master) 

## Contact us
WSO2 Carbon developers can be contacted via the mailing lists:

* Carbon Developers List : dev@wso2.org
* Carbon Architecture List : architecture@wso2.org
