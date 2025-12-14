# ABRA Routing Simulation Test

This directory contains a Python script to simulate and test the ABRA routing system.

## Prerequisites

1. **ABRA backend running** on `localhost:8080`
2. **Python 3** with `requests` library installed

## Setup

```bash
# Install dependencies
pip install requests

# Or using pip3
pip3 install requests
```

## Running the Simulation

1. First, start the ABRA backend:
   ```bash
   cd /home/matip/projects/ABRA-backend
   ./gradlew bootRun
   ```

2. In another terminal, run the simulation:
   ```bash
   cd /home/matip/projects/ABRA-backend/test-simulation
   python3 simulate_routing.py
   ```

## API Endpoints Used

The script uses the following REST API endpoints:
- `GET/POST /api/domains` - Domain management
- `DELETE /api/domains/{id}` - Delete domain
- `POST /api/tests` - Create tests
- `POST /api/variants` - Create variants  
- `POST /api/endpoints` - Create endpoints

## What the Simulation Tests

### Test Data Setup
- Creates domain `sklep.pl`
- Creates test for `/cart` with 2 variants (50/50 weight)
- Creates test for `/user` with 3 variants (50/30/20 weight)
- Each variant has 2 endpoints on localhost (ports 9001-9016)

### Test 1: Weighted Variant Distribution
Sends 100 requests each to `/cart` and `/user` with different sessions (simulating different users).
Expected distribution should roughly match the configured weights.

### Test 2: Cookie-Based Session Persistence
Same user (same session/cookies) making multiple requests should always be routed to the same variant.

### Test 3: Round-Robin Load Balancing
Within a variant, requests should be distributed evenly across all healthy endpoints using round-robin.

### Test 4: Multiple Tests, Same User
Verifies that a user can be in different variants for different tests (e.g., variant A for /cart but variant B for /user).

## Manual Testing

After the simulation, you can manually test:

```bash
# Test /cart routing
curl -H 'Host: sklep.pl' http://localhost:8080/cart -v

# Test /user routing
curl -H 'Host: sklep.pl' http://localhost:8080/user -v

# Test with cookies (session persistence)
curl -H 'Host: sklep.pl' http://localhost:8080/cart -v -c cookies.txt
curl -H 'Host: sklep.pl' http://localhost:8080/cart -v -b cookies.txt
```

## Mock Servers

The script automatically starts mock HTTP servers on ports 9001-9016 that respond to health checks and routing requests.
