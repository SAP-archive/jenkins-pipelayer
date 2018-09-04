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

To help you understand this project, [we made a sample](https://github.wdf.sap.corp/devops-ci/corydoras-sample/)

To understand how jobdsl works [have a look at the documentation](https://github.com/jenkinsci/job-dsl-plugin/wiki/Tutorial---Using-the-Jenkins-Job-DSL). You could also go see the [jobdsl api documentation](https://jenkinsci.github.io/job-dsl-plugin/)

## Requirements

Import the library in Jenkins. From __Manage__ page, go to __Configure System__
Under section __Global Pipeline Libraries__, add a new library with name
`corydoras-lib`, default version `master`, modern scm git https://github.wdf.sap.corp/devops-ci/corydoras.git


approve from page __In Process Script Approval__:

```groovy
method groovy.lang.GroovyObject invokeMethod java.lang.String java.lang.Object
```

## Usage

To generate pipeline jobs from folder `jobs` into jenkins folder `pipelines`, call function `generateJobs`.

```groovy
script {
    commit = checkout scm
}
generateJobs 'jobs/**/*.groovy', 'pipelines', commit
```

**Warning: this method is now deprecated: use `generateJobsMultiSources` instead.**


To generate pipeline jobs from templates configurable from property files located in folder `config`, run `processTemplates`

```groovy
script {
    commit = checkout scm
}
processTemplates 'config/*.properties', commit
```

**Warning: this method is now deprecated: use `generateJobsMultiSources` instead.**


To generate pipeline jobs from multiple files `Jenkinsfile` located in subfolders, run `generateMultiPipeline`

```groovy
script {
    commit = checkout scm
}
generateMultipipeline commit
```

To generate jobs from jobs or templates configuration files, use `generateJobsMultiSources`.

Usage:

```groovy
    generateJobsMultiSources path, destination, commit, additionalParameters
```

- **path**: path to your files (either jobs or templates configuration files)

- **destination**: where to create the jobs

- **commit**: commit info

- **additionalParameters**:
    - useTemplate: specifies wether you want to use templates. If so, it will process files in `config/*.properties` by default.
    - withContent: specifies wether you want to use your files contents as groovy scripts for the jobs. If not set, groovy scripts will be fetched from GitHub when running the jobs. This parameter is useless if you use templates.
    - copySrc: specifies wheter you want to copy your project's files to Jenkins. If yes, files will be copied to `$JENKINS_HOME/job_resources/{{destination}}`

```groovy
script {
    commit = checkout scm
}
generateJobsMultiSources 'jobs/**/*.groovy', 'pipelines', commit, additionalParameters: [ copySrc: true ]
```

**Note:** you will certainly have to approve the generated scripts in *Manage Jenkins -> In-process Script Approval*

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

