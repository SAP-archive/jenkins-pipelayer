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

## How to Contribute

1. If you introduce a new functionnality that can be unit tested, add a [spock unit tests](http://spockframework.org/spock/docs/1.3/spock_primer.html#_expect_blocks).
1. Create a pull request
1. [Travis](https://travis-ci.com/SAP/jenkins-pipelayer/builds/) will approve your PR if build, [lint](https://github.com/SAP/jenkins-pipelayer/blob/master/codenarc.groovy) and tests pass
1. Wait for our code review and approval, possibly enhancing your change on request
1. Once the change has been approved and merged, we will inform you in a comment.
1. Celebrate!
