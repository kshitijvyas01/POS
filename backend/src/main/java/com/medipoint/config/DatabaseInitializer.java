package com.medipoint.config;

import com.medipoint.model.User;
import com.medipoint.model.UserRole;
import com.medipoint.model.UserStatus;
import com.medipoint.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final UserRepository userRepository;
    private final com.medipoint.repository.MedicineRepository medicineRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.initial-owner.email:owner@medipoint.com}")
    private String initialOwnerEmail;

    @Value("${app.initial-owner.password:owner123}")
    private String initialOwnerPassword;

    @Value("${app.initial-owner.name:Store Owner}")
    private String initialOwnerName;

    @Value("${app.initial-owner.phone:+91 9876543210}")
    private String initialOwnerPhone;

    public DatabaseInitializer(UserRepository userRepository,
                               com.medipoint.repository.MedicineRepository medicineRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.medicineRepository = medicineRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initializeInitialOwner();
        initializeInitialWorker();
        seedInitialMedicines();
    }

    private void initializeInitialOwner() {
        String email = initialOwnerEmail.trim().toLowerCase();

        if (userRepository.findByEmailIgnoreCase(email).isEmpty()) {
            logger.info("Initializing primary Store Owner account [{}] in MySQL database...", email);

            User owner = new User(
                    initialOwnerName,
                    email,
                    initialOwnerPhone,
                    passwordEncoder.encode(initialOwnerPassword),
                    UserRole.OWNER,
                    UserStatus.ACTIVE
            );

            userRepository.save(owner);
            logger.info("Primary Store Owner [{}] successfully provisioned in MySQL.", email);
        } else {
            logger.info("Store Owner [{}] already exists in MySQL. Skipping initialization.", email);
        }
    }

    private void initializeInitialWorker() {
        String workerEmail = "worker@medipoint.com";

        if (userRepository.findByEmailIgnoreCase(workerEmail).isEmpty()) {
            logger.info("Initializing default Pharmacy Worker account [{}] in MySQL database for testing...", workerEmail);

            User worker = new User(
                    "Pharmacy Worker",
                    workerEmail,
                    "+91 9876543211",
                    passwordEncoder.encode("worker123"),
                    UserRole.WORKER,
                    UserStatus.ACTIVE
            );

            userRepository.save(worker);
            logger.info("Pharmacy Worker [{}] successfully provisioned in MySQL.", workerEmail);
        } else {
            logger.info("Pharmacy Worker [{}] already exists in MySQL.", workerEmail);
        }
    }

    private void seedInitialMedicines() {
        if (medicineRepository.count() == 0) {
            logger.info("Seeding initial fictional pharmacy medicines in MySQL database for testing...");
            java.time.LocalDate today = java.time.LocalDate.now();

            java.util.List<com.medipoint.model.Medicine> demoMedicines = java.util.List.of(
                new com.medipoint.model.Medicine(
                    "Paracetamol 500mg", "Acetaminophen", "Crocin Advance", "Pain Relief", "GSK Healthcare",
                    "PCM-2024-A1", today.minusMonths(6), today.plusMonths(18),
                    new java.math.BigDecimal("15.00"), new java.math.BigDecimal("28.00"), 120, 20,
                    "Strip", "SUPP-001", "Apollo Med Logistics", "Analgesic and antipyretic tablets", "ACTIVE"
                ),
                new com.medipoint.model.Medicine(
                    "Azithromycin 500mg", "Azithromycin IP", "Azee 500", "Antibiotic", "Cipla Ltd",
                    "AZI-2024-B2", today.minusMonths(3), today.plusMonths(21),
                    new java.math.BigDecimal("72.00"), new java.math.BigDecimal("119.50"), 45, 15,
                    "Strip", "SUPP-002", "MedPlus Distributors", "Macrolide antibiotic 3-day course", "ACTIVE"
                ),
                new com.medipoint.model.Medicine(
                    "Cetirizine 10mg", "Cetirizine Dihydrochloride", "Cetzine", "Allergy", "Dr. Reddy's",
                    "CET-2024-C3", today.minusMonths(4), today.plusMonths(14),
                    new java.math.BigDecimal("12.00"), new java.math.BigDecimal("24.00"), 85, 25,
                    "Strip", "SUPP-001", "Apollo Med Logistics", "Second-generation antihistamine", "ACTIVE"
                ),
                new com.medipoint.model.Medicine(
                    "Vitamin C 500mg", "Ascorbic Acid IP", "Limcee", "Vitamin", "Abbott Healthcare",
                    "VIT-2024-D4", today.minusMonths(2), today.plusMonths(10),
                    new java.math.BigDecimal("18.00"), new java.math.BigDecimal("35.00"), 60, 20,
                    "Strip", "SUPP-003", "National Pharma Distributors", "Chewable Orange Vitamin C", "ACTIVE"
                ),
                new com.medipoint.model.Medicine(
                    "ORS Sachet", "Oral Rehydration Salts IP", "Electral", "First Aid", "FDC Limited",
                    "ORS-2024-E5", today.minusMonths(1), today.plusMonths(24),
                    new java.math.BigDecimal("16.50"), new java.math.BigDecimal("22.50"), 150, 30,
                    "Sachet", "SUPP-003", "National Pharma Distributors", "WHO standard rehydration formula", "ACTIVE"
                ),
                new com.medipoint.model.Medicine(
                    "Pantoprazole 40mg", "Pantoprazole Sodium", "Pan 40", "Digestive", "Alkem Laboratories",
                    "PAN-2024-F6", today.minusMonths(5), today.plusMonths(16),
                    new java.math.BigDecimal("85.00"), new java.math.BigDecimal("145.00"), 8, 15,
                    "Strip", "SUPP-002", "MedPlus Distributors", "Proton-pump inhibitor for acid reflux (LOW STOCK demo)", "ACTIVE"
                ),
                new com.medipoint.model.Medicine(
                    "Ibuprofen 400mg", "Ibuprofen IP", "Brufen 400", "Pain Relief", "Abbott Healthcare",
                    "IBU-2024-G7", today.minusMonths(8), today.plusDays(15),
                    new java.math.BigDecimal("9.50"), new java.math.BigDecimal("18.00"), 35, 10,
                    "Strip", "SUPP-001", "Apollo Med Logistics", "NSAID pain relief tablets (EXPIRING SOON demo)", "ACTIVE"
                ),
                new com.medipoint.model.Medicine(
                    "Amoxicillin 250mg", "Amoxicillin Trihydrate", "Novamox", "Antibiotic", "Cipla Ltd",
                    "AMX-2023-H8", today.minusMonths(18), today.minusDays(5),
                    new java.math.BigDecimal("45.00"), new java.math.BigDecimal("75.00"), 12, 10,
                    "Strip", "SUPP-002", "MedPlus Distributors", "Broad-spectrum penicillin antibiotic (EXPIRED demo)", "ACTIVE"
                ),
                new com.medipoint.model.Medicine(
                    "Dextromethorphan Syrup", "Dextromethorphan HBr", "Benadryl DR", "Cold & Cough", "Johnson & Johnson",
                    "DXM-2024-J9", today.minusMonths(2), today.plusMonths(12),
                    new java.math.BigDecimal("80.00"), new java.math.BigDecimal("125.00"), 0, 10,
                    "Bottle", "SUPP-003", "National Pharma Distributors", "Dry cough relief syrup (OUT OF STOCK demo)", "ACTIVE"
                )
            );

            medicineRepository.saveAll(demoMedicines);
            logger.info("Successfully seeded {} initial pharmacy medicines into MySQL.", demoMedicines.size());
        } else {
            logger.info("Medicines already present in MySQL (count: {}). Skipping seed.", medicineRepository.count());
        }
    }
}
