package com.github.pukkaone.accession.schema.registry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.util.FileSystemUtils;

/**
 * Prepares a Git repository to act as the remote repository the schema registry plugin will clone.
 */
public class StubGitRepository implements BeforeAllCallback {

  private Git git;
  private Path workTree;

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    workTree = Files.createTempDirectory("remote");
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

    var repository = new FileRepositoryBuilder()
        .setWorkTree(workTree.toFile())
        .build();
    repository.create();

    Files.write(workTree.resolve("README.adoc"), new byte[0]);

    git = new Git(repository);
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

  public Git getGit() {
    return git;
  }

  public Path getWorkTree() {
    return workTree;
  }
}
