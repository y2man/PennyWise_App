package com.pennywise.ui.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.pennywise.ui.PennyWiseApp;
import com.pennywise.ui.model.*;
import com.pennywise.ui.service.ApiClient;
import com.pennywise.ui.util.SessionStore;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;

public class DashboardController {

    // Sidebar nav
    @FXML
    private Button navDashboard, navAccounts, navAnalytics;
    @FXML
    private Button navIncome, navExpenses, navTransfers;
    @FXML
    private Button navBudget, navSavings, navDebt;
    @FXML
    private Button navCategories, navAdd;

    // User labels
    @FXML
    private Label sidebarInitials, sidebarUserName, sidebarUserEmail;

    // Sections
    @FXML
    private VBox sectionDashboard, sectionAccounts, sectionAnalytics;
    @FXML
    private VBox sectionIncome, sectionExpenses, sectionTransfers;
    @FXML
    private VBox sectionBudget, sectionSavings, sectionDebt;
    @FXML
    private VBox sectionCategories, sectionAdd;

    // Dashboard stats
    @FXML
    private Label balanceLabel, incomeLabel, expenseLabel, savedLabel;
    @FXML
    private Label cardBalLabel, cashBalLabel, savBalLabel, totalBalLabel;

    // Tx list
    @FXML
    private VBox txList, txEmptyState;
    @FXML
    private Label txCount;

    // Quick add
    @FXML
    private ToggleButton quickIncomeBtn, quickExpenseBtn;
    @FXML
    private TextField quickText, quickAmount;
    @FXML
    private DatePicker quickDate;
    @FXML
    private ComboBox<String> quickAccount, quickCategory;

    // Month filter
    @FXML
    private ComboBox<String> monthFilter;

    // Planning widgets
    @FXML
    private VBox dashBudgetList, dashSavingsList, dashDebtList;

    // Accounts section
    @FXML
    private Label cardBalFull, cardIncomeLabel, cardExpenseLabel;
    @FXML
    private Label cashBalFull, cashIncomeLabel, cashExpenseLabel;
    @FXML
    private Label savBalFull, savIncomeLabel, savExpenseLabel;
    @FXML
    private ComboBox<String> tfFrom, tfTo;
    @FXML
    private TextField tfAmount, tfNote;
    @FXML
    private DatePicker tfDate;

    // Income section
    @FXML
    private Label incomeTotalLabel, incomeCountLabel;
    @FXML
    private VBox incomeList;
    @FXML
    private TextField incomeSearch;

    // Expense section
    @FXML
    private Label expenseTotalLabel, expenseCountLabel;
    @FXML
    private VBox expenseList;
    @FXML
    private TextField expenseSearch;

    // Transfer section
    @FXML
    private Label transferTotalLabel, transferCountLabel;
    @FXML
    private VBox transferList;

    // Budget section
    @FXML
    private Label budgetTotalLabel, budgetRemainingLabel;
    @FXML
    private FlowPane budgetCards;
    @FXML
    private VBox budgetEmpty;

    // Savings section
    @FXML
    private Label savingsTotalLabel, savingsCountLabel;
    @FXML
    private FlowPane savingsCards;
    @FXML
    private VBox savingsEmpty;

    // Debt section
    @FXML
    private Label debtTotalLabel, debtPaidLabel;
    @FXML
    private FlowPane debtCards;
    @FXML
    private VBox debtEmpty;
    @FXML
    private javafx.scene.layout.HBox debtReminderBanner;

    // Categories
    @FXML
    private FlowPane incomeChipBox, expenseChipBox;
    @FXML
    private TextField newIncomeCategory, newExpenseCategory;

    // Page title
    @FXML
    private Label pageTitle;

    // Add Transaction section (full-page form)
    @FXML
    private ToggleButton incomeBtn2, expenseBtn2;
    @FXML
    private TextField text2, amount2;
    @FXML
    private DatePicker date2;
    @FXML
    private ComboBox<String> account2, category2;
    private String addType2 = "income";

    // Data
    private List<Transaction> transactions = new ArrayList<>();
    private List<Budget> budgets = new ArrayList<>();
    private List<SavingsGoal> goals = new ArrayList<>();
    private List<Debt> debts = new ArrayList<>();
    private List<AccountTransfer> transfers = new ArrayList<>();

    private List<String> incCats = new ArrayList<>(List.of("Salary", "Freelance", "Investment", "Business", "Bonus"));
    private List<String> expCats = new ArrayList<>(List.of("Food", "Transport", "Bills", "Shopping", "Health", "Entertainment"));

    private String quickType = "income";

    private record PendingExpense(String text, BigDecimal amount, String account, String category) {

    }
    private PendingExpense pending = null;

    @FXML
    public void initialize() {
        incCats = new ArrayList<>(SessionStore.getIncomeCategories(incCats));
        expCats = new ArrayList<>(SessionStore.getExpenseCategories(expCats));
        loadCachedDashboardState();
        configureHistoryList(txList);
        configureHistoryList(transferList);
        setL(sidebarInitials, SessionStore.getInitials());
        setL(sidebarUserName, SessionStore.getName());
        setL(sidebarUserEmail, SessionStore.getEmail());

        if (quickDate != null) {
            quickDate.setValue(LocalDate.now());
        }
        if (tfDate != null) {
            tfDate.setValue(LocalDate.now());
        }

        // Month filter
        if (monthFilter != null) {
            monthFilter.getItems().add("All time");
            LocalDate now = LocalDate.now();
            for (int i = 0; i < 12; i++) {
                monthFilter.getItems().add(now.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM")));
            }
            monthFilter.setValue("All time");
            monthFilter.setOnAction(e -> loadAll());
        }

        // Quick type
        if (quickIncomeBtn != null) {
            quickIncomeBtn.setSelected(true);
            quickIncomeBtn.setOnAction(e -> {
                quickType = "income";
                loadCats();
            });
        }
        if (quickExpenseBtn != null) {
            quickExpenseBtn.setOnAction(e -> {
                quickType = "expense";
                loadCats();
            });
        }

        // Account combos
        if (quickAccount != null) {
            quickAccount.getItems().addAll("card", "cash", "savings");
            quickAccount.setValue("card");
        }

        if (tfFrom != null) {
            tfFrom.getItems().addAll("card", "cash", "savings");
            tfFrom.setValue("card");
        }
        if (tfTo != null) {
            tfTo.getItems().addAll("card", "cash", "savings");
            tfTo.setValue("savings");
        }

        // Add Transaction section (sectionAdd) — account2/category2
        if (account2 != null) {
            account2.getItems().addAll("card", "cash", "savings");
            account2.setValue("card");
        }
        if (date2 != null) {
            date2.setValue(LocalDate.now());
        }
        if (incomeBtn2 != null) {
            incomeBtn2.setSelected(true);
            incomeBtn2.setOnAction(e -> {
                addType2 = "income";
                loadCats2();
            });
        }
        if (expenseBtn2 != null) {
            expenseBtn2.setOnAction(e -> {
                addType2 = "expense";
                loadCats2();
            });
        }
        loadCats2();

        if (incomeSearch != null) {
            incomeSearch.textProperty().addListener((o, a, b) -> filterIncome(b));
        }
        if (expenseSearch != null) {
            expenseSearch.textProperty().addListener((o, a, b) -> filterExpense(b));
        }

        loadCats();
        renderCategories();
        showSection("dashboard");

        // navigateTo() now guarantees it runs on the FX thread, and SessionStore
        // is set before navigateTo is called, so we can call loadAll() directly.
        // No delay needed — if token is missing, redirect to login immediately.
        if (!com.pennywise.ui.util.SessionStore.isLoggedIn()) {
            System.err.println("[Dashboard] No session — redirecting to login");
            com.pennywise.ui.PennyWiseApp.navigateTo("login");
            return;
        }
        loadAll();
        // Debug helper: add a few dummy rows to verify rendering
        try {
            if (txList != null && txList.getChildren().isEmpty()) {
                Transaction d1 = new Transaction();
                d1.setText("DEBUG Tx 1");
                d1.setAmount(new java.math.BigDecimal("12.34"));
                d1.setAccount("card");
                d1.setCategory("Salary");
                d1.setDate(java.time.LocalDate.now());
                Transaction d2 = new Transaction();
                d2.setText("DEBUG Tx 2");
                d2.setAmount(new java.math.BigDecimal("-5.00"));
                d2.setAccount("cash");
                d2.setCategory("Food");
                d2.setDate(java.time.LocalDate.now());
                Transaction d3 = new Transaction();
                d3.setText("DEBUG Tx 3");
                d3.setAmount(new java.math.BigDecimal("7.00"));
                d3.setAccount("savings");
                d3.setCategory("Bonus");
                d3.setDate(java.time.LocalDate.now());
                txList.getChildren().add(makeTxRow(d1));
                txList.getChildren().add(makeTxRow(d2));
                txList.getChildren().add(makeTxRow(d3));
            }
        } catch (Exception ex) {
            System.err.println("[debugRows] failed to add debug rows: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ── Navigation ──
    private void showSection(String id) {
        List<VBox> all = new ArrayList<>();
        for (VBox v : new VBox[]{sectionDashboard, sectionAccounts, sectionAnalytics, sectionIncome, sectionExpenses,
            sectionTransfers, sectionBudget, sectionSavings, sectionDebt, sectionCategories, sectionAdd}) {
            if (v != null) {
                v.setVisible(false);
                v.setManaged(false);
                all.add(v);
            }
        }
        VBox active = switch (id) {
            case "accounts" ->
                sectionAccounts;
            case "analytics" ->
                sectionAnalytics;
            case "income" ->
                sectionIncome;
            case "expenses" ->
                sectionExpenses;
            case "transfers" ->
                sectionTransfers;
            case "budget" ->
                sectionBudget;
            case "savings" ->
                sectionSavings;
            case "debt" ->
                sectionDebt;
            case "categories" ->
                sectionCategories;
            case "add" ->
                sectionAdd;
            default ->
                sectionDashboard;
        };
        if (active != null) {
            active.setVisible(true);
            active.setManaged(true);
        }
        Map<String, String> titles = Map.of("dashboard", "Dashboard", "accounts", "Accounts", "analytics", "Analytics",
                "income", "Income", "expenses", "Expenses", "transfers", "Transfers",
                "budget", "Budget", "savings", "Savings Goals", "debt", "Debt");
        setL(pageTitle, titles.getOrDefault(id, id));

        List<Button> btns = new ArrayList<>();
        for (Button b : new Button[]{navDashboard, navAccounts, navAnalytics, navIncome, navExpenses,
            navTransfers, navBudget, navSavings, navDebt, navCategories, navAdd}) {
            if (b != null) {
                b.getStyleClass().remove("nav-active");
                btns.add(b);
            }
        }
        Button ab = switch (id) {
            case "accounts" ->
                navAccounts;
            case "analytics" ->
                navAnalytics;
            case "income" ->
                navIncome;
            case "expenses" ->
                navExpenses;
            case "transfers" ->
                navTransfers;
            case "budget" ->
                navBudget;
            case "savings" ->
                navSavings;
            case "debt" ->
                navDebt;
            case "categories" ->
                navCategories;
            case "add" ->
                navAdd;
            default ->
                navDashboard;
        };
        if (ab != null) {
            ab.getStyleClass().add("nav-active");
        }

        if ("accounts".equals(id)) {
            renderAccounts();
        }
        if ("income".equals(id)) {
            renderIncome();
        }
        if ("expenses".equals(id)) {
            renderExpenses();
        }
        if ("transfers".equals(id)) {
            renderTransfers();
        }
        if ("budget".equals(id)) {
            renderBudget();
        }
        if ("savings".equals(id)) {
            renderSavings();
        }
        if ("debt".equals(id)) {
            renderDebt();
        }
        if ("categories".equals(id)) {
            renderCategories();
        }
        if ("analytics".equals(id)) {
            renderAnalytics();
        }
    }

    @FXML
    public void navToDashboard() {
        showSection("dashboard");
    }

    @FXML
    public void navToAccounts() {
        showSection("accounts");
    }

    @FXML
    public void navToAnalytics() {
        showSection("analytics");
    }

    @FXML
    public void navToIncome() {
        showSection("income");
    }

    @FXML
    public void navToExpenses() {
        showSection("expenses");
    }

    @FXML
    public void navToTransfers() {
        showSection("transfers");
        loadAll();
    }

    @FXML
    public void navToBudget() {
        showSection("budget");
    }

    @FXML
    public void navToSavings() {
        showSection("savings");
    }

    @FXML
    public void navToDebt() {
        showSection("debt");
    }

    @FXML
    public void navToCategories() {
        showSection("categories");
    }

    @FXML
    public void navToAdd() {
        showSection("add");
    }

    // ── Load all ──
    private void loadAll() {
        // Guard: ensure we have a valid token before making API calls
        if (!com.pennywise.ui.util.SessionStore.isLoggedIn()) {
            System.err.println("[Dashboard] Session expired — returning to login");
            Platform.runLater(() -> {
                com.pennywise.ui.util.SessionStore.clear();
                com.pennywise.ui.PennyWiseApp.navigateTo("login");
            });
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String month = (monthFilter != null && !"All time".equals(monthFilter.getValue())) ? monthFilter.getValue() : null;
                String txPath = month != null ? "/transactions?month=" + month : "/transactions";
                JsonNode txNode = ApiClient.get(txPath);
                transactions = ApiClient.mapper.convertValue(txNode, new TypeReference<>() {
                });
                System.err.println("[loadAll] fetched transactions: " + (transactions != null ? transactions.size() : 0));
                if (transactions != null && !transactions.isEmpty()) {
                    try {
                        System.err.println("[loadAll] sample tx: " + ApiClient.mapper.writeValueAsString(transactions.get(0)));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (Exception e) {
                System.err.println("[loadAll] transactions: " + e.getMessage());
            }

            try {
                JsonNode bNode = ApiClient.get("/budgets");
                budgets = ApiClient.mapper.convertValue(bNode, new TypeReference<>() {
                });
            } catch (Exception e) {
                System.err.println("[loadAll] budgets: " + e.getMessage());
            }

            try {
                JsonNode gNode = ApiClient.get("/savings-goals");
                goals = ApiClient.mapper.convertValue(gNode, new TypeReference<>() {
                });
            } catch (Exception e) {
                System.err.println("[loadAll] savings-goals: " + e.getMessage());
            }

            try {
                JsonNode dNode = ApiClient.get("/debts");
                debts = ApiClient.mapper.convertValue(dNode, new TypeReference<>() {
                });
            } catch (Exception e) {
                System.err.println("[loadAll] debts: " + e.getMessage());
            }

            try {
                JsonNode trNode = ApiClient.get("/transfers");
                transfers = ApiClient.mapper.convertValue(trNode, new TypeReference<>() {
                });
                if (transfers == null || transfers.isEmpty()) {
                    transfers = deriveTransfersFromTransactions();
                }
                System.err.println("[loadAll] fetched transfers: " + (transfers != null ? transfers.size() : 0));
                if (transfers != null && !transfers.isEmpty()) {
                    try {
                        System.err.println("[loadAll] sample tr: " + ApiClient.mapper.writeValueAsString(transfers.get(0)));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (Exception e) {
                System.err.println("[loadAll] transfers: " + e.getMessage());
                transfers = deriveTransfersFromTransactions();
            }

            Platform.runLater(this::refreshAll);
            persistDashboardState();
        });
    }

    private void loadCachedDashboardState() {
        transactions = readCachedList("txCache", new TypeReference<List<Transaction>>() {
        }, transactions);
        budgets = readCachedList("budgetCache", new TypeReference<List<Budget>>() {
        }, budgets);
        goals = readCachedList("goalsCache", new TypeReference<List<SavingsGoal>>() {
        }, goals);
        debts = readCachedList("debtCache", new TypeReference<List<Debt>>() {
        }, debts);
        transfers = readCachedList("transferCache", new TypeReference<List<AccountTransfer>>() {
        }, transfers);
    }

    private <T> List<T> readCachedList(String key, TypeReference<List<T>> typeRef, List<T> fallback) {
        String raw = SessionStore.getScopedData(key);
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>(fallback);
        }
        try {
            List<T> loaded = ApiClient.mapper.readValue(raw, typeRef);
            return new ArrayList<>(loaded != null ? loaded : fallback);
        } catch (Exception e) {
            System.err.println("[cache] Failed to read " + key + ": " + e.getMessage());
            return new ArrayList<>(fallback);
        }
    }

    private void persistDashboardState() {
        persistList("txCache", transactions);
        persistList("budgetCache", budgets);
        persistList("goalsCache", goals);
        persistList("debtCache", debts);
        persistList("transferCache", transfers);
        SessionStore.setIncomeCategories(incCats);
        SessionStore.setExpenseCategories(expCats);
    }

    private void persistList(String key, Object value) {
        try {
            SessionStore.setScopedData(key, ApiClient.mapper.writeValueAsString(value));
        } catch (Exception e) {
            System.err.println("[cache] Failed to write " + key + ": " + e.getMessage());
        }
    }

    private void configureHistoryList(VBox list) {
        if (list == null) {
            return;
        }
        list.setFillWidth(true);
        list.setSpacing(0);
    }

    private List<AccountTransfer> deriveTransfersFromTransactions() {
        Map<String, List<Transaction>> grouped = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            if (!isTransferTransaction(t) || t.getDate() == null || t.getAmount() == null) {
                continue;
            }
            String key = t.getDate() + "|" + t.getAmount().abs().stripTrailingZeros().toPlainString();
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }

        List<AccountTransfer> fallback = new ArrayList<>();
        for (List<Transaction> pair : grouped.values()) {
            if (pair.isEmpty()) {
                continue;
            }
            Transaction debit = pair.stream().filter(Transaction::isExpense).findFirst().orElse(null);
            Transaction credit = pair.stream().filter(Transaction::isIncome).findFirst().orElse(null);
            Transaction primary = debit != null ? debit : (credit != null ? credit : pair.get(0));
            AccountTransfer transfer = new AccountTransfer();
            transfer.setAmount(primary.getAmount() != null ? primary.getAmount().abs() : BigDecimal.ZERO);
            transfer.setDate(primary.getDate());
            transfer.setFromAccount(debit != null && debit.getAccount() != null ? debit.getAccount() : primary.getAccount());
            transfer.setToAccount(credit != null && credit.getAccount() != null ? credit.getAccount() : primary.getAccount());
            transfer.setNote(buildTransferNote(transfer.getFromAccount(), transfer.getToAccount()));
            fallback.add(transfer);
        }
        fallback.sort(Comparator.comparing(AccountTransfer::getDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return fallback;
    }

    private boolean isTransferTransaction(Transaction t) {
        return t != null && t.getCategory() != null && "transfer".equalsIgnoreCase(t.getCategory());
    }

    private String buildTransferNote(String from, String to) {
        String fromLabel = from != null && !from.isBlank() ? cap(from) : "From";
        String toLabel = to != null && !to.isBlank() ? cap(to) : "To";
        return fromLabel + " -> " + toLabel;
    }

    private void refreshAll() {
        runRender("dashboard", this::renderDashboard);
        runRender("accounts", this::renderAccounts);
        runRender("income", this::renderIncome);
        runRender("expenses", this::renderExpenses);
        runRender("transfers", this::renderTransfers);
        runRender("budget", this::renderBudget);
        runRender("savings", this::renderSavings);
        runRender("debt", this::renderDebt);
    }

    // ── Dashboard ──
    private void renderDashboard() {
        BigDecimal inc = BigDecimal.ZERO, exp = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (t.isIncome()) {
                inc = inc.add(t.getAmount());
            } else {
                exp = exp.add(t.getAmount().abs());

            }
        }
        BigDecimal net = inc.subtract(exp).max(BigDecimal.ZERO);
        setL(balanceLabel, "$" + fmt(net));
        setL(incomeLabel, "$" + fmt(inc));
        setL(expenseLabel, "$" + fmt(exp));
        updateAccountStrip();
        renderTxList();

        BigDecimal saved = goals.stream().map(g -> g.getSaved() != null ? g.getSaved() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        setL(savedLabel, "$" + fmt(saved));
        renderBudgetWidget();
        renderSavingsWidget();
        renderDebtWidget();
    }

    private void runRender(String section, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            System.err.println("[render] " + section + " failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderAnalytics() {
        if (sectionAnalytics == null) {
            return;
        }
        sectionAnalytics.getChildren().clear();

        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(18, 20, 18, 20));

        Label title = new Label("Overview");
        title.getStyleClass().add("card-title");

        HBox row1 = new HBox(12);
        row1.getChildren().addAll(
                makeAnalyticsTile("Income", "$" + fmt(transactions.stream().filter(Transaction::isIncome).map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)), "color-green"),
                makeAnalyticsTile("Expenses", "$" + fmt(transactions.stream().filter(Transaction::isExpense).map(t -> t.getAmount().abs()).reduce(BigDecimal.ZERO, BigDecimal::add)), "color-red"),
                makeAnalyticsTile("Transactions", String.valueOf(transactions.size()), "color-gold")
        );

        HBox row2 = new HBox(12);
        row2.getChildren().addAll(
                makeAnalyticsTile("Budgets", String.valueOf(budgets.size()), "color-blue"),
                makeAnalyticsTile("Savings Goals", String.valueOf(goals.size()), "color-purple"),
                makeAnalyticsTile("Debts", String.valueOf(debts.size()), "color-red")
        );

        card.getChildren().addAll(title, row1, row2);
        sectionAnalytics.getChildren().add(card);
    }

    private VBox makeAnalyticsTile(String label, String value, String colorClass) {
        VBox tile = new VBox(4);
        tile.getStyleClass().add("stat-card");
        tile.setPadding(new Insets(14, 16, 14, 16));
        tile.setPrefWidth(180);
        Label l1 = new Label(label);
        l1.getStyleClass().add("stat-label");
        Label l2 = new Label(value);
        l2.getStyleClass().addAll("stat-value", colorClass);
        tile.getChildren().addAll(l1, l2);
        return tile;
    }

    private void updateAccountStrip() {
        setL(cardBalLabel, "$" + fmt(accBal("card")));
        setL(cashBalLabel, "$" + fmt(accBal("cash")));
        setL(savBalLabel, "$" + fmt(accBal("savings")));
        setL(totalBalLabel, "$" + fmt(accBal("card").add(accBal("cash")).add(accBal("savings"))));
    }

    private BigDecimal accBal(String acc) {
        return transactions.stream().filter(t -> acc.equals(t.getAccount()))
                .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).max(BigDecimal.ZERO);
    }

    private BigDecimal accBalRaw(String acc) {
        return transactions.stream().filter(t -> acc.equals(t.getAccount()))
                .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void renderTxList() {
        if (txList == null) {
            return;
        }
        txList.getChildren().clear();
        List<Transaction> recent = transactions.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Transaction::getDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(10)
                .toList();
        if (txEmptyState != null) {
            txEmptyState.setVisible(recent.isEmpty());
            txEmptyState.setManaged(recent.isEmpty());
        }
        if (txCount != null) {
            txCount.setText(recent.size() + " items");
        }
        for (Transaction t : recent) {
            try {
                txList.getChildren().add(makeTxRow(t));
            } catch (Exception e) {
                System.err.println("[txList] Skipping malformed transaction: " + e.getMessage());
                e.printStackTrace();
                txList.getChildren().add(makeSimpleTxRow(t));
            }
        }
    }

    // ── Accounts ──
    private void renderAccounts() {
        for (String acc : new String[]{"card", "cash", "savings"}) {
            BigDecimal bal = accBal(acc);
            BigDecimal in = transactions.stream().filter(t -> acc.equals(t.getAccount()) && t.isIncome()).map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal ex = transactions.stream().filter(t -> acc.equals(t.getAccount()) && t.isExpense()).map(t -> t.getAmount().abs()).reduce(BigDecimal.ZERO, BigDecimal::add);
            switch (acc) {
                case "card" -> {
                    setL(cardBalFull, "$" + fmt(bal));
                    setL(cardIncomeLabel, "$" + fmt(in));
                    setL(cardExpenseLabel, "$" + fmt(ex));
                }
                case "cash" -> {
                    setL(cashBalFull, "$" + fmt(bal));
                    setL(cashIncomeLabel, "$" + fmt(in));
                    setL(cashExpenseLabel, "$" + fmt(ex));
                }
                case "savings" -> {
                    setL(savBalFull, "$" + fmt(bal));
                    setL(savIncomeLabel, "$" + fmt(in));
                    setL(savExpenseLabel, "$" + fmt(ex));
                }
            }
        }
    }

    @FXML
    public void handleTransfer() {
        if (tfFrom == null || tfTo == null || tfAmount == null || tfDate == null) {
            return;
        }
        String from = tfFrom.getValue(), to = tfTo.getValue(), amtStr = tfAmount.getText().trim();
        if (from.equals(to)) {
            showInfo("Error", "Choose different accounts");
            return;
        }
        if (amtStr.isEmpty()) {
            showInfo("Error", "Enter an amount");
            return;
        }
        if (tfDate.getValue() == null) {
            showInfo("Error", "Select a date");
            return;
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(amtStr);
        } catch (Exception e) {
            showInfo("Error", "Invalid amount");
            return;
        }
        BigDecimal src = accBalRaw(from);
        if (amount.compareTo(src) > 0) {
            showInfo("Insufficient Balance", cap(from) + " balance: $" + fmt(src.max(BigDecimal.ZERO)));
            return;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("fromAccount", from);
        body.put("toAccount", to);
        body.put("amount", amount);
        body.put("date", tfDate.getValue().toString());
        body.put("note", tfNote != null ? tfNote.getText().trim() : "");
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JsonNode result = ApiClient.post("/transfers", body);
                AccountTransfer savedTransfer = ApiClient.mapper.convertValue(result, AccountTransfer.class);
                Platform.runLater(() -> {
                    Transaction debit = new Transaction();
                    debit.setText("Transfer to " + cap(to));
                    debit.setAmount(amount.negate());
                    debit.setCategory("Transfer");
                    debit.setAccount(from);
                    debit.setDate(tfDate.getValue());

                    Transaction credit = new Transaction();
                    credit.setText("Transfer from " + cap(from));
                    credit.setAmount(amount);
                    credit.setCategory("Transfer");
                    credit.setAccount(to);
                    credit.setDate(tfDate.getValue());

                    transactions.add(0, credit);
                    transactions.add(0, debit);
                    if (savedTransfer != null) {
                        transfers.removeIf(t -> Objects.equals(t.getId(), savedTransfer.getId()));
                        transfers.add(0, savedTransfer);
                    }
                    if (tfAmount != null) {
                        tfAmount.clear();

                    }
                    refreshAll();
                    persistDashboardState();
                    loadAll();
                    showInfo("Success", "Transferred $" + fmt(amount));
                });
            } catch (Exception e) {
                Platform.runLater(() -> showInfo("Error", e.getMessage()));
            }
        });
    }

    // ── Income / Expenses ──
    private void renderIncome() {
        List<Transaction> data = transactions.stream().filter(Transaction::isIncome).sorted(Comparator.comparing(Transaction::getDate).reversed()).toList();
        BigDecimal tot = data.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        setL(incomeTotalLabel, "$" + fmt(tot));
        setL(incomeCountLabel, String.valueOf(data.size()));
        if (incomeList != null) {
            incomeList.getChildren().clear();
            data.forEach(t -> incomeList.getChildren().add(makeTableRow(t, "income")));
        }
    }

    private void renderExpenses() {
        List<Transaction> data = transactions.stream().filter(Transaction::isExpense).sorted(Comparator.comparing(Transaction::getDate).reversed()).toList();
        BigDecimal tot = data.stream().map(t -> t.getAmount().abs()).reduce(BigDecimal.ZERO, BigDecimal::add);
        setL(expenseTotalLabel, "$" + fmt(tot));
        setL(expenseCountLabel, String.valueOf(data.size()));
        if (expenseList != null) {
            expenseList.getChildren().clear();
            data.forEach(t -> expenseList.getChildren().add(makeTableRow(t, "expense")));
        }
    }

    private void filterIncome(String q) {
        if (incomeList == null) {
            return;

        }
        incomeList.getChildren().clear();
        transactions.stream().filter(Transaction::isIncome)
                .filter(t -> t.getText().toLowerCase().contains(q.toLowerCase()))
                .sorted(Comparator.comparing(Transaction::getDate).reversed())
                .forEach(t -> incomeList.getChildren().add(makeTableRow(t, "income")));
    }

    private void filterExpense(String q) {
        if (expenseList == null) {
            return;

        }
        expenseList.getChildren().clear();
        transactions.stream().filter(Transaction::isExpense)
                .filter(t -> t.getText().toLowerCase().contains(q.toLowerCase()))
                .sorted(Comparator.comparing(Transaction::getDate).reversed())
                .forEach(t -> expenseList.getChildren().add(makeTableRow(t, "expense")));
    }

    // ── Transfers ──
    private void renderTransfers() {
        BigDecimal tot = transfers.stream().map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        setL(transferTotalLabel, "$" + fmt(tot));
        setL(transferCountLabel, String.valueOf(transfers.size()));
        if (transferList != null) {
            transferList.getChildren().clear();
            if (transfers.isEmpty()) {
                transferList.getChildren().add(makeWidgetEmpty("No transfers yet", "transfers"));
            } else {
                for (AccountTransfer t : transfers) {
                    try {
                        transferList.getChildren().add(makeTransferRow(t));
                    } catch (Exception e) {
                        System.err.println("[transferList] Skipping malformed transfer: " + e.getMessage());
                        e.printStackTrace();
                        transferList.getChildren().add(makeSimpleTransferRow(t));
                    }
                }
            }
        }
    }

    // ── Budget ──
    private void renderBudget() {
        if (budgetCards == null) {
            return;

        }
        budgetCards.getChildren().clear();
        BigDecimal totalB = BigDecimal.ZERO, totalS = BigDecimal.ZERO;
        for (Budget b : budgets) {
            String budgetCategory = b.getCategory() != null ? b.getCategory() : "Other";
            BigDecimal spent = transactions.stream().filter(Transaction::isExpense).filter(t -> budgetCategory.equals(t.getCategory())).map(t -> t.getAmount().abs()).reduce(BigDecimal.ZERO, BigDecimal::add);
            double pct = b.getLimit().compareTo(BigDecimal.ZERO) > 0 ? spent.divide(b.getLimit(), 4, RoundingMode.HALF_UP).doubleValue() : 0;
            totalB = totalB.add(b.getLimit());
            totalS = totalS.add(spent);
            String sc = pct >= 1.0 ? "danger" : pct >= 0.8 ? "warn" : "ok";
            budgetCards.getChildren().add(makePlanCard(b.getId(), "budget", budgetCategory, "$" + fmt(spent) + " / $" + fmt(b.getLimit()), Math.min(pct, 1.0), sc, b.getPeriod() != null ? b.getPeriod() : "monthly"));
        }
        setL(budgetTotalLabel, "$" + fmt(totalB));
        setL(budgetRemainingLabel, "$" + fmt(totalB.subtract(totalS).max(BigDecimal.ZERO)));
        if (budgetEmpty != null) {
            budgetEmpty.setVisible(budgets.isEmpty());
            budgetEmpty.setManaged(budgets.isEmpty());
        }
    }

    private void renderBudgetWidget() {
        if (dashBudgetList == null) {
            return;

        }
        dashBudgetList.getChildren().clear();
        if (budgets.isEmpty()) {
            dashBudgetList.getChildren().add(makeWidgetEmpty("No budgets", "budget"));
            return;
        }
        budgets.stream().limit(4).forEach(b -> {
            BigDecimal spent = transactions.stream().filter(Transaction::isExpense).filter(t -> b.getCategory().equals(t.getCategory())).map(t -> t.getAmount().abs()).reduce(BigDecimal.ZERO, BigDecimal::add);
            double pct = b.getLimit().compareTo(BigDecimal.ZERO) > 0 ? spent.divide(b.getLimit(), 4, RoundingMode.HALF_UP).doubleValue() : 0;
            dashBudgetList.getChildren().add(makeWidgetRow(b.getCategory(), Math.min(pct, 1.0), pct >= 1.0 ? "#e05c5c" : pct >= 0.8 ? "#e8a44a" : "#5cba8a", (int) (pct * 100) + "%"));
        });
    }

    // ── Savings ──
    private void renderSavings() {
        if (savingsCards == null) {
            return;

        }
        savingsCards.getChildren().clear();
        BigDecimal tot = goals.stream().map(g -> g.getSaved() != null ? g.getSaved() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        setL(savingsTotalLabel, "$" + fmt(tot));
        setL(savingsCountLabel, String.valueOf(goals.size()));
        setL(savedLabel, "$" + fmt(tot));
        goals.forEach(g -> {
            BigDecimal s = g.getSaved() != null ? g.getSaved() : BigDecimal.ZERO;
            BigDecimal t = g.getTarget() != null ? g.getTarget() : BigDecimal.ONE;
            double pct = s.divide(t.compareTo(BigDecimal.ZERO) > 0 ? t : BigDecimal.ONE, 4, RoundingMode.HALF_UP).doubleValue();
            savingsCards.getChildren().add(makePlanCard(g.getId(), "savings", g.getName(), "$" + fmt(s) + " of $" + fmt(t), Math.min(pct, 1.0), "gold", g.getTargetDate() != null ? "Target: " + g.getTargetDate() : "No target date"));
        });
        if (savingsEmpty != null) {
            savingsEmpty.setVisible(goals.isEmpty());
            savingsEmpty.setManaged(goals.isEmpty());
        }
    }

    private void renderSavingsWidget() {
        if (dashSavingsList == null) {
            return;

        }
        dashSavingsList.getChildren().clear();
        if (goals.isEmpty()) {
            dashSavingsList.getChildren().add(makeWidgetEmpty("No goals", "savings"));
            return;
        }
        goals.stream().limit(3).forEach(g -> {
            BigDecimal s = g.getSaved() != null ? g.getSaved() : BigDecimal.ZERO;
            BigDecimal t = g.getTarget() != null ? g.getTarget() : BigDecimal.ONE;
            double pct = s.divide(t.compareTo(BigDecimal.ZERO) > 0 ? t : BigDecimal.ONE, 4, RoundingMode.HALF_UP).doubleValue();
            dashSavingsList.getChildren().add(makeWidgetRow(g.getName(), Math.min(pct, 1.0), "#c9a84c", (int) (pct * 100) + "%"));
        });
    }

    // ── Debt ──
    private void renderDebt() {
        if (debtCards == null) {
            return;

        }
        debtCards.getChildren().clear();
        BigDecimal owed = BigDecimal.ZERO, paid = BigDecimal.ZERO;
        for (Debt d : debts) {
            BigDecimal p = d.getPaid() != null ? d.getPaid() : BigDecimal.ZERO;
            BigDecimal tot = d.getTotal() != null ? d.getTotal() : BigDecimal.ONE;
            BigDecimal rem = tot.subtract(p).max(BigDecimal.ZERO);
            double pct = p.divide(tot.compareTo(BigDecimal.ZERO) > 0 ? tot : BigDecimal.ONE, 4, RoundingMode.HALF_UP).doubleValue();
            owed = owed.add(rem);
            paid = paid.add(p);
            String sc = pct >= 1.0 ? "ok" : pct >= 0.5 ? "warn" : "danger";
            String sub = (pct >= 1.0 ? "Fully paid!" : "$" + fmt(rem) + " remaining") + (d.getInterestRate() != null ? " · " + d.getInterestRate() + "%" : "") + (d.getDueDate() != null ? " · Due " + d.getDueDate() : "");
            debtCards.getChildren().add(makePlanCard(d.getId(), "debt", d.getName(), "$" + fmt(p) + " paid of $" + fmt(tot), Math.min(pct, 1.0), sc, sub));
        }
        setL(debtTotalLabel, "$" + fmt(owed));
        setL(debtPaidLabel, "$" + fmt(paid));
        if (debtEmpty != null) {
            debtEmpty.setVisible(debts.isEmpty());
            debtEmpty.setManaged(debts.isEmpty());
        }
    }

    private void renderDebtWidget() {
        if (dashDebtList == null) {
            return;

        }
        dashDebtList.getChildren().clear();
        if (debts.isEmpty()) {
            dashDebtList.getChildren().add(makeWidgetEmpty("No debts", "debt"));
            return;
        }
        debts.stream().limit(3).forEach(d -> {
            BigDecimal p = d.getPaid() != null ? d.getPaid() : BigDecimal.ZERO;
            BigDecimal tot = d.getTotal() != null ? d.getTotal() : BigDecimal.ONE;
            BigDecimal rem = tot.subtract(p).max(BigDecimal.ZERO);
            double pct = p.divide(tot.compareTo(BigDecimal.ZERO) > 0 ? tot : BigDecimal.ONE, 4, RoundingMode.HALF_UP).doubleValue();
            dashDebtList.getChildren().add(makeWidgetRow(d.getName(), 1.0 - Math.min(pct, 1.0), "#e05c5c", "$" + fmt(rem) + " left"));
        });
    }

    // ── Categories ──
    private void renderCategories() {
        if (incomeChipBox != null) {
            incomeChipBox.getChildren().clear();
            if (incCats.isEmpty()) {
                incomeChipBox.getChildren().add(makeWidgetEmpty("No income categories yet", "categories"));
            } else {
                incCats.forEach(c -> incomeChipBox.getChildren().add(makeChip(c, "income")));
            }
        }
        if (expenseChipBox != null) {
            expenseChipBox.getChildren().clear();
            if (expCats.isEmpty()) {
                expenseChipBox.getChildren().add(makeWidgetEmpty("No expense categories yet", "categories"));
            } else {
                expCats.forEach(c -> expenseChipBox.getChildren().add(makeChip(c, "expense")));
            }
        }
        SessionStore.setIncomeCategories(incCats);
        SessionStore.setExpenseCategories(expCats);
        persistDashboardState();
    }

    @FXML
    public void addIncomeCategory() {
        if (newIncomeCategory == null) {
            return;

        }
        String v = newIncomeCategory.getText().trim();
        if (!v.isEmpty() && !incCats.contains(v)) {
            incCats.add(v);
            newIncomeCategory.clear();
            renderCategories();
            loadCats();
            loadCats2();
        }
    }

    @FXML
    public void addExpenseCategory() {
        if (newExpenseCategory == null) {
            return;

        }
        String v = newExpenseCategory.getText().trim();
        if (!v.isEmpty() && !expCats.contains(v)) {
            expCats.add(v);
            newExpenseCategory.clear();
            renderCategories();
            loadCats();
            loadCats2();
        }
    }

    private void loadCats() {
        List<String> cats = quickType.equals("income") ? incCats : expCats;
        if (quickCategory != null) {
            quickCategory.getItems().setAll(cats);
            if (!cats.isEmpty()) {
                quickCategory.setValue(cats.get(0));

            }
        }
    }

    private void loadCats2() {
        List<String> cats = addType2.equals("income") ? incCats : expCats;
        if (category2 != null) {
            category2.getItems().setAll(cats);
            if (!cats.isEmpty()) {
                category2.setValue(cats.get(0));

            }
        }
    }

    @FXML
    public void handleAdd2() {
        if (text2 == null || amount2 == null || account2 == null || category2 == null || date2 == null) {
            return;
        }
        String txt = text2.getText().trim();
        String amtStr = amount2.getText().trim();
        String acc = account2.getValue();
        String cat = category2.getValue();
        if (txt.isEmpty() || amtStr.isEmpty() || acc == null || cat == null) {
            showInfo("Error", "Fill all fields");
            return;
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(amtStr);
        } catch (Exception e) {
            showInfo("Error", "Invalid amount");
            return;
        }
        LocalDate date = date2.getValue() != null ? date2.getValue() : LocalDate.now();
        submitTx(txt, addType2.equals("expense") ? amount.negate() : amount, acc, cat, date, () -> {
            text2.clear();
            amount2.clear();
            date2.setValue(LocalDate.now());
        });
    }

    // ── Quick add ──
    @FXML
    public void handleQuickAdd() {
        if (quickText == null || quickAmount == null || quickAccount == null || quickCategory == null || quickDate == null) {
            return;
        }
        String text = quickText.getText().trim();
        String amtStr = quickAmount.getText().trim();
        String acc = quickAccount.getValue();
        String cat = quickCategory.getValue();
        if (text.isEmpty() || amtStr.isEmpty() || acc == null || cat == null) {
            showInfo("Error", "Fill all fields");
            return;
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(amtStr);
        } catch (Exception e) {
            showInfo("Error", "Invalid amount");
            return;
        }
        if (quickType.equals("expense")) {
            BigDecimal bal = accBalRaw(acc);
            // Only warn if balance is already positive (has income recorded)
            // Don't block if no transactions exist yet
            if (bal.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(bal) > 0) {
                pending = new PendingExpense(text, amount, acc, cat);
                showBalanceDialog(acc, bal, amount);
                return;
            }
        }
        LocalDate date = quickDate.getValue() != null ? quickDate.getValue() : LocalDate.now();
        submitTx(text, quickType.equals("expense") ? amount.negate() : amount, acc, cat, date, () -> {
            quickText.clear();
            quickAmount.clear();
        });
    }

    private void submitTx(String text, BigDecimal amount, String acc, String cat, LocalDate date, Runnable onDone) {
        Map<String, Object> body = new HashMap<>();
        body.put("text", text != null && !text.isEmpty() ? text : "Transaction");
        body.put("amount", amount);
        body.put("account", acc != null ? acc : "card");
        body.put("category", cat != null ? cat : "Other");
        body.put("date", date != null ? date.toString() : LocalDate.now().toString());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JsonNode result = ApiClient.post("/transactions", body);
                System.out.println("[TX] Saved: " + result);
                Platform.runLater(() -> {
                    onDone.run();
                    Transaction saved = new Transaction();
                    saved.setText(body.get("text") != null ? String.valueOf(body.get("text")) : "Transaction");
                    saved.setAmount(body.get("amount") instanceof BigDecimal ? (BigDecimal) body.get("amount") : new BigDecimal(String.valueOf(body.get("amount"))));
                    saved.setAccount(String.valueOf(body.get("account")));
                    saved.setCategory(String.valueOf(body.get("category")));
                    saved.setDate(LocalDate.parse(String.valueOf(body.get("date"))));
                    transactions.add(0, saved);
                    refreshAll();
                    persistDashboardState();
                });
            } catch (Exception e) {
                System.err.println("[TX] Error: " + e.getMessage());
                String txMsg = e.getMessage() != null ? e.getMessage() : "";
                if (txMsg.contains("401") || txMsg.contains("403") || txMsg.contains("Not authenticated")) {
                    System.err.println("[TX] Session/auth issue: " + txMsg);
                } else {
                    Platform.runLater(() -> showInfo("Transaction Error",
                            "Failed to save transaction:\n" + txMsg
                            + "\n\nMake sure the backend is running on port 8080."));
                }
            }
        });
    }

    private void showBalanceDialog(String acc, BigDecimal bal, BigDecimal need) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Insufficient Balance");
        a.setHeaderText("Not enough funds in " + cap(acc));
        a.setContentText("Balance: $" + fmt(bal.max(BigDecimal.ZERO)) + "\nNeeded:  $" + fmt(need) + "\n\nGo to Debt tab to take a loan first.");
        ButtonType goDebt = new ButtonType("Go to Debt tab");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        a.getButtonTypes().setAll(goDebt, cancel);
        a.showAndWait().ifPresent(b -> {
            if (b == goDebt) {
                showSection("debt");
                if (debtReminderBanner != null) {
                    debtReminderBanner.setVisible(true);
                    debtReminderBanner.setManaged(true);
                }
            } else {
                pending = null;

            }
        });
    }

    // ── Plan dialogs ──
    @FXML
    public void openAddBudget() {
        openBudgetDialog(null);
    }

    @FXML
    public void openAddSavings() {
        openSavingsDialog(null);
    }

    @FXML
    public void openAddDebt() {
        openDebtDialog(null);
    }

    @FXML
    public void openAddIncome() {
        openTxDialog("income", "card");
    }

    @FXML
    public void openAddCardIncome() {
        openTxDialog("income", "card");
    }

    @FXML
    public void openAddCashIncome() {
        openTxDialog("income", "cash");
    }

    @FXML
    public void openAddSavingsIncome() {
        openTxDialog("income", "savings");
    }

    @FXML
    public void openAddExpense() {
        openTxDialog("expense", "card");
    }

    private void openTxDialog(String type, String defaultAccount) {
        Dialog<Map<String, String>> d = new Dialog<>();
        d.setTitle("Add " + cap(type));
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(12);
        g.setPadding(new Insets(16));
        TextField txt = new TextField();
        txt.setPromptText("Description");
        TextField amt = new TextField();
        amt.setPromptText("0.00");
        DatePicker dp = new DatePicker(LocalDate.now());
        ComboBox<String> acc = new ComboBox<>();
        acc.getItems().addAll("card", "cash", "savings");
        acc.setValue(defaultAccount != null && !defaultAccount.isBlank() ? defaultAccount : "card");
        ComboBox<String> cat = new ComboBox<>();
        List<String> cats = type.equals("income") ? incCats : expCats;
        cat.getItems().addAll(cats);
        if (!cats.isEmpty()) {
            cat.setValue(cats.get(0));
        }
        g.add(new Label("Description:"), 0, 0);
        g.add(txt, 1, 0);
        g.add(new Label("Amount ($):"), 0, 1);
        g.add(amt, 1, 1);
        g.add(new Label("Date:"), 0, 2);
        g.add(dp, 1, 2);
        g.add(new Label("Account:"), 0, 3);
        g.add(acc, 1, 3);
        g.add(new Label("Category:"), 0, 4);
        g.add(cat, 1, 4);
        d.getDialogPane().setContent(g);
        d.setResultConverter(b -> b == ButtonType.OK ? Map.of("text", txt.getText().trim(), "amount", amt.getText().trim(), "date", dp.getValue() != null ? dp.getValue().toString() : LocalDate.now().toString(), "account", acc.getValue(), "category", cat.getValue()) : null);
        d.showAndWait().ifPresent(r -> {
            if (r.get("text").isEmpty() || r.get("amount").isEmpty()) {
                return;
            }
            try {
                BigDecimal a = new BigDecimal(r.get("amount"));
                if (type.equals("expense")) {
                    BigDecimal bal = accBalRaw(r.get("account"));
                    if (bal.compareTo(BigDecimal.ZERO) > 0 && a.compareTo(bal) > 0) {
                        pending = new PendingExpense(r.get("text"), a, r.get("account"), r.get("category"));
                        showBalanceDialog(r.get("account"), bal, a);
                        return;
                    }
                    a = a.negate();
                }
                submitTx(r.get("text"), a, r.get("account"), r.get("category"), LocalDate.parse(r.get("date")), () -> {
                });
            } catch (Exception e) {
                showInfo("Error", "Invalid amount");
            }
        });
    }

    private void openBudgetDialog(Budget ex) {
        Dialog<Map<String, String>> d = new Dialog<>();
        d.setTitle(ex == null ? "New Budget" : "Edit Budget");
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(12);
        g.setPadding(new Insets(16));
        ComboBox<String> cat = new ComboBox<>();
        cat.getItems().addAll(expCats);
        cat.setValue(ex != null ? ex.getCategory() : expCats.isEmpty() ? "" : expCats.get(0));
        TextField lim = new TextField(ex != null ? ex.getLimit().toString() : "");
        lim.setPromptText("0.00");
        ComboBox<String> per = new ComboBox<>();
        per.getItems().addAll("monthly", "weekly", "yearly");
        per.setValue(ex != null ? ex.getPeriod() : "monthly");
        g.add(new Label("Category:"), 0, 0);
        g.add(cat, 1, 0);
        g.add(new Label("Limit ($):"), 0, 1);
        g.add(lim, 1, 1);
        g.add(new Label("Period:"), 0, 2);
        g.add(per, 1, 2);
        d.getDialogPane().setContent(g);
        d.setResultConverter(b -> b == ButtonType.OK ? Map.of("category", cat.getValue(), "limit", lim.getText().trim(), "period", per.getValue()) : null);
        d.showAndWait().ifPresent(r -> {
            if (r.get("limit").isEmpty()) {
                return;
            }
            Map<String, Object> body = new HashMap<>();
            body.put("category", r.get("category"));
            body.put("limit", new BigDecimal(r.get("limit")));
            body.put("period", r.get("period"));
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    Budget savedBudget;
                    if (ex == null) {
                        JsonNode result = ApiClient.post("/budgets", body);
                        savedBudget = ApiClient.mapper.convertValue(result, Budget.class);
                    } else {
                        JsonNode result = ApiClient.patch("/budgets/" + ex.getId(), body);
                        savedBudget = ApiClient.mapper.convertValue(result, Budget.class);

                    }
                    final Budget finalSavedBudget = savedBudget;
                    Platform.runLater(() -> {
                        if (finalSavedBudget != null) {
                            budgets.removeIf(existing -> Objects.equals(existing.getId(), finalSavedBudget.getId()));
                            budgets.add(finalSavedBudget);
                        }
                        refreshAll();
                        persistDashboardState();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showInfo("Error", e.getMessage()));
                }
            });
        });
    }

    private void openSavingsDialog(SavingsGoal ex) {
        Dialog<Map<String, String>> d = new Dialog<>();
        d.setTitle(ex == null ? "New Goal" : "Edit Goal");
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(12);
        g.setPadding(new Insets(16));
        TextField name = new TextField(ex != null ? ex.getName() : "");
        name.setPromptText("Goal name");
        TextField tgt = new TextField(ex != null ? ex.getTarget().toString() : "");
        tgt.setPromptText("0.00");
        TextField sav = new TextField(ex != null && ex.getSaved() != null ? ex.getSaved().toString() : "0");
        sav.setPromptText("0.00");
        DatePicker dp = new DatePicker(ex != null ? ex.getTargetDate() : null);
        g.add(new Label("Name:"), 0, 0);
        g.add(name, 1, 0);
        g.add(new Label("Target ($):"), 0, 1);
        g.add(tgt, 1, 1);
        g.add(new Label("Saved ($):"), 0, 2);
        g.add(sav, 1, 2);
        g.add(new Label("Target date:"), 0, 3);
        g.add(dp, 1, 3);
        d.getDialogPane().setContent(g);
        d.setResultConverter(b -> b == ButtonType.OK ? Map.of("name", name.getText().trim(), "target", tgt.getText().trim(), "saved", sav.getText().trim(), "date", dp.getValue() != null ? dp.getValue().toString() : "") : null);
        d.showAndWait().ifPresent(r -> {
            if (r.get("name").isEmpty() || r.get("target").isEmpty()) {
                return;
            }
            Map<String, Object> body = new HashMap<>();
            body.put("name", r.get("name"));
            body.put("target", new BigDecimal(r.get("target")));
            body.put("saved", r.get("saved").isEmpty() ? BigDecimal.ZERO : new BigDecimal(r.get("saved")));
            if (!r.get("date").isEmpty()) {
                body.put("targetDate", r.get("date"));
            }
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    SavingsGoal savedGoal;
                    if (ex == null) {
                        JsonNode result = ApiClient.post("/savings-goals", body);
                        savedGoal = ApiClient.mapper.convertValue(result, SavingsGoal.class);
                    } else {
                        JsonNode result = ApiClient.patch("/savings-goals/" + ex.getId(), body);
                        savedGoal = ApiClient.mapper.convertValue(result, SavingsGoal.class);

                    }
                    final SavingsGoal finalSavedGoal = savedGoal;
                    Platform.runLater(() -> {
                        if (finalSavedGoal != null) {
                            goals.removeIf(existing -> Objects.equals(existing.getId(), finalSavedGoal.getId()));
                            goals.add(finalSavedGoal);
                        }
                        refreshAll();
                        persistDashboardState();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showInfo("Error", e.getMessage()));
                }
            });
        });
    }

    private void openDebtDialog(Debt ex) {
        Dialog<Map<String, String>> d = new Dialog<>();
        d.setTitle(ex == null ? "Add Debt / Loan" : "Edit Debt");
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(12);
        g.setPadding(new Insets(16));
        TextField name = new TextField(ex != null ? ex.getName() : "");
        name.setPromptText("e.g. Bank loan");
        ComboBox<String> acc = new ComboBox<>();
        acc.getItems().addAll("card", "cash", "savings");
        acc.setValue(ex != null && ex.getAccount() != null ? ex.getAccount() : "card");
        TextField tot = new TextField(ex != null ? ex.getTotal().toString() : "");
        tot.setPromptText("0.00");
        TextField paid = new TextField(ex != null && ex.getPaid() != null ? ex.getPaid().toString() : "0");
        paid.setPromptText("0.00");
        TextField rate = new TextField(ex != null && ex.getInterestRate() != null ? ex.getInterestRate().toString() : "");
        rate.setPromptText("e.g. 12.5");
        DatePicker dp = new DatePicker(ex != null ? ex.getDueDate() : null);
        g.add(new Label("Name:"), 0, 0);
        g.add(name, 1, 0);
        g.add(new Label("Credit to:"), 0, 1);
        g.add(acc, 1, 1);
        g.add(new Label("Total ($):"), 0, 2);
        g.add(tot, 1, 2);
        g.add(new Label("Paid ($):"), 0, 3);
        g.add(paid, 1, 3);
        g.add(new Label("Interest (%):"), 0, 4);
        g.add(rate, 1, 4);
        g.add(new Label("Due date:"), 0, 5);
        g.add(dp, 1, 5);
        d.getDialogPane().setContent(g);
        d.setResultConverter(b -> b == ButtonType.OK ? Map.of("name", name.getText().trim(), "account", acc.getValue(), "total", tot.getText().trim(), "paid", paid.getText().trim(), "rate", rate.getText().trim(), "date", dp.getValue() != null ? dp.getValue().toString() : "") : null);
        d.showAndWait().ifPresent(r -> {
            if (r.get("name").isEmpty() || r.get("total").isEmpty()) {
                return;
            }
            Map<String, Object> body = new HashMap<>();
            body.put("name", r.get("name"));
            body.put("account", r.get("account"));
            body.put("total", new BigDecimal(r.get("total")));
            body.put("paid", r.get("paid").isEmpty() ? BigDecimal.ZERO : new BigDecimal(r.get("paid")));
            if (!r.get("rate").isEmpty()) {
                body.put("interestRate", new BigDecimal(r.get("rate")));
            }
            if (!r.get("date").isEmpty()) {
                body.put("dueDate", r.get("date"));
            }
            boolean isNew = ex == null;
            Long debtId = ex != null ? ex.getId() : null;
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    Debt savedDebt;
                    if (isNew) {
                        JsonNode result = ApiClient.post("/debts", body);
                        savedDebt = ApiClient.mapper.convertValue(result, Debt.class);
                    } else {
                        JsonNode result = ApiClient.patch("/debts/" + debtId, body);
                        savedDebt = ApiClient.mapper.convertValue(result, Debt.class);
                    }
                    final Debt finalSavedDebt = savedDebt;
                    Platform.runLater(() -> {
                        if (finalSavedDebt != null) {
                            debts.removeIf(existing -> Objects.equals(existing.getId(), finalSavedDebt.getId()));
                            debts.add(finalSavedDebt);
                        }
                        if (isNew && pending != null) {
                            BigDecimal newBal = accBalRaw(r.get("account")).add(new BigDecimal(r.get("total")));
                            if (newBal.compareTo(pending.amount()) >= 0) {
                                showInfo("Balance Updated", cap(r.get("account")) + " now covers your $" + fmt(pending.amount()) + " expense. You can now add it.");
                                pending = null;
                                if (debtReminderBanner != null) {
                                    debtReminderBanner.setVisible(false);
                                    debtReminderBanner.setManaged(false);
                                }
                            }
                        }
                        refreshAll();
                        persistDashboardState();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showInfo("Error", e.getMessage()));
                }
            });
        });
    }

    // ── Delete ──
    private void deleteTx(Long id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApiClient.delete("/transactions/" + id);
                Platform.runLater(() -> {
                    transactions.removeIf(t -> Objects.equals(t.getId(), id));
                    refreshAll();
                    persistDashboardState();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showInfo("Error", e.getMessage()));
            }
        });
    }

    private void deletePlan(String type, Long id) {
        String path = switch (type) {
            case "budget" ->
                "/budgets/" + id;
            case "savings" ->
                "/savings-goals/" + id;
            case "debt" ->
                "/debts/" + id;
            default ->
                null;
        };
        if (path == null) {
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApiClient.delete(path);
                Platform.runLater(() -> {
                    if ("budget".equals(type)) {
                        budgets.removeIf(b -> Objects.equals(b.getId(), id));
                    } else if ("savings".equals(type)) {
                        goals.removeIf(g -> Objects.equals(g.getId(), id));
                    } else if ("debt".equals(type)) {
                        debts.removeIf(d -> Objects.equals(d.getId(), id));
                    }
                    refreshAll();
                    persistDashboardState();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showInfo("Error", e.getMessage()));
            }
        });
    }

    // ── Logout ──
    @FXML
    public void handleLogout() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Log out");
        a.setHeaderText("Are you sure?");
        a.setContentText("Your data stays saved.");
        a.showAndWait().ifPresent(b -> {
            if (b == ButtonType.OK) {
                SessionStore.clear();
                ApiClient.clearAuthToken();
                PennyWiseApp.navigateTo("login");
            }
        });
    }

    // ── UI builders ──
    private HBox makeTxRow(Transaction t) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setMaxWidth(Double.MAX_VALUE);
        row.setMinHeight(54);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 10px; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 0 1 0; -fx-border-radius: 10px;");
        row.getStyleClass().add("tx-row");
        StackPane icon = new StackPane();
        icon.getStyleClass().addAll("tx-icon", t.isIncome() ? "tx-icon-inc" : "tx-icon-exp");
        icon.setPrefSize(34, 34);
        icon.setMinSize(34, 34);
        icon.setMaxSize(34, 34);
        FontIcon fi = new FontIcon(t.isIncome() ? "fas-plus" : "fas-minus");
        fi.setIconSize(13);
        icon.getChildren().add(fi);
        VBox info = new VBox(3);
        String text = t.getText() != null && !t.getText().isBlank() ? t.getText() : "Transaction";
        String account = t.getAccount() != null && !t.getAccount().isBlank() ? t.getAccount() : "card";
        String category = t.getCategory() != null && !t.getCategory().isBlank() ? t.getCategory() : "Other";
        String dateText = t.getDate() != null ? t.getDate().toString() : "No date";
        Label nm = new Label(text);
        nm.getStyleClass().add("tx-name");
        nm.setStyle("-fx-text-fill: #f0ede6;");
        Label meta = new Label(cap(account) + " · " + category + " · " + dateText);
        meta.getStyleClass().add("tx-meta");
        meta.setStyle("-fx-text-fill: #7a7870;");
        info.getChildren().addAll(nm, meta);
        HBox.setHgrow(info, Priority.ALWAYS);
        BigDecimal amount = t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO;
        Label amt = new Label((amount.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "-") + "$" + fmt(amount.abs()));
        amt.getStyleClass().addAll("tx-amount", t.isIncome() ? "color-green" : "color-red");
        amt.setStyle(t.isIncome() ? "-fx-text-fill: #5cba8a;" : "-fx-text-fill: #e05c5c;");
        Button del = new Button();
        del.getStyleClass().add("icon-btn-danger");
        del.setGraphic(new FontIcon("fas-xmark"));
        del.setOnAction(e -> deleteTx(t.getId()));
        row.getChildren().addAll(icon, info, amt, del);
        return row;
    }

    private HBox makeSimpleTxRow(Transaction t) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setMaxWidth(Double.MAX_VALUE);
        row.setMinHeight(54);
        row.getStyleClass().add("table-row");

        Label nm = new Label(t != null && t.getText() != null && !t.getText().isBlank() ? t.getText() : "Transaction");
        nm.setMinWidth(130);
        nm.setMaxWidth(130);

        Label acc = new Label(cap(t != null ? t.getAccount() : null));
        acc.getStyleClass().add("badge-acc");

        Label cat = new Label(t != null && t.getCategory() != null ? t.getCategory() : "Other");
        cat.getStyleClass().addAll("badge-cat", t != null && t.isIncome() ? "badge-inc" : "badge-exp");

        BigDecimal amount = t != null && t.getAmount() != null ? t.getAmount().abs() : BigDecimal.ZERO;
        Label amt = new Label((t != null && t.isIncome() ? "+" : "-") + "$" + fmt(amount));
        amt.getStyleClass().add(t != null && t.isIncome() ? "color-green" : "color-red");

        Label dt = new Label(t != null && t.getDate() != null ? t.getDate().toString() : "No date");
        dt.getStyleClass().add("color-muted");

        HBox.setHgrow(nm, Priority.ALWAYS);
        row.getChildren().addAll(nm, acc, cat, amt, dt);
        return row;
    }

    private HBox makeTableRow(Transaction t, String type) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.getStyleClass().add("table-row");
        Label nm = new Label(t.getText());
        nm.setMinWidth(130);
        nm.setMaxWidth(130);
        Label acc = new Label(cap(t.getAccount()));
        acc.getStyleClass().add("badge-acc");
        Label cat = new Label(t.getCategory());
        cat.getStyleClass().addAll("badge-cat", type.equals("income") ? "badge-inc" : "badge-exp");
        Label amt = new Label((type.equals("income") ? "+" : "-") + "$" + fmt(t.getAmount().abs()));
        amt.getStyleClass().add(type.equals("income") ? "color-green" : "color-red");
        Label dt = new Label(t.getDate().toString());
        dt.getStyleClass().add("color-muted");
        HBox.setHgrow(nm, Priority.ALWAYS);
        Button edit = new Button();
        edit.getStyleClass().add("icon-btn");
        edit.setGraphic(new FontIcon("fas-pen"));
        edit.setOnAction(e -> openTxDialog(type, t.getAccount() != null ? t.getAccount() : "card"));
        Button del = new Button();
        del.getStyleClass().add("icon-btn-danger");
        del.setGraphic(new FontIcon("fas-trash"));
        del.setOnAction(e -> deleteTx(t.getId()));
        row.getChildren().addAll(nm, acc, cat, amt, dt, edit, del);
        return row;
    }

    private HBox makeTransferRow(AccountTransfer t) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.getStyleClass().add("table-row");
        Label note = new Label(t.getNote() != null ? t.getNote() : "—");
        HBox.setHgrow(note, Priority.ALWAYS);
        Label from = new Label(cap(t.getFromAccount()));
        from.getStyleClass().add("badge-acc");
        Label to = new Label(cap(t.getToAccount()));
        to.getStyleClass().add("badge-acc");
        Label amt = new Label("$" + fmt(t.getAmount()));
        amt.getStyleClass().add("color-blue");
        Label dt = new Label(t.getDate() != null ? t.getDate().toString() : "");
        dt.getStyleClass().add("color-muted");
        Button del = new Button();
        del.getStyleClass().add("icon-btn-danger");
        del.setGraphic(new FontIcon("fas-trash"));
        del.setOnAction(e -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    ApiClient.delete("/transfers/" + t.getId());
                    Platform.runLater(this::loadAll);
                } catch (Exception ex2) {
                    Platform.runLater(() -> showInfo("Error", ex2.getMessage()));
                }
            });
        });
        row.getChildren().addAll(note, from, to, amt, dt, del);
        return row;
    }

    private HBox makeSimpleTransferRow(AccountTransfer t) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.getStyleClass().add("table-row");

        Label note = new Label(t != null && t.getNote() != null && !t.getNote().isBlank() ? t.getNote() : "Transfer");
        HBox.setHgrow(note, Priority.ALWAYS);

        Label from = new Label(cap(t != null ? t.getFromAccount() : null));
        from.getStyleClass().add("badge-acc");

        Label to = new Label(cap(t != null ? t.getToAccount() : null));
        to.getStyleClass().add("badge-acc");

        Label amt = new Label("$" + fmt(t != null && t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO));
        amt.getStyleClass().add("color-blue");

        Label dt = new Label(t != null && t.getDate() != null ? t.getDate().toString() : "");
        dt.getStyleClass().add("color-muted");

        row.getChildren().addAll(note, from, to, amt, dt);
        return row;
    }

    private VBox makePlanCard(Long id, String kind, String title, String nums, double pct, String barCls, String sub) {
        VBox card = new VBox(10);
        card.getStyleClass().add("plan-card");
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setPrefWidth(280);
        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);
        Label tl = new Label(title);
        tl.getStyleClass().add("plan-title");
        HBox.setHgrow(tl, Priority.ALWAYS);
        String bl = switch (barCls) {
            case "danger" ->
                "At limit";
            case "warn" ->
                "Near limit";
            case "ok" ->
                "On track";
            case "gold" ->
                (int) (pct * 100) + "% saved";
            default ->
                (int) (pct * 100) + "% paid";
        };
        Label badge = new Label(bl);
        badge.getStyleClass().addAll("plan-badge", "badge-" + barCls);
        Button eb = new Button();
        eb.getStyleClass().add("icon-btn");
        eb.setGraphic(new FontIcon("fas-pen"));
        Button db = new Button();
        db.getStyleClass().add("icon-btn-danger");
        db.setGraphic(new FontIcon("fas-trash"));
        eb.setOnAction(e -> {
            switch (kind) {
                case "budget" ->
                    budgets.stream().filter(b -> b.getId().equals(id)).findFirst().ifPresent(this::openBudgetDialog);
                case "savings" ->
                    goals.stream().filter(g -> g.getId().equals(id)).findFirst().ifPresent(this::openSavingsDialog);
                case "debt" ->
                    debts.stream().filter(d -> d.getId().equals(id)).findFirst().ifPresent(this::openDebtDialog);
            }
        });
        db.setOnAction(e -> deletePlan(kind, id));
        top.getChildren().addAll(tl, badge, eb, db);
        Label nl = new Label(nums);
        nl.getStyleClass().add("plan-nums");
        ProgressBar pb = new ProgressBar(pct);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.getStyleClass().addAll("plan-bar", "bar-" + barCls);
        Label sl = new Label(sub);
        sl.getStyleClass().add("plan-sub");
        card.getChildren().addAll(top, nl, pb, sl);
        return card;
    }

    private HBox makeWidgetRow(String label, double pct, String color, String val) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 16, 8, 16));
        row.getStyleClass().add("widget-row");
        Label l = new Label(label);
        l.getStyleClass().add("widget-label");
        HBox.setHgrow(l, Priority.ALWAYS);
        ProgressBar pb = new ProgressBar(pct);
        pb.setPrefWidth(90);
        pb.setStyle("-fx-accent:" + color + ";");
        pb.getStyleClass().add("widget-bar");
        Label v = new Label(val);
        v.getStyleClass().add("widget-val");
        row.getChildren().addAll(l, pb, v);
        return row;
    }

    private Label makeWidgetEmpty(String msg, String nav) {
        Label l = new Label(msg + " — click to add");
        l.getStyleClass().add("widget-empty");
        l.setPadding(new Insets(12, 16, 12, 16));
        l.setOnMouseClicked(e -> showSection(nav));
        return l;
    }

    private HBox makeChip(String cat, String type) {
        HBox chip = new HBox(6);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.getStyleClass().addAll("cat-chip", type.equals("income") ? "chip-inc" : "chip-exp");
        chip.setPadding(new Insets(5, 10, 5, 10));
        Label l = new Label(cat);
        l.setStyle(type.equals("income") ? "-fx-text-fill: #5cba8a;" : "-fx-text-fill: #e05c5c;");
        Button del = new Button();
        del.getStyleClass().add("chip-del");
        del.setGraphic(new FontIcon("fas-times"));
        del.setOnAction(e -> {
            if (type.equals("income")) {
                incCats.remove(cat);
            } else {
                expCats.remove(cat);

            }
            renderCategories();
            loadCats();
        });
        chip.getChildren().addAll(l, del);
        return chip;
    }

    // ── Util ──
    private String fmt(BigDecimal v) {
        return v == null ? "0.00" : v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String cap(String s) {
        return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private void setL(Label l, String v) {
        if (l != null) {
            l.setText(v);

        }
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }
}
