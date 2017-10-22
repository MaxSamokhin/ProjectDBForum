package ru.max.forumDb.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/service")
public class ServiceController {

    private ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @PostMapping("/clear")
    public ResponseEntity<?> clearDatabase() {
        serviceService.clear();

        return ResponseEntity.status(HttpStatus.OK).body("clear!");
    }

    @GetMapping("/status")
    public ResponseEntity<?> getInfoDatabase() {
        SeviceModel service = serviceService.status();

        return ResponseEntity.status(HttpStatus.OK).body(service.getJson().toString());
    }
}

