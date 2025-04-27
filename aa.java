AKIA[0-9A-Z]{16}  # AWS Access Key
(?<![A-Za-z0-9])[A-Za-z0-9]{40}(?![A-Za-z0-9])  # Generic secret
-----BEGIN (RSA|DSA|EC) PRIVATE KEY-----  # Private keys
