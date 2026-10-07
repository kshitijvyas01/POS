package com.medipoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MediPointApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediPointApplication.class, args);
        System.out.println("\n========================================================");
        System.out.println("  MediPoint - Smart Medical Store POS & Management System");
        System.out.println("  Phase 1 Backend Services Started Successfully!");
        System.out.println("========================================================\n");
    }
}
