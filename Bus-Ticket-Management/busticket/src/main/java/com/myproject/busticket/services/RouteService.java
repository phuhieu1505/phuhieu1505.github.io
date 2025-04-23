package com.myproject.busticket.services;

import java.util.List;

import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.myproject.busticket.mapper.RouteMapper;
import com.myproject.busticket.models.Route;
import com.myproject.busticket.repositories.RouteRepository;

@Service
public class RouteService {
    @Autowired
    private RouteRepository routeRepository;

    RouteMapper routeMapper = Mappers.getMapper(RouteMapper.class);

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public List<Route> findRouteByDistance(double min, double max) {
        return routeRepository.findRouteByDistance(min, max);
    }

    public Route getRouteByCode(String code) {
        return routeRepository.findByCode(code).orElse(null);
    }

    public Page<Route> getAll(Pageable pageable) {
        return routeRepository.findAll(pageable);
    }

    public List<Route> getAll() {
        return routeRepository.findAll();
    }

    public Route save(Route route) {
        return routeRepository.save(route);
    }

    public void deleteById(int id) {
        routeRepository.deleteById(id);
    }

    public Route deleteByCode(String code) {
        Route route = routeRepository.findByCode(code).orElse(null);
        if (route != null) {
            routeRepository.delete(route);
        }
        return route;
    }

    public Page<Route> searchRouteByCodeAndName(Pageable pageable, String query) {
        return routeRepository.findByCodeContainingOrNameContainingAllIgnoreCase(query, query, pageable);
    }
}
