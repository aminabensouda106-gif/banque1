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
 * Jeu de données de démonstration (profil dev uniquement).
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

        Client ahmed = saveClient("CL-00001", "CD789012", "Ahmed", "Benali",
                "ahmed.benali@email.ma", "0612345678", "12 Rue Hassan II, Casablanca", null);
        Client fatima = saveClient("CL-00002", "CD456789", "Fatima", "Alaoui",
                "fatima.alaoui@email.ma", "0623456789", "45 Bd Zerktouni, Marrakech", "Commerçante");
        Client youssef = saveClient("CL-00003", "BE123456", "Youssef", "Idrissi",
                "youssef.idrissi@email.ma", "0634567890", "8 Av Mohammed V, Rabat", null);
        Client khadija = saveClient("CL-00004", "BE987654", "Khadija", "Tazi",
                "khadija.tazi@email.ma", "0645678901", "3 Rue Allal Ben Abdellah, Fès", null);
        Client omar = saveClient("CL-00005", "BE654321", "Omar", "Berrada",
                "omar.berrada@email.ma", "0656789012", "27 Rue Ibn Khaldoun, Tanger", null);

        Account ahmedCourant = saveAccount("ACC-00001", ahmed, AccountType.COURANT);
        Account ahmedEpargne = saveAccount("ACC-00002", ahmed, AccountType.EPARGNE);
        Account fatimaPro = saveAccount("ACC-00003", fatima, AccountType.PROFESSIONNEL);
        Account youssefCourant = saveAccount("ACC-00004", youssef, AccountType.COURANT);
        Account youssefEpargne = saveAccount("ACC-00005", youssef, AccountType.EPARGNE);
        Account khadijaCourant = saveAccount("ACC-00006", khadija, AccountType.COURANT);
        Account omarCourant = saveAccount("ACC-00007", omar, AccountType.COURANT);
        Account omarEpargne = saveAccount("ACC-00008", omar, AccountType.EPARGNE);

        Instant base = Instant.now().minus(14, ChronoUnit.DAYS);

        deposit(ahmedCourant, bd("10000"), agent, base.plus(1, ChronoUnit.DAYS), "Versement initial démo");
        withdraw(ahmedCourant, bd("500"), agent, base.plus(2, ChronoUnit.DAYS), "Retrait espèces");
        transfer(ahmedCourant, ahmedEpargne, bd("1000"), agent, base.plus(3, ChronoUnit.DAYS), "Épargne mensuelle");
        deposit(fatimaPro, bd("1500"), agent, base.plus(3, ChronoUnit.DAYS), "Apport professionnel");
        deposit(youssefCourant, bd("5000"), agent, base.plus(4, ChronoUnit.DAYS), "Salaire");
        withdraw(youssefCourant, bd("200"), agent, base.plus(5, ChronoUnit.DAYS), "Retrait DAB");
        transfer(youssefCourant, youssefEpargne, bd("800"), agent, base.plus(6, ChronoUnit.DAYS), "Transfert épargne");
        deposit(khadijaCourant, bd("2000"), agent, base.plus(6, ChronoUnit.DAYS), "Dépôt guichet");
        deposit(omarCourant, bd("1000"), agent, base.plus(7, ChronoUnit.DAYS), "Versement");
        deposit(omarEpargne, bd("800"), agent, base.plus(7, ChronoUnit.DAYS), "Placement épargne");
        transfer(omarCourant, omarEpargne, bd("300"), agent, base.plus(8, ChronoUnit.DAYS), "Renfort épargne");
        withdraw(khadijaCourant, bd("150"), agent, base.plus(9, ChronoUnit.DAYS), "Retrait");
        payBill(ahmedCourant, "LYDEC", "REF-LYDEC-78451", bd("250"), agent, base.plus(10, ChronoUnit.DAYS));
        payBill(youssefCourant, "IAM", "REF-IAM-99210", bd("150"), agent, base.plus(11, ChronoUnit.DAYS));
        deposit(fatimaPro, bd("500"), agent, base.plus(11, ChronoUnit.DAYS), "Encaissement");
        withdraw(omarCourant, bd("100"), agent, base.plus(12, ChronoUnit.DAYS), "Petit retrait");
        transfer(youssefEpargne, youssefCourant, bd("400"), agent, base.plus(12, ChronoUnit.DAYS), "Réapprovisionnement courant");
        deposit(ahmedCourant, bd("300"), agent, base.plus(13, ChronoUnit.DAYS), "Dépôt complémentaire");
        withdraw(ahmedEpargne, bd("50"), agent, base.plus(13, ChronoUnit.DAYS), "Retrait épargne");
        deposit(khadijaCourant, bd("200"), agent, base.plus(14, ChronoUnit.DAYS), "Versement fin de mois");

        saveCheckbookOrder("CHQ-00001", ahmedCourant, ahmed, 1, CheckbookSheetCount.FEUILLES_20,
                CheckbookOrderStatus.DELIVERED, agent, base.plus(9, ChronoUnit.DAYS),
                base.plus(10, ChronoUnit.DAYS), base.plus(12, ChronoUnit.DAYS), "Livraison agence");
        saveCheckbookOrder("CHQ-00002", youssefCourant, youssef, 2, CheckbookSheetCount.FEUILLES_40,
                CheckbookOrderStatus.PENDING, agent, base.plus(13, ChronoUnit.DAYS),
                null, null, "Demande en cours");

        demoPortalSync.enablePortal(ahmed, DemoPortalSync.DEMO_PASSWORD);
        demoPortalSync.enablePortal(youssef, DemoPortalSync.DEMO_PASSWORD);

        log.info("Données de démonstration chargées : 5 clients, 8 comptes, 20 transactions, "
                + "2 paiements facture, 2 commandes chéquier, 2 accès portail client.");
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

    private Account saveAccount(String number, Client client, AccountType type) {
        Account account = new Account();
        account.setAccountNumber(number);
        account.setClient(client);
        account.setType(type);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(BigDecimal.ZERO);
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
