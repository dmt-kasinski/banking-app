package com.dmt.bankingapp.springTests.entitiesTests;

import com.dmt.bankingapp.entity.Account;
import com.dmt.bankingapp.entity.Account.AccountType;
import com.dmt.bankingapp.entity.Transaction;
import com.dmt.bankingapp.entity.Client;
import com.dmt.bankingapp.entity.Loan;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Transactional
public class TransactionTests {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void testNewTransaction() {
        //Arrange
        String giverClient = "clientFirst";
        String receiverClient = "clientSecond";

        Client client1 = new Client(giverClient, false, "test123");
        Client client2 = new Client(receiverClient, false, "SuperCoolTest");

        String giverAccountNumber = "222111222";
        String receiverAccountNumber = "111333999";

        Account giverAccount = new Account(giverAccountNumber, AccountType.DEPOSIT, client1);
        Account receiverAccount = new Account(receiverAccountNumber, AccountType.DEPOSIT, client2);
        double initialAmount = 500.0;
        giverAccount.setAccountBalance(initialAmount, false);
        entityManager.persist(giverAccount);
        entityManager.persist(receiverAccount);

        //Act
        double transactionAmount = 300.0;
        Transaction transaction = new Transaction(giverAccount, receiverAccount, transactionAmount);
        entityManager.persist(transaction);

        Account foundGiverAccount = entityManager.find(Account.class, giverAccount.getAccountID());
        Account foundReceiverAccount = entityManager.find(Account.class, receiverAccount.getAccountID());

        //Assert
        assertThat(foundGiverAccount).isNotNull();
        assertThat(foundReceiverAccount).isNotNull();

        assertEquals(initialAmount - transactionAmount, foundGiverAccount.getAccountBalance());
        assertEquals(transactionAmount, foundReceiverAccount.getAccountBalance());
    }

    @Test
    public void testDateAndTime(){
        //Arrange
        String giverClient = "clientFirst";
        String receiverClient = "clientSecond";

        Client client1 = new Client(giverClient, false, "test123");
        Client client2 = new Client(receiverClient, false, "SuperCoolTest");

        String giverAccountNumber = "222111222";
        String receiverAccountNumber = "111333999";

        Account giverAccount = new Account(giverAccountNumber, AccountType.DEPOSIT, client1);
        Account receiverAccount = new Account(receiverAccountNumber, AccountType.DEPOSIT, client2);
        double amount = 500.0;
        giverAccount.setAccountBalance(amount, false);
        entityManager.persist(giverAccount);
        entityManager.persist(receiverAccount);

        double amountInTransaction = 300.0;
        Transaction transaction = new Transaction(giverAccount, receiverAccount, amountInTransaction);
        entityManager.persist(transaction);
        String now = LocalDateTime.now().toString().split("\\.")[0];

        //Act
        Transaction transaction1 = entityManager.find(Transaction.class, transaction.getTransactionID());

        //LocalDateTime look like YYYY-MM-DD{timezone}HH:MM:SS.{9 decimal points for milisecond}
        //so I am comparing whole string before coma
        String timestampFromTransactionRecord = transaction1.getTimestamp().toString().split("\\.")[0];

        //Assert
        assertEquals(now, timestampFromTransactionRecord);
    }
    
    @Test
    public void testTransactionGreaterThanLoan() {
        // Arrange
        Client loanTaker = new Client("loanTaker", false, "09876");
        Client bankClient = new Client("bankSTERS", false, "12345");

        entityManager.persist(loanTaker);
        entityManager.persist(bankClient);

        Account loanAccount = new Account("loanAccNum", AccountType.LOAN, loanTaker);
        Account checkingAccount = new Account("checkingAccNum", AccountType.CHECKING, loanTaker);
        Account bankAccount = new Account("bankAccNum", AccountType.BANK, bankClient);
        
        double checkingAccountBalance = 999999.99;
        checkingAccount.setAccountBalance(checkingAccountBalance, false);
        entityManager.persist(loanAccount);
        entityManager.persist(checkingAccount);
        entityManager.persist(bankAccount);

        double principalAmount = 20000.0;
        double interestRate = 3.8;
        double commisionRate = 5.0;
        int loanDuration = 48;

        // Act - loan
        Loan testLoan = new Loan(loanAccount, checkingAccount, principalAmount, interestRate, commisionRate, loanDuration, bankAccount);
        entityManager.persist(testLoan);

        loanAccount.setLoan(testLoan);
        entityManager.persist(loanAccount);

        Loan foundLoan = entityManager.find(Loan.class, testLoan.getLoanID());
        double foundLoanTotalAmount = foundLoan.getTotalLoanAmount();
        double overpay = 999.99;
        double initialTransferAmount = foundLoanTotalAmount + overpay;

        // Act - transaction
        Transaction testTransaction = new Transaction(checkingAccount, loanAccount, initialTransferAmount);
        entityManager.persist(testTransaction);

        Transaction foundTransaction = entityManager.find(Transaction.class, testTransaction.getTransactionID());
        double foundTransferAmount = foundTransaction.getAmount();

        // Assert
        assertThat(foundTransaction).isNotNull();
        assertNotEquals(initialTransferAmount, foundTransferAmount);
        assertEquals(initialTransferAmount, foundTransferAmount + overpay, 0.01);
        assertEquals(checkingAccountBalance - foundTransferAmount, checkingAccountBalance - initialTransferAmount + overpay, 0.01);
    }

    @Test
    public void testTransactionToAccountWithPaidLoan() {
        // Arrange
        Client loanTaker = new Client("loanTaker", false, "09876");
        Client bankClient = new Client("bankSTERS", false, "12345");

        entityManager.persist(loanTaker);
        entityManager.persist(bankClient);

        Account loanAccount = new Account("loanAccNum", AccountType.LOAN, loanTaker);
        Account checkingAccount = new Account("checkingAccNum", AccountType.CHECKING, loanTaker);
        Account bankAccount = new Account("bankAccNum", AccountType.BANK, bankClient);
        
        double checkingAccountBalance = 999999.99;
        checkingAccount.setAccountBalance(checkingAccountBalance, false);
        entityManager.persist(loanAccount);
        entityManager.persist(checkingAccount);
        entityManager.persist(bankAccount);

        double principalAmount = 20000.0;
        double interestRate = 3.8;
        double commisionRate = 5.0;
        int loanDuration = 48;

        // Act - loan
        Loan testLoan = new Loan(loanAccount, checkingAccount, principalAmount, interestRate, commisionRate, loanDuration, bankAccount);
        entityManager.persist(testLoan);

        loanAccount.setLoan(testLoan);
        entityManager.persist(loanAccount);

        Loan foundLoan = entityManager.find(Loan.class, testLoan.getLoanID());
        double foundLoanTotalAmount = foundLoan.getTotalLoanAmount();

        // Act - first transaction to redeem the loan
        Transaction firstTransaction = new Transaction(checkingAccount, loanAccount, foundLoanTotalAmount);
        entityManager.persist(firstTransaction);

        // Act - second transaction that should throw exception
        double secondTransfer = 0.01;
        IllegalStateException exceptionThrown = assertThrows(IllegalStateException.class, () -> {
            new Transaction(checkingAccount, loanAccount, secondTransfer);
        });

        // Assert
        assertEquals("You cannot transfer money to this loan account since the loan has already been paid!", exceptionThrown.getMessage());
    }

    @Test
    public void testTransactionGreaterThanBalance() {
        // Arrange
        Client customer1 = new Client("customer1", false, "09876");
        Client customer2 = new Client("customer2", false, "12345");

        entityManager.persist(customer1);
        entityManager.persist(customer2);

        Account checkingAccount1 = new Account("checkingAccNum1", AccountType.CHECKING, customer1);
        Account checkingAccount2 = new Account("checkingAccNum2", AccountType.CHECKING, customer2);
        
        double checkingAccountBalance1 = 99.99;
        checkingAccount1.setAccountBalance(checkingAccountBalance1, false);
        entityManager.persist(checkingAccount1);

        // Act
        double testTransfer = 100;
        IllegalStateException exceptionThrown = assertThrows(IllegalStateException.class, () -> {
            new Transaction(checkingAccount1, checkingAccount2, testTransfer);
        });

        // Assert
        assertEquals("You cannot transfer more money than you have on the account!", exceptionThrown.getMessage());
    }
}