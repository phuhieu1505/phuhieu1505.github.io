package com.myproject.busticket.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.busticket.models.Checkpoint;
import com.myproject.busticket.models.Route_Checkpoint;
import com.myproject.busticket.services.CheckpointService;
import com.myproject.busticket.services.RouteCheckpointService;

@RestController
@RequestMapping("/api/checkpoint")
public class CheckpointAPI {
    @Autowired
    private CheckpointService checkpointService;

    @Autowired
    private RouteCheckpointService routeCheckpointService;

    @GetMapping("/checkpoints")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCheckpoints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String searchValue) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Checkpoint> checkpointPages;

        if (searchValue != null && !searchValue.isEmpty()) {
            checkpointPages = checkpointService.searchCheckpoints(searchValue, pageable);
        } else {
            checkpointPages = checkpointService.getAll(pageable);
        }

        List<Checkpoint> checkpoints = checkpointPages.getContent().stream()
                .map(checkpoint -> new Checkpoint(
                        checkpoint.getCheckpointId(),
                        checkpoint.getPlaceName(),
                        checkpoint.getAddress(),
                        checkpoint.getProvince(),
                        checkpoint.getCity(),
                        checkpoint.getPhone(),
                        checkpoint.getRegion()))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("checkpoints", checkpoints);
        response.put("currentPage", page);
        response.put("totalPages", checkpointPages.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{checkpointId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCheckpointById(@PathVariable int checkpointId) {
        Checkpoint checkpoint = checkpointService.getById(checkpointId);
        if (checkpoint == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Checkpoint not found.");
            return ResponseEntity.badRequest().body(response);
        }

        List<Route_Checkpoint> routeCheckpoints = routeCheckpointService.findByCheckpoint(checkpoint);
        List<Map<String, Object>> assignedRoutes = routeCheckpoints.stream()
                .map(rc -> {
                    Map<String, Object> routeCheckpointData = new HashMap<>();
                    routeCheckpointData.put("routeCode", rc.getRoute().getCode());
                    routeCheckpointData.put("routeName", rc.getRoute().getName());
                    routeCheckpointData.put("checkpointOrder", rc.getCheckpointOrder());
                    routeCheckpointData.put("type", rc.getType().name());
                    return routeCheckpointData;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("checkpointId", checkpoint.getCheckpointId());
        response.put("placeName", checkpoint.getPlaceName());
        response.put("address", checkpoint.getAddress());
        response.put("province", checkpoint.getProvince());
        response.put("city", checkpoint.getCity());
        response.put("phone", checkpoint.getPhone());
        response.put("region", checkpoint.getRegion());
        response.put("assignedRoutes", assignedRoutes);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/new-checkpoint")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> newCheckpoint(@RequestBody Map<String, Object> checkpointRequest) {
        Map<String, Object> response = new HashMap<>();

        String placeName = (String) checkpointRequest.get("placeName");
        String address = (String) checkpointRequest.get("address");
        String province = (String) checkpointRequest.get("province");
        String city = (String) checkpointRequest.get("city");
        String phone = (String) checkpointRequest.get("phone");
        String region = (String) checkpointRequest.get("region");
        if (region.equals("mien_bac")) {
            region = "Miền Bắc";
        } else if (region.equals("mien_trung")) {
            region = "Miền Trung";
        } else if (region.equals("mien_nam")) {
            region = "Miền Nam";
        } else {
            response.put("message", "Invalid region.");
            return ResponseEntity.badRequest().body(response);
        }
        // String validationMessage =
        // CheckpointValidation.validateCheckpointFields(placeName, address, province,
        // city,
        // phone, region);
        // if (validationMessage != null) {
        // response.put("message", validationMessage);
        // return ResponseEntity.badRequest().body(response);
        // }

        Checkpoint checkpoint = new Checkpoint();
        checkpoint.setCheckpointId(0);
        checkpoint.setPlaceName(placeName);
        checkpoint.setAddress(address);
        checkpoint.setProvince(province);
        checkpoint.setCity(city);
        checkpoint.setPhone(phone);
        checkpoint.setRegion(region);

        checkpointService.save(checkpoint);
        response.put("message", "Checkpoint created successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-checkpoint")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCheckpoint(@RequestBody Map<String, Object> checkpointRequest) {
        Map<String, Object> response = new HashMap<>();
        String checkpointIdStr = (String) checkpointRequest.get("checkpointId");
        int checkpointId;
        try {
            checkpointId = Integer.parseInt(checkpointIdStr);
        } catch (NumberFormatException e) {
            response.put("message", "Invalid checkpoint ID format.");
            return ResponseEntity.badRequest().body(response);
        }

        Checkpoint checkpoint = checkpointService.getById(checkpointId);
        if (checkpoint == null) {
            response.put("message", "Checkpoint not found.");
            return ResponseEntity.badRequest().body(response);
        }

        String placeName = (String) checkpointRequest.get("placeName");
        String address = (String) checkpointRequest.get("address");
        String province = (String) checkpointRequest.get("province");
        String city = (String) checkpointRequest.get("city");
        String phone = (String) checkpointRequest.get("phone");
        String region = (String) checkpointRequest.get("region");

        if (region.equals("mien_bac")) {
            region = "Miền Bắc";
        } else if (region.equals("mien_trung")) {
            region = "Miền Trung";
        } else if (region.equals("mien_nam")) {
            region = "Miền Nam";
        } else {
            response.put("message", "Invalid region.");
            return ResponseEntity.badRequest().body(response);
        }

        // String validationMessage =
        // CheckpointValidation.validateCheckpointFields(placeName, address, province,
        // city,
        // phone, region);
        // if (validationMessage != null) {
        // response.put("message", validationMessage);
        // return ResponseEntity.badRequest().body(response);
        // }

        checkpoint.setPlaceName(placeName);
        checkpoint.setAddress(address);
        checkpoint.setProvince(province);
        checkpoint.setCity(city);
        checkpoint.setPhone(phone);
        checkpoint.setRegion(region);

        checkpointService.save(checkpoint);
        response.put("message", "Checkpoint updated successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete-checkpoint")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCheckpoint(@RequestBody Map<String, Object> checkpointRequest) {
        Map<String, Object> response = new HashMap<>();
        String checkpointIdStr = (String) checkpointRequest.get("checkpointId");
        int checkpointId;
        try {
            checkpointId = Integer.parseInt(checkpointIdStr);
        } catch (NumberFormatException e) {
            response.put("message", "Invalid checkpoint ID format.");
            return ResponseEntity.badRequest().body(response);
        }

        Checkpoint checkpoint = checkpointService.getById(checkpointId);
        if (checkpoint == null) {
            response.put("message", "Checkpoint not found.");
            return ResponseEntity.badRequest().body(response);
        }

        List<Route_Checkpoint> routeCheckpoints = routeCheckpointService.findByCheckpoint(checkpoint);
        if (!routeCheckpoints.isEmpty()) {
            response.put("message", "Checkpoint is used in a route and cannot be deleted.");
            return ResponseEntity.badRequest().body(response);
        }

        checkpointService.deleteById(checkpointId);
        response.put("message", "Checkpoint deleted successfully.");
        return ResponseEntity.ok(response);
    }

}
