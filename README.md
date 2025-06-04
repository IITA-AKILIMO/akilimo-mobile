# AKILIMO Mobile

[![Android CI](https://github.com/IITA-AKILIMO/akilimo-mobile/actions/workflows/android.yml/badge.svg)](https://github.com/IITA-AKILIMO/akilimo-mobile/actions/workflows/android.yml)
[![Quality gate](https://sonar.munywele.co.ke/api/project_badges/quality_gate?project=IITA-AKILIMO_akilimo-mobile_abcb50d1-1abd-4e32-bd76-b385f65cfc5d&token=sqb_31a18546176db4735c7afc45a9561b931d046803)](https://sonar.munywele.co.ke/dashboard?id=IITA-AKILIMO_akilimo-mobile_abcb50d1-1abd-4e32-bd76-b385f65cfc5d)
[![Security Issues](https://sonar.munywele.co.ke/api/project_badges/measure?project=IITA-AKILIMO_akilimo-mobile_abcb50d1-1abd-4e32-bd76-b385f65cfc5d&metric=software_quality_security_issues&token=sqb_31a18546176db4735c7afc45a9561b931d046803)](https://sonar.munywele.co.ke/dashboard?id=IITA-AKILIMO_akilimo-mobile_abcb50d1-1abd-4e32-bd76-b385f65cfc5d)
[![Maintainability Rating](https://sonar.munywele.co.ke/api/project_badges/measure?project=IITA-AKILIMO_akilimo-mobile_abcb50d1-1abd-4e32-bd76-b385f65cfc5d&metric=software_quality_maintainability_rating&token=sqb_31a18546176db4735c7afc45a9561b931d046803)](https://sonar.munywele.co.ke/dashboard?id=IITA-AKILIMO_akilimo-mobile_abcb50d1-1abd-4e32-bd76-b385f65cfc5d)

## Overview

AKILIMO Mobile is an Android-based decision support tool developed by the International Institute of
Tropical Agriculture (IITA) under the African Cassava Agronomy Initiative (ACAI). 

The app provides site-specific agronomic advice tailored to farmers in sub-Saharan Africa, enabling them to optimize fertilizer
use, improve yields, and make informed farming decisions for crops including cassava, maize, and sweet potato.

## Features

- **Customized Fertilizer Recommendations**: Offers tailored advice on fertilizer application based
  on user inputs such as location, yield targets, and input prices.

- **Intercropping Support**: Provides recommendations for intercropping systems (cassava-maize).

- **Planting Practices**: Guidance on optimal planting times and methods.

- **Multi-Channel Delivery**: Provides recommendations directly within the app, via SMS, or through
  email.

- **Location-Based Advice**: Uses GPS and mapping technology to provide site-specific recommendations.

- **Multi-Country Support**: Available for farmers in Tanzania, Rwanda, Ghana, and Burundi.

- **Multilingual Interface**: Supports English, Kiswahili, and Kinyarwanda.

- **User-Friendly Interface**: Designed with simplicity in mind to cater to farmers with varying
  levels of digital literacy.

## Supported Countries

- Tanzania
- Rwanda
- Ghana
- Burundi

## Installation

To build and run the AKILIMO Mobile app locally:

1. Clone the repository:

   ```bash
   git clone https://github.com/IITA-AKILIMO/akilimo-mobile.git
   cd akilimo-mobile
   ```

2. Open the project in Android Studio.

3. Configure your local.properties file with required API keys:
   - Mapbox API key
   - Firebase configuration

4. Build the project:

   ```bash
   ./gradlew build
   ```

5. Run the app on an emulator or physical device:

   ```bash
   ./gradlew installDebug
   ```

## Technology Stack

- **Language**: Kotlin
- **Minimum SDK**: 19 (Android 4.4 KitKat)
- **Target SDK**: 34 (Android 14)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room Persistence Library
- **Networking**: Retrofit, OkHttp
- **JSON Parsing**: Jackson
- **Mapping**: Mapbox
- **UI Components**: Material Design, RecyclerView, CardView
- **Error Tracking**: Sentry
- **Analytics**: Firebase Analytics
- **Push Notifications**: Firebase Cloud Messaging
- **Remote Config**: Firebase Remote Config
- **CI/CD**: GitHub Actions

## Development

### Prerequisites

- Android Studio Arctic Fox (2021.3.1) or newer
- JDK 17
- Android SDK 34

### Code Quality

This project uses several tools to maintain code quality:

- **SonarQube**: For code quality analysis
- **Detekt**: For Kotlin static code analysis
- **JaCoCo**: For code coverage reporting

### Testing

Run the tests with:

```bash
./gradlew test
```

For UI tests:

```bash
./gradlew connectedAndroidTest
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the terms specified in the [LICENSE.md](LICENSE.md) file.

## Contact

International Institute of Tropical Agriculture (IITA) - [https://www.iita.org/](https://www.iita.org/)

Project Link: [https://github.com/IITA-AKILIMO/akilimo-mobile](https://github.com/IITA-AKILIMO/akilimo-mobile)
