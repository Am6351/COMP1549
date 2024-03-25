# COMP1549

Description:

The coursework project aims to implement a networked distributed system for group-based client-server communication, adhering to specific requirements. The system can be operated either automatically or manually, providing flexibility in its usage.

Key Features:
1. **Client-Server Communication**: Clients connect to the server and exchange messages within a group.
2. **Dynamic Coordinator Assignment**: The first member to join becomes the coordinator, and subsequent members are informed about the current coordinator.
3. **State Maintenance**: The coordinator periodically checks the state of active group members, ensuring smooth communication.
4. **Message Broadcasting**: Members can send private or broadcast messages to other group members through the server.
5. **Graceful Exiting**: Members can leave the group at any time, and if the coordinator leaves, another member automatically assumes the role.
6. **Fault Tolerance**: The system ensures uninterrupted communication among remaining members even if some members leave.

Implementation Principles:
- **Design Patterns**: The project employs design patterns to ensure efficient and maintainable code structure.
- **JUnit Testing**: The application is thoroughly tested using JUnit to verify its correctness and robustness.
- **Fault Tolerance**: Measures are implemented to handle failures gracefully, ensuring the system's reliability.
- **Version Control System (VCS)**: The project utilizes a Version Control System (VCS) for collaborative development, enabling efficient code management and collaboration among team members.

By adhering to these principles and practices, the project aims to deliver a reliable, scalable, and maintainable networked distributed system for group-based communication, meeting the requirements specified in the coursework.
