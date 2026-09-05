package com.veteran;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String dbHash = "$2a$10$u2PY9lP143jir6.I6pmL1OOzNb9n3kau8CtCZeJbSV0O.kT1TjP5.";
        boolean matches = encoder.matches("admin123", dbHash);
        System.out.println("admin123 matches: " + matches);

        // Generate new hashes
        String newAdmin = encoder.encode("admin123");
        String newReviewer = encoder.encode("reviewer123");
        System.out.println("New admin hash: " + newAdmin);
        System.out.println("New reviewer hash: " + newReviewer);
    }
}
