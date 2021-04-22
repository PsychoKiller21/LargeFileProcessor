Steps to run:(In Linux/MAC)

DB setup
- install MYSQL
- login as root user into the default "mysql" database and run the following commands:
    create table product(name varchar(50) not null, sku varchar(50) not null, description text, primary key(sku));
    create table aggregate(name varchar(50) not null, no_of_products int, primary key(name));
- Start your local MYSQL server

Code setup:
- install docker
- Download products.csv from https://drive.google.com/drive/folders/1X3qomdbjWU1oOTbBvxchTzjLMAwYBWFT
- unzip LargeFileProcessor.zip file in any directory (Say Documents)
- put the CSV file "products.csv" in demo/app
- Open demo/src/main/java/com/example/demo/LargeFileProcessor.java and edit this line with your db credentials.
    DriverManager.getConnection("jdbc:mysql://host.docker.internal:3306/mysql","user","password")
- run the following commands in docker:
    cd ~/Documents/demo
    docker build -t demo .
    docker run -v "$(pwd)"/app:/app demo:latest
- wait 2-3 mins for the output. 
    Here, we are have simulated 2 users both doing parallel input as well as fetch
- run the last command again. You should see a different output. 
    Reason : In a real world scenario, information might update during read.
- run again to see the updated output again.   

Query:
- Open demo/src/main/java/com/example/demo/DemoApplication.java
- Add your own tasks to the main function. 
    In a multi-threaded environment, each task represents a new request from a user.
    Each request can either insert data or fetch data or do both. 
    Each task carries the following arguments : 
        username  - name of the user submitting the request
        csvFilePath - input file path, relative to "demo" (leave null/empty if no input)
        requestedNamesList - names to query in the DB (leave null/empty if no output)
- You may also create different types of task by extending the Task class 

What is done from Point To Achieve:

1. Your code should follow concept of OOPS
- Achieved. The code is modular and follows an extensible multithreaded design. All error cases have been handled with proper exceptions.

2. Support for regular non-blocking parallel ingestion of the given file into a table. Consider thinking about the scale of what should happen if the file is to be processed in 2 mins.
- Achieved. During insert, we ensure that the primary keys are unique. We also put a lock using "synchronized" keyword when checking for uniqueness.

3. Support for updating existing products in the table based on `sku` as the primary key. (Yes, we know about the kind of data in the file. You need to find a workaround for it)
- Working on it to achieve

4. All product details are to be ingested into a single table
- Achieved. See "product" table.

5. An aggregated table on above rows with `name` and `count` as the columns
- Achieved. See "aggregate" table.

Sample rows for product table
# name            ,  sku                , description
'Jessica Williams', 'a-personal-country', 'Your her by food economy room. Citizen care we also visit. Hear several table against party conference.'
'Stephen Ray', 'a-their-safe-but', 'Young choice save health feeling. Practice serious worker lawyer country.\nUntil important lose. Consider away fear by difference year probably. Music wear require law require east. Act should road.'
'Charles Norman', 'a-indicate-because', 'Young behavior third huge article. Quality wall production by firm else. Car expert site student west.'
'William Cook', 'ability-movement', 'You whom south customer. Wind sell bill turn sit attorney play. Choose hot watch piece place.'
'Samantha Payne', 'ability-attorney-in', 'Wrong sound effort worry than. Everyone court most apply social seven coach city. Nearly already buy enjoy management.\nJust serious official trouble across. Miss water or.'
'Kristin Johnson', 'ability-establish', 'Wrong rest dinner space. Born network story when add stuff.'
'Lee Allison', 'ability-red-figure', 'Wrong north respond art resource bill. Either real final occur contain beat street medical.\nUnder close improve color list. Support movie job bag open study a.'
'Christine Nguyen', 'a-newspaper', 'Wrong bed ask admit picture table. Public reveal time skill marriage cut.\nWish training heavy agree. Can meet institution total at rather.'
'Anthony Camacho', 'a-crime-test-next', 'Writer scientist shake consumer party. Prove scientist evening crime. Hour religious plant five.\nFeeling window born commercial physical senior fish little. Majority let ten career oil.'
'Taylor Payne', 'ability-each-up', 'Writer eight control sing. Sometimes state detail environment. Left east town color film spend.\nProvide morning front change. Wind top world apply key increase.'

Sample rows for aggregate table
# name        , count
'Aaron Abbott', '1'
'Aaron Acevedo', '1'
'Aaron Acosta', '2'
'Aaron Adams', '6'
'Aaron Aguilar', '1'
'Aaron Alexander', '1'
'Aaron Allen', '5'
'Aaron Allison', '1'
'Aaron Alvarado', '2'
'Aaron Alvarez', '4'


Future Improvements if given more days:
1. Use SQL Query Builder to avoid writing raw queries. Raw queries are prone to hacking via sql code injection.
2. Use enum to represent columns.
3. Support Windows-OS


In case of any issue, feel free to mail me on "thakurkunal32@gmail.com"