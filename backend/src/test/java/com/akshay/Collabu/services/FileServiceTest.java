package com.akshay.Collabu.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;

import com.akshay.Collabu.dto.FileDTO;
import com.akshay.Collabu.models.File;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.models.User;
import com.akshay.Collabu.repositories.FileRepository;
import com.akshay.Collabu.repositories.RepositoryRepository;

class FileServiceTest {
	public static final Logger logger = LoggerFactory.getLogger(FileServiceTest.class);

    @Mock
    private FileRepository fileRepository;
    
    @Mock
    private RepositoryRepository repositoryRepository;

    @InjectMocks
    private FileService fileService;
    
    @Mock
    private FileCacheService fileCacheService;
    
    @Mock 
    private CacheService cacheService;

	private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        this.userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("test")
                .password("testing")
                .roles("USER") 
                .build();
    }

    @Test
    void testGetFileById() {
        File file = new File();
        file.setId(1L);
        file.setName("README.md");
        User user = new User();
        user.setId(1L);
        user.setEmail("don@gmail.com");
        user.setUsername("don123");
        user.setRole("ROLE_USER");
        user.setIsActive(true);
        
        Repository_ repo = new Repository_();
        repo.setId(1L);
        repo.setName("TestRepo");
        repo.setDescription("Description");
        repo.setOwner(user);
        repo.setDefaultBranch("master");
        repo.setIsDeleted(false);
        repo.setStarsCount(0L);
        repo.setForksCount(0L);
        
        file.setRepository(repo);

        when(fileRepository.findById(anyLong())).thenReturn(Optional.of(file));

        FileDTO fileDTO = new FileDTO();
        fileDTO.setName("README.md");
        fileDTO.setId(1L);
        fileDTO.setPath("/");
        fileDTO.setRepositoryName(repo.getName());
        when(fileCacheService.mapEntityToDTO(any())).thenReturn(fileDTO);
        FileDTO result = fileService.getFileById(1L);
        assertEquals("README.md", result.getName());
    }
        
    @Test
    void testGetFilesByRepositoryId() {
        File file = new File();
        file.setId(1L);
        file.setName("README.md");
        
        User user = new User();
        user.setId(1L);
        user.setEmail("don@gmail.com");
        user.setUsername("don123");
        user.setRole("ROLE_USER");
        user.setIsActive(true);
        
        Repository_ repo = new Repository_();
        repo.setId(1L);
        repo.setName("TestRepo");
        repo.setDescription("Description");
        repo.setOwner(user);
        repo.setDefaultBranch("master");
        repo.setIsDeleted(false);
        repo.setStarsCount(0L);
        repo.setForksCount(0L);
        
        file.setRepository(repo);
        
        List<File> files = new ArrayList<>();
        files.add(file);    
        
        FileDTO fileDTO = new FileDTO();
        fileDTO.setName("README.md");
        fileDTO.setId(1L);
        fileDTO.setPath("/");
        fileDTO.setRepositoryName(repo.getName());
        when(fileCacheService.mapEntityToDTO(any())).thenReturn(fileDTO);

        when(fileRepository.findByRepositoryId(anyLong())).thenReturn(files);

        List<FileDTO> result = fileService.getFilesByRepositoryId(1L);
        assertEquals("README.md", result.get(0).getName());
    }
        
    @Test
    void testCreateFile1() {
        File file = new File();
        file.setId(1L);
        file.setName("README.md");
        
        User user = new User();
        user.setId(1L);
        user.setEmail("don@gmail.com");
        user.setUsername("don123");
        user.setRole("ROLE_USER");
        user.setIsActive(true);
        
        Repository_ repo = new Repository_();
        repo.setId(1L);
        repo.setName("TestRepo");
        repo.setDescription("Description");
        repo.setOwner(user);
        repo.setDefaultBranch("master");
        repo.setIsDeleted(false);
        repo.setStarsCount(0L);
        repo.setForksCount(0L);
        
        file.setRepository(repo);      
        
        
        FileDTO fileDTO = new FileDTO();
        fileDTO.setName("README.md");
        fileDTO.setId(1L);
        fileDTO.setPath("/");
        fileDTO.setRepositoryName(repo.getName());
        when(fileCacheService.mapEntityToDTO(any())).thenReturn(fileDTO);

        when(repositoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        try {
        	MockMultipartFile firstFile = new MockMultipartFile("data", "README.md", "text/plain", "some xml".getBytes());
        	fileService.createFile(fileDTO,firstFile,userDetails);
        }
        catch (Exception e) {
			logger.info("Repository doesn't exists");
        }
    }
    
    @Test
    void testCreateFile2() {
        File file = new File();
        file.setId(1L);
        file.setName("README.md");
        file.setPath("/");
        
        User user = new User();
        user.setId(1L);
        user.setEmail("don@gmail.com");
        user.setUsername("don123");
        user.setRole("ROLE_USER");
        user.setIsActive(true);
        
        Repository_ repo = new Repository_();
        repo.setId(1L);
        repo.setName("TestRepo");
        repo.setDescription("Description");
        repo.setOwner(user);
        repo.setDefaultBranch("master");
        repo.setIsDeleted(false);
        repo.setStarsCount(0L);
        repo.setForksCount(0L);
        
        file.setRepository(repo);      
        
        
        FileDTO fileDTO = new FileDTO();
        fileDTO.setName("README.md");
        fileDTO.setId(1L);
        fileDTO.setPath("/");
        fileDTO.setRepositoryName(repo.getName());
        when(fileCacheService.mapEntityToDTO(any())).thenReturn(fileDTO);

        when(repositoryRepository.findById(anyLong())).thenReturn(Optional.of(repo));
        
        when(fileRepository.existsByNameAndPathAndBranchId(anyString(),anyString(),anyLong())).thenReturn(Boolean.TRUE);
        
        try {
            MockMultipartFile firstFile = new MockMultipartFile("data", "README.md", "text/plain", "some xml".getBytes());
        	fileService.createFile(fileDTO,firstFile,userDetails);
        }
        catch (Exception e) {
			logger.info("File name already exists for this path in given repository");
        }
    }
    
    @Test
    void testCreateFile3() {
        File file = new File();
        file.setId(1L);
        file.setName("README.md");
        file.setPath("/");
        
        User user = new User();
        user.setId(1L);
        user.setEmail("don@gmail.com");
        user.setUsername("don123");
        user.setRole("ROLE_USER");
        user.setIsActive(true);
        
        Repository_ repo = new Repository_();
        repo.setId(1L);
        repo.setName("TestRepo");
        repo.setDescription("Description");
        repo.setOwner(user);
        repo.setDefaultBranch("master");
        repo.setIsDeleted(false);
        repo.setStarsCount(0L);
        repo.setForksCount(0L);
        
        file.setRepository(repo);      
        
        FileDTO fileDTO = new FileDTO();
        fileDTO.setName("README.md");
        fileDTO.setId(1L);
        fileDTO.setPath("/");
        fileDTO.setRepositoryName(repo.getName());
        when(fileCacheService.mapEntityToDTO(any())).thenReturn(fileDTO);
        
		when(repositoryRepository.findById(anyLong())).thenReturn(Optional.of(repo));
        
        when(fileRepository.existsByNameAndPathAndBranchId(anyString(),anyString(),anyLong())).thenReturn(false);
        
        when(fileRepository.save(any())).thenReturn(file);       
        when(cacheService.getRepositoryId(anyString())).thenReturn(1L);

        MockMultipartFile firstFile = new MockMultipartFile("data", "filename.txt", "text/plain", "some xml".getBytes());
        fileService.createFile(fileDTO,firstFile, userDetails);
    }

    @Test
	void testDeleteFile() {
	                    
	    when(fileRepository.existsById(anyLong())).thenReturn(false);
	    
	    try {
	    	fileService.deleteFile(1L);
	    }
	    catch (Exception e) {
			logger.info("Branch doesn't exist for id: {}", 1L);
		}
	
	    when(fileRepository.existsById(anyLong())).thenReturn(true);
	
		fileService.deleteFile(1L);       
	}
        
    @Test
    void testUpdateFile() {
        File file = new File();
        file.setId(1L);
        file.setName("README.md");
        file.setPath("/");
        
        User user = new User();
        user.setId(1L);
        user.setEmail("don@gmail.com");
        user.setUsername("don123");
        user.setRole("ROLE_USER");
        user.setIsActive(true);
        
        Repository_ repo = new Repository_();
        repo.setId(1L);
        repo.setName("TestRepo");
        repo.setDescription("Description");
        repo.setOwner(user);
        repo.setDefaultBranch("master");
        repo.setIsDeleted(false);
        repo.setStarsCount(0L);
        repo.setForksCount(0L);
        
        file.setRepository(repo);      
        
        FileDTO fileDTO = new FileDTO();
        fileDTO.setName("README.md");
        fileDTO.setId(1L);
        fileDTO.setPath("/");
        fileDTO.setRepositoryName(repo.getName());
        when(fileCacheService.mapEntityToDTO(any())).thenReturn(fileDTO);
        
		when(fileRepository.findById(anyLong())).thenReturn(Optional.empty());
		try {
	    	fileService.updateFile(fileDTO);
	    }
	    catch (Exception e) {
			logger.info("Branch doesn't exist for id: {}", 1L);
		}
		
		when(fileRepository.findById(anyLong())).thenReturn(Optional.of(file));
                
        when(fileRepository.save(any())).thenReturn(file);       
        
        FileDTO result = fileService.updateFile(fileDTO);
        
        assertEquals("README.md", result.getName());
    }
    
    
    
}