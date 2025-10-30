# Contributing to SUMVILTAD Connect

Thank you for your interest in contributing to SUMVILTAD Connect! This document provides guidelines and instructions for contributing to the project.

## 📋 Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [How Can I Contribute?](#how-can-i-contribute)
3. [Development Setup](#development-setup)
4. [Coding Standards](#coding-standards)
5. [Commit Guidelines](#commit-guidelines)
6. [Pull Request Process](#pull-request-process)
7. [Reporting Bugs](#reporting-bugs)
8. [Suggesting Features](#suggesting-features)
9. [Documentation](#documentation)

---

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors. We pledge to:

- Be respectful and considerate
- Welcome diverse perspectives
- Accept constructive criticism gracefully
- Focus on what's best for the community
- Show empathy toward other community members

### Unacceptable Behavior

- Harassment, discrimination, or offensive comments
- Trolling, insulting, or derogatory remarks
- Publishing others' private information
- Any conduct inappropriate in a professional setting

---

## How Can I Contribute?

### 🐛 Reporting Bugs

1. **Check Existing Issues**: Search [Issues](https://github.com/Vincentjhon31/SUMVILTAD/issues) first
2. **Use Bug Template**: Follow the bug report template
3. **Provide Details**: Include:
   - Device and Android version
   - App version
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots or logs
   - Any error messages

### 💡 Suggesting Features

1. **Check Roadmap**: Review planned features in README
2. **Open Discussion**: Start with a discussion, not a PR
3. **Be Specific**: Clearly describe the feature and its benefits
4. **Consider Scope**: Keep features aligned with project goals

### 🔧 Code Contributions

1. **Find an Issue**: Look for issues labeled `good first issue` or `help wanted`
2. **Claim It**: Comment on the issue to let others know you're working on it
3. **Fork & Branch**: Create a feature branch from `main`
4. **Develop**: Write code following our standards
5. **Test**: Ensure all tests pass
6. **Submit PR**: Create a pull request with detailed description

### 📝 Documentation

- Fix typos or unclear explanations
- Add code examples
- Improve README sections
- Write tutorials or guides
- Translate documentation

### 🧪 Testing

- Write unit tests for new features
- Add instrumented tests for UI components
- Test on different devices and Android versions
- Report test coverage improvements

---

## Development Setup

### Prerequisites

```bash
# Required
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11 or higher
- Git
- Android SDK with API 24-36

# Optional
- ADB for device testing
- Firebase CLI for backend testing
```

### Setup Steps

1. **Fork the Repository**

   ```bash
   # Click 'Fork' on GitHub, then:
   git clone https://github.com/YourUsername/SUMVILTAD.git
   cd SUMVILTAD
   ```

2. **Add Upstream Remote**

   ```bash
   git remote add upstream https://github.com/Vincentjhon31/SUMVILTAD.git
   ```

3. **Create Feature Branch**

   ```bash
   git checkout -b feature/your-feature-name
   ```

4. **Configure Firebase**

   - Add `google-services.json` to `app/` directory
   - Contact maintainers for test Firebase project credentials

5. **Build Project**

   ```bash
   ./gradlew build
   ```

6. **Run Tests**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

---

## Coding Standards

### Kotlin Style Guide

Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)

**Key Points:**

```kotlin
// ✅ Good
class DiseaseDetector(
    private val context: Context,
    private val modelPath: String
) {
    fun detectDisease(image: Bitmap): DetectionResult {
        // Clear, concise logic
        return processImage(image)
    }

    private fun processImage(image: Bitmap): DetectionResult {
        // Implementation
    }
}

// ❌ Bad
class dd(c: Context, mp: String) {
    fun detect(i: Bitmap): DetectionResult {
        // Unclear names, missing documentation
    }
}
```

### Naming Conventions

- **Classes**: PascalCase (`DiseaseDetector`, `CropHealthViewModel`)
- **Functions**: camelCase (`detectDisease`, `uploadImage`)
- **Variables**: camelCase (`imageUri`, `confidenceScore`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_IMAGE_SIZE`, `API_BASE_URL`)
- **Composables**: PascalCase (`DiseaseDetectionScreen`, `TaskCard`)

### File Organization

```kotlin
package com.zynt.sumviltadconnect.ui.screens

// Imports organized: Android → Third-party → Project
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zynt.sumviltadconnect.ui.viewmodel.DashboardViewModel

// Constants at top
private const val TAG = "DashboardScreen"
private const val REFRESH_INTERVAL = 30_000L

// Main composable
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    // Implementation
}

// Private helper composables
@Composable
private fun DashboardHeader() {
    // Implementation
}
```

### Comments & Documentation

```kotlin
/**
 * Detects rice diseases from an input image using a PyTorch ML model.
 *
 * @param bitmap The input image containing a rice leaf
 * @return DetectionResult containing disease name, confidence, and recommendations
 * @throws ModelLoadException if the ML model fails to load
 */
fun detectDisease(bitmap: Bitmap): DetectionResult {
    // Preprocess image
    val processedImage = preprocessImage(bitmap)

    // Run inference
    val output = model.forward(processedImage)

    // Parse results
    return parseModelOutput(output)
}
```

### Jetpack Compose Best Practices

```kotlin
// ✅ Good: Hoisted state, clear parameters
@Composable
fun TaskCard(
    task: Task,
    onTaskClick: (Task) -> Unit,
    onCompleteClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onTaskClick(task) },
        modifier = modifier
    ) {
        // Content
    }
}

// ❌ Bad: Stateful, unclear responsibilities
@Composable
fun TaskCard(task: Task) {
    var isExpanded by remember { mutableStateOf(false) }
    // Too much logic in UI
}
```

---

## Commit Guidelines

### Commit Message Format

```
type(scope): subject

body (optional)

footer (optional)
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic change)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `chore`: Build process or tooling changes

### Examples

```bash
# Good commits
feat(disease-detection): add support for Tungro disease
fix(tasks): correct date sorting in task list
docs(readme): update installation instructions
refactor(viewmodel): simplify state management in DashboardViewModel
test(detection): add unit tests for image preprocessing

# With body
feat(notifications): implement task reminder system

Add FCM-based push notifications for task reminders.
Users will receive alerts 1 hour before task due time.

Closes #45
```

### Commit Best Practices

- Use present tense ("add feature" not "added feature")
- Use imperative mood ("fix bug" not "fixes bug")
- Keep subject line under 72 characters
- Reference issues in footer (`Closes #123`, `Refs #456`)
- Make atomic commits (one logical change per commit)

---

## Pull Request Process

### Before Submitting

1. **Update from Upstream**

   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Run All Tests**

   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ./gradlew lint
   ```

3. **Update Documentation**

   - Update README if adding features
   - Add/update code comments
   - Update CHANGELOG.md

4. **Build Successfully**
   ```bash
   ./gradlew assembleRelease
   ```

### Creating the PR

1. **Push Your Branch**

   ```bash
   git push origin feature/your-feature-name
   ```

2. **Open Pull Request**

   - Go to GitHub repository
   - Click "New Pull Request"
   - Select your fork and branch
   - Fill out the PR template

3. **PR Description Should Include**
   - What changes were made
   - Why the changes were needed
   - How to test the changes
   - Screenshots (for UI changes)
   - Related issue numbers

### PR Template

```markdown
## Description

Brief description of changes

## Type of Change

- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## How Has This Been Tested?

- [ ] Unit tests
- [ ] Instrumented tests
- [ ] Manual testing on device
- Device: Pixel 6, Android 13

## Checklist

- [ ] Code follows style guidelines
- [ ] Self-reviewed my code
- [ ] Commented complex code
- [ ] Updated documentation
- [ ] Added tests
- [ ] All tests pass
- [ ] No new warnings
```

### Review Process

1. **Automated Checks**: CI/CD must pass
2. **Code Review**: At least one maintainer approval required
3. **Address Feedback**: Make requested changes
4. **Squash Commits**: Clean up commit history if requested
5. **Merge**: Maintainer will merge when approved

---

## Reporting Bugs

### Bug Report Template

```markdown
**Bug Description**
Clear and concise description of the bug

**To Reproduce**
Steps to reproduce:

1. Go to '...'
2. Click on '...'
3. See error

**Expected Behavior**
What should have happened

**Actual Behavior**
What actually happened

**Screenshots**
If applicable, add screenshots

**Environment**

- Device: [e.g., Samsung Galaxy S21]
- Android Version: [e.g., Android 12]
- App Version: [e.g., 1.0.0]

**Additional Context**
Any other relevant information

**Logs**
```

Paste any relevant logs here

```

```

---

## Suggesting Features

### Feature Request Template

```markdown
**Is your feature request related to a problem?**
Clear description of the problem

**Describe the solution you'd like**
Clear description of what you want to happen

**Describe alternatives you've considered**
Other solutions you've thought about

**Use Case**
Who would benefit and how?

**Mockups/Examples**
Visual representations if applicable

**Additional Context**
Any other relevant information
```

---

## Documentation

### Documentation Standards

- **Clear Language**: Write for non-native English speakers
- **Code Examples**: Include working code snippets
- **Screenshots**: Add visuals for UI features
- **Keep Updated**: Update docs with code changes
- **API Docs**: Document public APIs with KDoc

### KDoc Format

```kotlin
/**
 * Uploads a crop health image to the backend API.
 *
 * This function handles image compression, metadata extraction, and
 * multipart upload to the Laravel backend. It also includes retry
 * logic for failed uploads.
 *
 * @param imageUri URI of the image to upload
 * @param location Farm location where the image was taken
 * @param notes Optional notes about the crop condition
 * @return Result<UploadResponse> Success with upload data or failure with error
 * @throws NetworkException if network is unavailable
 * @throws ImageProcessingException if image cannot be processed
 *
 * @see CropHealthRepository.uploadImage
 * @since 1.0.0
 */
suspend fun uploadCropHealthImage(
    imageUri: Uri,
    location: String,
    notes: String? = null
): Result<UploadResponse>
```

---

## Development Workflow

### Branch Strategy

```
main
 ├── feature/disease-detection-improvement
 ├── feature/weather-integration
 ├── fix/camera-crash-android13
 └── docs/api-documentation
```

### Branch Naming

- `feature/description` - New features
- `fix/description` - Bug fixes
- `refactor/description` - Code refactoring
- `docs/description` - Documentation
- `test/description` - Test improvements

### Version Updates

When bumping version (maintainers only):

1. Update `versionCode` and `versionName` in `app/build.gradle.kts`
2. Update `CHANGELOG.md` with changes
3. Commit: `chore(release): bump version to 1.1.0`
4. Tag: `git tag -a v1.1.0 -m "Release v1.1.0"`
5. Push: `git push origin main --tags`
6. Create GitHub Release with APK

---

## Code Review Guidelines

### For Contributors

- **Be Open**: Accept feedback gracefully
- **Explain**: Justify design decisions
- **Iterate**: Make requested changes promptly
- **Learn**: Use reviews as learning opportunities

### For Reviewers

- **Be Kind**: Critique code, not people
- **Be Specific**: Give actionable feedback
- **Acknowledge**: Praise good work
- **Educate**: Explain reasoning behind suggestions

---

## Getting Help

### Resources

- **Documentation**: Check README and Wiki first
- **Discussions**: Ask questions in GitHub Discussions
- **Issues**: Search existing issues
- **Contact**: Reach out to maintainers

### Communication Channels

- **GitHub Issues**: Bug reports and feature requests
- **GitHub Discussions**: General questions and ideas
- **Email**: vincentjhon31@gmail.com (for sensitive matters)

---

## Recognition

Contributors will be recognized in:

- GitHub contributors page
- CHANGELOG.md for significant contributions
- README.md acknowledgments section

---

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to SUMVILTAD Connect! Together, we're helping Filipino farmers improve their harvests. 🌾
