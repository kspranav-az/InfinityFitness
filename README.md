# Gym Billing and Management App

This Android application is designed for managing gym operations, including customer registration, subscription management, billing, and notifications for due payments. The app provides a user-friendly interface to keep track of gym customers, their subscription plans, and billing history.

## Features

- **Customer Registration**: Register new customers with their details and subscription plans.
- **Profile Management**: Users can view and edit their profile details.
- **Billing System**: Generate and send invoices for customer subscriptions via WhatsApp.
- **Customer Data Management**: View, update, and filter customer data based on subscription status.
- **Due Payment Notifications**: Get notified of customers with subscriptions due within the next 2 days.
- **Local Data Storage**: All data is stored locally using Room Database.

## Screenshots
![image](https://github.com/user-attachments/assets/14d73a21-96a1-4b35-b617-afcdbbc233e7)
![image](https://github.com/user-attachments/assets/d9437a5e-e8f8-4117-a105-9a2524859ffd)
![image](https://github.com/user-attachments/assets/92896d15-22af-47ba-a154-d59db95fa610)
![image](https://github.com/user-attachments/assets/d06db753-bdec-4249-9902-f12ac3698881)
![image](https://github.com/user-attachments/assets/19576dc1-7eea-4054-b074-9f68bbd61e4f)

## App Structure

### Fragments

- **HomeFragment**: The landing page of the app, showing an overview of gym operations and shortcuts to key actions like registration and billing.
- **ProfileFragment**: Displays and manages user profile details, allowing the user to edit their information.
- **RegisterFragment**: Facilitates customer registration by taking in customer details like name, phone number, and subscription plan.
- **UserDataFragment**: Displays a detailed list of all registered users, allowing the admin to manage and review customer information.

### Activities

- **CustDataActivity**: Displays customer-specific details, including their subscription history, current plan, and payments made.
- **DueCustActivity**: Displays a list of customers whose subscriptions are about to expire, filtered to show those with due dates within 2 days.
- **EditDataActivity**: Allows for editing customer details such as name, subscription, or billing information.
- **HomeActivity**: The main activity that acts as a container for fragments such as `HomeFragment`, `ProfileFragment`, `UserDataFragment` and `RegisterFragment`.
- **MainActivity**: The entry point of the app where the user logs in or is redirected to the main app functionality.

## Getting Started

### Prerequisites

- Android Studio (Latest Version)
- Gradle
- Minimum Android SDK: 21 (Android 5.0 Lollipop)
- Recommended Android SDK: 30 (Android 11)

### Installation

1. Clone the repository:
    ```bash
    git clone https://github.com/yourusername/gym-billing-app.git
    ```
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Build and run the app on an emulator or a physical device.

### Usage

1. **Home**: From the `HomeFragment`, you can quickly navigate to customer management, billing, and user profile settings.
2. **Register Customers**: Use the `RegisterFragment` to add new customers and assign them to a subscription plan.
3. **Manage Customer Data**: View and manage customer data through `UserDataFragment`. Edit customer details with `EditDataActivity`.
4. **Send Invoices**: After customer registration, their subscription bill can be sent via WhatsApp directly from the `CustDataActivity`.
5. **View Due Subscriptions**: `DueCustActivity` helps filter and display customers whose subscriptions are nearing expiration.

## Technology Stack

- **Java/Kotlin**: Main programming language for Android app development.
- **Room Database**: Local database for storing customer and billing data.
- **RecyclerView**: Used for displaying lists of customers, due subscriptions, and other data.
- **Android Navigation Component**: Manages fragment navigation within the app.
- **WhatsApp API**: Used for sending invoices to customers via WhatsApp.

## App Flow

1. **MainActivity**: Entry point where the user logs in.
2. **HomeActivity**: Primary container for displaying `HomeFragment`, `ProfileFragment`, `RegisterFragment`, and more.
3. **Customer Management**: Registration, editing, and viewing customer data occur through `RegisterFragment`, `EditDataActivity`, `CustDataActivity`, and `UserDataFragment`.
4. **Billing**: Generate and send bills from `CustDataActivity`.
5. **Due Notifications**: Manage and filter due subscriptions through `DueCustActivity`.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
