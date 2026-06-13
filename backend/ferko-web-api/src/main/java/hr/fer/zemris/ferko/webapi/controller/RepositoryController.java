package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.repository.RepositoryService;
import hr.fer.zemris.ferko.application.usecase.repository.RepositoryViews;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Course file repository ("Repozitorij"): list and download for anyone authenticated; teaching
 * staff upload. Binary content is kept by the file-storage adapter.
 */
@RestController
@RequestMapping("/api/v1/academic")
public class RepositoryController {

  private static final String CAN_MANAGE =
      "hasAnyRole('ADMIN', 'NOSITELJ', 'NASTAVNIK', 'ASISTENT_ORGANIZATOR', 'ASISTENT')";

  private final RepositoryService repositoryService;

  public RepositoryController(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }

  @GetMapping("/courses/{courseId}/files")
  public List<RepositoryViews.FileView> list(@PathVariable long courseId) {
    return repositoryService.list(courseId);
  }

  @PostMapping("/courses/{courseId}/files")
  @PreAuthorize(CAN_MANAGE)
  @ResponseStatus(HttpStatus.CREATED)
  public UploadedResponse upload(
      @PathVariable long courseId,
      @RequestParam("file") MultipartFile file,
      Authentication authentication) {
    try {
      long id =
          repositoryService.upload(
              courseId,
              file.getOriginalFilename(),
              file.getContentType(),
              file.getBytes(),
              authentication.getName());
      return new UploadedResponse(id);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
  }

  @GetMapping("/files/{fileId}/download")
  public ResponseEntity<Resource> download(@PathVariable long fileId) {
    RepositoryViews.DownloadedFile file =
        repositoryService
            .download(fileId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Datoteka ne postoji."));
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
        .contentType(MediaType.parseMediaType(file.contentType()))
        .body(new ByteArrayResource(file.content()));
  }

  public record UploadedResponse(long id) {}
}
