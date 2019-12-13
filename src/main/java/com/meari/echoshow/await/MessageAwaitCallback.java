package com.meari.echoshow.await;


import com.meari.echoshow.pojo.Message;

/**
 * 消息等待回调
 *
 * @author uhira
 */
public interface MessageAwaitCallback {

    @FunctionalInterface
    interface Success {

        /**
         * 等待成功回调函数
         *
         * @param message
         * @return
         */
        Message success(Message message);
    }

    @FunctionalInterface
    interface Timeout {

        /**
         * 等待超时回调函数
         *
         * @return
         */
        Message timeout();
    }
}