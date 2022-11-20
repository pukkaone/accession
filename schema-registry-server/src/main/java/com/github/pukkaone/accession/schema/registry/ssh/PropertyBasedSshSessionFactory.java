package com.github.pukkaone.accession.schema.registry.ssh;

import com.github.pukkaone.accession.schema.registry.config.SchemaRegistryProperties;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.keyprovider.KeyIdentityProvider;
import org.apache.sshd.common.session.SessionContext;
import org.eclipse.jgit.internal.transport.ssh.OpenSshConfigFile;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshConfigStore;
import org.eclipse.jgit.transport.sshd.ServerKeyDatabase;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;

/**
 * Configures SSH from application properties.
 */
@RequiredArgsConstructor
public class PropertyBasedSshSessionFactory extends SshdSessionFactory {

  private final SchemaRegistryProperties properties;

  @Override
  protected File getSshConfig(File dir) {
    // Do not use a config file.
    return null;
  }

  @Override
  protected SshConfigStore createSshConfigStore(
      File homeDir,
      File configFile,
      String localUserName
  ) {
    return new SshConfigStore() {
      @Override
      public HostConfig lookup(String hostName, int port, String userName) {
        return new OpenSshConfigFile.HostEntry();
      }

      @Override
      public HostConfig lookupDefault(String hostName, int port, String userName) {
        return lookup(hostName, port, userName);
      }
    };
  }

  @Override
  protected ServerKeyDatabase getServerKeyDatabase(File homeDir, File sshDir) {
    return new ServerKeyDatabase() {
      private PublicKey getServerPublicKey() {
        try {
          return AuthorizedKeyEntry.parseAuthorizedKeyEntry(
                  properties.getHostKeyAlgorithm() + " " + properties.getHostKey())
              .resolvePublicKey(null, null);
        } catch (IOException | GeneralSecurityException e) {
          throw new IllegalStateException("Cannot get server public key", e);
        }
      }

      @Override
      public List<PublicKey> lookup(
          String connectAddress, InetSocketAddress remoteAddress, Configuration config
      ) {
        return List.of(getServerPublicKey());
      }

      @Override
      public boolean accept(
          String connectAddress,
          InetSocketAddress remoteAddress,
          PublicKey serverKey,
          Configuration config,
          CredentialsProvider provider
      ) {
        if (config.getStrictHostKeyChecking() == Configuration.StrictHostKeyChecking.ACCEPT_ANY) {
          return true;
        }

        List<PublicKey> knownServerKeys = lookup(connectAddress, remoteAddress, config);
        return KeyUtils.findMatchingKey(serverKey, knownServerKeys) != null;
      }
    };
  }

  @Override
  protected Iterable<KeyPair> getDefaultKeys(File sshDir) {
    return new SingleKeyIdentityProvider(properties);
  }

  @RequiredArgsConstructor
  private static class SingleKeyIdentityProvider implements KeyIdentityProvider, Iterable<KeyPair> {
    private final SchemaRegistryProperties properties;

    @Override
    public Iterator<KeyPair> iterator() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<KeyPair> loadKeys(SessionContext session)
        throws IOException, GeneralSecurityException {
      return KeyPairUtils.load(session, properties.getPrivateKey());
    }
  }
}
