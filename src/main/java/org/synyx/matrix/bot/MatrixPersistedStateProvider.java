package org.synyx.matrix.bot;

import java.util.Optional;

public interface MatrixPersistedStateProvider {

  Optional<String> getLastBatch();

  void setLastBatch(String value);
}
