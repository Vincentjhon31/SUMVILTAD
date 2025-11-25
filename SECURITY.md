# Security Policy

## Overview

The SUMVILTAD Agricultural Management System handles sensitive agricultural data, farmer information, and crop health records. We take security seriously and appreciate the community's help in identifying vulnerabilities.

## Supported Versions

The following versions of SUMVILTAD are currently supported with security updates:

| Version | Supported          | Release Date | Notes                          |
| ------- | ------------------ | ------------ | ------------------------------ |
| 1.1.x   | :white_check_mark: | 2024-2025    | Current stable release         |
| 1.0.x   | :white_check_mark: | 2024         | Legacy support until June 2025 |
| < 1.0   | :x:                | -            | No longer supported            |

**Components:**

- **Web Dashboard**: Laravel 11 + Inertia.js v2 + React 18
- **Mobile App**: Android (Kotlin + Jetpack Compose)
- **ML API**: Python/PyTorch rice disease detection

## Reporting a Vulnerability

### How to Report

If you discover a security vulnerability, please report it responsibly:

**DO NOT** open a public GitHub issue for security vulnerabilities.

Instead, please report via one of these methods:

1. **Email**: Send details to your configured security contact email
2. **GitHub Private Vulnerability Reporting**: Use the "Report a vulnerability" button in the Security tab (when enabled)

### What to Include

Please provide the following information in your report:

- **Description**: Clear description of the vulnerability
- **Component**: Which part (Web, Mobile, ML API) is affected
- **Steps to Reproduce**: Detailed steps to reproduce the issue
- **Impact**: Potential impact and severity assessment
- **Proof of Concept**: Optional code or screenshots demonstrating the issue
- **Suggested Fix**: If you have ideas for remediation (optional)

### Response Timeline

- **Initial Response**: Within 48 hours of report submission
- **Status Updates**: Every 7 days until resolution
- **Fix Timeline**:
  - Critical: 7-14 days
  - High: 14-30 days
  - Medium: 30-60 days
  - Low: 60-90 days

### What to Expect

**If Accepted:**

- We will confirm the vulnerability and its severity
- You'll receive updates on fix progress
- Credit will be given in the security advisory (if desired)
- A security patch will be released with acknowledgment

**If Declined:**

- We will explain why the report doesn't qualify as a security issue
- Alternative recommendations may be provided
- You're welcome to discuss the decision

## Security Best Practices for Contributors

### API Keys & Secrets

- **NEVER** commit sensitive data to the repository:

  - Database credentials
  - API keys (Firebase, ML API)
  - Release keystore passwords
  - Service account JSON files
  - `.env` files with real credentials

- Use `.gitignore` to exclude sensitive files
- Store secrets in environment variables
- Use GitHub Secrets for CI/CD workflows

### Authentication & Authorization

- **Laravel Backend**: Uses Sanctum for API authentication
- **Mobile App**: FCM tokens stored securely, never logged
- **Role-Based Access**: Admin and Farmer roles with proper middleware
- Always validate user permissions before operations

### Data Protection

- **Crop Health Images**: Stored with proper access controls
- **Farmer Information**: Protected by authentication
- **Disease Detection**: ML API uses HTTPS (production)
- **Database**: Sanitize all inputs, use prepared statements

### Dependency Management

- Regularly update dependencies to patch known vulnerabilities
- **Android**: Update Gradle dependencies and target SDK
- **Laravel**: `composer update` (check for security advisories)
- **Python**: Update requirements.txt packages
- **JavaScript**: `npm audit` and update packages

## Known Security Considerations

### Current Implementation

1. **Release Keystore** (Android):

   - Stored locally in `app/sumviltad-release.keystore`
   - **Excluded from Git** via `.gitignore`
   - **Critical**: Backup required for future updates
   - Credentials stored securely outside repository

2. **Firebase Service Account** (Laravel):

   - JSON file in `storage/app/firebase-service-account.json`
   - Not committed to repository
   - Required for push notifications
   - Scoped to Firebase Cloud Messaging only

3. **ML API Communication**:

   - Currently HTTP localhost for development
   - **TODO**: Use HTTPS in production deployment
   - Implement API key authentication for production

4. **APK Distribution**:
   - GitHub Releases for public distribution
   - Firebase App Distribution for testing groups
   - Proper code signing with release keystore (v1.1.2+)

### Recommended Security Enhancements

**High Priority:**

- [ ] Enable Private Vulnerability Reporting on GitHub
- [ ] Set up Dependabot alerts for dependency scanning
- [ ] Implement HTTPS for ML API in production
- [ ] Add API rate limiting for authentication endpoints
- [ ] Enable Laravel rate limiting on login/register

**Medium Priority:**

- [ ] Set up automated security scanning (Snyk, SonarQube)
- [ ] Implement Content Security Policy (CSP) headers
- [ ] Add input validation on all API endpoints
- [ ] Enable CORS properly for production domains
- [ ] Implement API versioning for backward compatibility

**Low Priority:**

- [ ] Add security headers (HSTS, X-Frame-Options, etc.)
- [ ] Implement audit logging for admin actions
- [ ] Set up honeypot for bot detection
- [ ] Add CAPTCHA for public-facing forms

## Secure Development Checklist

Before committing code, ensure:

- [ ] No hardcoded credentials or API keys
- [ ] Sensitive files are in `.gitignore`
- [ ] Input validation on all user inputs
- [ ] SQL queries use parameterized statements
- [ ] Authentication checks on protected routes
- [ ] Authorization checks for role-based actions
- [ ] Error messages don't leak sensitive info
- [ ] Dependencies are up to date
- [ ] Code follows principle of least privilege

## Security Contacts

For urgent security matters, please contact:

- **GitHub**: [@Vincentjhon31](https://github.com/Vincentjhon31)
- **Repository**: [SUMVILTAD](https://github.com/Vincentjhon31/SUMVILTAD)

## Security Updates

Security patches will be announced via:

1. GitHub Security Advisories
2. Release notes in CHANGELOG.md
3. GitHub Releases with security tags

Subscribe to repository notifications to stay informed.

## Acknowledgments

We appreciate security researchers who help improve SUMVILTAD's security. Contributors who responsibly disclose vulnerabilities will be acknowledged (with permission) in:

- Security advisories
- Release notes
- Project documentation

Thank you for helping keep SUMVILTAD secure! 🌾🔒
