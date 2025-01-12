import webSocketService from "../services/webSocketService";

const webSocketMiddleware = (store) => (next) => (action) => {
    if (action.type === "websocket/connect") {
        webSocketService.connect(action.payload.url);
    } else if (action.type === "websocket/sendMessage") {
        webSocketService.sendMessage(action.payload.message);
    }
    return next(action);
};

export default webSocketMiddleware;
