
# Bus Ticket Management Web Application 🚌


## Table of Contents
1. [Introduction](#introduction)
2. [Features](#features)
3. [Technologies Used](#technologies-used)
4. [System Architecture](#system-architecture)
5. [Database Structure](#database-structure)
6. [Usage](#usage)
7. [Screenshots](#screenshots)
8. [Future Improvements](#future-improvements)
9. [Contributors](#contributors)


---

## 1. Introduction
The **Bus Ticket Management Web App** is designed to facilitate easy booking, managing trips, and invoicing for both passengers and administrators. This system offers a seamless experience, ensuring accurate ticket reservations and customer notifications.

<p align="center">
  <img src="https://github.com/user-attachments/assets/d6497955-19a5-4f0b-87fa-3ab1f96bb70b" alt="Home Page" width="1000">
</p>

---

## 2. Features
- User registration and login.
- Search and filter trips by route and date.
- Book tickets for single and round trips..
- Admin dashboard for managing trips, routes, customers and bill.
- Generate and send invoices upon successful booking.
- View booking history and manage cancellations.

---

## 3. Technologies Used
- **Frontend**: HTML, CSS, JavaScript, Bootstrap, Thymleaf
- **Backend**: Spring Boot
- **Database**: MySQL
- **Others**: Postman (API Testing)

---

## 4. System Architecture
This project follows a client-server architecture, with separate components for the frontend, backend, and database for improved scalability and maintainability.

---

## 5. Database Structure
The core entities include:
- **Account**: Manages user account information.
- **Customer, Driver, Controller**: Manages information about customers, drivers, and bus controllers in the system.
- **Bill**: Manages payment invoices.
- **Bill_Detail**: Stores details of each invoice.
- **Bus**: Manages information about buses.
- **Seat**: Manages information about the seats on buses.
- **Trip**: Manages trip information.
- **Booking**: Manages ticket information.
- **Route**: Manages route information.
- **Checkpoint**: Manages bus stops and rest stations.
- **RouteCheckpoint**: Manages checkpoints on each route.
- **SeatReservation**: Manages the reservation status of seats on buses.
  
<p align="center">
  <img src="https://github.com/user-attachments/assets/54b005fc-b49f-42ae-91d8-bc7cf433cec2" alt="Database Diagram" width="800">
</p>


---

## 7. Usage
1. **Login or Register** as a user or admin.
2. **Search for available trips** by entering origin, destination, and date.
3. **Book a trip** and proceed to payment via online (VNPAY).
4. **Receive a booking confirmation and invoice via Email**.
5. **Admin access**: Manage trips, routes, users, and view reports.

---

## 8. Screenshots
### Login Page
<p align="center">
  <img src="https://github.com/user-attachments/assets/c1ca2604-4542-4d33-8c24-bf47d40c98f1" alt="Login Page" width="1000">
</p>


### Trip Search and Booking
<p align="center">
  <img src="(https://github.com/user-attachments/assets/204ed904-7d41-4201-8a23-43befeb8286c" alt="Trip Search" width="1000">
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/5d10a146-fdf1-424b-a25a-7d4974dd6f2b" alt="Booking" width="1000">
</p>

### Admin Dashboard
<p align="center">
  <img src="https://github.com/user-attachments/assets/9beb8a91-3bbb-4eb2-9466-e5d599a47f43" alt="Admin Dashboard" width="1000">
</p>
---

## 9. Future Improvements
- **Add more payment method online (Momo,...)**
- **Support for multiple languages**.
- **View history booking**
- **Mobile app version** for Android and iOS.
- **Analytics dashboard** for sales reports and insights.

---

## 10. Contributors
- **Do Nguyen Hoang Tuan** – Manages the ticket booking process
- **Phan Duy** - Manages the ticket booking process
- **Nguyen Anh Danh** - Manages admin feature
- **Van Phu Hieu** - Manages admin feature

---

## 11. License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

### Thank you for using our Bus Ticket Management Web App! 🎟️  
