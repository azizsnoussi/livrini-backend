-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL, -- CUSTOMER, COURIER, RESTAURANT, ADMIN
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Restaurants Table
CREATE TABLE restaurants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    rating DECIMAL(3,2) DEFAULT 0.0,
    delivery_time_min INTEGER DEFAULT 30,
    delivery_fee DECIMAL(10,3) DEFAULT 0.000,
    image_url VARCHAR(255),
    location geometry(Point, 4326),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Dishes Table
CREATE TABLE dishes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id UUID REFERENCES restaurants(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,3) NOT NULL,
    category VARCHAR(50),
    image_url VARCHAR(255),
    available_options JSONB, -- e.g., ["Harissa", "Thon", "Oeuf", "Fromage"]
    is_available BOOLEAN DEFAULT true
);

-- Orders Table
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID REFERENCES users(id),
    restaurant_id UUID REFERENCES restaurants(id),
    courier_id UUID REFERENCES users(id),
    total_amount DECIMAL(10,3) NOT NULL,
    status VARCHAR(20) NOT NULL, -- PENDING, ACCEPTED, PREPARING, PICKED_UP, ON_THE_WAY, DELIVERED, CANCELLED
    delivery_plan VARCHAR(20) NOT NULL, -- STANDARD, EXPRESS, SCHEDULED, PLUS
    delivery_address geometry(Point, 4326),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Order Items Table
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID REFERENCES orders(id),
    dish_id UUID REFERENCES dishes(id),
    quantity INTEGER NOT NULL,
    selected_options JSONB,
    price_at_time DECIMAL(10,3) NOT NULL
);

-- Couriers Table
CREATE TABLE couriers (
    id UUID PRIMARY KEY REFERENCES users(id),
    vehicle_type VARCHAR(20),
    is_online BOOLEAN DEFAULT false,
    current_location geometry(Point, 4326),
    rating DECIMAL(3,2) DEFAULT 0.0
);
