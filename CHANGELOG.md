# CHANGELOG

## 1.0.8
* All channels are active=false when exported and there is no need to deploy inactive listening channels right now so activate all listening channels

## 1.0.7
* Automatically update active listening channels after deploying for trading partner projects. This fixes an issue where the channel would be disabled on the 2nd deployment going forward and then require a restart of the soa_server after enabling in the GUI

## 1.0.6
* Clean the target/classes directory automatically to avoid cruft from previous run being deployed
* Consistent order on XML elements so that when B2B changes attribute order slightly, it will not show up as a change with source control diff tools
* Package goal creates a file w/ JAR extension rather than ZIP to be consistent with artifacts

## 1.0.5
* Final round of Windows locking fixes

## 1.0.4
* Skip renaming activity if trading partner ID is the same
* More Windows locking fixes

## 1.0.3
* First fixes for Windows file renaming

## 1.0.2
* Automatically adjust Weblogic max message size in the ANT task file

## 1.0.0
* Initial release