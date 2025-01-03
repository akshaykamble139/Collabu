package com.akshay.Collabu.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "repositories")
@Data
public class Repository_ {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<File> files;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Commit> commits;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Branch> branches;

    @Column(name = "default_branch", nullable = false)
    private String defaultBranch = "main";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = false)
    private String visibility = "public";

    @ManyToOne
    @JoinColumn(name = "forked_from_id")
    private Repository_ forkedFrom;

    @Column(name = "stars_count", nullable = false)
    private Long starsCount = 0L;

    @Column(name = "forks_count", nullable = false)
    private Long forksCount = 0L;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Repository_ that = (Repository_) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}