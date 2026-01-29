# High-Concurrency Enterprise Banking Engine

![Java](https://img.shields.io/badge/Java-21%2B-orange)
![Build](https://img.shields.io/badge/Build-Maven-blue)
![License](https://img.shields.io/badge/License-MIT-green)
![Status](https://img.shields.io/badge/Status-Completed-brightgreen)

## 📌 Project Overview
This project is a thread-safe, modular **Banking Simulation Engine** built in Java SE. It is designed to simulate a high-volume transactional environment where multiple threads access shared resources (bank accounts) simultaneously.

The core objective was to demonstrate **concurrency control**, **deadlock prevention**, and **atomic operations** without relying on database-level locking, implementing these concepts purely in memory using the `java.util.concurrent` package.

## 🚀 Key Features

### 1. Advanced Concurrency Control
* **ReentrantLock Implementation:** Replaces standard `synchronized` blocks to offer advanced locking mechanisms.
* **Fairness Policy:** The engine uses `new ReentrantLock(true)` to ensure First-In-First-Out (FIFO) access to threads, preventing **thread starvation** during high traffic.
* **Atomic Transactions:** Transactions (Deposit, Withdraw, Transfer) are atomic; either the entire operation succeeds, or it fails safely without data corruption.

### 2. Deadlock Prevention Strategy
* **Resource Ordering:** Implements a "Dining Philosophers" solution for transfers. When moving funds between Account A and Account B, the system enforces a strict locking order based on Account IDs. This mathematically guarantees that a circular wait (Deadlock) cannot occur.

### 3. System Engineering Add-ons
* **Audit Logging:** Asynchronous logging of every transaction (Success/Fail) with timestamps and executing Thread IDs to `system_audit.log`.
* **State Persistence:** Simulates a "database dump" by serializing the in-memory state of all accounts to a CSV file (`inventory.csv`) upon system shutdown.
* **Graceful Shutdown:** Uses `ExecutorService` lifecycle management to ensure all active transactions complete before the application closes.

## 🛠 Tech Stack
* **Language:** Java 17+
* **Build Tool:** Maven
* **Testing:** JUnit 5 (Jupiter)
* **Concurrency:** `java.util.concurrent` (Executors, ReentrantLock, ConcurrentHashMap)

## 📂 Project Structure
```text
src/main/java/com/bank
├── model
│   └── BankAccount.java       // POJO with embedded ReentrantLock
├── service
│   └── BankEngine.java        // Business logic & Deadlock prevention
├── util
│   └── AuditLogger.java       // File I/O for audit logs
└── Main.java                  // Simulation Entry Point

⚙️ How to Run
Prerequisites
Java JDK 17 or higher

Maven 3.x

Running the Simulation
Clone the repository:

Bash
git clone [https://github.com/YOUR_USERNAME/BankEngine.git](https://github.com/YOUR_USERNAME/BankEngine.git)
Navigate to the project directory:

Bash
cd BankEngine
Run the application using your IDE (IntelliJ/Eclipse) or Maven:

Bash
mvn clean compile exec:java -Dexec.mainClass="com.bank.Main"
Expected Output: The console will show the shutdown sequence, and two files will be generated in the root directory:

inventory.csv: The final balance of all accounts.

system_audit.log: A history of every thread operation.

🧪 Testing
The project includes a JUnit 5 test suite to verify thread safety and race conditions.

To run the tests:

Bash
mvn test
Key Tests:

testRaceConditionOnDeposit: Spawns 1,000 threads to deposit $1.00 into a single account. Verifies that the final balance is exactly $1,000.00 (proving no "lost updates").

testDeadlockFreedom: Forces high-frequency cross-transfers (A->B and B->A) to ensure the locking strategy prevents the system from freezing.

📄 License
This project is licensed under the MIT License - see the LICENSE file for details.


---

### **One Final Detail (The License)**
To make that "License" badge in the README valid, you should add a standard license file.

**Next Step:** Would you like me to generate the text for a standard **MIT License** file so you can add that to your repo as well?
