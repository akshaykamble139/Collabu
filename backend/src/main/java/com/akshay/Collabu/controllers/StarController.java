package com.akshay.Collabu.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.akshay.Collabu.dto.StarDTO;
import com.akshay.Collabu.services.StarService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/stars")
public class StarController {
    @Autowired
    private StarService starService;

    @GetMapping("/repository/{repositoryId}")
    public ResponseEntity<Integer> getStarCountByRepositoryId(@PathVariable Long repositoryId) {
        int count = starService.getStarCountByRepositoryId(repositoryId);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/toggle")
    public ResponseEntity<String> toggleStar(@RequestBody @Valid StarDTO starDTO, @AuthenticationPrincipal UserDetails userDetails) {
        StarDTO responseDTO = starService.toggleStar(userDetails.getUsername(),starDTO);
        return ResponseEntity.ok(responseDTO.getIsActive() ? "Starred" : "Unstarred");
    }
    
    @PostMapping("/status")
    public ResponseEntity<StarDTO> getStarStatus(@RequestBody @Valid StarDTO starDTO, @AuthenticationPrincipal UserDetails userDetails) {
        StarDTO starStatus = starService.getStarStatus(userDetails.getUsername(),starDTO);
        return ResponseEntity.ok(starStatus);
    }
    
}

