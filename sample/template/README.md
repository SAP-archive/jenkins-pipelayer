# Sample - Job generation templated

The Jenkinsfile of this sample will go through every `*.properties` file in folder config.
`processTemplates` default path is `config/*.properties` when set to null but can be overriden with another path.

Variable `jenkins.job.template` indicates where to look for the template.
`helloWorld` variable overrides the variables in the template declared between brackets `{{helloWorld}}`

have a look to `USAGE.md` for more information
