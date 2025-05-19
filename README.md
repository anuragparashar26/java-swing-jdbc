# Hostel Management System  

A **Hostel Management System** built using Java and MongoDB for efficient hostel administration.  

## Prerequisites  

- **MongoDB** (Installed and running)  
- **MongoDB Java Sync Driver 4.9.1** 

## Setup Instructions  

### 1. Set Up the Project in IntelliJ IDEA  
- Open **IntelliJ IDEA**.  
- Select **Open Project** and name it `HostelManagementSystem`.  
- Ensure **Maven** is selected as the build system. 

### 2. Configure the MongoDB Driver  
- Download **MongoDB Java Sync Driver (4.9.1)** from [Maven's Repo](https://mvnrepository.com/artifact/org.mongodb/mongodb-driver-sync).  
- Move the downloaded **JAR file** to the project's `lib` folder (`HostelManagementSystem/lib`).  


### 3. Add Dependency in `pom.xml`  
Inside the `<dependencies>` section of `pom.xml`, add the MongoDB driver dependency:  
```xml
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-sync</artifactId>
    <version>4.9.1</version>
</dependency>
```

### 4. Paste Code Inside `src/`  
Ensure your existing code (from this GitHub repository) is inside:  
```
HostelManagementSystem\src\main\java\org.example\HostelManagementSystem.java
```
Sync the file.

### 5. If running locally, give the connection string as:
```xml
mongodb://localhost:27017/
```

### 6. Run the Project  
- Open `HostelManagementSystem.java` in IntelliJ IDEA.  
- Click **Run**.

