package com.queue_it.connector;

import java.util.List;

import com.queue_it.connector.models.CustomData;

public interface IEnqueueTokenProvider {

    String getEnqueueToken(String waitingRoomId) throws Exception;

    List<CustomData> getEnqueueTokenCustomData(String waitingRoom) throws Exception;

    String getEnqueueTokenKey(String waitingRoomId) throws Exception;
}