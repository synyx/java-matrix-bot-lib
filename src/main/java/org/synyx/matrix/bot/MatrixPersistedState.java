package org.synyx.matrix.bot;

import java.util.Optional;

public interface MatrixPersistedState {

    Optional<String> getLastBatch();

    void setLastBatch(String value);
}
