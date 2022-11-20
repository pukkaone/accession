package com.github.pukkaone.accession.schema.registry.ssh;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.Collection;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader;
import org.apache.sshd.common.session.SessionContext;
import org.apache.sshd.common.util.io.resource.AbstractIoResource;
import org.apache.sshd.common.util.security.SecurityUtils;

/**
 * Converts string to SSH private key.
 */
final class KeyPairUtils {

  private static final KeyPairResourceLoader LOADER = SecurityUtils.getKeyPairResourceParser();

  private KeyPairUtils() {
    throw new UnsupportedOperationException("Should not create instances of this class");
  }

  /**
   * Converts string to SSH private key.
   *
   * @param session
   *     session context
   * @param privateKey
   *     string-formatted private key
   * @return key pair
   */
  static Collection<KeyPair> load(SessionContext session, String privateKey)
      throws IOException, GeneralSecurityException {

    return LOADER.loadKeyPairs(session, new StringResource(privateKey), FilePasswordProvider.EMPTY);
  }

  private static class StringResource extends AbstractIoResource<String> {
    protected StringResource(String resourceValue) {
      super(String.class, resourceValue);
    }

    @Override
    public InputStream openInputStream() {
      return new ByteArrayInputStream(this.getResourceValue().getBytes());
    }
  }
}
