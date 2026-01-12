package co.casterlabs.caffeinated.app.koi;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import co.casterlabs.caffeinated.app.config.AppConfig;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.koi.api.types.KoiEventType;
import co.casterlabs.koi.api.types.events.ChannelPointsEvent;
import co.casterlabs.koi.api.types.events.FollowEvent;
import co.casterlabs.koi.api.types.events.LikeEvent;
import co.casterlabs.koi.api.types.events.MessageMetaEvent;
import co.casterlabs.koi.api.types.events.RaidEvent;
import co.casterlabs.koi.api.types.events.RichMessageEvent;
import co.casterlabs.koi.api.types.events.SubscriptionEvent;
import co.casterlabs.koi.api.types.events.ViewerJoinEvent;
import co.casterlabs.koi.api.types.events.ViewerLeaveEvent;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import co.casterlabs.rakurai.json.validation.JsonValidationException;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class KoiHistory {
    private static final FastLogger LOGGER = new FastLogger();
    private static final int MAX_HISTORY_CHUNK = 500;

    static {
        try {
            AppConfig.preferencesConnection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS koi_historical (timestamp INTEGER NOT NULL, eventId TEXT NOT NULL PRIMARY KEY, data BLOB NOT NULL);"
            ).execute();
        } catch (SQLException x) {
            LOGGER.severe("Could not make historical data table:\n%s", x);
            throw new RuntimeException(x);
        }
    }

    static List<KoiEvent> getHistoryAtOrBeforeTimestamp(long beforeOrAt) {
        List<byte[]> history = new LinkedList<>();

        try (PreparedStatement ps = AppConfig.preferencesConnection.prepareStatement("SELECT data FROM koi_historical WHERE timestamp <= ?1 ORDER BY timestamp DESC LIMIT ?2;")) {
            ps.setLong(1, beforeOrAt);
            ps.setInt(2, MAX_HISTORY_CHUNK);
            ps.execute();

            ResultSet results = ps.getResultSet();
            while (results.next()) {
                history.add(results.getBytes("data"));
            }
        } catch (Throwable t) {
            LOGGER.severe("Could not add onto historical data:\n%s", t);
        }

        return history.parallelStream()
            .map((entry) -> {
                try {
                    return KoiEventType.get(Rson.DEFAULT.fromJson(decompress(entry), JsonObject.class));
                } catch (JsonParseException e) {
                    LOGGER.warn("Could not parse historical event JSON:\n%s", e);
                    return null;
                }
            })
            .filter((event) -> event != null)
            .sorted((event1, event2) -> Long.compare(event1.timestamp.toEpochMilli(), event2.timestamp.toEpochMilli()))
            .collect(Collectors.toList());
    }

    static void storeEvent(KoiEvent event, JsonElement eventJson) {
        if (event.streamer.platform == UserPlatform.CASTERLABS_SYSTEM) return; // Don't store test events.

        String eventId = getEventId(event);
        if (eventId == null) return;

        try (PreparedStatement ps = AppConfig.preferencesConnection.prepareStatement("INSERT OR REPLACE INTO koi_historical (timestamp, eventId, data) VALUES (?1, ?2, ?3);")) {
            ps.setLong(1, event.timestamp.toEpochMilli());
            ps.setString(2, eventId);
            ps.setBytes(3, compress(eventJson.toString(false)));
            ps.execute();
        } catch (Throwable t) {
            LOGGER.severe("Could not add onto historical data:\n%s", t);
        }
    }

    static void handleMetaEvent(MessageMetaEvent metaEvent) {
        if (metaEvent.streamer.platform == UserPlatform.CASTERLABS_SYSTEM) return; // Don't update test events.

        String metaId = String.format("id|%s|%s", metaEvent.streamer.platform.name(), metaEvent.metaId);

        try (PreparedStatement ps = AppConfig.preferencesConnection.prepareStatement("SELECT data FROM koi_historical WHERE eventId = ?1;")) {
            ps.setString(1, metaId);
            ps.execute();

            ResultSet results = ps.getResultSet();
            if (!results.next()) {
                LOGGER.warn("No historical data found for message '%s' / '%s', cannot update metadata.", metaEvent.metaId, metaId);
                return; // No data, don't try to modify.
            }

            String eventJson = decompress(results.getBytes("data"));
            JsonObject eventToModify = Rson.DEFAULT.fromJson(eventJson, JsonObject.class);

            eventToModify.put("upvotes", metaEvent.upvotes);
            eventToModify.put("is_visible", metaEvent.visible);

            try (PreparedStatement ps2 = AppConfig.preferencesConnection.prepareStatement("UPDATE koi_historical SET data = ?2 WHERE eventId = ?1;")) {
                ps2.setString(1, metaId);
                ps2.setBytes(2, compress(eventToModify.toString(false)));
                ps2.execute();
            }
        } catch (Throwable t) {
            LOGGER.severe("Could not modify historical data for event '%s':\n%s", metaEvent.metaId, t);
        }
    }

    static void optimisticallyDelete(UserPlatform platform, String id) {
        if (platform == UserPlatform.CASTERLABS_SYSTEM) return; // Don't update test events.

        String metaId = null;

        try (PreparedStatement ps = AppConfig.preferencesConnection.prepareStatement("SELECT data FROM koi_historical WHERE eventId = ?1;")) {
            String trueId = getTrueId(id);
            metaId = String.format("id|%s|%s", platform.name(), trueId);

            ps.setString(1, metaId);
            ps.execute();

            ResultSet results = ps.getResultSet();
            if (!results.next()) {
                LOGGER.warn("No historical data found for message '%s' / '%s', cannot delete optimistically.", id, metaId);
                return; // No data, don't try to modify.
            }

            String eventJson = decompress(results.getBytes("data"));
            JsonObject eventToModify = Rson.DEFAULT.fromJson(eventJson, JsonObject.class);

            eventToModify.put("is_visible", false);

            try (PreparedStatement ps2 = AppConfig.preferencesConnection.prepareStatement("UPDATE koi_historical SET data = ?2 WHERE eventId = ?1;")) {
                ps2.setString(1, metaId);
                ps2.setBytes(2, compress(eventToModify.toString(false)));
                ps2.execute();
            }
        } catch (Throwable t) {
            LOGGER.severe("Could not modify historical data for event '%s':\n%s", metaId, t);
        }
    }

    private static byte[] compress(String input) {
        Deflater d = new Deflater();
        d.setLevel(5);
        d.setInput(input.getBytes(StandardCharsets.UTF_8));
        d.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (!d.finished()) {
            int compressedSize = d.deflate(buffer);
            outputStream.write(buffer, 0, compressedSize);
        }

        return outputStream.toByteArray();
    }

    @SneakyThrows
    private static String decompress(byte[] input) {
        Inflater inflater = new Inflater();
        inflater.setInput(input);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int decompressedSize = inflater.inflate(buffer);
            outputStream.write(buffer, 0, decompressedSize);
        }

        byte[] bytes = outputStream.toByteArray();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static String getEventId(KoiEvent event) {
        switch (event.type()) {
            case FOLLOW:
                return String.format("genid|%d|%s|%s", event.timestamp.toEpochMilli(), event.type(), ((FollowEvent) event).follower.UPID);

            case SUBSCRIPTION: {
                String recipientIds = ((SubscriptionEvent) event).giftRecipients
                    .stream()
                    .map((u) -> u.UPID)
                    .collect(Collectors.joining("+"));
                return String.format("genid|%d|%s|%s>%s", event.timestamp.toEpochMilli(), event.type(), ((SubscriptionEvent) event).subscriber.UPID, recipientIds);
            }

            case VIEWER_JOIN:
                return String.format("genid|%d|%s|%s", event.timestamp.toEpochMilli(), event.type(), ((ViewerJoinEvent) event).viewer.UPID);

            case VIEWER_LEAVE:
                return String.format("genid|%d|%s|%s", event.timestamp.toEpochMilli(), event.type(), ((ViewerLeaveEvent) event).viewer.UPID);

            case RAID:
                return String.format("genid|%d|%s|%s", event.timestamp.toEpochMilli(), event.type(), ((RaidEvent) event).host.UPID);

            case CHANNEL_POINTS:
                return String.format("genid|%d|%s|%s", event.timestamp.toEpochMilli(), event.type(), ((ChannelPointsEvent) event).sender.UPID);

            case LIKE:
                return String.format("genid|%d|%s|%s", event.timestamp.toEpochMilli(), event.type(), ((LikeEvent) event).liker.UPID);

            case RICH_MESSAGE:
                return String.format("id|%s|%s", event.streamer.platform.name(), ((RichMessageEvent) event).metaId);

            case PLATFORM_MESSAGE:
            case CLEARCHAT:
            case STREAM_STATUS:
            case VIEWER_COUNT:
            case VIEWER_LIST:
                return String.format("genid|%d|%s", event.timestamp.toEpochMilli(), event.type());

            default:
                return null;
        }
    }

    static String getTrueId(String id) throws JsonValidationException, JsonParseException {
        // I hate this. Oh well...
        String json = new String(
            Base64
                .getDecoder()
                .decode(id),
            StandardCharsets.UTF_8
        );

        JsonObject obj = Rson.DEFAULT.fromJson(json, JsonObject.class);
        if (obj.containsKey("true_id")) {
            return obj.getString("true_id");
        } else if (obj.containsKey("trueId")) {
            return obj.getString("trueId");
        } else {
            return "<invalid>";
        }
    }

}
