package org.sgornostal

import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.DefaultRepositorySystemSession
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.impl.DefaultServiceLocator
import org.eclipse.aether.repository.Authentication
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.util.repository.AuthenticationBuilder

class AetherUtil {

    static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system, LocalRepository localRepo) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession()
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo))
        session.setTransferListener(new ConsoleTransferListener())
        session.setRepositoryListener(new ConsoleRepositoryListener())
        session
    }

    static List<RemoteRepository> newRepositories(RemoteRepository remoteRepository) {
        new ArrayList<RemoteRepository>(Arrays.asList(remoteRepository != null ? remoteRepository : newCentralRepository()))
    }

    static RemoteRepository newCentralRepository() {
        new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build()
    }

    static RemoteRepository newRemoteRepository(String url, String user, String pass) {
        RemoteRepository.Builder builder = new RemoteRepository.Builder("custom", "default", url)
        if (user != null && pass != null) {
            Authentication auth = new AuthenticationBuilder().addUsername(user).addPassword(pass).build()
            builder.setAuthentication(auth)
        }
        return builder.build()
    }

    static RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator()
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class)
        locator.addService(TransporterFactory.class, FileTransporterFactory.class)
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class)
        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override
            void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                exception.printStackTrace()
            }
        })
        return locator.getService(RepositorySystem.class)
    }
}
