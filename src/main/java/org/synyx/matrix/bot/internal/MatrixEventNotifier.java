package org.synyx.matrix.bot.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.synyx.matrix.bot.MatrixEventConsumer;
import org.synyx.matrix.bot.MatrixState;
import org.synyx.matrix.bot.domain.MatrixEmoteMessage;
import org.synyx.matrix.bot.domain.MatrixEventId;
import org.synyx.matrix.bot.domain.MatrixMessage;
import org.synyx.matrix.bot.domain.MatrixMessageType;
import org.synyx.matrix.bot.domain.MatrixNoticeMessage;
import org.synyx.matrix.bot.domain.MatrixRoom;
import org.synyx.matrix.bot.domain.MatrixRoomId;
import org.synyx.matrix.bot.domain.MatrixRoomInvite;
import org.synyx.matrix.bot.domain.MatrixTextMessage;
import org.synyx.matrix.bot.domain.MatrixUserId;
import org.synyx.matrix.bot.internal.api.dto.ClientEventDto;
import org.synyx.matrix.bot.internal.api.dto.StrippedStateEventDto;
import org.synyx.matrix.bot.internal.api.dto.SyncResponseDto;
import org.synyx.matrix.bot.internal.api.dto.event.MemberEventContentDto;
import org.synyx.matrix.bot.internal.api.dto.event.MessageEventContentDto;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class MatrixEventNotifier {

    private final ObjectMapper objectMapper;
    @Getter
    private final MatrixEventConsumer consumer;

    public static Optional<MatrixEventNotifier> from(ObjectMapper objectMapper, MatrixEventConsumer consumer) {

        if (consumer == null) {
            return Optional.empty();
        }

        return Optional.of(new MatrixEventNotifier(objectMapper, consumer));
    }

    public void notifyFromSynchronizationResponse(MatrixState state, SyncResponseDto syncResponse) {

        final var maybeRooms = Optional.ofNullable(syncResponse.rooms());
        final var invitedRooms = maybeRooms
                .flatMap(syncRoomsDto -> Optional.ofNullable(syncRoomsDto.invite()))
                .orElseGet(HashMap::new);

        for (var entry : invitedRooms.entrySet()) {

            final var roomId = MatrixRoomId.from(entry.getKey())
                    .orElseThrow(IllegalStateException::new);
            final var maybeRoom = state.getInvitedRooms().stream()
                    .filter(invitedRoom -> invitedRoom.getId().equals(roomId))
                    .findAny();

            if (maybeRoom.isEmpty()) {
                continue;
            }

            final var room = maybeRoom.get();
            Optional.ofNullable(entry.getValue())
                    .flatMap(roomDto -> Optional.ofNullable(roomDto.inviteState()))
                    .flatMap(inviteStateDto -> Optional.ofNullable(inviteStateDto.events()))
                    .orElseGet(List::of)
                    .forEach(eventDto -> notifyAboutInviteEvent(state, room, eventDto));
        }

        final var joinedRooms = maybeRooms
                .flatMap(syncRoomsDto -> Optional.ofNullable(syncRoomsDto.join()))
                .orElseGet(HashMap::new);

        for (var entry : joinedRooms.entrySet()) {

            final var roomId = MatrixRoomId.from(entry.getKey())
                    .orElseThrow(IllegalStateException::new);
            final var maybeRoom = state.getJoinedRooms().stream()
                    .filter(joinedRoom -> joinedRoom.getId().equals(roomId))
                    .findAny();

            if (maybeRoom.isEmpty()) {
                continue;
            }

            final var room = maybeRoom.get();
            Optional.ofNullable(entry.getValue())
                    .flatMap(roomDto -> Optional.ofNullable(roomDto.timeline()))
                    .flatMap(timelineDto -> Optional.ofNullable(timelineDto.events()))
                    .orElseGet(List::of)
                    .forEach(eventDto -> notifyAboutTimelineEvent(state, room, eventDto));
        }

        final var leftRooms = maybeRooms
                .flatMap(syncRoomsDto -> Optional.ofNullable(syncRoomsDto.leave()))
                .orElseGet(HashMap::new);

        for (var entry : leftRooms.entrySet()) {
            final var roomId = MatrixRoomId.from(entry.getKey()).orElseThrow(IllegalStateException::new);

            try {
                consumer.onSelfLeaveRoom(state, roomId);
            } catch (Exception e) {
                log.error("Uncaught exception when consuming room leave", e);
            }
        }
    }

    private void notifyAboutTimelineEvent(MatrixState state, MatrixRoom room, ClientEventDto event) {

        switch (event.type()) {
            case MessageEventContentDto.TYPE:
                notifyAboutMessageEvent(state, room, event);
                break;
            case MemberEventContentDto.TYPE:
                notifyAboutMemberEvent(state, room, event);
                break;
        }
    }

    private void notifyAboutMessageEvent(MatrixState state, MatrixRoom room, ClientEventDto event) {

        MessageEventContentDto content;
        try {
            content = objectMapper.treeToValue(event.content(), MessageEventContentDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (content.messageType() == null || content.body() == null) {
            log.error("Could not notify about invalid message: {}", event);
            return;
        }

        final var eventId = MatrixEventId.from(event.eventId())
                .orElseThrow(IllegalStateException::new);
        final var sender = MatrixUserId.from(event.sender())
                .orElseThrow(IllegalStateException::new);

        Optional<MatrixMessage> maybeMessage = switch (content.messageType()) {
            case TEXT -> MatrixTextMessage.from(eventId, content.body(), sender).map(MatrixMessage.class::cast);
            case EMOTE -> MatrixEmoteMessage.from(eventId, content.body(), sender).map(MatrixMessage.class::cast);
            case NOTICE -> MatrixNoticeMessage.from(eventId, content.body(), sender).map(MatrixMessage.class::cast);
            default -> Optional.empty();
        };

        try {
            maybeMessage
                    // We should not handle notice messages as they should not be handled automatically
                    .filter(message -> message.getType() != MatrixMessageType.NOTICE)
                    .ifPresent(message -> consumer.onMessage(state, room, message));
        } catch (Exception e) {
            log.error("Uncaught exception when consuming message", e);
        }
    }

    private void notifyAboutMemberEvent(MatrixState state, MatrixRoom room, ClientEventDto event) {

        MemberEventContentDto content;
        try {
            content = objectMapper.treeToValue(event.content(), MemberEventContentDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        final var sender = MatrixUserId.from(event.sender())
                .orElseThrow(IllegalStateException::new);

        try {
            if (content.membership() == MemberEventContentDto.MembershipState.LEAVE || content.membership() == MemberEventContentDto.MembershipState.BAN) {
                consumer.onUserLeaveRoom(state, room, sender);
            } else if (content.membership() == MemberEventContentDto.MembershipState.JOIN && !sender.equals(state.getOwnUserId())) {
                consumer.onUserJoinRoom(state, room, sender);
            }
        } catch (Exception e) {
            log.error("Uncaught exception when consuming member event", e);
        }
    }

    private void notifyAboutInviteEvent(MatrixState state, MatrixRoom room, StrippedStateEventDto event) {

        if (!MemberEventContentDto.TYPE.equals(event.type())) {
            return;
        }

        MemberEventContentDto messageEventContent;
        try {
            messageEventContent = objectMapper.treeToValue(event.content(), MemberEventContentDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (messageEventContent.membership() != MemberEventContentDto.MembershipState.INVITE) {
            return;
        }

        final var maybeSender = MatrixUserId.from(event.sender())
                .flatMap(senderId -> room.getRoomUsers()
                        .stream()
                        .filter(matrixUser -> matrixUser.getId().equals(senderId))
                        .findAny()
                );

        final var roomInvite = MatrixRoomInvite.from(room, maybeSender.orElse(null))
                .orElseThrow(IllegalStateException::new);

        try {
            consumer.onInviteToRoom(state, roomInvite);
        } catch (Exception e) {
            log.error("Uncaught exception when consuming room invite", e);
        }
    }
}
