package com.meari.echoshow.await;

import com.meari.echoshow.pojo.Message;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

/**
 * 内部服务返回类型
 * AWAIT
 * 挂起Message，等待被唤醒
 *
 * @author uhira
 */
public class AwaitMessageMode<T> extends Message<T>{

    private static final long serialVersionUID = 622693043645771159L;

    private final MessageAwaitCallback.Success successCallback;
    private final MessageAwaitCallback.Timeout timeoutCallback;
    private final long timeoutSeconds;
    private MonoSink<Message> monoSink;
    private long at;

    void setMonoSink(MonoSink<Message> monoSink) {
        this.monoSink = monoSink;
    }

    MonoSink<Message> getMonoSink() {
        return monoSink;
    }

    long getAt() {
        return at;
    }

    void setAt(long at) {
        this.at = at;
    }

    boolean isTimeout() {
        if (timeoutSeconds == -1) {
            return false;
        }
        return System.currentTimeMillis() - at > timeoutSeconds * 1000;
    }

    Message success(Message message) {
        if (successCallback != null) {
            return successCallback.success(message);
        }
        return message;
    }

    Message timeout() {
        if (timeoutCallback != null) {
            return timeoutCallback.timeout();
        }
        return null;
    }

    public AwaitMessageMode(
            MessageAwaitCallback.Success successCallback,
            MessageAwaitCallback.Timeout timeoutCallback,
            long timeoutSeconds,
            String messageId
    ) {
        this.successCallback = successCallback;
        this.timeoutCallback = timeoutCallback;
        this.timeoutSeconds = timeoutSeconds;
        this.setMessageId(messageId);
    }

    long getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
