## 2.27.3 (2024-05-21)

### Fix

- Added support for RN styling and fixed PrimerTheme handling

## 2.27.2 (2024-05-06)

### Fix

- Added missing paypal checkout tokenization params - first and last name (#565)

## 2.27.1 (2024-04-22)

### Fix

- Fixed scoping of NolPay components by taking into account current Headless session (#556)

## 2.27.0 (2024-04-09)

### Feat

- **CVV-recapture**: Adds cvv recapture to drop in (#531)
- **paypal**: Added externalPayerId to tokenization request, exposed externalPayerId, first and last name in payment instrument (#538)
- **google-pay**: hide GooglePay from checkout when no card in the wallet (#529)

## 2.26.1 (2024-04-04)

### Fix

- enable authorization button after Klarna payment view is loaded (#535)
- **proguard**: Added rules to consumer-rules.pro to avoid issues in release mode (#534)
- Klarna drop-in stuck in loading state when SDK returns error (#533)
- **analytics**: Renamed networkCallType -> callType to align with other platforms (#532)
- fixed Nol Pay Vault capability and regression with payment completion (#530)

## 2.26.0 (2024-03-01)

### Feat

- Implement Klarna headless API
- Implement ability to select Klarna payment category in Klarna Drop-in
- Implement support for CHECKOUT payment intents for Klarna Drop-in
- Implement support for Klarna Extra Merchant Data

## 2.25.0 (2024-02-15)

### Feat

- Co-badged DX, updated example app, added card networks and asse… (#460)

### Fix

- Fixed crash in Headless Activity after process death (#517)

## 2.24.0 (2024-02-06)

### Feat

- bumped Primer 3DS to 1.4.0 (#515)
- enable google pay when amount in zero in session token

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
