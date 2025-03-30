package com.ray.archive.service.impl;

import com.ray.archive.entity.Archive;
import com.ray.archive.repository.ArchiveRepository;
import com.ray.archive.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ArchiveServiceImpl implements ArchiveService {

    private final ArchiveRepository archiveRepository;

    @Autowired
    public ArchiveServiceImpl(ArchiveRepository archiveRepository) {
        this.archiveRepository = archiveRepository;
    }

    @Override
    public Archive save(Archive archive) {
        return archiveRepository.save(archive);
    }

    @Override
    public Optional<Archive> findById(Long id) {
        return archiveRepository.findById(id);
    }

    @Override
    public Optional<Archive> findByFileNumber(String fileNumber) {
        return archiveRepository.findByFileNumber(fileNumber);
    }

    @Override
    public Page<Archive> findAll(Pageable pageable) {
        return archiveRepository.findAll(pageable);
    }

    @Override
    public List<Archive> findByCategory(String category) {
        return archiveRepository.findByCategory(category);
    }

    @Override
    public List<Archive> search(String keyword) {
        return archiveRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword);
    }

    @Override
    public void deleteById(Long id) {
        archiveRepository.deleteById(id);
    }

    @Override
    public boolean existsByFileNumber(String fileNumber) {
        return archiveRepository.findByFileNumber(fileNumber).isPresent();
    }

    @Override
    public List<Archive> findByStatus(String status) {
        return archiveRepository.findByStatus(status);
    }
} 