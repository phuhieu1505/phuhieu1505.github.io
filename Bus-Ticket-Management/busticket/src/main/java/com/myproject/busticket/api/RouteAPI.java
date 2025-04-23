package com.myproject.busticket.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.busticket.dto.RouteCheckpointDTO;
import com.myproject.busticket.enums.CheckpointType;
import com.myproject.busticket.models.Route;
import com.myproject.busticket.models.Route_Checkpoint;
import com.myproject.busticket.services.CheckpointService;
import com.myproject.busticket.services.RouteCheckpointService;
import com.myproject.busticket.services.RouteService;
import com.myproject.busticket.services.TripService;

@RestController
@RequestMapping("/api/route")
public class RouteAPI {
    @Autowired
    private RouteService routeService;

    @Autowired
    private RouteCheckpointService routeCheckpointService;

    @Autowired
    private CheckpointService checkpointService;

    @Autowired
    private TripService tripService;

    @GetMapping("/routes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRoutes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String searchValue) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Route> routePages;

        if (searchValue != null && !searchValue.isEmpty()) {
            routePages = routeService.searchRouteByCodeAndName(pageable, searchValue);
        } else {
            routePages = routeService.getAll(pageable);
        }

        List<Route> routes = routePages.getContent().stream()
                .map(route -> new Route(route.getRouteId(), route.getCode(), route.getName(),
                        route.getTime(), route.getDistance()))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("routes", routes);
        response.put("currentPage", page);
        response.put("totalPages", routePages.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/route-detail/{routeCode}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRouteDetails(@PathVariable String routeCode) {
        Route route = routeService.getRouteByCode(routeCode);
        List<Route_Checkpoint> routeCheckpoints = routeCheckpointService.findByRoute(route);

        if (routeCheckpoints.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("errorMessage", "No checkpoints found for this route.");
            return ResponseEntity.ok(response);
        }

        List<RouteCheckpointDTO> routeCheckpointsDTO = routeCheckpoints.stream()
                .map(rc -> new RouteCheckpointDTO(
                        rc.getCheckpoint().getCheckpointId(),
                        rc.getCheckpoint().getPlaceName(),
                        rc.getCheckpoint().getAddress(),
                        rc.getCheckpoint().getProvince(),
                        rc.getCheckpoint().getCity(),
                        rc.getCheckpointOrder(),
                        rc.getType()))
                .collect(Collectors.toList());

        routeCheckpointsDTO.sort((rc1, rc2) -> rc1.getCheckpointOrder() - rc2.getCheckpointOrder());

        Map<String, Object> response = new HashMap<>();
        response.put("route", route);
        response.put("checkpoints", routeCheckpointsDTO);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/new-route")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveRoute(@RequestBody Map<String, Object> routeRequest) {
        Map<String, Object> response = new HashMap<>();

        // Extract route details from the request
        String code = (String) routeRequest.get("code");
        String name = (String) routeRequest.get("name");
        String time = (String) routeRequest.get("time");
        double distance = Double.parseDouble(routeRequest.get("distance").toString());

        // Validate route details
        if (code == null || code.isEmpty() ||
                name == null || name.isEmpty() ||
                time == null || time.isEmpty() ||
                distance <= 0) {
            response.put("message", "Invalid input data.");
            return ResponseEntity.badRequest().body(response);
        }

        // Check if the route already exists
        Route existingRoute = routeService.getRouteByCode(code);
        if (existingRoute != null) {
            response.put("message", "Route with this code already exists.");
            return ResponseEntity.badRequest().body(response);
        }

        Route newRoute = new Route();
        newRoute.setCode(code);
        newRoute.setName(name);
        newRoute.setTime(time);
        newRoute.setDistance(distance);
        routeService.save(newRoute);

        List<Map<String, Object>> checkpoints = (List<Map<String, Object>>) routeRequest.get("checkpoints");
        if (checkpoints.isEmpty()) {
            response.put("message", "No checkpoints found for this route.");
            return ResponseEntity.badRequest().body(response);
        }

        if (checkpoints.size() < 2) {
            response.put("message", "A route must have at least 2 checkpoints.");
            routeService.deleteByCode(code);
            return ResponseEntity.badRequest().body(response);
        }

        int index = 0;
        int checkpointOrder = 0;
        for (Map<String, Object> checkpointData : checkpoints) {
            Route_Checkpoint routeCheckpoint = new Route_Checkpoint();
            routeCheckpoint.setRoute(newRoute);
            routeCheckpoint.setCheckpointOrder(checkpointOrder++);

            // Ensure checkpointId is correctly retrieved and cast to an integer
            Object checkpointIdObj = checkpointData.get("checkpointId");
            if (checkpointIdObj == null) {
                response.put("message", "Checkpoint ID is missing.");
                routeService.deleteByCode(code);
                return ResponseEntity.badRequest().body(response);
            }

            Integer checkpointId;
            try {
                checkpointId = Integer.parseInt(checkpointIdObj.toString());
            } catch (NumberFormatException e) {
                routeService.deleteByCode(code);
                response.put("message", "Invalid checkpoint ID format.");
                return ResponseEntity.badRequest().body(response);
            }

            routeCheckpoint.setCheckpoint(checkpointService.getById(checkpointId));
            if (index == 0) {
                routeCheckpoint.setType(CheckpointType.departure);
            }
            if (index == checkpoints.size() - 1) {
                routeCheckpoint.setType(CheckpointType.drop_off);
            } else if (index > 0 && index < checkpoints.size() - 1) {
                routeCheckpoint.setType(CheckpointType.en_route);
            }
            index += 1;
            routeCheckpointService.save(routeCheckpoint);
        }

        response.put("message", "Route saved successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-route/{routeCode}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateRoute(@PathVariable String routeCode,
            @RequestBody Map<String, Object> routeRequest) {
        Map<String, Object> response = new HashMap<>();

        String name = (String) routeRequest.get("name");
        String time = (String) routeRequest.get("time");
        double distance = Double.parseDouble(routeRequest.get("distance").toString());

        if (!isValidInput(name, time, distance)) {
            response.put("message", "Invalid input data.");
            return ResponseEntity.badRequest().body(response);
        }

        Route existingRoute = routeService.getRouteByCode(routeCode);
        if (existingRoute == null) {
            response.put("message", "Route not found.");
            return ResponseEntity.badRequest().body(response);
        }

        List<Route_Checkpoint> oldRouteCheckpoints = routeCheckpointService.findByRoute(existingRoute);
        List<Map<String, Object>> newCheckpoints = (List<Map<String, Object>>) routeRequest.get("checkpoints");

        if (newCheckpoints.isEmpty()) {
            response.put("message", "No checkpoints found for this route.");
            return ResponseEntity.badRequest().body(response);
        }

        if (!hasChanges(existingRoute, name, time, distance, oldRouteCheckpoints, newCheckpoints)) {
            response.put("message", "Nothing has been changed.");
            return ResponseEntity.badRequest().body(response);
        }

        // Proceed with the update
        updateRouteDetails(existingRoute, name, time, distance);
        ResponseEntity<Map<String, Object>> checkpointUpdateResponse = updateRouteCheckpoints(existingRoute,
                oldRouteCheckpoints, newCheckpoints);
        if (checkpointUpdateResponse.getStatusCode() != HttpStatus.OK) {
            return checkpointUpdateResponse;
        }

        response.put("message", "Route updated successfully.");
        return ResponseEntity.ok(response);
    }

    private boolean isValidInput(String name, String time, double distance) {
        return name != null && !name.isEmpty() && time != null && distance > 0;
    }

    private boolean hasChanges(Route existingRoute, String name, String time, double distance,
            List<Route_Checkpoint> oldRouteCheckpoints, List<Map<String, Object>> newCheckpoints) {
        if (!existingRoute.getName().equals(name) ||
                !existingRoute.getTime().equals(time) ||
                existingRoute.getDistance() != distance) {
            return true;
        }

        if (oldRouteCheckpoints.size() != newCheckpoints.size()) {
            return true;
        }

        oldRouteCheckpoints.sort(Comparator.comparingInt(Route_Checkpoint::getCheckpointOrder));
        newCheckpoints.sort(Comparator.comparingInt(c -> (int) c.get("checkpointOrder")));

        // Check for changes in checkpoint type:
        for (int i = 0; i < newCheckpoints.size(); i++) {
            Map<String, Object> checkpointData = newCheckpoints.get(i);
            String type = (String) checkpointData.get("checkpointType");

            Route_Checkpoint routeCheckpoint = oldRouteCheckpoints.get(i);

            if (!routeCheckpoint.getType().name().equals(type)) {
                return true;
            }
        }

        // Check if same checkpoints but different order by checkpointOrder:
        for (int i = 0; i < newCheckpoints.size(); i++) {
            Map<String, Object> checkpointData = newCheckpoints.get(i);
            int checkpointId = (int) checkpointData.get("checkpointId");
            String type = (String) checkpointData.get("checkpointType");
            int checkpointOrder = (int) checkpointData.get("checkpointOrder");

            Route_Checkpoint routeCheckpoint = oldRouteCheckpoints.get(i);

            if (routeCheckpoint.getCheckpoint().getCheckpointId() != checkpointId ||
                    !routeCheckpoint.getType().name().equals(type) ||
                    routeCheckpoint.getCheckpointOrder() != checkpointOrder) {
                return true;
            }
        }

        return false;
    }

    private void updateRouteDetails(Route existingRoute, String name, String time, double distance) {
        existingRoute.setName(name);
        existingRoute.setTime(time);
        existingRoute.setDistance(distance);
        routeService.save(existingRoute);
    }

    private ResponseEntity<Map<String, Object>> updateRouteCheckpoints(Route existingRoute,
            List<Route_Checkpoint> oldRouteCheckpoints,
            List<Map<String, Object>> newCheckpoints) {
        Map<String, Object> response = new HashMap<>();

        if (newCheckpoints.size() < 2) {
            response.put("message", "A route must have at least 2 checkpoints.");
            return ResponseEntity.badRequest().body(response);
        }

        // Validate new checkpoints
        List<Route_Checkpoint> newRouteCheckpoints = new ArrayList<>();
        int i = 1;
        for (Map<String, Object> checkpointData : newCheckpoints) {
            Route_Checkpoint routeCheckpoint = new Route_Checkpoint();
            routeCheckpoint.setRoute(existingRoute);

            Integer checkpointId;
            try {
                checkpointId = parseCheckpointId(checkpointData.get("checkpointId"));
            } catch (IllegalArgumentException e) {
                response.put("message", "Invalid checkpoint ID format for checkpoint " + i);
                return ResponseEntity.badRequest().body(response);
            }
            routeCheckpoint.setCheckpoint(checkpointService.getById(checkpointId));

            String type = (String) checkpointData.get("checkpointType");
            if (type == null || type.isEmpty()) {
                response.put("message", "Checkpoint type is missing for checkpoint " + i);
                return ResponseEntity.badRequest().body(response);
            }

            if (i == 1) {
                routeCheckpoint.setType(CheckpointType.departure);
            } else if (i == newCheckpoints.size()) {
                routeCheckpoint.setType(CheckpointType.drop_off);
            } else {
                CheckpointType checkpointType;
                try {
                    checkpointType = parseCheckpointType(type);
                } catch (IllegalArgumentException e) {
                    response.put("message", "Invalid checkpoint type for checkpoint " + i);
                    return ResponseEntity.badRequest().body(response);
                }
                if (checkpointType == CheckpointType.departure || checkpointType == CheckpointType.drop_off) {
                    response.put("message",
                            "Only the first checkpoint can be 'departure' and the last checkpoint can be 'drop_off'.");
                    return ResponseEntity.badRequest().body(response);
                }
                routeCheckpoint.setType(checkpointType);
            }

            routeCheckpoint.setCheckpointOrder(checkpointData.get("checkpointOrder") == null ? i++
                    : (int) checkpointData.get("checkpointOrder"));
            newRouteCheckpoints.add(routeCheckpoint);
            ++i;
        }

        // If validation passes, proceed with the update
        // Delete old checkpoints
        for (Route_Checkpoint oldRouteCheckpoint : oldRouteCheckpoints) {
            routeCheckpointService.delete(oldRouteCheckpoint);
        }

        // Save new checkpoints
        for (Route_Checkpoint newRouteCheckpoint : newRouteCheckpoints) {
            routeCheckpointService.save(newRouteCheckpoint);
        }

        response.put("message", "Checkpoints updated successfully.");
        return ResponseEntity.ok(response);
    }

    private Integer parseCheckpointId(Object checkpointIdObj) {
        try {
            return Integer.parseInt(checkpointIdObj.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid checkpoint ID format.");
        }
    }

    private CheckpointType parseCheckpointType(String type) {
        try {
            return CheckpointType.valueOf(type.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid checkpoint type.");
        }
    }

    @PostMapping("/delete-route")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteRoute(@RequestBody Map<String, Object> routeRequest) {
        Map<String, Object> response = new HashMap<>();
        String code = (String) routeRequest.get("code");
        Route existingRoute = routeService.getRouteByCode(code);
        if (existingRoute == null) {
            response.put("message", "Route not found.");
            return ResponseEntity.badRequest().body(response);
        }

        if (!tripService.findTripByRouteCode(existingRoute).isEmpty()) {
            response.put("message", "Route has trips and cannot be deleted.");
            return ResponseEntity.badRequest().body(response);
        }

        List<Route_Checkpoint> routeCheckpoints = routeCheckpointService.findByRoute(existingRoute);
        for (Route_Checkpoint routeCheckpoint : routeCheckpoints) {
            routeCheckpointService.delete(routeCheckpoint);
        }

        routeService.deleteByCode(code);
        response.put("message", "Route deleted successfully.");
        return ResponseEntity.ok(response);
    }

}
