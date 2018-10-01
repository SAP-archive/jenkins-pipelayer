# Corydas

pipeline job generation made easy

![logo](https://github.wdf.sap.corp/raw/devops-ci/corydoras/master/resources/logo-corydoras.png)

## Description

This Jenkins Library introduces new ways to import jobs programmatically into jenkins:

 - Pipeline Jobs generated from a folder containing declarative pipelines
 - Pipeline Jobs from configuration files and templates


## Requirements

Import the library in Jenkins. From __Manage__ page, go to __Configure System__
Under section __Global Pipeline Libraries__, add a new library with name
`corydoras-lib`, default version `master`, modern scm git https://github.wdf.sap.corp/devops-ci/corydoras.git


approve from page __In Process Script Approval__:

```groovy
method groovy.lang.GroovyObject invokeMethod java.lang.String java.lang.Object
```

## Pipeline step usage

To generate jobs from jobs or templates configuration files, use `generateJobs`.

Usage:

```groovy
    generateJobs {
        gitConfigJenkinsBranch = 'master'
        gitRemoteUrl = 'git-uri'
    }
```
- **gitConfigJenkinsBranch** [mandatory]: the branch on which the jobs are located

- **gitRemoteUrl** [mandatory]: the url to the repository is used to set the project url property of the job. This set Github link in jenkins menu to redirect to the repository

- **path**: path to your files (either jobs or templates configuration files) [defaults: 'config/*.properties' for templates otherwise 'jobs/**/*.groovy']

- **destination**: where to create the jobs. empty is at jenkins root, if not, will create a folder

- **useTemplate**: specifies wether you want to use templates. If so, it will process files in `config/*.properties` by default.

- **withContent**: specifies wether you want to use your files contents as groovy scripts for the jobs. If not set, groovy scripts will be fetched from GitHub when running the jobs. This parameter is useless if you use templates.

- **copySrc**: specifies wheter you want to copy your project's files to Jenkins. If yes, files will be copied to `$JENKINS_HOME/job_resources/{{destination}}`. The full path to this destination can be retrieved in templates with `{{sources.directory}}`.

- **localPath**: the process working directory. ie: your jobs are in a folder ./devops/config and you execute this step from within a ./devops/Jenkinsfile

## Approve scripts

You need to add step `approveScripts` to approve generated scripts when `useTemplate = true` parameter is used.
You could also do it manually from page *Manage Jenkins -> In-process Script Approval* 

`approveScripts` looks for all non approved scripts and methods and approve them.

Use it like this:

```
    stage('approve') {
        steps {
            approveScripts 'jenkins_user', this
        }
    }
```

In this example, `jenkins_user` is a jenkins credential id from <https://<your_jenkins>/credentials/>
The credential must be of type "username and password" and be a valid jenkins user.


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
Set property `jenkins.job.template` to the pipeline template you want to use.
Set a name for the job with property `jenkins.job.name`


example of property file `./config/my-dymmy-job.properties`:

```
hcp.host=int.sap.hana.ondemand.com
jenkins.job.name=dummy-job
jenkins.job.pipeline=templates/my-dummy-job.groovy
```

you can also set `jenkins.job.destination` to import the job with name `jenkins.job.name` to folder `jenkins.job.destination`

`jenkins.job.template` also supports ant pattern to get multiple files.
If there are multiple files, `jenkins.job.name` won't be used.
An other method to set a job name is to write a comment as follow in the pipeline file:
`//@jobName=my-generated-job`
you can also act on the display name of the job with:
`//@DisplayName=my cool name`

## How it is made possible

Corydoras uses JobDSL to create pipelines jobs from declarative pipelines

The JobDSK seed is located in `resources` folder.

To help you understand this project, [we made a sample](https://github.wdf.sap.corp/devops-ci/corydoras-sample/)

To understand how jobdsl works [have a look at the documentation](https://github.com/jenkinsci/job-dsl-plugin/wiki/Tutorial---Using-the-Jenkins-Job-DSL). You could also go see the [jobdsl api documentation](https://jenkinsci.github.io/job-dsl-plugin/)

## Known limitations

 - Pipeline keyword `jobDsl` is called, therefore you cannot cummulate multiple time the generation step within the same job
 - Don't execute these methods on a docker. A jenkins workspace must exist. This lib uses `writeFile` pipeline keyword

