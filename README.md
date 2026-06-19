# HandiSpace - Handmade E-Commerce Android App
=================================================

HandiSpace is a native Android e-commerce application specialized in handmade products. It provides a seamless mobile shopping experience for customers and a comprehensive management tool for shop owners, operating on a B2C (Business-to-Consumer) model.

### **Tech Stack & Architecture**
-----------------------------------
* **Kotlin** is the primary programming language, providing a safe and high-performance foundation.
* **Jetpack Compose** is used for building a modern, declarative, and smooth User Interface.
* **Firebase** serves as the Backend-as-a-Service (BaaS). Utilizes Firestore (NoSQL Database), Authentication, and Storage.
* **Google Gemini AI API** is integrated to power an intelligent virtual assistant for DIY handmade tutorials.
* **Cloudinary** and **Coil** are utilized for optimal image storage, rendering, and caching.
* The application strictly follows the **MVVM (Model-View-ViewModel)** architecture, combining the **Repository Pattern** and **Dagger Hilt** (Dependency Injection) to separate UI from business logic.

### **Key Features**
-----------------------------------
* **Storefront (For Customers):** Secure authentication, product search and filtering, real-time shopping cart, checkout with automatic voucher application, loyalty ranking system, order tracking, and product reviews.
* **Admin Control Panel:** A dedicated management interface to track revenue dashboards, manage products and categories (CRUD), update order statuses (Approve/Deliver/Cancel), and manage voucher campaigns.
* **Intelligent Support System:** Features a built-in Real-time Chat for direct communication between customers and the shop, alongside an AI Chatbot that suggests materials and guides users on making DIY handmade items.
* **Database Design:** Optimized NoSQL hierarchical structure utilizing embedded data arrays to reduce read operations and ensure fast, real-time synchronization.

### **Installation & Setup Guide**
-----------------------------------
1. Clone this project to your machine using **Git**.
2. Open the cloned directory in **Android Studio** (Iguana or newer recommended).
3. Create a project on the Firebase Console and enable Authentication (Email/Password), Cloud Firestore, and Storage.
4. Download the `google-services.json` file from your Firebase project and place it into the `app/` directory of the source code.
5. Setup your API Keys (Google Gemini API, Cloudinary) inside the `local.properties` file.
6. Click **Sync Project with Gradle Files** to download all required dependencies.
7. Click **Run** to build and install the application on an Android Emulator or a physical device running Android 8.0 (API level 26) or higher.

### **Author**
-----------------------------------
* **Ngo Phi Bin**
* **GitHub:** [@phibinngo](https://github.com/phibinngo)
