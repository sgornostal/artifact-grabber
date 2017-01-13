package org.sgornostal

import org.apache.commons.io.FileUtils
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.VersionRangeRequest

import static org.sgornostal.AetherUtil.*

/**
 * @author Slava Gornostal
 */
class ArtifactGrabber {

    static final String OPEN_RANGE = '[0,)'

    static void main(String[] args) {
        def cli = new CliBuilder(usage: 'java -jar artifact-grabber.jar [options] <coordinates>')
        cli.with {
            r longOpt: 'repository-url', args: 1, required: true, 'remote repo url'
            u longOpt: 'user', args: 1, required: false, 'remote repo username:password'
            o longOpt: 'output', args: 1, required: false, 'directory to download (default is current)'
            n longOpt: 'name', args: 1, required: false, 'new name of a downloaded jar file'
        }
        cli.setFooter('''
            You can specify one or more artifact coordinates of the form:
              <groupId>:<artifactId>[:<extension>[:<classifier>]][:<version>]
            The default repo.remote url is: https://repo1.maven.org/maven2/            
            ''')
        def options = cli.parse(args)
        if (options == null || !options.r) {
            cli.usage()
            System.exit(1)
        }
        def username = null
        def password = null
        if (options.u) {
            (username, password) = options.u.tokenize(':')
            if (!username || !password) {
                System.err.println "Invalid credentials"
                cli.usage()
                System.exit(1)
            }
        }
        if (!options.arguments()) {
            System.err.println "Please specify an artifact to download"
        }
        String artifactName = options.arguments().first()

        def remoteRepo = newRemoteRepository(options.r as String, username, password)
        def localRepo = new LocalRepository(System.getProperty('java.io.tmpdir') + '/.artifact-grabber')
        def system = newRepositorySystem()
        def session = newRepositorySystemSession(system, localRepo)
        def remotes = newRepositories(remoteRepo)

        if (artifactName.split(':').length == 2) {
            artifactName = "$artifactName:$OPEN_RANGE"
        }
        def artifact = new DefaultArtifact(artifactName)

        boolean releaseWanted = artifact.version.equalsIgnoreCase('RELEASE')
        if (releaseWanted) {
            artifact.setVersion(OPEN_RANGE)
        }

        def rangeRequest = new VersionRangeRequest()
        rangeRequest.setArtifact(artifact)
        rangeRequest.setRepositories(remotes)

        def rangeResult = system.resolveVersionRange(session, rangeRequest)
        def version
        if (releaseWanted) {
            version = rangeResult.getVersions()
                    .reverse()
                    .find { v -> !v.toString().endsWith('SNAPSHOT') }
        } else {
            version = rangeResult.getHighestVersion()
        }

        def artifactRequest = new ArtifactRequest()
        def download = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(),
                artifact.getClassifier(), artifact.getExtension(), version.toString())
        artifactRequest.setArtifact(download)
        artifactRequest.setRepositories(remotes)

        def artifactResult = system.resolveArtifact(session, artifactRequest)
        artifact = artifactResult.artifact
        def saveAs = new File((options.o ?: '') + (options.n ?: artifact.file.name))

        FileUtils.copyFile(artifactResult.artifact.file, saveAs)
        FileUtils.deleteDirectory(localRepo.basedir)

    }

}
