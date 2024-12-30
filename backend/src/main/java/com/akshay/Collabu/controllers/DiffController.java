package com.akshay.Collabu.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.akshay.Collabu.dto.DiffDTO;
import com.akshay.Collabu.services.DiffService;

@RestController
@RequestMapping("/api/diffs")
public class DiffController {
	
	@Autowired
    private DiffService diffService;

    @PostMapping
    public ResponseEntity<DiffDTO> createDiff(@RequestBody DiffDTO diffDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diffService.saveDiff(diffDTO));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<List<DiffDTO>> getDiffsByFile(@PathVariable Long fileId) {
        List<DiffDTO> diffs = diffService.getDiffsByFileId(fileId);
        return ResponseEntity.ok(diffs);
    }

    @GetMapping("/{fileId}/versions/{fromVersion}/{toVersion}")
    public ResponseEntity<DiffDTO> getDiff(
            @PathVariable Long fileId,
            @PathVariable int fromVersion,
            @PathVariable int toVersion
    ) {
        DiffDTO diffDTO = diffService.getDiff(fileId, fromVersion, toVersion);
        
        return ResponseEntity.ok(diffDTO);
    }
}

