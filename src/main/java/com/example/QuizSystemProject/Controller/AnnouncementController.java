package com.example.QuizSystemProject.Controller;

import com.example.QuizSystemProject.Model.Announcement;
import com.example.QuizSystemProject.Model.Admin;
import com.example.QuizSystemProject.Model.User;
import com.example.QuizSystemProject.Repository.AnnouncementRepository;
import com.example.QuizSystemProject.Repository.UserRepository;
import com.example.QuizSystemProject.security.CustomUserDetails;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

  
    public AnnouncementController(AnnouncementRepository announcementRepository, 
                                UserRepository userRepository) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new announcement (Admin only)
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<?> createAnnouncement(@RequestBody Announcement announcement) {
        try {
            // Get authentication from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Log authentication details for debugging
            System.out.println("Creating announcement. Authenticated user: " + authentication.getName());
            System.out.println("Authentication principal class: " + authentication.getPrincipal().getClass().getName());
            System.out.println("Authorities: " + authentication.getAuthorities());

            // Get the current user from the security context
            User currentUser = null;
            
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                // Get the user from our CustomUserDetails
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                currentUser = userRepository.findById(userDetails.getId())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userDetails.getId()));
            } else {
                // Fallback to username lookup if not using CustomUserDetails
                String username = authentication.getName();
                currentUser = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            }

            // Debug: Print user info
            System.out.println("Current user: " + currentUser.getUsername() + " (ID: " + currentUser.getId() + ")");
            
            // Set the user and save the announcement
            announcement.setUser(currentUser);
            announcement.setDate(LocalDateTime.now());

            // Save the announcement
            Announcement savedAnnouncement = announcementRepository.save(announcement);
            System.out.println("Announcement created successfully with ID: " + savedAnnouncement.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(savedAnnouncement);

        } catch (UsernameNotFoundException e) {
            System.err.println("User not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Kullanıcı bulunamadı: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error creating announcement: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Duyuru oluşturulurken bir hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Get all announcements (public)
     */
    @GetMapping
    public ResponseEntity<List<Announcement>> getAllAnnouncements() {
        List<Announcement> announcements = announcementRepository.findAll();
        return ResponseEntity.ok(announcements);
    }

    /**
     * Test authentication and user roles
     */
    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuth() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Log authentication details
            System.out.println("\n=== Test Auth Endpoint ===");
            System.out.println("Authenticated user: " + authentication.getName());
            System.out.println("Principal class: " + authentication.getPrincipal().getClass().getName());
            System.out.println("Authorities: " + authentication.getAuthorities());
            
            // Get the user from database
            User currentUser = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + authentication.getName()));
                    
            System.out.println("User from DB - ID: " + currentUser.getId() + ", Username: " + currentUser.getUsername());
            System.out.println("User class: " + currentUser.getClass().getName());
            System.out.println("User role: " + currentUser.getRole());
            System.out.println("Is instance of Admin: " + (currentUser instanceof Admin));
            
            return ResponseEntity.ok("Check server logs for authentication details");
                    
        } catch (Exception e) {
            System.err.println("Error in test-auth: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Get a specific announcement by ID (public)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAnnouncement(@PathVariable int id) {
        return announcementRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete an announcement (only the owner can delete their own announcements)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<?> deleteAnnouncement(@PathVariable int id) {
        try {
            // Get the current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + authentication.getName()));

            // Find the announcement
            Announcement announcement = announcementRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Duyuru bulunamadı"));

            // Check if the current user is the owner of the announcement or an admin
            if (announcement.getUser().getId() != currentUser.getId() && 
                !currentUser.getRole().equals("ROLE_ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Bu duyuruyu silme yetkiniz yok");
            }

            // Delete the announcement
            announcementRepository.delete(announcement);
            return ResponseEntity.ok("Duyuru başarıyla silindi");

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Duyuru silinirken bir hata oluştu: " + e.getMessage());
        }
    }
}
