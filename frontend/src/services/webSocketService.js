class WebSocketService {
    constructor() {
        this.socket = null;
    }

    connect(url) {
        this.socket = new WebSocket(url);
        this.socket.onopen = () => console.log("WebSocket connected");
        this.socket.onclose = () => console.log("WebSocket disconnected");
        this.socket.onerror = (error) => console.error("WebSocket error:", error);
    }

    sendMessage(message) {
        if (this.socket && this.socket.readyState === WebSocket.OPEN) {
            this.socket.send(message);
        } else {
            console.error("WebSocket is not open");
        }
    }

    addMessageListener(callback) {
        this.socket.onmessage = (event) => callback(event.data);
    }
}

const webSocketService = new WebSocketService();
export default webSocketService;
