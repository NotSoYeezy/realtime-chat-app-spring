# Real-Time Chat Application

## Authors
Franciszek Dyląg, Mateusz Guzowski, Marcin Wojdalski

---

## Architecture

**3-Tier Architecture:**
- **Frontend**: Vue.js
- **Backend**: Spring Boot (REST + WebSocket)
- **Database**: PostgreSQL

---

## Core Components

### 1. Backend (Spring Boot)

**Main Modules:**
- **Authentication & Authorization** - Managing user authentication and authorization
- **WebSocket Layer** - Real-time message delivery
- **REST API** - User management, conversation history, group operations
- **Message Service** - Handle message exchanging process

---

### 2. Frontend (Vue.js)

**Main Features:**
- Login/Registration
- Chat interface (conversation list + message panel)
- Real-time message updates via WebSocket
- User search & contact management
- Group creation & management
- Message notifications

---

### 3. Database (PostgreSQL)

**Core Tables:**
- `users` - User accounts
- `conversations` - Private chats & groups
- `conversation_participants` - Many-to-many relationship
- `messages` - All messages with timestamps

Note: We might also use Redis as a message broker (i.e. when sending async email to user)

---

## Key Considerations

- **Message Delivery**: Handle offline users (store & forward when online)
- **Security**: Validate user permissions for conversations
- **File Sharing**: Add file upload service later if needed

--- 

## Additional Features:
- Read receipts & typing indicators
- Calendar integration -> user status might be automatically toggled to busy when having a meeting in his calendar
- Dockerizing our application
- Swagger documentation