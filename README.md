# What is minisql ?
Mini SQL is a database agnostic frontend that aims to be very small yet powerfull and easy to use. It can run on all major platforms that support Java and connect to major DBMS that provide a public Java JDBC driver.

Mini SQL aim to be :
- very lightweight (core program is less than one MB)
- very fast 
- full of essential functionnalities

# Database support 
Current supported database management systems are :
- Oracle
- TDS based : Sybase and MSSQL server
- Postgres

 # About database vendor drivers
 Virtually, Mini SQL can connect to any JDBC compliant database server. 
 However, we included in the distribution only major vendors.
 If you need to include a driver that is not in the provided distribution, clone this project and add the maven dependency for your driver in the 
 root project pom.xml file. Then Build the project using maven so a binary distribution with the newly added driver will be produced.
