/**
 * WebSocket 连接管理器
 * 用于在多个页面间共享 WebSocket 连接
 */
class WebSocketManager {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.connectionPromise = null;
        this.subscribers = new Map(); // 存储订阅回调函数
        this.messageQueue = []; // 存储待发送的消息队列
    }

    /**
     * 连接到 WebSocket 服务器
     */
    connect() {
        // 如果已经有连接，直接返回
        if (this.connected) {
            return Promise.resolve();
        }

        // 如果正在连接中，返回现有的 Promise
        if (this.connectionPromise) {
            return this.connectionPromise;
        }

        // 创建新的连接 Promise
        this.connectionPromise = new Promise((resolve, reject) => {
            try {
                const socket = new SockJS('/ws');
                this.stompClient = Stomp.over(socket);

                this.stompClient.connect({}, (frame) => {
                    console.log('Connected: ' + frame);
                    this.connected = true;
                    this.connectionPromise = null;
                    
                    // 处理消息队列中的消息
                    this.processMessageQueue();
                    
                    // 重新订阅所有主题
                    this.resubscribeAll();
                    
                    resolve();
                }, (error) => {
                    console.error('WebSocket connection error:', error);
                    this.connected = false;
                    this.connectionPromise = null;
                    reject(error);
                });
            } catch (error) {
                console.error('Failed to create WebSocket connection:', error);
                this.connectionPromise = null;
                reject(error);
            }
        });

        return this.connectionPromise;
    }

    /**
     * 断开 WebSocket 连接
     */
    disconnect() {
        if (this.stompClient && this.connected) {
            this.stompClient.disconnect(() => {
                console.log('Disconnected');
                this.connected = false;
                this.stompClient = null;
            });
        }
    }

    /**
     * 订阅主题
     * @param {string} topic - 主题路径
     * @param {function} callback - 回调函数
     * @returns {string} subscriptionId - 订阅ID，用于取消订阅
     */
    subscribe(topic, callback) {
        const subscriptionId = this.generateSubscriptionId();
        
        // 存储订阅信息
        this.subscribers.set(subscriptionId, {
            topic: topic,
            callback: callback,
            subscription: null
        });

        // 如果已经连接，立即订阅
        if (this.connected && this.stompClient) {
            const subscriber = this.subscribers.get(subscriptionId);
            subscriber.subscription = this.stompClient.subscribe(topic, (message) => {
                try {
                    callback(JSON.parse(message.body));
                } catch (error) {
                    console.error('Error parsing WebSocket message:', error);
                    callback(message.body);
                }
            });
        }

        return subscriptionId;
    }

    /**
     * 取消订阅
     * @param {string} subscriptionId - 订阅ID
     */
    unsubscribe(subscriptionId) {
        const subscriber = this.subscribers.get(subscriptionId);
        if (subscriber) {
            // 如果有活动的订阅，先取消订阅
            if (subscriber.subscription) {
                subscriber.subscription.unsubscribe();
            }
            // 从订阅列表中移除
            this.subscribers.delete(subscriptionId);
        }
    }

    /**
     * 重新订阅所有主题
     */
    resubscribeAll() {
        for (const [subscriptionId, subscriber] of this.subscribers.entries()) {
            if (this.stompClient) {
                subscriber.subscription = this.stompClient.subscribe(subscriber.topic, (message) => {
                    try {
                        subscriber.callback(JSON.parse(message.body));
                    } catch (error) {
                        console.error('Error parsing WebSocket message:', error);
                        subscriber.callback(message.body);
                    }
                });
            }
        }
    }

    /**
     * 发送消息
     * @param {string} destination - 目标地址
     * @param {object} message - 消息内容
     */
    sendMessage(destination, message) {
        // 如果尚未连接，将消息加入队列
        if (!this.connected || !this.stompClient) {
            this.messageQueue.push({ destination, message });
            // 尝试连接
            this.connect().catch(error => {
                console.error('Failed to connect for sending message:', error);
            });
            return;
        }

        // 发送消息
        try {
            this.stompClient.send(destination, {}, JSON.stringify(message));
        } catch (error) {
            console.error('Error sending WebSocket message:', error);
        }
    }

    /**
     * 处理消息队列
     */
    processMessageQueue() {
        while (this.messageQueue.length > 0 && this.connected && this.stompClient) {
            const { destination, message } = this.messageQueue.shift();
            try {
                this.stompClient.send(destination, {}, JSON.stringify(message));
            } catch (error) {
                console.error('Error sending queued WebSocket message:', error);
            }
        }
    }

    /**
     * 生成唯一的订阅ID
     */
    generateSubscriptionId() {
        return 'sub_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    }

    /**
     * 检查是否已连接
     */
    isConnected() {
        return this.connected;
    }
}

// 创建全局实例
const webSocketManager = new WebSocketManager();

// 导出供其他模块使用
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { webSocketManager };
} else if (typeof window !== 'undefined') {
    window.webSocketManager = webSocketManager;
}