# Vulnerable Code Example
import requests

# Hardcoded API Key (Intentional for Testing)
API_KEY = "sk_test_4eC39HqLyjWDarjtT1zdp7dc"

def make_request():
    url = "https://api.example.com/data"
    headers = {
        "Authorization": f"Bearer {API_KEY}"
    }
    response = requests.get(url, headers=headers)
    print(response.json())

if __name__ == "__main__":
    make_request()
