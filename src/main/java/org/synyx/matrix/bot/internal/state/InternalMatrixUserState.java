package org.synyx.matrix.bot.internal.state;

import java.util.Optional;
import org.synyx.matrix.bot.domain.MatrixUserId;
import org.synyx.matrix.bot.domain.state.MatrixUser;

public class InternalMatrixUserState implements MatrixUser {

  private final MatrixUserId id;
  private String displayName;

  private InternalMatrixUserState(MatrixUserId id) {

    this.id = id;
    this.displayName = null;
  }

  public static Optional<InternalMatrixUserState> from(MatrixUserId id) {

    if (id == null) {
      return Optional.empty();
    }

    return Optional.of(new InternalMatrixUserState(id));
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
