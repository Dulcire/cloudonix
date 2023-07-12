# Cloudonix Challenge

# Description
Application get the closest word in word list based on value and lexical.

### Pre - requisites ###

* Git
* Install JDK 17
* Define JDK in JAVA_HOME
* Install Apache Maven 3
* Docker
    

**You can change DB properties in application.properties**
**or If you are using the docker image you can set DB properties in docker-compose.yaml**
### Run Application ###
* Clone cloudonix to in/your/path:
* git clone https://github.com/Dulcire/cloudonix.git

**There are two ways to run the application**
### Option 1 : By Docker ### 
* If you want to run the application by Docker follow this steps
* 1. Go to your capitole-challenge:
*      cd cloudonix-challenge
* 2. execute:
*     mvn clean install  
* 3. execute:
*      docker build -t cloudonix-challenge:latest .
* 4. execute:
*      docker compose up

### Option 2: Manually ###
* Run postgres db
* Go to your cloudonix-challenge: 
*     cd cloudonix-challenge
* Set your properties with your database information
*  NOTE: You should change your URL (flyway and db) propertie as postgresql://{your-server}:{your-port}/{your-db-name}
* execute:
*     mvn clean install  
* execute:
*     java -jar ./target/cloudonix-challenge-0.0.1.jar

### Test Application ###
* If you want to test the application you can use Postman and use this endpoint 
*     POST-> http://localhost:8080/analyze
*     Body ->
*            {
*              "text":"pasa"
*            }
