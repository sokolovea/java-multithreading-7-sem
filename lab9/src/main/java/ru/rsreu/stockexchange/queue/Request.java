package ru.rsreu.stockexchange.queue;

public class Request {
    final RequestType requestType;
    public Request(RequestType requestType) {
        this.requestType = requestType;
    }
}
