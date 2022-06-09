 # Overview
 
There are two types of users - regular customers and couriers.

Regular customers can create packages to be transported by couriers. Once created, each courier can offer a percentage of the base price in order to deliver the package. Customer who created the package then chooses which courier offer to accept.

A courier can start a delivery of the selected packages (only the packages for which the courier has his offer accepted are eligible), delivering them in *FCFS* order. Each courier has a vehicle with certain consumption factors based on which (including the distance traveled) the base price is calculated.

# Implementation Details

- *ERwin Data Modeler* tool was used to create a model for the database. Model contains tables, relationships, column constraints and default values as well as configured *RI*s.
- Database itself has two simple stored procedures used to create/delete a courier request as well as a trigger used to remove data from temporary tables once the data is not needed.
- There is an interface for basic *CRUD* operations as well as for simulating package transportation. Said interface provides methods which communicate with the database using *MS-SQL JDBC*.
