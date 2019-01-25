package com.github.skystardust.ultracore.core.redis.listeners;

import com.github.skystardust.ultracore.core.redis.models.AbstractRedisModel;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.ParameterizedType;

@Getter
@Setter
@AllArgsConstructor
public abstract class DefaultChannelListener<MessageType extends AbstractRedisModel> extends RedisPubSubAdapter<String, String> {
    private String channel;

    public abstract void onReceived(MessageType messageType);

    public void onException(Exception e) {
        //ignored
    }

    @Override
    public void message(String channel, String message) {
        if (this.channel.equals(channel)) {
            try {
                onReceived(AbstractRedisModel.valueOf(message, (Class<MessageType>) ((ParameterizedType) getClass()
                        .getGenericSuperclass()).getActualTypeArguments()[0]));
            } catch (Exception e) {
                onException(e);
            }
        }
    }
}
