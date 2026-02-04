CREATE DATABASE nails_identity;
CREATE DATABASE nails_booking;
CREATE DATABASE nails_reviews;
CREATE DATABASE nails_catalog;
CREATE DATABASE nails_search;

-- Enable PostGIS extension for search-service spatial queries
\c nails_search;
CREATE EXTENSION IF NOT EXISTS postgis;
