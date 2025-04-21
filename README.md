# Apartment Accessibility Analyzer

## Overview

The **Apartment Accessibility Analyzer** is a Java-based application designed to fetch, process, and rank apartment listings from Daft.ie based on their accessibility to a predefined destination. The application calculates travel times using various transportation modes and categorizes apartments into walkable, light rail, and bus-accessible groups.

## Features

- Fetches apartment listings from Daft.ie.
- Extracts and processes listing details such as coordinates, URLs, and IDs.
- Calculates travel times using the Google Maps Routing API.
- Categorizes apartments based on accessibility:
  - **Walkable Routes**: Within 20 minutes of walking.
  - **Light Rail Routes**: Reachable by Luas/Dart within 30 minutes.
  - **Bus Routes**: Reachable by bus within 30 minutes.
- Outputs categorized results with Google Maps links and Daft.ie URLs.

## Project Structure

### Main Components

1. **`Main.main()`**:
   - Entry point of the application.
   - Fetches and processes apartment listings.
   - Outputs categorized results to the console.

2. **`RoutesCalculator`**:
   - Handles travel time calculations using the Google Maps Routing API.
   - Supports walking, light rail, and bus travel modes.

3. **`RoutesRanker`**:
   - Categorizes apartments into walkable, light rail, and bus-accessible groups.

4. **`Apartment`**:
   - Represents an apartment listing with attributes like coordinates, itineraries, and URLs.

### Key Libraries Used

- **Jsoup**: For parsing HTML content.
- **Gson**: For parsing JSON data.
- **Google Maps Routing API**: For calculating travel times.
- **Java HTTP Client**: For making HTTP requests.

## Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd <repository-directory>