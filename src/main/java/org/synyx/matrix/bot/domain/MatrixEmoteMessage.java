package org.synyx.matrix.bot.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MatrixEmoteMessage implements MatrixMessage {

    private final MatrixEventId eventId;
    private final String body;
    private final MatrixUserId sender;

    public static Optional<MatrixEmoteMessage> from(
            MatrixEventId eventId,
            String body,
            MatrixUserId sender
    ) {

        if (eventId == null || body == null || sender == null) {
            return Optional.empty();
        }

        return Optional.of(new MatrixEmoteMessage(
                eventId,
                body,
                sender
        ));
    }

    @Override
    public MatrixEventId getEventId() {

        return eventId;
    }

    @Override
    public String getBody() {

        return body;
    }

    @Override
    public MatrixUserId getSender() {

        return sender;
    }

    @Override
    public MatrixMessageType getType() {

        return MatrixMessageType.EMOTE;
    }
}
