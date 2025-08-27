# Hotel Pricing Application

A Spring Boot application for managing hotel pricing data with external API integration, user management, and admin functionality.

## 🚀 Features

- **Hotel Price Management**: Track and manage hotel pricing data
- **External API Integration**: Fetch pricing data from Booking.com API
- **User Management**: User registration, authentication, and approval system
- **Admin Panel**: Admin functionality for user approval and management
- **Database Migrations**: Flyway-based database schema management
- **RESTful API**: Comprehensive REST endpoints for all operations

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.2.0
- **Database**: MySQL 8.0
- **ORM**: JPA/Hibernate
- **Migration**: Flyway
- **Build Tool**: Maven
- **Java Version**: 21
- **Password Hashing**: BCrypt
- **HTTP Client**: WebClient (Spring WebFlux)

## 📋 Prerequisites

- Java 21 or higher
- MySQL 8.0
- Maven 3.6+

## 🗄️ Database Setup

1. Create a MySQL database:
```sql
CREATE DATABASE hotel_app;
```

2. Update database configuration in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_app?useSSL=false&serverTimezone=America/Chicago&allowPublicKeyRetrieval=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## 🚀 Running the Application

### 1. Clone the repository
```bash
git clone <repository-url>
cd spring-boot-app
```

### 2. Build the project
```bash
mvn clean compile
```

### 3. Run database migrations
```bash
mvn flyway:migrate
```

### 4. Start the application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📊 API Endpoints

### User Management
- `POST /api/auth/signup` - User registration
- `GET /api/admin/users/pending` - Get pending users (Admin only)
- `POST /api/admin/users/{id}/approve` - Approve user (Admin only)

### Hotel Pricing
- `POST /api/ingest/booking` - Ingest booking data
- `GET /api/prices/single-day` - Get single-day prices
- `GET /api/prices/multi-day` - Get multi-day prices
- `POST /api/fetch/single` - Fetch single hotel data
- `POST /api/fetch/multi-sum` - Fetch multi-night sum data

### UI Endpoints
- `GET /api/ui/single-day` - UI for single-day pricing
- `GET /api/ui/multi-day-simple` - UI for multi-day simple view
- `GET /api/ui/per-night-simple` - UI for per-night pricing
- `GET /api/ui/price-table/single-day` - UI for price table

### User CRUD
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

## 🏗️ Project Structure

```
src/main/java/com/example/springbootapp/
├── config/
│   └── WebConfig.java                 # CORS configuration
├── controller/
│   ├── PricingController.java         # Pricing-related endpoints
│   └── UserController.java            # User management endpoints
├── dto/
│   ├── AdminActionResponse.java       # Admin action responses
│   ├── PriceRow.java                  # Price data transfer objects
│   ├── SignupRequest.java             # User signup request
│   ├── UserDto.java                   # User data transfer object
│   └── UserResponse.java              # User response objects
├── entity/
│   ├── ExternalHotel.java             # External hotel entity
│   ├── PriceSnapshot.java             # Price snapshot entity
│   ├── PriceTableExternalHotel.java   # Price table hotel mapping
│   ├── User.java                      # User entity
│   └── UserStatus.java                # User status enum
├── repository/
│   ├── ExternalHotelRepo.java         # External hotel repository
│   ├── PriceSnapshotRepo.java         # Price snapshot repository
│   ├── PriceTableExternalHotelRepo.java # Price table repository
│   └── UserRepository.java            # User repository
├── service/
│   ├── BookingFetchService.java       # Booking API service
│   ├── PricingIngestService.java      # Pricing ingestion service
│   ├── PriceTableUiService.java       # Price table UI service
│   ├── UiPriceService.java            # UI price service
│   └── UserService.java               # User service
├── util/
│   ├── BookingApiClient.java          # Booking API client
│   ├── BookingMapper.java             # Booking data mapper
│   ├── DateUtils.java                 # Date utility functions
│   └── UserMapper.java                # User data mapper
└── SpringBootAppApplication.java      # Main application class
```

## 🔧 Configuration

### Application Properties
Key configuration in `application.properties`:
- Database connection settings
- Flyway migration configuration
- Server port (8080)
- Actuator endpoints
- External API keys

### External API Configuration
The application integrates with external APIs for hotel pricing data. Update the API keys in `application.properties`:
```properties
booking.api.key=your_booking_api_key
booking.api.base.url=https://booking-com.p.rapidapi.com
```

## 🔐 Security

- **Password Hashing**: BCrypt with salt rounds
- **Admin Authentication**: Header-based admin key validation
- **User Status Management**: PENDING → APPROVED → REJECTED workflow

## 📝 Database Schema

### Core Tables
- `users` - User accounts and authentication
- `external_hotels` - External hotel information
- `price_snapshots` - Historical price data
- `price_tables` - Price table definitions
- `price_table_external_hotels` - Hotel-to-price-table mappings
- `owner_accounts` - Account ownership

### Migrations
- `V1__core_external_hotels.sql` - Core hotel and pricing tables
- `V2__price_tables.sql` - Price table management
- `V3__users_status_password.sql` - User authentication and status

## 🧪 Testing

Run tests with:
```bash
mvn test
```

## 📈 Monitoring

The application includes Spring Boot Actuator for monitoring:
- Health check: `GET /actuator/health`
- Application info: `GET /actuator/info`

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For support and questions, please open an issue in the repository.
