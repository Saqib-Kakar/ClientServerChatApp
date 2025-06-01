 ClientServerChatApp
Java GUI-based client-server chat application with socket programming

This is a Java-based Client-Server Chat Application using GUI and Socket Programming. It allows multiple clients to connect to a server, exchange messages, send requests (like commands or data queries), and shut down independently. The server responds accordingly and manages all clients through a visual interface.

---

 🛠 Technologies Used

- Java SE
- Java Swing (GUI)
- Java Socket Programming
- Multithreading (ExecutorService)

---

 ✨ Features

 Client Side
- Graphical chat interface
- Sends text messages, commands, or data requests
- Receives real-time responses from server
- Displays its own unique client name (e.g., Client1, Client2)
- Individual shutdown option
- Automatically handles lost server connection

 Server Side
- GUI console with logs
- Handles multiple clients simultaneously
- Each client has its own tab in the interface
- Responds to basic messages and custom commands
- Graceful shutdown for all clients
- Shows message origin and response clearly

---

 🧪 Supported Commands

Clients can send the following messages:

| Message             | Server Response                        |
|---------------------|----------------------------------------|
| `hi` / `hello`      | Hello / Hi                             |
| `how are you`       | I am fine, and you?                    |
| `bye`               | Goodbye!                               |
| `get data`          | Data retrieval successful              |
| `run command`       | Command executed                       |
| `get file`          | File service not yet implemented       |
| Any other message   | Server will echo it with a prefix      |

---

 📁 How to Run

1. Open the project in any Java IDE (NetBeans, IntelliJ, Eclipse)
2. Run `ServerGUI.java`
3. Run `ClientGUI.java` (multiple times to simulate multiple clients)

---

 📌 Project Structure
clientserverchatapplication/
├── ClientGUI.java
├── ServerGUI.java

---

 📚 License
This project is for educational purposes only.

---
 👨‍💻 Created By
Muhammad Saqib & Saad Ahmad 
BS Software Engineering  6th semester
BUITEMS Quetta  
