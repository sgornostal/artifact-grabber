# Maven 2 Artifact Grabber 

## What is it?

A standalone executable jar which uses Eclipse Aether libraries to download via HTTP(S) an artifact from a
remote Maven 2 format repository.

This is a modified version of a [sonatype's artifact-resolver tool](https://github.com/sonatype/artifact-resolver) app

## Build executable jar

```
./gradlew build
```

## How to use

```
java -jar artifact-grabber.jar 
    --repository-url "http://remote-repository.com/public/" 
    --user user:pass // optional if authentication not required
    --output // directory download to, optional (default is current)
    --name // artifact new name, optional
    com.mypackage:artifact
```
If version of the artifact is not specified then you will get latest version (including SNAPSHOT versions). 
To get latest non-SNAPSHOT version use pseudo version value 'RELEASE'. Example: ```com.mypackage:artifact:RELEASE ```

## Using args file

The script uses [Groovy CliBuilder](http://docs.groovy-lang.org/next/html/gapi/groovy/util/CliBuilder.html) to process arguments. 
You can create a file and put arguments, like auth, in that file instead.

Example:

1. Create a file named `script.args` in the same directory as the jar
2. The contents of the file can contain an argument on each line, like this:
     <pre>
     --repository-url "http://remote-repository.com/public/"
     --user user:pass   
     --name artifact.jar
     </pre>
3. Use the special `@` prefix and pass the file name as an argument to the script. Each line of the file will be read as if
it was passed on the command line. Example:
    <pre>
    java -jar artifact-grabber.jar @script.args com.mypackage:artifact
    </pre>

