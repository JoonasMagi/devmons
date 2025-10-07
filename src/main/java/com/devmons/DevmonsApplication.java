package com.devmons;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for DevMons Project Management System.
 * 
 * This application provides comprehensive project management functionality including:
 * - User authentication and authorization
 * - Project and issue tracking
 * - Kanban boards and sprint management
 * - Team collaboration features
 */
@SpringBootApplication
public class DevmonsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevmonsApplication.class, args);
    }
}

