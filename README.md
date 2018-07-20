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
processTemplates 'config/*.properties', commit
```

To generate pipeline jobs from multiple files `Jenkinsfile` located in subfolders, run `generateMultiPipeline`

```groovy
script {
    commit = checkout scm
}
generateMultipipeline commit
```

## Template Engine

### Create a template

Templates are pipelines that contains variables in the form of `{{key}}`
This syntax is inspired by the logic less template engine [mustache](https://mustache.github.io), but does only sypport the variable replacement functionnality.

example of template:

```
pipeline {
    agent any
    parameters {
        booleanParam(name: 'HCP_HOST', defaultValue: "{{hcp.host}}")
    }
    stage('dummy') {
        steps {
            println params.HCP_HOST
        }
    }
}
```

### Use the template

Create or modify a property file in `config` folder.
Set property `jenkins.job.pipeline` to the pipeline template you want to use.
Set a name for the job with property `jenkins.job.name`


example of property file `./config/my-dymmy-job.properties`:

```
hcp.host=int.sap.hana.ondemand.com
jenkins.job.name=dummy-job
jenkins.job.pipeline=templates/my-dummy-job.groovy
```

you can also set `jenkins.job.destination` to import the job with name `jenkins.job.name` to folder `jenkins.job.destination`

## Multipipeline

Now you generate jobs, from Jenkinsfile in subfolders, with method `generateMultipipeline`, you can trigger only the job that got new changes with method `triggerOnChanges`

```
generateMultipipeline commit
triggerOnChanges this
```

## Known limitations

 - Pipeline keyword `jobDsl` is called, therefore you cannot cummulate multiple generation methods within the same job.
   Workaround if you call corydoras generation methods from within a Jenkins file is to condition depending on branches.
 - Don't execute these methods on a docker. A jenkins workspace must exist. This lib uses `writeFile` pipeline keyword

