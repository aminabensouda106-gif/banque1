package com.banque.agence.config;

import com.banque.agence.domain.entity.Account;
import com.banque.agence.domain.entity.BillPayment;
import com.banque.agence.domain.entity.BillProvider;
import com.banque.agence.domain.entity.CheckbookOrder;
import com.banque.agence.domain.entity.Client;
import com.banque.agence.domain.entity.Transaction;
import com.banque.agence.domain.entity.User;
import com.banque.agence.domain.enums.AccountStatus;
import com.banque.agence.domain.enums.AccountType;
import com.banque.agence.domain.enums.CheckbookOrderStatus;
import com.banque.agence.domain.enums.CheckbookSheetCount;
import com.banque.agence.domain.enums.ClientStatus;
import com.banque.agence.domain.enums.TransactionType;
import com.banque.agence.repository.AccountRepository;
import com.banque.agence.repository.BillPaymentRepository;
import com.banque.agence.repository.BillProviderRepository;
import com.banque.agence.repository.CheckbookOrderRepository;
import com.banque.agence.repository.ClientRepository;
import com.banque.agence.repository.TransactionRepository;
import com.banque.agence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Jeu de données de démonstration réaliste (profil dev uniquement).
 * S'exécute une seule fois si aucun client n'existe encore en base.
 */
@Component
@Profile("dev")
@Order(2)
@ConditionalOnProperty(name = "banque.demo.seed-enabled", havingValue = "true")
public class DevDemoDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDemoDataInitializer.class);

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final BillProviderRepository billProviderRepository;
    private final BillPaymentRepository billPaymentRepository;
    private final CheckbookOrderRepository checkbookOrderRepository;
    private final UserRepository userRepository;
    private final DemoPortalSync demoPortalSync;

    public DevDemoDataInitializer(ClientRepository clientRepository,
                                  AccountRepository accountRepository,
                                  TransactionRepository transactionRepository,
                                  BillProviderRepository billProviderRepository,
                                  BillPaymentRepository billPaymentRepository,
                                  CheckbookOrderRepository checkbookOrderRepository,
                                  UserRepository userRepository,
                                  DemoPortalSync demoPortalSync) {
        this.clientRepository = clientRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.billProviderRepository = billProviderRepository;
        this.billPaymentRepository = billPaymentRepository;
        this.checkbookOrderRepository = checkbookOrderRepository;
        this.userRepository = userRepository;
        this.demoPortalSync = demoPortalSync;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (clientRepository.count() > 0) {
            return;
        }

        User agent = userRepository.findByUsername("agent")
                .orElseThrow(() -> new IllegalStateException("Utilisateur agent introuvable — lancer DevUserInitializer d'abord."));

        Client moncef = saveClient("CL-00001", "MB654321", "Moncef", "Bensouda",
                "moncef.bensouda@email.ma", "0611223344", "18 Bd Anfa, Casablanca", "Directeur commercial");
        Client abdellah = saveClient("CL-00002", "AI876543", "Abdellah", "Idrissi",
                "abdellah.idrissi@email.ma", "0622334455", "9 Rue Baghdad, Rabat", "Ingénieur informatique");
        Client karim = saveClient("CL-00003", "KA234567", "Karim", "El Amrani",
                "karim.elamrani@email.ma", "0633445566", "22 Av Mohammed VI, Marrakech", "Gérant restaurant");
        Client nadia = saveClient("CL-00004", "NB987654", "Nadia", "Bennani",
                "nadia.bennani@email.ma", "0644556677", "5 Rue Sebou, Fès", null);
        Client mehdi = saveClient("CL-00005", "MC123789", "Mehdi", "Chraibi",
                "mehdi.chraibi@email.ma", "0655667788", "14 Bd Pasteur, Tanger", null);

        Instant opened = Instant.now().minus(21, ChronoUnit.DAYS);

        Account moncefCourant = saveAccount("ACC-00001", moncef, AccountType.COURANT, opened.plus(1, ChronoUnit.DAYS));
        Account moncefEpargne = saveAccount("ACC-00002", moncef, AccountType.EPARGNE, opened.plus(2, ChronoUnit.DAYS));
        Account moncefPro = saveAccount("ACC-00003", moncef, AccountType.PROFESSIONNEL, opened.plus(3, ChronoUnit.DAYS));
        Account karimCourant = saveAccount("ACC-00004", karim, AccountType.COURANT, opened.plus(18, ChronoUnit.DAYS));
        Account nadiaCourant = saveAccount("ACC-00005", nadia, AccountType.COURANT, opened.plus(19, ChronoUnit.DAYS));
        Account mehdiCourant = saveAccount("ACC-00006", mehdi, AccountType.COURANT, opened.plus(20, ChronoUnit.DAYS));
        Account abdellahCourant = saveAccount("ACC-00007", abdellah, AccountType.COURANT, opened.plus(21, ChronoUnit.DAYS));

        Instant base = Instant.now().minus(21, ChronoUnit.DAYS);

        // Moncef Bensouda — client le plus aisé (~620 000 MAD au total)
        deposit(moncefCourant, bd("75000"), agent, base.plus(1, ChronoUnit.DAYS), "Versement initial — compte courant");
        deposit(moncefEpargne, bd("185000"), agent, base.plus(2, ChronoUnit.DAYS), "Placement épargne long terme");
        deposit(moncefPro, bd("320000"), agent, base.plus(3, ChronoUnit.DAYS), "Apport compte professionnel");
        transfer(moncefCourant, moncefEpargne, bd("15000"), agent, base.plus(5, ChronoUnit.DAYS), "Renfort épargne trimestriel");
        withdraw(moncefCourant, bd("2500"), agent, base.plus(7, ChronoUnit.DAYS), "Retrait espèces");
        payBill(moncefCourant, "LYDEC", "REF-LYDEC-2026-4412", bd("680"), agent, base.plus(9, ChronoUnit.DAYS));
        payBill(moncefCourant, "IAM", "REF-IAM-2026-1187", bd("299"), agent, base.plus(11, ChronoUnit.DAYS));
        deposit(moncefPro, bd("45000"), agent, base.plus(13, ChronoUnit.DAYS), "Encaissement honoraires");
        transfer(moncefPro, moncefCourant, bd("12000"), agent, base.plus(15, ChronoUnit.DAYS), "Virement vers compte courant");

        // Abdellah Idrissi — second profil portail, soldes confortables
        deposit(abdellahCourant, bd("22000"), agent, base.plus(4, ChronoUnit.DAYS), "Salaire et primes");
        deposit(abdellahCourant, bd("14500"), agent, base.plus(6, ChronoUnit.DAYS), "Épargne logement");
        withdraw(abdellahCourant, bd("800"), agent, base.plus(10, ChronoUnit.DAYS), "Retrait DAB");

        deposit(karimCourant, bd("15000"), agent, base.plus(4, ChronoUnit.DAYS), "Recettes activité");
        withdraw(karimCourant, bd("1200"), agent, base.plus(8, ChronoUnit.DAYS), "Paiement fournisseur");
        payBill(karimCourant, "ONEE", "REF-ONEE-88341", bd("420"), agent, base.plus(12, ChronoUnit.DAYS));

        deposit(nadiaCourant, bd("14500"), agent, base.plus(5, ChronoUnit.DAYS), "Versement guichet et épargne");
        withdraw(nadiaCourant, bd("300"), agent, base.plus(16, ChronoUnit.DAYS), "Retrait espèces");

        deposit(mehdiCourant, bd("4200"), agent, base.plus(6, ChronoUnit.DAYS), "Dépôt initial");
        withdraw(mehdiCourant, bd("150"), agent, base.plus(17, ChronoUnit.DAYS), "Petit retrait");

        saveCheckbookOrder("CHQ-00001", moncefCourant, moncef, 1, CheckbookSheetCount.FEUILLES_40,
                CheckbookOrderStatus.DELIVERED, agent, base.plus(10, ChronoUnit.DAYS),
                base.plus(12, ChronoUnit.DAYS), base.plus(15, ChronoUnit.DAYS), "Livraison agence Casablanca");
        saveCheckbookOrder("CHQ-00002", mehdiCourant, mehdi, 1, CheckbookSheetCount.FEUILLES_20,
                CheckbookOrderStatus.PENDING, agent, base.plus(18, ChronoUnit.DAYS),
                null, null, "Demande en attente de validation");

        demoPortalSync.enablePortal(moncef, DemoPortalSync.DEMO_PASSWORD);
        demoPortalSync.enablePortal(abdellah, DemoPortalSync.DEMO_PASSWORD);

        log.info("Données de démonstration chargées : 5 clients marocains, 7 comptes, "
                + "Moncef Bensouda (client principal), 2 accès portail client.");
    }

    private Client saveClient(String number, String cin, String firstName, String lastName,
                              String email, String phone, String address, String professionalInfo) {
        Client client = new Client();
        client.setClientNumber(number);
        client.setCin(cin);
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setEmail(email);
        client.setPhone(phone);
        client.setAddress(address);
        client.setProfessionalInfo(professionalInfo);
        client.setStatus(ClientStatus.ACTIVE);
        return clientRepository.save(client);
    }

    private Account saveAccount(String number, Client client, AccountType type, Instant openedAt) {
        Account account = new Account();
        account.setAccountNumber(number);
        account.setClient(client);
        account.setType(type);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(BigDecimal.ZERO);
        account.setOpenedAt(openedAt);
        return accountRepository.save(account);
    }

    private void deposit(Account account, BigDecimal amount, User agent, Instant at, String description) {
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        saveTransaction(TransactionType.DEPOT, amount, null, account, agent, at, description);
    }

    private void withdraw(Account account, BigDecimal amount, User agent, Instant at, String description) {
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        saveTransaction(TransactionType.RETRAIT, amount, account, null, agent, at, description);
    }

    private void transfer(Account source, Account destination, BigDecimal amount, User agent, Instant at, String description) {
        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));
        accountRepository.save(source);
        accountRepository.save(destination);
        saveTransaction(TransactionType.VIREMENT, amount, source, destination, agent, at, description);
    }

    private void payBill(Account account, String providerCode, String reference, BigDecimal amount,
                         User agent, Instant at) {
        BillProvider provider = billProviderRepository.findAll().stream()
                .filter(p -> providerCode.equals(p.getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Facturier " + providerCode + " introuvable."));

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction tx = saveTransaction(TransactionType.PAIEMENT_FACTURE, amount, account, null, agent, at,
                provider.getName() + " — réf. " + reference);

        BillPayment payment = new BillPayment();
        payment.setAccount(account);
        payment.setBillProvider(provider);
        payment.setClientReference(reference);
        payment.setAmount(amount);
        payment.setTransaction(tx);
        billPaymentRepository.save(payment);
    }

    private Transaction saveTransaction(TransactionType type, BigDecimal amount,
                                        Account source, Account destination,
                                        User agent, Instant at, String description) {
        Transaction tx = new Transaction();
        tx.setType(type);
        tx.setAmount(amount);
        tx.setSourceAccount(source);
        tx.setDestinationAccount(destination);
        tx.setExecutedBy(agent);
        tx.setExecutedAt(at);
        tx.setDescription(description);
        return transactionRepository.save(tx);
    }

    private void saveCheckbookOrder(String orderNumber, Account account, Client client, int quantity,
                                    CheckbookSheetCount sheetCount, CheckbookOrderStatus status,
                                    User agent, Instant requestedAt, Instant processedAt,
                                    Instant deliveredAt, String notes) {
        CheckbookOrder order = new CheckbookOrder();
        order.setOrderNumber(orderNumber);
        order.setAccount(account);
        order.setClient(client);
        order.setQuantity(quantity);
        order.setSheetCount(sheetCount);
        order.setStatus(status);
        order.setRequestedBy(agent);
        order.setNotes(notes);
        order.setRequestedAt(requestedAt);
        order.setProcessedAt(processedAt);
        order.setDeliveredAt(deliveredAt);
        checkbookOrderRepository.save(order);
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
