# Corydas

pipeline job generation made easy 

## Description

This Jenkins Library introduces new ways to import jobs programmatically into jenkins:

 - Pipeline Jobs generated from a folder containing declarative pipelines
 - Pipeline Jobs from configuration files and templates
 - Multibranch Pipeline Jobs from sub folders containing Jenkinsfile


## How it works

Uses JobDSL to generate pipelines with checkout from scm.
The seeds are in `resources` folder.


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
script {
    commit = checkout scm
}
generateJobs 'jobs/**/*.groovy', 'pipelines', commit
```

To generate pipeline jobs from templates configurable from property files located in folder `config`, run `processTemplates`

```groovy
script {
    commit = checkout scm
}
generateJobs 'config/*.properties', commit
```

To generate pipeline jobs from multiple files `Jenkinsfile` located in subfolders, run `generateMultiPipeline`

```groovy
script {
    commit = checkout scm
}
generateMultipipeline commit
```

## Known limitation

 - Pipeline keyword `jobDsl` is called, therefore you cannot cummulate multiple generation methods within the same job.
   Workaround if you call corydoras generation methods from within a Jenkins file is to condition depending on branches.
 - Don't execute these methods on a docker. A jenkins workspace must exist. This lib uses `writeFile` pipeline keyword

