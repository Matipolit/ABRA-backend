#!/usr/bin/env python3
"""
ABRA Backend Routing Simulation Test

This script simulates real-world usage of the ABRA routing system:
1. Authenticates with the admin API using JWT tokens
2. Sets up domains, tests, variants, and endpoints via the admin API
3. Starts mock endpoint servers that respond to health checks
4. Performs various routing requests to demonstrate:
   - Weighted variant selection (50/50 and 50/30/20)
   - Round-robin load balancing across endpoints
   - Cookie-based session persistence (same user stays on same variant)

Prerequisites:
- ABRA backend running on localhost:8080
- Admin host configured as 'localhost' in application.properties
- Default user configured (username: 'admin', password: 'admin')

Usage:
    python3 simulate_routing.py
"""

import http.server
import json
import random
import socketserver
import string
import threading
import time
from collections import defaultdict
from http.cookies import SimpleCookie
from urllib.parse import urljoin

import requests

# Configuration
ADMIN_BASE_URL = "http://localhost:8080"
DOMAIN_HOST = "sklep.pl"

# Authentication credentials (default user from application.properties)
ADMIN_USERNAME = "admin"
ADMIN_PASSWORD = "admin"

# Global variable to store JWT token
jwt_token = None

# Endpoint ports for our mock servers
ENDPOINT_PORTS = {
    # Variant endpoints (for A/B test routing)
    "cart_v1_e1": 9001,
    "cart_v1_e2": 9002,
    "cart_v2_e1": 9003,
    "cart_v2_e2": 9004,
    "user_v1_e1": 9011,
    "user_v1_e2": 9012,
    "user_v2_e1": 9013,
    "user_v2_e2": 9014,
    "user_v3_e1": 9015,
    "user_v3_e2": 9016,
    # Default domain endpoints (fallback when no test matches)
    "domain_default_1": 9100,
    "domain_default_2": 9101,
}

# Track requests received by each mock server
request_counts = defaultdict(int)
request_counts_lock = threading.Lock()


class MockEndpointHandler(http.server.BaseHTTPRequestHandler):
    """Simple HTTP handler that responds to all requests with 200 OK"""
    
    def do_GET(self):
        port = self.server.server_address[1]
        endpoint_name = next(
            (name for name, p in ENDPOINT_PORTS.items() if p == port),
            f"port-{port}"
        )
        
        with request_counts_lock:
            request_counts[endpoint_name] += 1
        
        self.send_response(200)
        self.send_header("Content-Type", "text/plain")
        self.end_headers()
        response = f"Response from endpoint: {endpoint_name} (port {port})\nPath: {self.path}"
        self.wfile.write(response.encode())
    
    def log_message(self, format, *args):
        # Suppress default logging
        pass


class ReusableTCPServer(socketserver.TCPServer):
    allow_reuse_address = True


def start_mock_server(port):
    """Start a mock HTTP server on the given port"""
    handler = MockEndpointHandler
    with ReusableTCPServer(("", port), handler) as httpd:
        httpd.serve_forever()


def start_all_mock_servers():
    """Start all mock endpoint servers in background threads"""
    print("\n" + "=" * 60)
    print("STARTING MOCK ENDPOINT SERVERS")
    print("=" * 60)
    
    threads = []
    for name, port in ENDPOINT_PORTS.items():
        thread = threading.Thread(target=start_mock_server, args=(port,), daemon=True)
        thread.start()
        threads.append(thread)
        print(f"  ✓ Started mock server: {name} on port {port}")
    
    # Give servers time to start
    time.sleep(1)
    print(f"\nAll {len(ENDPOINT_PORTS)} mock servers running!")
    return threads


def login_and_get_token():
    """Login to the admin API and get JWT token"""
    print("\n" + "=" * 60)
    print("AUTHENTICATING WITH ADMIN API")
    print("=" * 60)
    
    url = urljoin(ADMIN_BASE_URL, "/api/auth/login")
    login_data = {
        "login": ADMIN_USERNAME,
        "password": ADMIN_PASSWORD
    }
    
    try:
        response = requests.post(url, json=login_data, headers={"Content-Type": "application/json"})
        if response.status_code == 200:
            token_data = response.json()
            token = token_data.get("token")
            if token:
                print(f"  ✓ Successfully authenticated as '{ADMIN_USERNAME}'")
                print(f"  ✓ JWT token obtained (expires in 1 hour)")
                return token
            else:
                print(f"  ✗ Login succeeded but no token in response")
                return None
        else:
            print(f"  ✗ Authentication failed: {response.status_code} - {response.text}")
            return None
    except Exception as e:
        print(f"  ✗ Authentication error: {e}")
        return None


def admin_request(method, endpoint, data=None):
    """Make a request to the admin API with JWT authentication"""
    url = urljoin(ADMIN_BASE_URL, endpoint)
    headers = {"Content-Type": "application/json"}
    
    # Add JWT token to headers if available
    if jwt_token:
        headers["Authorization"] = f"Bearer {jwt_token}"
    
    if method == "GET":
        response = requests.get(url, headers=headers)
    elif method == "POST":
        response = requests.post(url, headers=headers, json=data)
    elif method == "DELETE":
        response = requests.delete(url, headers=headers)
    else:
        raise ValueError(f"Unknown method: {method}")
    
    return response


def cleanup_existing_data():
    """Remove any existing test data"""
    print("\n" + "=" * 60)
    print("CLEANING UP EXISTING DATA")
    print("=" * 60)
    
    # First, get all domains to find the one we need to delete
    try:
        response = admin_request("GET", "/api/domains")
        if response.status_code == 200:
            domains = response.json()
            for domain in domains:
                if domain.get("host") == DOMAIN_HOST:
                    domain_id = domain.get("domain_id")
                    # Delete associated endpoints, variants, tests first (cascade may handle this)
                    delete_response = admin_request("DELETE", f"/api/domains/{domain_id}")
                    if delete_response.status_code in [200, 204]:
                        print(f"  ✓ Deleted existing domain: {DOMAIN_HOST} (ID: {domain_id})")
                    else:
                        print(f"  - Could not delete domain: {delete_response.status_code}")
                    break
            else:
                print(f"  - Domain {DOMAIN_HOST} did not exist")
    except Exception as e:
        print(f"  - Cleanup note: {e}")


def setup_test_data():
    """Create all the test data via admin API"""
    print("\n" + "=" * 60)
    print("SETTING UP TEST DATA")
    print("=" * 60)
    
    # 1. Create domain
    print("\n[1] Creating domain...")
    domain_data = {
        "host": DOMAIN_HOST,
        "active": True,
        "description": "Test e-commerce domain"
    }
    response = admin_request("POST", "/api/domains", domain_data)
    if response.status_code in [200, 201]:
        domain = response.json()
        domain_id = domain.get("domain_id")
        print(f"  ✓ Created domain: {DOMAIN_HOST} (ID: {domain_id})")
    else:
        print(f"  ✗ Failed to create domain: {response.status_code} - {response.text}")
        return None
    
    # 2. Create tests
    print("\n[2] Creating tests...")
    
    # Test for /cart
    cart_test_data = {
        "name": "Cart A/B Test",
        "subpath": "/cart",
        "active": True,
        "description": "Testing cart page variants",
        "domainModel": {"domain_id": domain_id}
    }
    response = admin_request("POST", "/api/tests", cart_test_data)
    if response.status_code in [200, 201]:
        cart_test = response.json()
        cart_test_id = cart_test.get("test_id")
        print(f"  ✓ Created test: Cart A/B Test (ID: {cart_test_id})")
    else:
        print(f"  ✗ Failed to create cart test: {response.status_code} - {response.text}")
        return None
    
    # Test for /user
    user_test_data = {
        "name": "User Page Test",
        "subpath": "/user",
        "active": True,
        "description": "Testing user page variants",
        "domainModel": {"domain_id": domain_id}
    }
    response = admin_request("POST", "/api/tests", user_test_data)
    if response.status_code in [200, 201]:
        user_test = response.json()
        user_test_id = user_test.get("test_id")
        print(f"  ✓ Created test: User Page Test (ID: {user_test_id})")
    else:
        print(f"  ✗ Failed to create user test: {response.status_code} - {response.text}")
        return None
    
    # 3. Create variants
    print("\n[3] Creating variants...")
    
    variants = {}
    
    # Cart variants (50/50)
    for i, (name, weight) in enumerate([("Cart-Variant-A", 50), ("Cart-Variant-B", 50)]):
        variant_data = {
            "name": name,
            "active": True,
            "weight": weight,
            "description": f"Cart variant with {weight}% weight",
            "testModel": {"test_id": cart_test_id}
        }
        response = admin_request("POST", "/api/variants", variant_data)
        if response.status_code in [200, 201]:
            variant = response.json()
            variants[f"cart_v{i+1}"] = variant.get("variant_id")
            print(f"  ✓ Created variant: {name} (weight: {weight}, ID: {variant.get('variant_id')})")
        else:
            print(f"  ✗ Failed to create variant {name}: {response.status_code} - {response.text}")
    
    # User variants (50/30/20)
    for i, (name, weight) in enumerate([
        ("User-Variant-A", 50),
        ("User-Variant-B", 30),
        ("User-Variant-C", 20)
    ]):
        variant_data = {
            "name": name,
            "active": True,
            "weight": weight,
            "description": f"User variant with {weight}% weight",
            "testModel": {"test_id": user_test_id}
        }
        response = admin_request("POST", "/api/variants", variant_data)
        if response.status_code in [200, 201]:
            variant = response.json()
            variants[f"user_v{i+1}"] = variant.get("variant_id")
            print(f"  ✓ Created variant: {name} (weight: {weight}, ID: {variant.get('variant_id')})")
        else:
            print(f"  ✗ Failed to create variant {name}: {response.status_code} - {response.text}")
    
    # 4. Create endpoints for variants (these handle A/B test traffic)
    print("\n[4] Creating variant endpoints...")
    
    endpoint_configs = [
        # Cart variant 1 endpoints
        ("cart_v1_e1", "cart_v1", "Cart-V1-Endpoint-1"),
        ("cart_v1_e2", "cart_v1", "Cart-V1-Endpoint-2"),
        # Cart variant 2 endpoints
        ("cart_v2_e1", "cart_v2", "Cart-V2-Endpoint-1"),
        ("cart_v2_e2", "cart_v2", "Cart-V2-Endpoint-2"),
        # User variant 1 endpoints
        ("user_v1_e1", "user_v1", "User-V1-Endpoint-1"),
        ("user_v1_e2", "user_v1", "User-V1-Endpoint-2"),
        # User variant 2 endpoints
        ("user_v2_e1", "user_v2", "User-V2-Endpoint-1"),
        ("user_v2_e2", "user_v2", "User-V2-Endpoint-2"),
        # User variant 3 endpoints
        ("user_v3_e1", "user_v3", "User-V3-Endpoint-1"),
        ("user_v3_e2", "user_v3", "User-V3-Endpoint-2"),
    ]
    
    for endpoint_key, variant_key, description in endpoint_configs:
        port = ENDPOINT_PORTS[endpoint_key]
        endpoint_data = {
            "url": f"http://localhost:{port}",
            "active": True,
            "alive": True,  # Will be updated by health checker
            "description": description,
            "variantModel": {"variant_id": variants[variant_key]}
            # Note: No domainModel - these are variant-specific endpoints
        }
        response = admin_request("POST", "/api/endpoints", endpoint_data)
        if response.status_code in [200, 201]:
            print(f"  ✓ Created variant endpoint: {description} -> localhost:{port}")
        else:
            print(f"  ✗ Failed to create endpoint {description}: {response.status_code} - {response.text}")
    
    # 5. Create default domain endpoints (fallback when no test matches)
    print("\n[5] Creating default domain endpoints...")
    
    default_endpoint_configs = [
        ("domain_default_1", "Domain-Default-Endpoint-1"),
        ("domain_default_2", "Domain-Default-Endpoint-2"),
    ]
    
    for endpoint_key, description in default_endpoint_configs:
        port = ENDPOINT_PORTS[endpoint_key]
        endpoint_data = {
            "url": f"http://localhost:{port}",
            "active": True,
            "alive": True,
            "description": description,
            "domainModel": {"domain_id": domain_id}
            # Note: No variantModel - these are domain default endpoints
        }
        response = admin_request("POST", "/api/endpoints", endpoint_data)
        if response.status_code in [200, 201]:
            print(f"  ✓ Created default endpoint: {description} -> localhost:{port}")
        else:
            print(f"  ✗ Failed to create endpoint {description}: {response.status_code} - {response.text}")
    
    return {
        "domain_id": domain_id,
        "cart_test_id": cart_test_id,
        "user_test_id": user_test_id,
        "variants": variants
    }


def wait_for_health_check():
    """Wait for the health checker to mark endpoints as alive"""
    print("\n" + "=" * 60)
    print("WAITING FOR HEALTH CHECKS")
    print("=" * 60)
    print("  Health checks run every 30 seconds...")
    print("  Waiting up to 35 seconds for endpoints to be marked alive...")
    
    for i in range(35):
        time.sleep(1)
        print(f"  {i+1}/35 seconds...", end="\r")
    
    print("\n  ✓ Health check period complete!")


def generate_session_id():
    """Generate a random session ID for cookie testing"""
    return ''.join(random.choices(string.ascii_letters + string.digits, k=16))


def simulate_routing_requests():
    """Perform routing requests to demonstrate the system"""
    print("\n" + "=" * 60)
    print("SIMULATING ROUTING REQUESTS")
    print("=" * 60)
    
    # Statistics tracking
    variant_counts = defaultdict(int)
    endpoint_counts_by_variant = defaultdict(lambda: defaultdict(int))
    
    # Test 1: Weighted variant distribution (many different users)
    print("\n[TEST 1] Weighted Variant Distribution")
    print("-" * 40)
    print("Sending 100 requests to /cart (expected: ~50/50 split)")
    print("Sending 100 requests to /user (expected: ~50/30/20 split)")
    
    cart_variants = defaultdict(int)
    user_variants = defaultdict(int)
    
    # Requests to /cart - each with a new session (new user)
    for i in range(100):
        print(f"  /cart request {i+1}/100", end="\r")
        try:
            response = requests.get(
                f"http://localhost:8080/cart",
                headers={"Host": DOMAIN_HOST},
                allow_redirects=False,
                timeout=5
            )
            # Debug: print first few responses
            if i < 3:
                print(f"\n  DEBUG [{i+1}]: Status={response.status_code}, Location={response.headers.get('Location', 'N/A')}")
                if response.status_code != 302:
                    print(f"    Body: {response.text[:300]}")
            
            # The redirect location tells us which endpoint was selected
            if response.status_code == 302:
                location = response.headers.get("Location", "")
                # Extract port to identify variant
                if ":900" in location:
                    port = int(location.split(":")[2].split("/")[0])
                    if port in [9001, 9002]:
                        cart_variants["Cart-Variant-A"] += 1
                    elif port in [9003, 9004]:
                        cart_variants["Cart-Variant-B"] += 1
                elif i < 3:
                    print(f"  DEBUG: Location doesn't match pattern: {location}")
        except Exception as e:
            print(f"\n  - Request error: {e}")
    
    print(f"\n  /cart results (100 requests, new session each):")
    for variant, count in sorted(cart_variants.items()):
        print(f"    {variant}: {count} ({count}%)")
    
    # Requests to /user - each with a new session (new user)
    for i in range(100):
        try:
            response = requests.get(
                f"http://localhost:8080/user",
                headers={"Host": DOMAIN_HOST},
                allow_redirects=False,
                timeout=5
            )
            if response.status_code == 302:
                location = response.headers.get("Location", "")
                if ":901" in location:
                    port = int(location.split(":")[2].split("/")[0])
                    if port in [9011, 9012]:
                        user_variants["User-Variant-A (50)"] += 1
                    elif port in [9013, 9014]:
                        user_variants["User-Variant-B (30)"] += 1
                    elif port in [9015, 9016]:
                        user_variants["User-Variant-C (20)"] += 1
        except Exception as e:
            pass
    
    print(f"\n  /user results (100 requests, new session each):")
    for variant, count in sorted(user_variants.items()):
        print(f"    {variant}: {count} ({count}%)")
    
    # Test 2: Cookie persistence (same user stays on same variant)
    print("\n\n[TEST 2] Cookie-Based Session Persistence")
    print("-" * 40)
    print("Same user (with cookie) should always get the same variant")
    
    # Create a session to persist cookies
    session = requests.Session()
    
    print("\n  User A making 10 requests to /cart:")
    user_a_variants = []
    for i in range(10):
        try:
            response = session.get(
                f"http://localhost:8080/cart",
                headers={"Host": DOMAIN_HOST},
                allow_redirects=False,
                timeout=5
            )
            if response.status_code == 302:
                location = response.headers.get("Location", "")
                if ":900" in location:
                    port = int(location.split(":")[2].split("/")[0])
                    if port in [9001, 9002]:
                        user_a_variants.append("A")
                    elif port in [9003, 9004]:
                        user_a_variants.append("B")
        except:
            pass
    
    print(f"    Variants received: {' -> '.join(user_a_variants)}")
    if len(set(user_a_variants)) == 1:
        print(f"    ✓ SUCCESS: User A consistently got variant {user_a_variants[0]}")
    else:
        print(f"    ✗ ISSUE: User A got different variants (check cookie handling)")
    
    # Different user (new session)
    session2 = requests.Session()
    print("\n  User B making 10 requests to /cart:")
    user_b_variants = []
    for i in range(10):
        try:
            response = session2.get(
                f"http://localhost:8080/cart",
                headers={"Host": DOMAIN_HOST},
                allow_redirects=False,
                timeout=5
            )
            if response.status_code == 302:
                location = response.headers.get("Location", "")
                if ":900" in location:
                    port = int(location.split(":")[2].split("/")[0])
                    if port in [9001, 9002]:
                        user_b_variants.append("A")
                    elif port in [9003, 9004]:
                        user_b_variants.append("B")
        except:
            pass
    
    print(f"    Variants received: {' -> '.join(user_b_variants)}")
    if len(set(user_b_variants)) == 1:
        print(f"    ✓ SUCCESS: User B consistently got variant {user_b_variants[0]}")
    else:
        print(f"    ✗ ISSUE: User B got different variants (check cookie handling)")
    
    # Test 3: Round-robin load balancing within a variant
    print("\n\n[TEST 3] Round-Robin Load Balancing")
    print("-" * 40)
    print("Same variant should distribute requests across its endpoints evenly")
    
    # Reset request counts
    with request_counts_lock:
        request_counts.clear()
    
    # Use a session to stay on the same variant
    session3 = requests.Session()
    
    print("\n  Sending 20 requests with same session to /cart:")
    print("  (Should alternate between 2 endpoints of the same variant)")
    
    endpoints_hit = []
    for i in range(20):
        try:
            response = session3.get(
                f"http://localhost:8080/cart",
                headers={"Host": DOMAIN_HOST},
                allow_redirects=True,  # Actually follow redirect to hit endpoint
                timeout=5
            )
            if response.status_code == 200:
                # Parse response to see which endpoint responded
                text = response.text
                if "port" in text:
                    port = text.split("port ")[1].split(")")[0]
                    endpoints_hit.append(port)
        except Exception as e:
            pass
    
    if endpoints_hit:
        print(f"    Endpoints hit: {' -> '.join(endpoints_hit[:10])}...")
        
        # Count distribution
        endpoint_dist = defaultdict(int)
        for ep in endpoints_hit:
            endpoint_dist[ep] += 1
        
        print(f"    Distribution across endpoints:")
        for ep, count in sorted(endpoint_dist.items()):
            print(f"      Port {ep}: {count} requests")
        
        if len(endpoint_dist) == 2:
            counts = list(endpoint_dist.values())
            if abs(counts[0] - counts[1]) <= 2:
                print(f"    ✓ SUCCESS: Load balanced evenly between 2 endpoints")
            else:
                print(f"    ~ Somewhat balanced (difference: {abs(counts[0] - counts[1])})")
        elif len(endpoint_dist) == 1:
            print(f"    Note: Only one endpoint was hit (other may be unhealthy)")
    
    # Test 4: Multiple paths, same user
    print("\n\n[TEST 4] Multiple Tests, Same User")
    print("-" * 40)
    print("Same user accessing different paths gets potentially different variants")
    
    session4 = requests.Session()
    
    print("\n  User C accessing /cart and /user alternately (5 times each):")
    
    for i in range(5):
        try:
            # Request to /cart
            response = session4.get(
                f"http://localhost:8080/cart",
                headers={"Host": DOMAIN_HOST},
                allow_redirects=False,
                timeout=5
            )
            cart_port = ""
            if response.status_code == 302:
                location = response.headers.get("Location", "")
                if ":900" in location:
                    cart_port = location.split(":")[2].split("/")[0]
            
            # Request to /user
            response = session4.get(
                f"http://localhost:8080/user",
                headers={"Host": DOMAIN_HOST},
                allow_redirects=False,
                timeout=5
            )
            user_port = ""
            if response.status_code == 302:
                location = response.headers.get("Location", "")
                if ":901" in location:
                    user_port = location.split(":")[2].split("/")[0]
            
            print(f"    Request {i+1}: /cart -> port {cart_port}, /user -> port {user_port}")
        except Exception as e:
            print(f"    Request {i+1}: Error - {e}")
    
    # Final summary
    print("\n\n" + "=" * 60)
    print("MOCK SERVER REQUEST SUMMARY")
    print("=" * 60)
    
    with request_counts_lock:
        if request_counts:
            for endpoint, count in sorted(request_counts.items()):
                print(f"  {endpoint}: {count} requests")
        else:
            print("  No requests recorded (redirects may not have been followed)")


def main():
    global jwt_token
    
    print("""
╔═══════════════════════════════════════════════════════════════╗
║          ABRA ROUTING SIMULATION TEST                         ║
║                                                               ║
║  This script demonstrates:                                    ║
║  • JWT authentication for admin API                           ║
║  • Weighted variant selection (A/B testing)                   ║
║  • Round-robin load balancing across endpoints                ║
║  • Cookie-based session persistence                           ║
║  • Health checking of endpoints                               ║
╚═══════════════════════════════════════════════════════════════╝
""")
    
    # Check if ABRA backend is running
    print("Checking if ABRA backend is running...")
    try:
        response = requests.get(f"{ADMIN_BASE_URL}/api/auth/login", timeout=5)
        print(f"  ✓ ABRA backend is running at {ADMIN_BASE_URL}")
    except requests.exceptions.ConnectionError:
        print(f"  ✗ ERROR: Cannot connect to ABRA backend at {ADMIN_BASE_URL}")
        print(f"    Please start the backend first: ./gradlew bootRun")
        return
    
    # Authenticate and get JWT token
    jwt_token = login_and_get_token()
    if not jwt_token:
        print("\n✗ Failed to authenticate. Cannot proceed with admin API calls.")
        print(f"   Make sure the default user is configured:")
        print(f"   - Username: {ADMIN_USERNAME}")
        print(f"   - Password: {ADMIN_PASSWORD}")
        return
    
    # Start mock servers
    start_all_mock_servers()
    
    # Cleanup and setup
    cleanup_existing_data()
    test_data = setup_test_data()
    
    if not test_data:
        print("\n✗ Failed to set up test data. Exiting.")
        return
    
    # Wait for health checks
    # wait_for_health_check()
    
    # Run simulation
    simulate_routing_requests()
    
    print("\n" + "=" * 60)
    print("SIMULATION COMPLETE")
    print("=" * 60)
    print("\nThe mock servers will keep running. Press Ctrl+C to exit.")
    print("You can also manually test with:")
    print(f"  curl -H 'Host: {DOMAIN_HOST}' http://localhost:8080/cart -v")
    print(f"  curl -H 'Host: {DOMAIN_HOST}' http://localhost:8080/user -v")
    
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\n\nShutting down...")


if __name__ == "__main__":
    main()
