# Jenkins Pipelayer

Pipeline job generation made easy

## Description

This Jenkins Library introduces :

- a new way to import jobs programmatically into jenkins.
  Basically it is a parser that converts Declarative Pipelines to JobDSL Declarative Pipelines
  This embed a template engine to generage multiple jobs from one Jenkinsfile template (or more) parametarized by property files
- a clever way to reduce drastically the numbers of calls made by github by your pipelines
  this library automates the process of storing and updating a shared library on jenkins filesystem

## Use cases

- Reduce the number of jobs to maintain
- develop pipelines without the need configure jobs on jenkins
- Handle multiple microservices and their jenkinsfile from one repo. Meta repository is handy when the project you work on has too many microservices and was not designed with deployment in mind.

## Usage

Have a look at [USAGE.md](https://github.com/SAP/jenkins-pipelayer/blob/master/USAGE.md) for a description of the steps introduced by Jenkins Pipelayer.
We made samples to help you understand how to use the library. Please check out the [sample](https://github.com/SAP/jenkins-pipelayer/tree/master/sample) folder

## Contribute

Please check our [Contribution Guidelines](https://github.com/SAP/jenkins-pipelayer/blob/master/CONTRIBUTING.md).

## License

Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved. This file is licensed under the Apache Software License, Version 2.0 except as noted otherwise in the [LICENSE](https://github.com/SAP/jenkins-pipelayer/blob/master/LICENSE) file.
