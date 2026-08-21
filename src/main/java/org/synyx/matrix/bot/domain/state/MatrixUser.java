package org.synyx.matrix.bot.domain.state;

import java.util.Optional;
import org.synyx.matrix.bot.domain.MatrixUserId;

public interface MatrixUser {

  /** The ID of the user that uniquely identifies it. */
  MatrixUserId getId();

  /** The of the user that uniquely identifies it. */
  Optional<String> getDisplayName();
}
