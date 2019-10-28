# Contribute

## Setup development environment

You need the following requirements:

- Java SE Development Kit 8
- Jenkins (we tested the library against version LTS 2.190.1)
- Github (or any git server)


The [USAGE.md](https://github.com/SAP/jenkins-pipelayer/blob/master/USAGE.md) file describes how to install the library.

## Execute the tests locally

Execute lint with

```
gradlew codenarcMain
```

Execute unit tests with

```
gradlew test
```

## Contributor License Agreement

When you contribute code, documentation, or anything else, you have to be aware that your contribution is covered by the same Apache 2.0 License that is applied to jenkins-pipelayer itself.