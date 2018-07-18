# Corydas

## Description

pipeline job generation made easy 


## How it works

uses JobDSL to generate a pipeline with checkout from scm


## Requirements

Import the library in Jenkins. From __Manage__ page, go to __Configure System__
Under section __Global Pipeline Libraries__, add a new library with name
`corydoras-lib`, default version `master`, modern scm git https://github.wdf.sap.corp/devops-ci/corydoras.git


approve from page __In Process Script Approval__:

```groovy
method groovy.lang.GroovyObject invokeMethod java.lang.String java.lang.Object
```

## Usage

To generate pipeline jobs from folder `jobs` into jenkins folder `pipelines`, call function `generateJobs`

```groovy
commit = checkout scm
generateJobs 'jobs/**/*.groovy', 'pipelines', commit
```

To generate pipeline jobs from templates configurable from property files located in folder `config`, run `processTemplates`

```groovy
commit = checkout scm
generateJobs 'config/*.properties', commit
```

To generate pipeline jobs from multiple files `Jenkinsfile` located in subfolders, run `generateMultiPipeline`

```groovy
commit = checkout scm
generateMultipipeline commit
```

