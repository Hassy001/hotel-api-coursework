package com.hotel.store;

import com.hotel.model.Room;
import com.hotel.model.Sensor;
import com.hotel.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private DataStore() {}

    public static final AtomicInteger nextRoomId = new AtomicInteger(1);
    public static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    public static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    public static final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();
}
