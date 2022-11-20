package com.github.pukkaone.accession.schema.registry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileSystemUtils;

/**
 * Prepares a Git repository to act as the remote repository the schema registry server will clone.
 */
@TestConfiguration
public class GitTestConfiguration {

  static {
    try {
      prepareRemoteGitRepository();
    } catch (GitAPIException | IOException e) {
      throw new IllegalStateException("Cannot prepare Git remote repository", e);
    }
  }

  private static void prepareRemoteGitRepository() throws GitAPIException, IOException {
    Path workTree = Files.createTempDirectory("remote");
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          FileSystemUtils.deleteRecursively(workTree);
        } catch (IOException e) {
          // Do nothing.
        }
      }
    });
    System.setProperty("schema.registry.server.git.uri", workTree.toString());

    var repository = new FileRepositoryBuilder()
        .setWorkTree(workTree.toFile())
        .build();
    repository.create();

    Path subject1Dir = workTree.resolve("1234567_topic1-value");
    Files.createDirectory(subject1Dir);
    Files.copy(new ClassPathResource("001.avsc").getInputStream(), subject1Dir.resolve("001.avsc"));
    Files.copy(new ClassPathResource("002.avsc").getInputStream(), subject1Dir.resolve("002.avsc"));
    Path subject2Dir = workTree.resolve("2345678_topic2-value");
    Files.createDirectory(subject2Dir);
    Files.copy(new ClassPathResource("001.avsc").getInputStream(), subject2Dir.resolve("001.avsc"));
    Files.copy(new ClassPathResource("002.avsc").getInputStream(), subject2Dir.resolve("002.avsc"));

    Git git = new Git(repository);
    git.add()
        .addFilepattern(".")
        .call();
    git.commit()
        .setMessage("First commit")
        .call();
    git.branchRename()
        .setNewName("main")
        .call();
  }
}
