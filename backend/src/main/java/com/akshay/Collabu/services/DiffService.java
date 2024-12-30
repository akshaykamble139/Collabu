package com.akshay.Collabu.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.akshay.Collabu.dto.DiffDTO;
import com.akshay.Collabu.models.Diff;
import com.akshay.Collabu.models.File;
import com.akshay.Collabu.repositories.DiffRepository;
import com.akshay.Collabu.repositories.FileRepository;

@Service
public class DiffService {
	
	@Autowired
    private DiffRepository diffRepository;
	
	@Autowired
    private FileRepository fileRepository;
	
	private DiffDTO mapToDTO(Diff diff) {
        DiffDTO dto = new DiffDTO();
        dto.setId(diff.getId());
        dto.setFileId(diff.getFile().getId());
        dto.setFromVersion(diff.getVersionFrom());
        dto.setToVersion(diff.getVersionTo());
        dto.setDiffContent(diff.getDiffContent());
        return dto;
    }

	public DiffDTO saveDiff(DiffDTO diffDTO) {
        Diff diff = new Diff();
        File file = fileRepository.findById(diffDTO.getFileId())
                .orElseThrow(() -> new RuntimeException("File not found"));
        diff.setFile(file);
        
        diff.setVersionFrom(diffDTO.getFromVersion());
        diff.setVersionTo(diffDTO.getToVersion());
        diff.setDiffContent(diffDTO.getDiffContent());

        Diff savedDiff = diffRepository.save(diff);

        return mapToDTO(savedDiff);
    }

    public List<DiffDTO> getDiffsByFileId(Long fileId) {
    	List<Diff> diffs = diffRepository.findByFileId(fileId);
        return diffs.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public DiffDTO getDiff(Long fileId, int fromVersion, int toVersion) {
        Diff diff = diffRepository.findByFileIdAndVersionFromAndVersionTo(fileId, fromVersion, toVersion)
        		.orElseThrow(() -> new RuntimeException("Diff not found"));
        
        return mapToDTO(diff);
    }
}

