package com.meari.echoshow.await;

import com.meari.echoshow.pojo.Message;
import com.meari.echoshow.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 消息等待
 *
 * @author uhira
 */
public class MessageAwait {
    private static Logger logger = LoggerFactory.getLogger(MessageAwait.class);

    private static ConcurrentHashMap<String, AwaitMessageMode> awaitMap = new ConcurrentHashMap<>();

    static {
        Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "Await-Check-Scheduled"))
                .scheduleAtFixedRate(() -> {
                    Iterator<AwaitMessageMode> it = awaitMap.values().iterator();
                    while (it.hasNext()) {
                        AwaitMessageMode<?> next = it.next();
                        if (next.isTimeout()) {
                            logger.info("<messageId>{}, timeout:{}", next.getMessageId(), next.getAt());
                            next.getMonoSink().success(next.timeout());
                            it.remove();
                        }
                    }
                }, 10, 1, TimeUnit.SECONDS);
    }

    /**
     * 在await时需要指定唤醒者身份
     *
     * @param await
     * @return
     */
    public static Mono<Message> await(AwaitMessageMode await) {
        String messageId = await.getMessageId();
        long timeoutSeconds = await.getTimeoutSeconds();
        return Mono.create(monoSink -> {
            logger.info("<messageId>{}, await:{}s", messageId, timeoutSeconds);
            await.setAt(System.currentTimeMillis());
            await.setMonoSink(monoSink);
            awaitMap.put(messageId, await);
        });
    }

    /**
     * 只有合法的唤醒者才能唤醒
     *
     * @param message
     */
    public static void signal(Message message) {
        String messageId = message.getMessageId();
        AwaitMessageMode<?> await = awaitMap.get(messageId);
        //基于安全考虑，需要验证pk和dn
        if (await == null || StringUtil.isNull(messageId) || !messageId.equals(await.getMessageId())) {
            logger.error("<messageId>{}: not awaited, return!", messageId);
            return;
        }
        await.getMonoSink().success(await.success(message));
        awaitMap.remove(messageId);
        logger.info("<messageId>{}: signal:{}", messageId, await.getAt());
    }

}
