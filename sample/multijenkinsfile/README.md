# Sample for Corydoras

This is the sample for[Corydoras](https://github.wdf.sap.corp/devops-ci/corydoras), a jenkins library

## Setup the library

Import to jenkins the library `https://github.wdf.sap.corp/devops-ci/corydoras.git`
Details are documented on [Requirement Section](https://github.wdf.sap.corp/devops-ci/corydoras#requirements) on corydoras repository

## How the sample works

This sample is divided in 3 branches

 - sample-jobs
 - sample-multijenkinfile
 - sample-template

## Use it

Create a new pipeline from jenkins blue ocean and select this repository.
This will create one job per branch.


job sample-jobs demos function [generateJobs](https://github.wdf.sap.corp/devops-ci/corydoras/blob/master/vars/generateJobs.groovy)

job sample-multijenkinfile demos function  [generateMultiPipeline](https://github.wdf.sap.corp/devops-ci/corydoras/blob/master/vars/generateMultiPipeline.groovy)

job sample-template demos function  [processTemplates](https://github.wdf.sap.corp/devops-ci/corydoras/blob/master/vars/processTemplates.groovy)

## Limitation

if you call one method after another, or the same method twice, the latest will be taken into account.
Exemple: if I call `generateMultiPipeline` before `generateJobs`, only jobs generated with `generateJobs` will remain