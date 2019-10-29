# Github Multi Branch Job Generator Sample

This is a sample for[jenkins-pipelayer](https://github.com/SAP/jenkins-pipelayer), a jenkins library

## Setup the library

Import to jenkins the library `https://github.com/SAP/jenkins-pipelayer.git`
Details are documented on [Requirement Section](https://github.com/SAP/jenkins-pipelayer/blob/master/USAGE.md#requirements) on jenkins-pipelayer repository

## How the sample works

The `generateMultiPipelineGithub` step will look into every subfolder of a repository to find `Jenkinsfile` and generate a github multibranch pipeline for every one of them.
