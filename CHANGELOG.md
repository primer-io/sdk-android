## 2.23.0 (2024-01-30)

### Feat

- replace gson with org.json in HttpLoggerInterceptor (#502)
- HTTP request/response logging improvements (#483)

### Fix

- Obfuscate blacklisted headers when request is targeting PCI host (#507)
- Fixed wrong platform sent when using Web redirect payment methods (#504)
- mark fees.type as optional string (#505)

## 2.22.0 (2024-01-18)

### Feat

- implement form with redirect payment manager, bank list with web redirect support, update iDeal, DotPay drop-in to use new payment manager (#467)

## 2.21.0 (2023-12-15)

### Feat

- Support for 3DS SDK 1.3.0 version (#485)

## 2.20.0 (2023-12-11)

### Feat

- override PrimerError.context with ErrorContextParams where appropriate
- Removed Apaya integration (#477)

### Fix

- Improved handling of SDK analytics events (#481)
- adds "textNoSuggestions" to the input type of the card form input fields so that the keyboard cache will not be shown when the input field is selected

### Refactor

- ensure events are queued, refactored analytics data sender (#482)

## 2.19.2 (2023-11-14)

### Fix

- fixed empty phone number validation, fixed Nol Pay SDK being req… (#462)

## 2.19.1 (2023-11-07)

### Fix

- bumped Nol Pay version to 1.0.1 (#455)

## 2.19.0 (2023-11-03)

### Feat

- Introduces Nol Pay payment method and PrimerLogging functionalities (#446)

### Fix

- ensures AuthenticationDetails.kt and PrimerValidationError.kt are no longer obscured via proguard
- Fixed 3DS wrapper analytics field name (#447)

## 2.18.0 (2023-10-16)

### Feat

- removes koin third party dependency

## 2.17.3 (2023-09-07)

### Fix

- Fixed iPay88 UI events, updates to example app (#421)
- **PMT-1991**: Fixed 3DS not being triggered for GooglePay

## 2.16.2 (2023-01-20)

## 2.15.1 (2022-11-25)

## 2.13.0 (2022-10-20)

## 2.12.0 (2022-10-06)

## v2.0.0 (2022-06-09)

### Fix

- typo
- improve payment methods init error handling

## v1.0.0-beta.11 (2021-04-01)

## v1.0.0-beta.9 (2021-02-23)

## v1.0.0-beta.8 (2021-02-22)

## v1.0.0-beta.7 (2021-02-07)

## v1.0.0-beta.6 (2021-02-02)

## v1.0.0-beta.5 (2021-02-02)

## v1.0.0-beta.4 (2021-02-01)

## v1.0.0-beta.2 (2021-01-14)
