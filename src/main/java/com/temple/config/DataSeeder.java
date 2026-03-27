package com.temple.config;

import com.temple.entity.Admin;
import com.temple.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public void run(String... args) throws Exception {
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
    }
}