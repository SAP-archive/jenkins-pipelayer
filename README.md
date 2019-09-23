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

Have a look at `USAGE.md` for a description of the steps introduced by Jenkins Pipelayer.
We made samples to help you understand how to use the library. Please check out the `sample` folder

## Contribute

Your code must pass the linter. Write a unit test if you can. Please test your code
