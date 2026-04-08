package com.temple.config;

import com.temple.entity.Admin;
import com.temple.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(999)  // ✅ Run LAST after all beans initialized
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public void run(String... args) {
        // ✅ Try multiple times to handle slow DB initialization
        int maxRetries = 5;
        for (int i = 0; i < maxRetries; i++) {
            try {
                if (adminRepository.count() == 0) {
                    Admin admin = new Admin();
                    admin.setUsername("admin");
                    admin.setPassword("admin123");
                    admin.setName("Temple Administrator");
                    adminRepository.save(admin);
                    System.out.println("✅ Default admin created: admin / admin123");
                } else {
                    System.out.println("✅ Admin already exists.");
                }
                return; // ✅ Success - exit loop
            } catch (Exception e) {
                System.err.println("⚠️ DB not ready, retrying " + (i+1) + "/" + maxRetries + ": " + e.getMessage());
                try {
                    Thread.sleep(3000); // Wait 3 seconds before retry
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        System.err.println("❌ Could not initialize admin after " + maxRetries + " retries.");
    }
}