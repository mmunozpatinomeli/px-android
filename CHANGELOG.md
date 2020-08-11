## VERSION 4.54.0
_05_08_2020_
* FEATURE - Added flowName configuration from preference
* ENHANCEMENT - Configure tracking params in checkout builder
* ENHANCEMENT - Added payment method information in result view track
* ENHANCEMENT - Changed deploy script

## VERSION 4.53.1
_03_08_2020_
* FIX - Added keep rules for renderer's contructor in R8 fullmode

## VERSION 4.53.0
_30_07_2020_
* ENHANCEMENT - Deprecated old groups' api
* FIX - Exploding button listeners with destroy activity
* FIX - Exploding button position with destroy activity
* FIX - pay button not hidden when exploding with destroy activity

## VERSION 4.52.0
_23_07_2020_
* ENHANCEMENT - Better discount info.
* FIX - Crash tracking fix by concurrency.
* FIX - Friction track fixed to accept a nullable card object.
* FIX - Fix bad parcelable in payment processor mapper.

## VERSION 4.51.1
_14_07_2020_
* FIX - Re-added session check due to integrator's bad implementation of SplitPaymentProcessor
* FIX - Re-added session check in fragment due to integrator's bad implementation of SplitPaymentProcessor
* ENHANCEMENT - Improved friction tracking to differentiate new frictions

## VERSION 4.50.4
_13_07_2020_
* FIX - Re-added session check in fragment due to integrator's bad implementation of SplitPaymentProcessor

## VERSION 4.50.3
_13_07_2020_
* FIX - Re-added session check due to integrator's bad implementation of SplitPaymentProcessor

## VERSION 4.51.0
_08_07_2020_
* FEATURE - Added tracking behaviour to customize tracking from application
* FIX - fixed CongratsDeepLink track path

## VERSION 4.50.2
_01_07_2020_
* FIX - No token in remedies after guessing card flow

## VERSION 4.50.1
_01_07_2020_
* FIX - Last four digits ends with null
* FIX - Prevent back when pay button is exploding
* FIX - Cloning token when no token is present (remedies)
* FIX - GSON Concurrent modification because bad cache handling

## VERSION 4.50.0
_25_06_2020_
* FEATURE - Money split view in congrats
* ENHANCEMENT - Remedies tracking changes

## VERSION 4.48.6
_24_06_2020_
* FIX - Avoid to ask for cvv preventively on groups flow

## VERSION 4.48.5
_24_06_2020_
* FIX - Avoid clear token for old card association flow

## VERSION 4.48.4
_24_06_2020_
* FIX - Clear token on prepare payment

## VERSION 4.48.3
_24_06_2020_
* FIX - DisplayInfo within PaymentMethod was not parcelized

## VERSION 4.49.1
_18_06_2020_
* FIX - Moved module extensions to avoid clashing

## VERSION 4.49.0
_18_06_2020_
* FEATURE - Added tracking behaviour new signature.
* ENHANCEMENT - Removed session initialization check

## VERSION 4.48.2
_17_06_2020_
* FIX - Crash silverbullet remedy

## VERSION 4.47.1
_12_06_2020_
* ENHANCEMENT - Update CFTFormatter for CFTNA

## VERSION 4.48.1
_12_06_2020_
* ENHANCEMENT - Update CFTFormatter for CFTNA

## VERSION 4.46.4
_12_06_2020_
* ENHANCEMENT - Update CFTFormatter for CFTNA

## VERSION 4.48.0
_11_06_2020_
* FEATURE - experiments with installments row
* ENHANCEMENT - persist payment configuration
* FIX - top fragment position in default congrats order

## VERSION 4.47.0
_04_06_2020_
* FEATURE - Send security type of payment as header of payment wrapper api call
* ENHANCEMENT - Alternative order in congrats
* ENHANCEMENT - Added amount to security validation data
* ENHANCEMENT - Change account money logo
* ENHANCEMENT - Take security required parameter from biometrics lib callback

## VERSION 4.46.3
_03_06_2020_
* FIX - Crash on review and confirm

## VERSION 4.45.3
_03_06_2020_
* FIX - Crash on review and confirm

## VERSION 4.46.2
_29_05_2020_
* FIX - Flow id lost on session init

## VERSION 4.45.2
_29_05_2020_
* FIX - Flow id lost on session init

## VERSION 4.46.1
_28_05_2020_
* FIX - Payment method malformed in payment data

## VERSION 4.45.1
_28_05_2020_
* FIX - Payment method malformed in payment data

## VERSION 4.46.0
_28_05_2020_
* FEATURE - Accessibility support on congrats screen
* ENHANCEMENT - Removed execute network requests to avoid bad usage
* ENHANCEMENT - Accept language interceptor
* FIX - Exploding fragment button alignment
* FIX - Crash trying to remove fragments immediately

## VERSION 4.45.0
_21_05_2020_
* FEATURE - TouchPointView in congrats.
* FEATURE - Security Type flag on payment processor
* ENHANCEMENT - Pay button fragment on review and confirm screen
* FIX - Back button in one tap covered with logo

## VERSION 4.44.0
_14_05_2020_
* FEATURE - Support for Caixa virtual payment method
* ENHANCEMENT - Pay button fragment refactor
* ENHANCEMENT - Remedy tracking data from backend
* FIX - Double init crashes with lazy init
* FIX - Business congrats not showing after remedy

## VERSION 4.43.0
_07_05_2020_
* ENHANCEMENT - Flow id header
* ENHANCEMENT - Fake locale behaviour on sample app
* FIX - Avoid summary refresh with empty charges
* FIX - Empty view on review and confirm

## VERSION 4.42.0
_30_04_2020_
* FEATURE - New suggested payment method remedy
* ENHANCEMENT - Tracking session expired friction

## VERSION 4.41.0
_23_04_2020_
* ENHANCEMENT - Added meli deeplink to one tap

## VERSION 4.40.1
_17_04_2020_
* FIX - Disable pay button in pure offline method node

## VERSION 4.39.2
_17_04_2020_
* FIX - Disable pay button in pure offline method node

## VERSION 4.40.0
_16_04_2020_
* FEATURE - Remote resources for card assets
* FEATURE - Support for IFPE regulation
* ENHANCEMENT - Support for Picasso v2.7+
* FIX - Lowres was not applying correctly
* FIX - Crash tinting status bar after paying

## VERSION 4.39.1
_08_04_2020_
* FIX - Sec code length null after invalid esc

## VERSION 4.39.0
_02_04_2020_
* FEATURE - Accessibility functions.
* FEATURE - High risk remedy.
* FIX - Crash in offline methods fragment.
* FIX - Title color in offline methods fragment.

## VERSION 4.38.2
_30_03_2020_
* FIX - Pay button crash when session isn't initialized
* FIX - Prevent crash when font configurer isn't properly initialized
* FIX - Prevent Linkable text crash
* FIX - Last four digits were null after the new cvv remedy

## VERSION 4.38.1
_21_03_2020_
* FIX - Fixed crash on business congrats
* FIX - Fixed congrats layout on instructions and body error
* FIX - Fixed pay button disappearing in one tap when returning from card form

## VERSION 4.38.0
_19_03_2020_
* FEATURE - New security code remedy
* ENHANCEMENT - Generic payment button
* FIX - NPE in card form
* FIX - Sending session id on card form

## VERSION 4.37.0
_28_02_2020_
* ENHANCEMENT - Reset esc cap when user enter security code
* ENHANCEMENT - Exit with custom error code when session is irrecoverable

## VERSION 4.36.0
_28_02_2020_
* ENHANCEMENT - Adapt new signature of ESC Manager in PX Addons
* ENHANCEMENT - Use new signature to improve delete ESC Tracking

## VERSION 4.35.4
_27_02_2020_
* ENHANCEMENT - Removed card vault dependency in one tap.
* ENHANCEMENT - Summary layout changes.
* ENHANCEMENT - ESC blacklisted payment status comes from backend.
* ENHANCEMENT - Better CVV reason tracking.
* FIX - Card form animation with split payment.

## VERSION 4.35.3
_20_02_2020_
* FIX - Restored deleted colors used by Money In

## VERSION 4.35.2
_20_02_2020_
* ENHANCEMENT - Removed button to change payer information
* FIX - Exploding color for business payments
* FIX - Low res for offline payment methods in one tap

## VERSION 4.35.0
_13_02_2020_
* FEATURE - Credits custom terms by installment.
* FEATURE - Ignoring esc flag. Now always enabled.
* ENHANCEMENT - Business components tracker.
* ENHANCEMENT - Conversion tracker.
* FIX - View not attached call propagation.

## VERSION 4.34.0
_06_02_2020_
* FEATURE - Off Methods in One Tap
* FEATURE - Esc validation before payment
* ENHANCEMENT - Check business payment decorator on payment rewards

## VERSION 4.33.1
_29_01_2020_
* FIX - Added header for terms and conditions
* FIX - Reduce margins of MLBusinessDiscountBoxView in congrats.

## VERSION 4.33.0
_24_01_2020_
* FEATURE - Added local behaviour
* ENHANCEMENT - Summary detail animation on card form return
* FIX - Sold out discount message alignment

## VERSION 4.32.0
_16_01_2020_
* FEATURE - New card form

## VERSION 4.31.0
_09_01_2020_
* ENHANCEMENT - Added tracking behaviour to addons

## VERSION 4.30.0
_09_01_2020_
* FEATURE - Added babel for translations
* FEATURE - Redesign rejected and pending congrats
* FEATURE - Backend driven discount unavailability information

## VERSION 4.29.4
_02_01_2020_
* ENHANCEMENT - Changed debit payment methods assets
* ENHANCEMENT - Compressed images
* FIX - Validate view attached on api callback
* FIX - Disable pay button when validating user biometrics (Review and Confirm)

## VERSION 4.29.3
_19_12_2019_
* FIX - Disable pay button when validating user biometrics to prevent double payments if validation is delayed

## VERSION 4.29.2
_12_12_2019_
* ENHANCEMENT - Removed backend driven texts
* ENHANCEMENT - Improved logic for redraw disabled payment methods
* FIX - Send locale toLanguageTag for Accept-Language in api call headers
* FIX - Show one tap payment method highlight if current payer cost selected has benefits

## VERSION 4.29.1
_06_12_2019_
* FIX - benefit message building for max installments 1 flows

## VERSION 4.29.0
_05_12_2019_
* FEATURE - Benefits one tap flow
* ENHANCEMENT - Upgraded to Android min API level 19
* ENHANCEMENT - Upgraded to Meli card drawer 2

## VERSION 4.28.0
_28_11_2019_
* FEATURE - Improved one tap flow
* ENHANCEMENT - Added Order and Merchant Order Id into checkout preference
* ENHANCEMENT - Added flow name to payment rewards call
* ENHANCEMENT - Upgraded to meli ui 8
* FIX - Fixed some congrats font sizes and styles

## VERSION 4.27.1
_14_11_2019_
* ENHANCEMENT - Added upward compatibility with picasso 2.7
* FIX - Parsing string to long with parse long
* FIX - Added Serializable to new models
* FIX - Remove old fragments on new intent

## VERSION 4.27.0
_07_11_2019_
* FEATURE - New endpoint "checkout" integration in replace of "payment_method"
* FEATURE - Tracking experiments data
* FIX - Refactored card token handling in session to avoid payments with cvv null

## VERSION 4.26.1
_05_11_2019_
* FIX - Validate presenter state in fragment's onDetach event

## VERSION 4.25.3
_06_11_2019_
* FIX - Validate presenter state in fragment's onDetach event

## VERSION 4.26.0
_31_10_2019_
* IMPROVEMENT - Removed try-catches from activities/fragments creation

## VERSION 4.25.2
_22_10_2019_
* FIX - Rotation bug in one tap using postCreate
* FIX - Card association flow
* FIX - Removed non mandatory allowBackup from manifest

## VERSION 4.25.1
_17_10_2019_
* FIX - Restored deleted colors used by Money In

## VERSION 4.25.0
_17_10_2019_
* ENHANCEMENT - Dynamic status bar color
* ENHANCEMENT - Refactor json util
* ENHANCEMENT - Improved timeout handling
* FIX - Prevent crashes related to session

## VERSION 4.24.0
_10_10_2019_
* ENHANCEMENT - Refactored fonts usage
* ENHANCEMENT - refactored utils
* FIX - Fixed margins in congrats components

## VERSION 4.23.1
_02_10_2019_
* FEATURE - Added receiptIdList in BusinessPayment
* ENHANCEMENT - Using safe intent for cross selling actions
* ENHANCEMENT - Fixed inconsistent empty tracking values

## VERSION 4.23.0
_02_10_2019_
* FEATURE - Added rules sets to validate when to ask for biometrics or not
* ENHANCEMENT - Added screen density interceptor to add headers in api calls
* FIX - Added single click listener to prevent fast double clicks in views

## VERSION 4.22.0
_26_09_2019_
* FEATURE - New congrats design
* FEATURE - Business components in congrats
* FIX - Friction events
* FIX - Animation on detached view

## VERSION 4.21.0
_04_09_2019_
* FEATURE - Added secure payments behaviour
* FEATURE - Added important fragment in business payment
* ENHANCEMENT - Enable TLS 1.2 in HttpClient
* ENHANCEMENT - Added collectorId to CheckoutPreference
* FIX - Security code validation
* FIX - Strict mode warning

## VERSION 4.20.0
_29_08_2019_
* FEATURE - Added tracking of session time and checkout type.
* FEATURE - Added card drawer on express payment.
* FIX - CPF validation.

## VERSION 4.19.0
_22_08_2019_
* FEATURE - Added tracking of payment method selected index and available methods quantity.
* FEATURE - Unified product id setters.
* FIX - Consumer credits crash on low res.
* FIX - Payment method discount on one tap.
* ENHANCEMENT - Gradle task for local builds.
* ENHANCEMENT - Security Code screen refactor.

## VERSION 4.18.0
_01_08_2019_
* FIX - Prevent double click on Confirm Payment.
* FIX - Changed launch mode to singleTop to prevent double initialization.
* ENHANCEMENT - Migrated deploying to Bitrise.
* ENHANCEMENT - Updated Card Association ETEs.

## VERSION 4.17.1
_24_07_2019_
* FIX - Cancel exploding button loading on cvv comeback after payment recover
* FIX - Prevent double lazy initialization of PX.

## VERSION 4.16.4
_24_07_2019_
* FIX - Cancel exploding button loading on cvv comeback after payment recover
* FIX - Prevent double lazy initialization of PX.

## VERSION 4.17.0
_18_07_2019_
* FEATURE - Changed preference mail validator to required if no private key is setted
* ENHANCEMENT - Removed payer from init track for security reasons

## VERSION 4.16.3
_16_07_2019_
* FIX - Prevent library double initialization

## VERSION 4.15.2
_15_07_2019_
* FIX - Prevent library double initialization

## VERSION 4.16.2
_11_07_2019_
* FIX - Default marketplace to NONE

## VERSION 4.16.1
_10_07_2019_
* FIX - Handling context in http client interceptor
* FIX - Validating flow in onRestore validations in guessing card activity

## VERSION 4.16.0
_01_07_2019_
* Added support for max installments
* Custom product id header
* Default Payment Processor for black labeled payments
* Added support for consumer credits payment method

## VERSION 4.15.1
_19_06_2019_
* FIX - Incorrect amount on congrats when discount and one installment

## VERSION 4.14.3
_19_06_2019_
* FIX - Incorrect amount on congrats when discount and one installment

## VERSION 4.15.0
_14_06_2019_
* FEATURE - Added gateway mode support

## VERSION 4.14.2
_14_06_2019_
* ENHANCEMENT - Now express presenter saves the current payment method index

## VERSION 4.14.1
_13_06_2019_
* FIX - Added back deprecated method

## VERSION 4.14.0
_11_06_2019_
* FEATURE - Custom pay button text support
* FEATURE - BackHandler interface for visual Payment Processor
* ENHANCEMENT - Better error handling in Express Payment
* ENHANCEMENT - CheckoutActivity MVP refactor
* ENHANCEMENT - Constants refactor
* FIX - Congrats layout

## VERSION 4.12.6
_11_06_2019_
* Fix invalid tracks again
* Fix circle reveal animation crash on Android 6

## VERSION 4.13.0
_03_06_2019_
* FEATURE - Express Payment Subtitle
* FEATURE - Express Payment Charges
* FEATURE - Modified Processing Mode
* FEATURE - New agreement params for Payer Cost
* ENHANCEMENT - Dynamic Low res in Express Payment
* ENHANCEMENT - Context for Session using a Content Provider
* ENHANCEMENT - Added view tags for ETEs
* ENHANCEMENT - IPaymentDescriptor default methods
* ENHANCEMENT - Tracking cards ESC
* ENHANCEMENT - Payer Information ETEs refactor
* ENHANCEMENT - Migration to API 28
* FIX - Payer Information restore instance

## VERSION 4.12.5
_23_05_2019_
* Fix invalid tracks

## VERSION 4.12.4
_20_05_2019_
* Fix automatic selection when the only payment method isn't card
* Changed item validation
* Fix serialization of payer lastName and firstName
* Fix amount descriptor view crop

## VERSION 4.12.3
_13_05_2019_
* FIX - Business result crash when session isn't initialized correctly

## VERSION 4.12.2
_08_05_2019_
* FIX - Removed empty box and divider in rejected views
* FIX - Catch NPE in network status check

## VERSION 4.12.1
_07_05_2019_
* FIX - Populate card properties with custom options in one tap

## VERSION 4.12.0
_03_05_2019_
* FEATURE - Esc for guessing card
* FEATURE - Using discount name for his description
* ENHANCEMENT - Track friction event when invalid esc
* ENHANCEMENT - Add reason to cvv tracker
* FIX - Returning to one tap after esc recover
* FIX - Orientation issues

## VERSION 4.11.0
_26_04_2019_
* FEATURE - Congrats tracking new attributes
* FIX - Crash payment processor activity

## VERSION 4.10.3
_25_04_2019_
* FIX - Recover removed method avoiding breaking changes.

## VERSION 4.10.2
_25_04_2019_
* FIX - Creation of session id for tracking purpose

## VERSION 4.10.1
_22_04_2019_
* ENHANCEMENT - Added amount on call for auth rejection message
* FIX - User wants to split selection persist
* FIX - Correct amount on congrats
* FIX - Reset payment method slider position on payment method changed after rejection
* FIX - Show result using visual payment processor

## VERSION 4.10.0
_11_04_2019_
* FEATURE - Disable last selected payment method after reject and recover
* FEATURE - Express Payment's support for Single Player
* ENHANCEMENT - Updated Citibanamex logo
* FIX - Blank screen in installments

## VERSION 4.9.3
_29_03_2019_
* FIX - Friction rate in id card

## VERSION 4.9.2
_28_03_2019_
* FIX - Automatic selection flow
* FIX - Correct amount for split payment on Congrats
* FIX - Crash on Circle Transform for certain images

## VERSION 4.9.1
_25_03_2019_
* FIX - Retry card storage

## VERSION 4.9.0
_21_03_2019_
* FEATURE - Issuer images in Cards
* FEATURE - CNPJ payment with Boleto
* FEATURE - Skip congrats in storage card flow

## VERSION 4.8.1
_18_03_2019_
* FIX - Crash on guessing when retrying payment

## VERSION 4.8.0
_12_03_2019_
* FEATURE - Account money discounts
* FEATURE - CPF validation in card guessing
* ENHANCEMENT - Soldout discount communication
* ENHANCEMENT - Changes in Loyalty flow
* ENHANCEMENT - Much lower assets weight
* FIX - Money In UI fixes

## VERSION 4.7.3
_11_03_2019_
* FIX - disable back button on exploding animation

## VERSION 4.7.2
_27_02_2019_
* FIX - payment processor bundle mapping fix for vending
* FIX - animations split payment
* FIX - invalid state cardvault

## VERSION 4.7.1
_27_02_2019_
* FIX - signature card storage
* FIX - identification only cpf for brazil
* FIX - event data review and confirm

## VERSION 4.7.0
_22_02_2019_
* FEATURE - Split Payment.
* FEATURE - CPF Validation.
* ENHANCEMENT - Added abort and action events for congrats / business.
* FIX - Terms and conditions event data.
* FIX - Animations in PaymentVaultActivity.
* FIX - Loading identification types NPE.
* FIX - Add new method drives to groups if cards isn't present.

## VERSION 4.6.2
_12_02_2019_
* FIX - Added credit card date validation.  
* ENHANCEMENT - Added tracks. 
* ENHANCEMENT - Added discount terms and conditions.
* FIX - Rollback public method.
* FIX - Connectivity manager.
* FIX - Activity new flag support - Android 9.

## VERSION 4.6.1
_04_02_2019_
* FIX - Added correct discount id in PaymentData.
* FIX - Attached view when exploding animation finished.

## VERSION 4.5.2
_31_01_2019_
* FIX - NPE tracking events.

## VERSION 4.6.0
_24_01_2019_
* FEATURE - Support to payment method discount.

## VERSION 4.5.1
_10_01_2019_
* FIX - Crash in groups disk cache.

## VERSION 4.5.0
_02_01_2019_
* FEATURE - Account money as a first class member.
* FEATURE - Added event and view data for PXTrackingListener class.
* FIX - Crash on back on Sec code saved card.

## VERSION 4.4.1
_18_12_2018_

* FIX - Navigation on payment method changed
* FIX - Crash on back from payment vault

## VERSION 4.4.0
_13_12_2018_

* FIX - Installments list clip in groups flow
* FIX - Invalid tracks
* ENHANCEMENT - Check for additional info for payer
* ENHANCEMENT - Better deploy scripts

## VERSION 4.3.3
_4_12_2018_

* FIX - Crash in Checkout, PaymentResult and BusinessResult on application kill

## VERSION 4.3.2
_30_11_2018_

* FIX - Crash on Card Association congrats

## VERSION 4.3.1
_21_11_2018_

* FIX - Installments selection in express flow
* FIX - Account money invested in express flow
* FIX - PEC and payer information assets
* FIX - Instructions padding

## VERSION 4.3.0
_31_10_2018_

 * FEATURE - Express checkout.
 * FEATURE - Skip Payer Information.
 * FEATURE - Pec Payment Method.
 * FEATURE - Enabled MLU (Site Uruguay)
 * FEATURE - Interactive instructions in congrats.
 * FEATURE - Configurable titles for Payment Vault Screen.
 * FEATURE - added new tracking listener PXTrackingListener for MeliData compatibility
 ```java
  void setListener(@NonNull final PXTrackingListener listener,
              @NonNull final Map<String, ? extends Object> flowDetail, 
              @Nullable final String flowName)
```

## VERSION 4.2.1
_30_10_2018_

* FIX - Crash NPE when processing payment in background
* FIX - Crash on recovery payment

## VERSION 4.2.0
_25_10_2018_

* FEATURE - dynamic custom dialogs for certain locations.
* FEATURE - dynamic custom views for review and confirm.
* ENHANCEMENT - tracking screen's names and paths unified.
* ENHANCEMENT/FIX - card addition flow now supports installments and ESC.

## VERSION 4.1.3
_01_11_2018_

* FIX - Crash NPE on exploding button

## VERSION 4.1.2
_31_10_2018_

* FIX - Crash NPE when processing payment in background
* FIX - Crash on recovery payment

## VERSION 4.1.0
_04_10_2018_

* FIX - Show app bar when tap back from CVV screen.
* FIX - Tracking of PaymentMethodSearchItem.
* ENHANCEMENT - Standalone Card Association

## VERSION 4.0.6
_08_10_2018_

* FIX - Double congrats one tap.

## VERSION 4.0.5

* FIX - Show app bar when tap back from CVV screen.
* FIX - Tracking of PaymentMethodSearchItem.
* FIX - Payment processor - visual attach bug

## VERSION 4.0.4
_25_09_2018_

* FIX - DefaultPaymentTypeId debit card error, can't look for settings.
* FIX - Colombia currency utils.

## VERSION 4.0.3

_20_09_2018_

* FIX - Payment recovery call for auth.
* FIX - destroy activity behaviour.
* FIX - NPE no decimals for Site Colombia.
* ENHANCEMENT - Color customization detailed documentation.
* ENHANCEMENT - Loading improvements for visual payments (payment processor).

## VERSION 4.0.2

_05_09_2018_

* Fix: one tap with payment recovery
* Fix: payment processor background support
* Fix: added internal payment method change behaviour


## VERSION 4.0.1

_03_09_2018_

* Fix: dynamic id declaration
* Fix: code discount
* Fix: esc with one tap

## VERSION 4.0.0

_30_08_2018_
