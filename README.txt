1. Ensure you have the following dependencies installed
	• Java 8
	• Gradle
•	 PostgreSQL • Git
2. Download the code repository from https://asb224@git-teaching.cs.bham.ac.uk/mod-60cr- proj-2016/asb224.git
3. Setting up the database
	• Create a database on your local PostgreSQL server (either through terminal or a GUI)
	• Run ‘CREATE EXTENSION postgis;’ for your database
	• Import the .sql file found in /Resources of the code structure using ‘psql dbname <
path/to/Resources/Birmingham.sql’
	• Start the server
4. Import the code as a gradle project (using gradle files) into either IntelliJ (preferred) or Eclispe
	• In /src/Database/Query V3.java, you must change the credentials (lines 31-33) to match your PostgreSQL details (including username, password and database name)
5. To run the simulation run the src/GUI/Display.java class containing the main() method
6. Once the GUI appears, you can:
	• Draw your own bounding box using the checkbox (followed by start button) • Enable statistics mode using the checkbox (followed by start button)
	• Run default location by pressing start button
7. Changing parameters
	• Change the number of vehicles on line 27 of src/Simulation/Simulate.java
	• Change the parameters in increaseCars() (for statistic mode) on line 160 of src/GUI/Display.java
    	• Change the incremental_push variable (number of vehicles pushed per step) on line 25 in src/Simulation/Simulate.java
8. Remember that choosing large amounts of vehicles for a small grid may result in grid lock
