set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.11"
cd "C:\Users\mroko\apache-tomcat-10.1.60\bin"
./catalina.bat run


$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.11"
cd "C:\Users\mroko\apache-tomcat-10.1.60\bin"
.\catalina.bat run

##________________________________________________

# QuizRank

Online Quiz & Ranking System built with **Java Servlet, HTML, Tailwind CSS, MariaDB/MySQL and Apache Tomcat**.

---

## 1. Database Setup

Start **XAMPP → MySQL/MariaDB**.

Open phpMyAdmin:

```text
http://localhost/phpmyadmin
```

Create a database:

```sql
CREATE DATABASE quizrank;
```

Import the project SQL file:

```text
quizrank.sql
```

---

## 2. Database Configuration

In:

```text
backend/util/DBConnection.java
```

use:

```java
private static final String URL =
        "jdbc:mariadb://localhost:3306/quizrank";

private static final String USER = "root";

private static final String PASSWORD = "";
```

If your MariaDB root user has a password, change `PASSWORD`.

---

## 3. MariaDB JDBC Driver

Make sure this file exists:

```text
backend/lib/mariadb-java-client-3.5.10.jar
```

For the Tomcat application, also keep the driver inside:

```text
QuizRank/WEB-INF/lib/
```

---

## 4. Test Database Connection

Open PowerShell and go to the backend folder:

```powershell
cd "C:\Users\<YOUR_USERNAME>\Desktop\QuizRank\backend"
```

Compile:

```powershell
javac -cp "lib\mariadb-java-client-3.5.10.jar;." util\DBConnection.java util\TestConnection.java
```

Run:

```powershell
java -cp "lib\mariadb-java-client-3.5.10.jar;." util.TestConnection
```

Expected:

```text
Database connected successfully!
QuizRank Database Connection Successful!
```

---

## 5. Compile DBConnection for Tomcat

Compile `DBConnection.java` directly into the Tomcat application's classes folder:

```powershell
javac -cp "lib\mariadb-java-client-3.5.10.jar;." -d "C:\Users\<YOUR_USERNAME>\apache-tomcat-10.1.60\webapps\QuizRank\WEB-INF\classes" util\DBConnection.java
```

---

## 6. Start Apache Tomcat

Go to the Tomcat `bin` folder:

```powershell
cd "C:\Users\<YOUR_USERNAME>\apache-tomcat-10.1.60\bin"
```

Start Tomcat:

```powershell
.\catalina.bat run
```

Or:

```powershell
.\startup.bat
```

`catalina.bat run` is recommended because it shows the Tomcat logs directly in the terminal.

---

## 7. Run QuizRank

Open your browser:

```text
http://localhost:8080/QuizRank/
```

Login page:

```text
http://localhost:8080/QuizRank/login.html
```

---

## Project Flow

```text
XAMPP MariaDB
      ↓
quizrank Database
      ↓
Java Servlet
      ↓
MariaDB JDBC Driver
      ↓
Apache Tomcat
      ↓
QuizRank Web Application
```

---

## Requirements

* JDK 21
* XAMPP (MariaDB/MySQL)
* Apache Tomcat 10.1.x
* MariaDB JDBC Driver 3.5.10
* VS Code / any Java IDE

> Note: Apache Tomcat's own Apache HTTP Server is not required. XAMPP is only used here for MariaDB/phpMyAdmin.
 