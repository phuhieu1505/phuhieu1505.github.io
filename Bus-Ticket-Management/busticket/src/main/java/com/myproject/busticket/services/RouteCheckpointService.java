package com.myproject.busticket.services;

import com.myproject.busticket.models.Checkpoint;
import com.myproject.busticket.models.Route;
import com.myproject.busticket.models.Route_Checkpoint;
import com.myproject.busticket.repositories.RouteCheckpointRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteCheckpointService {
    @Autowired
    RouteCheckpointRepository routeCheckpointRepository;

    public RouteCheckpointService(RouteCheckpointRepository routeCheckpointRepository) {
        this.routeCheckpointRepository = routeCheckpointRepository;
    }

    public List<String> getAllProvinces() {
        return routeCheckpointRepository.findAllCheckpointProvinces();
    }

    public List<String> getAllCities() {
        return routeCheckpointRepository.findAllCheckpointCities();
    }

    public List<Route_Checkpoint> findByRoute(Route route) {
        return routeCheckpointRepository.findByRoute(route);
    }

    public List<Route_Checkpoint> findByCheckpoint(Checkpoint checkpoint) {
        return routeCheckpointRepository.findByCheckpoint(checkpoint);
    }

    public Route_Checkpoint save(Route_Checkpoint routeCheckpoint) {
        return routeCheckpointRepository.save(routeCheckpoint);
    }

    public void delete(Route_Checkpoint oldRouteCheckpoint) {
        routeCheckpointRepository.delete(oldRouteCheckpoint);
    }
}
