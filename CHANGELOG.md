<a name="unreleased"></a>
## [Unreleased]


<a name="22.0.0"></a>
## [22.0.0] - 2023-05-18
### Bug Fixes
- revised reference to removed layout element
- renamed invalid preference key
- added fix for selecting radiobutton for investment preference
- fixed ivalid export declaration
- added proper context for getting string resource
- updated email validation logic

### Code Refactoring
- removed the image tint color
- revised layout of first two view in wizard
- renamed fragment to match view name
- refactore layout container for checkboxes
- revised field size fragment
- removed bckground color in root layout
- added translations for tz and rwanda
- renamed text for investment preference fragment
- renamed prefernece Never to rarely
- renamed RiskAttFragment to InvestmentPrefFragment
- switched famr name summary
- revised stepper adapter
- revised english instruction text for location selection
- revised field location heading title
- optimized conditions for string checks in order to assign boolean flags
- re-ordered sumary page items
- updated string translations
- removed all instances of fontfamily declaration
- revised font styling and padding
- removed extraneous padding for text views and layout
- updated wording
- revised country selection picker
- revised data validation and default selection
- changed wording to revised version

### Continuous integration changes
- disabled pr createor for beta releases

### Features
- redesigned layout for tillage operations
- added option to skipp investment preference
- added skip logic for areaunit fragment in the step wizard
- added data deletion condition for profile info and mandatory infor tables
- revised areunit fragment
- revised planting date fragment layout
- refactore logic for investment preference
- added new translations
- revised ui layout for investment preferences fragment
- added info on farm name
- added verification of country location
- revised country location verification and validation
- redesigned fam location screen
- added reverse geocode logic
- updated location evaluation
- updated country validation
- revised texts and layouts
- updated summary text processing
- updated string translations

### BREAKING CHANGE



investment profile refactoring




<a name="21.5.0"></a>
## [21.5.0] - 2023-04-12
### Code Refactoring
- re-enabled steppers
- rewoded english question for farm location
- rearranged wizard screens for riskatt
- switched to using TextUtils.empty()
- enhanced functions

### Continuous integration changes
- change concurrency id for bunp and tag
- added bump and tag build step

### Docs
- updated documentation

### Features
- set new map zoom factor
- changed mapbox style
- added deletAll() method to all dao objects


<a name="21.4.4"></a>
## [21.4.4] - 2023-04-04
### Continuous integration changes
- set LATEST_TAG file to be non secret

### Features
- added new ui revisions


<a name="21.4.3"></a>
## [21.4.3] - 2022-10-04

<a name="21.4.2"></a>
## [21.4.2] - 2022-08-03
### Code Refactoring
- improved webview loading

### Features
- updated remote config logic


<a name="21.4.1"></a>
## [21.4.1] - 2022-08-03
### Bug Fixes
- updated in app terms link
- removed invalid line in function
- corrected state where variables values were swapped for summary view


<a name="21.4.0"></a>
## [21.4.0] - 2022-07-29
### Bug Fixes
- placeholder order correction

### Code Refactoring
- added translations to kiswahili
- removed debug context code

### Features
- ui revision
- added new layout


<a name="21.3.1-beta"></a>
## [21.3.1-beta] - 2022-04-07

<a name="21.3.1"></a>
## [21.3.1] - 2022-04-07
### Bug Fixes
- invalid min and maximum price


<a name="21.3.0"></a>
## [21.3.0] - 2022-04-05
### Features
- added burundi


<a name="21.2.4"></a>
## [21.2.4] - 2022-02-02
### Bug Fixes
- fixed seleted price
- fixed intercrop fertilizer prices


<a name="21.2.3"></a>
## [21.2.3] - 2022-02-01
### Bug Fixes
- updated endpoint


<a name="21.2.2"></a>
## [21.2.2] - 2021-11-25
### Bug Fixes
- added proper evaluation for step skipping for views
- added proper conversion for save valud for max investment

### Continuous integration changes
- updated concurrency group to use github ref


<a name="21.2.1"></a>
## [21.2.1] - 2021-11-25
### Bug Fixes
- added proper evaluation for step skipping for views
- added proper conversion for save valud for max investment

### Continuous integration changes
- updated concurrency group to use github ref


<a name="21.2.0"></a>
## [21.2.0] - 2021-11-24
### Bug Fixes
- added current practices skipping if country is ghana

### Continuous integration changes
- disabled mock building
- updated consurrency step section
- concurrency testing
- concurrency step
- updated stuff
- added concurrency group
- added concurrency bits
- fixed invalid closing line in line 16
- adde branch evaluation
- testing some more
- testing
- updated test steps
- added branch name testing


<a name="21.1.0-beta"></a>
## [21.1.0-beta] - 2021-11-23
### Bug Fixes
- disbaled rwanda data points
- corrected shared pref ref key for GHS rate
- fixed invalid index range for radio button tags
- added dynamic fertilizerpriecs

### Code Refactoring
- corrected sort order or records

### Continuous integration changes
- github actions
- revised on pull request conditions
- added on pull request action for non main branches
- updated repo
- resticted actions to be for specific branches

### Features
- added dynamic investment amount
- added dev endpoint to test new payload fetching
- added step skipper

### BREAKING CHANGE




<a name="21.1.0"></a>
## [21.1.0] - 2021-11-23
### Bug Fixes
- added evaluation for ARE area units
- wrong area unit checked
- area unit conversion

### Code Refactoring
- added checker for starch factory count
- corrected annotations and removed extra slashes in trnslations
- reenabled debugging views bypass
- revised kotlin version string

### Continuous integration changes
- updated build step java version
- added beta release channels
- added release status in google uploader
- todo actions
- added workflow
- added env variable

### Docs
- updated whats new release notes
- updated release notes

### Features
- added kinyarwanda translations
- added checks for country to determine use case and area units


<a name="21.0.2"></a>
## [21.0.2] - 2021-11-18

<a name="21.0.1"></a>
## [21.0.1] - 2021-11-18
### Bug Fixes
- added custom dialog fragment to prevent crashes on inflation

### Code Refactoring
- updated in app database name


<a name="21.0.0"></a>
## [21.0.0] - 2021-11-09
### Features
- ghana support


<a name="20.0.0"></a>
## [20.0.0] - 2021-10-11
### Code Refactoring
- removed bpkp folder
- package renaming

### Continuous integration changes
- disabled goog play publisher step

### Features
- harmonized packages

### BREAKING CHANGE

T


<a name="19.1.0"></a>
## [19.1.0] - 2021-09-15
### Bug Fixes
- crashing maps

### Code Refactoring
- removed extraneous semicolon
- git hooks

### Continuous integration changes
- updated env values

### Docs
- updated release notes

### Features
- Rwanda country selection
- translation

### BREAKING CHANGE




<a name="19.0.0"></a>
## [19.0.0] - 2021-06-22
### Bug Fixes
- translations
- updated repo names
- merge conflict

### Code Refactoring
- updated build steps in android.yml actions file

### Continuous integration changes
- updated java version

### Docs
- README update


<a name="18.19.1"></a>
## [18.19.1] - 2021-05-20
### Bug Fixes
- fixed null locate issue casuing interfaces to crash

### Continuous integration changes
- removed bundlerelease step in unit test step
- reenable github actions by renaming

### Docs
- updated changelog


<a name="18.19.0"></a>
## [18.19.0] - 2021-05-17
### Bug Fixes
- fixed ui tranlation strings for kiswahili
- added proper translation of swahili string and positioning
- added proper case switching fo swahili words

### Continuous integration changes
- enhanced build step to exclude tests

### Docs
- added release notes for swahili version
- updated changelog file

### BREAKING CHANGE




<a name="18.5.2"></a>
## [18.5.2] - 2021-05-13
### Bug Fixes
- added proper room database annotation
- fertilizer list clenup logic
- added proper planting windows check
- currency object processing

### Code Refactoring
- downgraded kotlin versions
- enabled correct entry activity

### Continuous integration changes
- disble Pr draft flag in pr automater
- added title to beta release workflow
- added auto pr action to github CI
- disabled on pull request build

### Docs
- updated changelog file
- updated changelog
- updated whats new notes

### Features
- added ui to handle harvest window prices for month 1&2
- dependencies updates

### BREAKING CHANGE






<a name="18.5.1"></a>
## [18.5.1] - 2021-04-27
### Continuous integration changes
- added step for beta branch build

### Docs
- added changelog genration


<a name="18.5.0"></a>
## [18.5.0] - 2021-04-27
### Continuous integration changes
- removed git quality check steps
- updated actions flow and branch with develop changes
- build condition step revision
- addee cz quality checker
- removed extra comments


<a name="18.4.0"></a>
## [18.4.0] - 2021-04-08

<a name="18.3.2"></a>
## [18.3.2] - 2021-04-07

<a name="18.3.1"></a>
## [18.3.1] - 2021-03-03

<a name="18.3.0"></a>
## [18.3.0] - 2021-03-02

<a name="18.2.0"></a>
## [18.2.0] - 2021-02-24

<a name="18.1.2"></a>
## [18.1.2] - 2021-02-24

<a name="18.1.1"></a>
## [18.1.1] - 2021-02-17

<a name="18.1.0"></a>
## [18.1.0] - 2021-02-11

<a name="18.0.2"></a>
## [18.0.2] - 2021-02-03

<a name="18.0.1"></a>
## [18.0.1] - 2021-02-02

<a name="18.0.0"></a>
## [18.0.0] - 2021-02-02

<a name="17.3.0"></a>
## [17.3.0] - 2020-11-05

<a name="17.2.1"></a>
## [17.2.1] - 2020-10-15

<a name="17.2.0"></a>
## [17.2.0] - 2020-10-14

<a name="17.2.0-beta+6"></a>
## [17.2.0-beta+6] - 2020-10-14

<a name="17.2.0beta5"></a>
## [17.2.0beta5] - 2020-10-13

<a name="17.1.0"></a>
## [17.1.0] - 2020-10-06

<a name="17.1.beta2020280"></a>
## [17.1.beta2020280] - 2020-10-06

<a name="17.0.3"></a>
## [17.0.3] - 2020-07-29

<a name="17.0.2"></a>
## [17.0.2] - 2020-07-28

<a name="17.0.1"></a>
## [17.0.1] - 2020-07-19

<a name="17.0.beta2020201"></a>
## [17.0.beta2020201] - 2020-07-19

<a name="16.3.beta2020201"></a>
## [16.3.beta2020201] - 2020-07-19

<a name="17.0.0"></a>
## [17.0.0] - 2020-07-19

<a name="16.3.beta26"></a>
## [16.3.beta26] - 2020-07-15

<a name="16.3.beta3"></a>
## [16.3.beta3] - 2020-07-15

<a name="16.3.4"></a>
## [16.3.4] - 2020-07-08

<a name="16.3.beta2"></a>
## [16.3.beta2] - 2020-07-08

<a name="16.3.3"></a>
## [16.3.3] - 2020-07-07

<a name="16.3.2"></a>
## [16.3.2] - 2020-07-07

<a name="16.1.0-rc-20"></a>
## [16.1.0-rc-20] - 2020-07-02

<a name="16.1.0"></a>
## [16.1.0] - 2020-06-30

<a name="16.0.1-rc-15"></a>
## [16.0.1-rc-15] - 2020-06-30

<a name="16.0.1"></a>
## [16.0.1] - 2020-06-27

<a name="16.0.0"></a>
## [16.0.0] - 2020-06-26

<a name="15.0.0"></a>
## [15.0.0] - 2020-06-22

<a name="14.0.0"></a>
## [14.0.0] - 2020-05-04

<a name="13.3.0"></a>
## [13.3.0] - 2020-04-27

<a name="13.2.0"></a>
## [13.2.0] - 2020-04-26

<a name="13.1.1"></a>
## [13.1.1] - 2020-04-26

<a name="13.1.0"></a>
## [13.1.0] - 2020-04-26

<a name="13.0.0"></a>
## [13.0.0] - 2020-04-26

<a name="v11.0.4"></a>
## [v11.0.4] - 2020-04-18

<a name="12.0.1"></a>
## [12.0.1] - 2020-04-18

<a name="12.0.0"></a>
## [12.0.0] - 2020-04-18

<a name="v11.0.2"></a>
## [v11.0.2] - 2020-04-14

<a name="11.0.0"></a>
## [11.0.0] - 2020-04-14

<a name="v10.0.1"></a>
## [v10.0.1] - 2020-04-02

<a name="10.0.0"></a>
## [10.0.0] - 2020-04-02

<a name="v9.3.1"></a>
## [v9.3.1] - 2020-04-01

<a name="9.10.1"></a>
## [9.10.1] - 2020-04-01

<a name="v9.3.18"></a>
## [v9.3.18] - 2020-04-01

<a name="9.10.0"></a>
## [9.10.0] - 2020-04-01

<a name="v9.3.17"></a>
## [v9.3.17] - 2020-03-31

<a name="v9.3.16"></a>
## [v9.3.16] - 2020-03-30

<a name="v9.3.15"></a>
## [v9.3.15] - 2020-03-28

<a name="v9.3.14"></a>
## [v9.3.14] - 2020-03-24

<a name="v9.3.13"></a>
## [v9.3.13] - 2020-03-18

<a name="v.6"></a>
## [v.6] - 2020-03-18

<a name="v8.2.67.21.beta"></a>
## [v8.2.67.21.beta] - 2020-03-13

<a name="v8.2.67.20.beta"></a>
## [v8.2.67.20.beta] - 2020-03-13

<a name="v8.2.67.7.beta"></a>
## [v8.2.67.7.beta] - 2020-03-13

<a name="v8.2.7"></a>
## [v8.2.7] - 2020-03-13

<a name="v8.2.6.beta"></a>
## [v8.2.6.beta] - 2020-03-13

<a name="v8.2.6"></a>
## [v8.2.6] - 2020-03-12

<a name="v8.0.1"></a>
## [v8.0.1] - 2020-03-09

<a name="v8.0.0"></a>
## [v8.0.0] - 2020-03-08

<a name="v4.2.13"></a>
## [v4.2.13] - 2020-01-22

<a name="v4.2.12"></a>
## [v4.2.12] - 2019-12-03

<a name="v4.2.11"></a>
## [v4.2.11] - 2019-12-03

<a name="v4.2.10"></a>
## [v4.2.10] - 2019-11-20

<a name="v4.2.8b"></a>
## [v4.2.8b] - 2019-11-12

<a name="v4.2.7"></a>
## [v4.2.7] - 2019-11-01

<a name="v4.2.6-hotfix"></a>
## [v4.2.6-hotfix] - 2019-11-01

<a name="v4.2.6"></a>
## [v4.2.6] - 2019-11-01

<a name="v4.2.5-hotfix"></a>
## [v4.2.5-hotfix] - 2019-10-30

<a name="v4.2.5"></a>
## [v4.2.5] - 2019-10-29

<a name="v4.2.4-hotfix1"></a>
## [v4.2.4-hotfix1] - 2019-10-28

<a name="v4.2.4"></a>
## [v4.2.4] - 2019-10-28

<a name="v4.2.3"></a>
## [v4.2.3] - 2019-10-24

<a name="v4.2.2"></a>
## [v4.2.2] - 2019-10-24

<a name="v4.1.3"></a>
## [v4.1.3] - 2019-10-15

<a name="v4.1.2"></a>
## v4.1.2 - 2019-10-15

[Unreleased]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/22.0.0...HEAD
[22.0.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.5.0...22.0.0
[21.5.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.4.4...21.5.0
[21.4.4]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.4.3...21.4.4
[21.4.3]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.4.2...21.4.3
[21.4.2]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.4.1...21.4.2
[21.4.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.4.0...21.4.1
[21.4.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.3.1-beta...21.4.0
[21.3.1-beta]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.3.1...21.3.1-beta
[21.3.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.3.0...21.3.1
[21.3.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.2.4...21.3.0
[21.2.4]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.2.3...21.2.4
[21.2.3]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.2.2...21.2.3
[21.2.2]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.2.1...21.2.2
[21.2.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.2.0...21.2.1
[21.2.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.1.0-beta...21.2.0
[21.1.0-beta]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.1.0...21.1.0-beta
[21.1.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.0.2...21.1.0
[21.0.2]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.0.1...21.0.2
[21.0.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/21.0.0...21.0.1
[21.0.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/20.0.0...21.0.0
[20.0.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/19.1.0...20.0.0
[19.1.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/19.0.0...19.1.0
[19.0.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/18.19.1...19.0.0
[18.19.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/18.19.0...18.19.1
[18.19.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/18.5.2...18.19.0
[18.5.2]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/18.5.1...18.5.2
[18.5.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/18.5.0...18.5.1
[18.5.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/18.4.0...18.5.0
[18.4.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/18.3.2...18.4.0
[18.3.2]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/18.3.1...18.3.2
[18.3.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/18.3.0...18.3.1
[18.3.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/18.2.0...18.3.0
[18.2.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/18.1.2...18.2.0
[18.1.2]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/18.1.1...18.1.2
[18.1.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/18.1.0...18.1.1
[18.1.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/18.0.2...18.1.0
[18.0.2]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/18.0.1...18.0.2
[18.0.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/18.0.0...18.0.1
[18.0.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/17.3.0...18.0.0
[17.3.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/17.2.1...17.3.0
[17.2.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/17.2.0...17.2.1
[17.2.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/17.2.0-beta+6...17.2.0
[17.2.0-beta+6]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/17.2.0beta5...17.2.0-beta+6
[17.2.0beta5]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/17.1.0...17.2.0beta5
[17.1.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/17.1.beta2020280...17.1.0
[17.1.beta2020280]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/17.0.3...17.1.beta2020280
[17.0.3]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/17.0.2...17.0.3
[17.0.2]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/17.0.1...17.0.2
[17.0.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/17.0.beta2020201...17.0.1
[17.0.beta2020201]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/16.3.beta2020201...17.0.beta2020201
[16.3.beta2020201]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/17.0.0...16.3.beta2020201
[17.0.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/16.3.beta26...17.0.0
[16.3.beta26]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/16.3.beta3...16.3.beta26
[16.3.beta3]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/16.3.4...16.3.beta3
[16.3.4]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/16.3.beta2...16.3.4
[16.3.beta2]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/16.3.3...16.3.beta2
[16.3.3]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/16.3.2...16.3.3
[16.3.2]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/16.1.0-rc-20...16.3.2
[16.1.0-rc-20]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/16.1.0...16.1.0-rc-20
[16.1.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/16.0.1-rc-15...16.1.0
[16.0.1-rc-15]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/16.0.1...16.0.1-rc-15
[16.0.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/16.0.0...16.0.1
[16.0.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/15.0.0...16.0.0
[15.0.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/14.0.0...15.0.0
[14.0.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/13.3.0...14.0.0
[13.3.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/13.2.0...13.3.0
[13.2.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/13.1.1...13.2.0
[13.1.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/13.1.0...13.1.1
[13.1.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/13.0.0...13.1.0
[13.0.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v11.0.4...13.0.0
[v11.0.4]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/12.0.1...v11.0.4
[12.0.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/12.0.0...12.0.1
[12.0.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v11.0.2...12.0.0
[v11.0.2]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/11.0.0...v11.0.2
[11.0.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v10.0.1...11.0.0
[v10.0.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/10.0.0...v10.0.1
[10.0.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v9.3.1...10.0.0
[v9.3.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/9.10.1...v9.3.1
[9.10.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v9.3.18...9.10.1
[v9.3.18]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/9.10.0...v9.3.18
[9.10.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v9.3.17...9.10.0
[v9.3.17]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v9.3.16...v9.3.17
[v9.3.16]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v9.3.15...v9.3.16
[v9.3.15]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v9.3.14...v9.3.15
[v9.3.14]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v9.3.13...v9.3.14
[v9.3.13]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v.6...v9.3.13
[v.6]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v8.2.67.21.beta...v.6
[v8.2.67.21.beta]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v8.2.67.20.beta...v8.2.67.21.beta
[v8.2.67.20.beta]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v8.2.67.7.beta...v8.2.67.20.beta
[v8.2.67.7.beta]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v8.2.7...v8.2.67.7.beta
[v8.2.7]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v8.2.6.beta...v8.2.7
[v8.2.6.beta]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v8.2.6...v8.2.6.beta
[v8.2.6]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v8.0.1...v8.2.6
[v8.0.1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v8.0.0...v8.0.1
[v8.0.0]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v4.2.13...v8.0.0
[v4.2.13]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v4.2.12...v4.2.13
[v4.2.12]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v4.2.11...v4.2.12
[v4.2.11]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v4.2.10...v4.2.11
[v4.2.10]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v4.2.8b...v4.2.10
[v4.2.8b]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v4.2.7...v4.2.8b
[v4.2.7]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v4.2.6-hotfix...v4.2.7
[v4.2.6-hotfix]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v4.2.6...v4.2.6-hotfix
[v4.2.6]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v4.2.5-hotfix...v4.2.6
[v4.2.5-hotfix]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v4.2.5...v4.2.5-hotfix
[v4.2.5]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v4.2.4-hotfix1...v4.2.5
[v4.2.4-hotfix1]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v4.2.4...v4.2.4-hotfix1
[v4.2.4]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v4.2.3...v4.2.4
[v4.2.3]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v4.2.2...v4.2.3
[v4.2.2]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v4.1.3...v4.2.2
[v4.1.3]: https://github.com/IITA-AKILIMO/akilimo-mobile/compare/v4.1.2...v4.1.3
