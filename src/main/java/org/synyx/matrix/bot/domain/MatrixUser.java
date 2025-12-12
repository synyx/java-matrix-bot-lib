package org.synyx.matrix.bot.domain;

import java.util.Optional;

public class MatrixUser {

  private final MatrixUserId id;
  private String displayName;

  private MatrixUser(MatrixUserId id) {

    this.id = id;
    this.displayName = null;
  }

  public static Optional<MatrixUser> from(MatrixUserId id) {

    if (id == null) {
      return Optional.empty();
    }

    return Optional.of(new MatrixUser(id));
  }

  public MatrixUserId getId() {

    return id;
  }

  public Optional<String> getDisplayName() {

    return Optional.ofNullable(displayName);
  }

  public void setDisplayName(String displayName) {

    this.displayName = displayName;
  }
}
