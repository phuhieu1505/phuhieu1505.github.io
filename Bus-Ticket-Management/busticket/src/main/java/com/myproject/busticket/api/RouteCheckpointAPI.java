package com.myproject.busticket.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.busticket.dto.RouteCheckpointDTO;
import com.myproject.busticket.models.Route;
import com.myproject.busticket.models.Route_Checkpoint;
import com.myproject.busticket.services.RouteCheckpointService;
import com.myproject.busticket.services.RouteService;

@RestController
@RequestMapping("/api/route-checkpoint")
public class RouteCheckpointAPI {
    @Autowired
    private RouteService routeService;

    @Autowired
    private RouteCheckpointService routeCheckpointService;

    @GetMapping("/selected-checkpoints/{routeCode}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSelectedCheckpoints(@PathVariable String routeCode) {
        Route route = routeService.getRouteByCode(routeCode);
        List<Route_Checkpoint> routeCheckpoints = routeCheckpointService.findByRoute(route);

        if (routeCheckpoints.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("errorMessage", "No checkpoints found for this route.");
            return ResponseEntity.ok(response);
        }

        List<RouteCheckpointDTO> selectedCheckpointsDTO = routeCheckpoints.stream()
                .map(rc -> new RouteCheckpointDTO(
                        rc.getCheckpoint().getCheckpointId(),
                        rc.getCheckpoint().getPlaceName(),
                        rc.getCheckpoint().getAddress(),
                        rc.getCheckpoint().getProvince(),
                        rc.getCheckpoint().getCity(),
                        rc.getCheckpointOrder(),
                        rc.getType()))
                .collect(Collectors.toList());

        selectedCheckpointsDTO.sort((rc1, rc2) -> rc1.getCheckpointOrder() - rc2.getCheckpointOrder());

        Map<String, Object> response = new HashMap<>();
        response.put("route", route);
        response.put("selectedCheckpoints", selectedCheckpointsDTO);

        return ResponseEntity.ok(response);
    }
}
