# TAJ EGY — Security Architecture & Guidelines

## 1. Zero Plaintext Credentials
API keys, OAuth tokens, and server secrets are never written in plaintext to the Android filesystem, SharedPreferences, or SQLite databases.

## 2. Hardware-Backed Android Keystore
- **Master Key**: 256-bit AES key generated directly inside `AndroidKeyStore`.
- **Cipher Transformation**: `AES/GCM/NoPadding` with a 128-bit authentication tag and randomized 12-byte initialization vectors (IV).
- **Hardware Security Module**: Handled by TEE (Trusted Execution Environment) or StrongBox on supported hardware.
- The secret key never leaves the secure hardware boundary.

## 3. Masked UI Representation
Stored credentials are only ever exposed in the user interface as masked strings (e.g. `••••••••••••ABCD`), exposing at most the trailing 4 characters for identification purposes.

## 4. Network Security & SSRF Protection
- Input URLs are normalized and verified against HTTP/HTTPS schemes.
- Auto-discovery probes inspect hostnames and issue warnings for private RFC-1918 subnets (10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16, loopback 127.0.0.1).
- No arbitrary port scanning or silent telemetry uploads.

## 5. Archive Protection (Zip Slip & Zip Bomb)
When importing Skills or external tool archives:
- The canonical path of every ZIP entry is checked to guarantee it does not traverse outside the target directory (`SecurityValidator.validateZipEntry`).
- Archive extraction strictly caps uncompressed size (50MB) and entry count (200 entries) to prevent decompression bomb denial-of-service.
