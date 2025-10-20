package com.example.spooler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    private JFrame frame;
    private JComboBox<String> printerComboBox;
    private JTable jobsTable;
    private DefaultTableModel tableModel;
    private DoublyLinkedList jobList;
    private SpoolerAdapter adapter;
    private JTextField jobIdField;
    private JTextField posField;
    private JTextField jobIdsField;
    private JLabel statusBar;

    public Main() throws Exception {
        adapter = new SpoolerAdapter();
        jobList = new DoublyLinkedList();
        initialize();
    }

    private void initialize() throws Exception {
        frame = new JFrame("Printer Spooler - Enterprise Edition");
        frame.setBounds(100, 100, 1000, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));

        // --- Top Toolbar ---
        JPanel topPanel = new JPanel(new BorderLayout());

        // Left: Printer selection
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        printerComboBox = new JComboBox<>();
        List<String> printers = adapter.listPrinters();
        Collections.reverse(printers); // reverse order
        for (String printer : printers) {
            printerComboBox.addItem(printer);
        }
        leftPanel.add(new JLabel("Select Printer: "));
        leftPanel.add(printerComboBox);
        topPanel.add(leftPanel, BorderLayout.WEST);

        // Right: Print / Apply / Reset
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton printButton = new JButton("Print");
        JButton applyButton = new JButton("Apply");
        JButton resetButton = new JButton("Reset");
        rightPanel.add(printButton);
        rightPanel.add(applyButton);
        rightPanel.add(resetButton);
        topPanel.add(rightPanel, BorderLayout.EAST);

        frame.add(topPanel, BorderLayout.NORTH);

        // --- Job Table ---
        String[] columns = {"Position", "Job ID", "Document", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jobsTable = new JTable(tableModel);
        jobsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane scrollPane = new JScrollPane(jobsTable);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Column widths
        jobsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        jobsTable.getColumnModel().getColumn(0).setPreferredWidth(60);   // Position
        jobsTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // Job ID
        jobsTable.getColumnModel().getColumn(2).setPreferredWidth(500);  // Document
        jobsTable.getColumnModel().getColumn(3).setPreferredWidth(250);  // Status

        // Auto-load jobs on printer select + Reset
        printerComboBox.addActionListener(e -> loadSelectedPrinterJobs());
        resetButton.addActionListener(e -> loadSelectedPrinterJobs());

        // Print & Apply
        printButton.addActionListener(e -> {
            handlePrintAction();
        });
        applyButton.addActionListener(e -> {
            handleApplyAction();
        });

        // --- Bottom Section (Reorder + Manage + Status Bar) ---
        JPanel mainBottomPanel = new JPanel(new BorderLayout(8, 8));
        mainBottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Left: Reorder Panel
        JPanel reorderPanel = new JPanel(new GridBagLayout());
        reorderPanel.setBorder(BorderFactory.createTitledBorder("Reorder Job"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        reorderPanel.add(new JLabel("Job ID:"), gbc);
        gbc.gridx = 1;
        jobIdField = new JTextField(8); // 8 columns
        Dimension jobIdSize = jobIdField.getPreferredSize();
        jobIdField.setPreferredSize(new Dimension(jobIdSize.width, jobIdSize.height + 2)); // slightly taller
        reorderPanel.add(jobIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        reorderPanel.add(new JLabel("New Position:"), gbc);
        gbc.gridx = 1;
        posField = new JTextField(8); // same as Job ID
        Dimension posSize = posField.getPreferredSize();
        posField.setPreferredSize(new Dimension(posSize.width, posSize.height + 2)); // slightly taller
        reorderPanel.add(posField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton moveButton = new JButton("Move");
        JButton clearButton = new JButton("Clear");
        btnPanel.add(moveButton);
        btnPanel.add(clearButton);
        reorderPanel.add(btnPanel, gbc);

        // Right: Manage Panel
        JPanel managePanel = new JPanel(new GridBagLayout());
        managePanel.setBorder(BorderFactory.createTitledBorder("Manage Jobs"));
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(5, 5, 5, 5);
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.anchor = GridBagConstraints.WEST;

        // Label
        gbc2.gridx = 0;
        gbc2.gridy = 0;
        gbc2.gridwidth = 2;
        managePanel.add(new JLabel("Job IDs (optional, comma-separated):"), gbc2);

        // Horizontal panel for textbox + clear button
        gbc2.gridx = 0;
        gbc2.gridy = 1;
        gbc2.gridwidth = 2;
        JPanel jobIdsPanel = new JPanel(new BorderLayout(5, 0)); // 5px gap
        jobIdsField = new JTextField();
        JButton clearJobIdsButton = new JButton("Clear");
        jobIdsPanel.add(jobIdsField, BorderLayout.CENTER);
        jobIdsPanel.add(clearJobIdsButton, BorderLayout.EAST);
        managePanel.add(jobIdsPanel, gbc2);

        // Buttons below textbox
        gbc2.gridx = 0;
        gbc2.gridy = 2;
        gbc2.gridwidth = 2;
        gbc2.anchor = GridBagConstraints.CENTER;
        JPanel btnPanel2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton resumeButton = new JButton("Resume");
        JButton pauseButton = new JButton("Pause");
        JButton cancelButton = new JButton("Cancel");
        btnPanel2.add(resumeButton);
        btnPanel2.add(pauseButton);
        btnPanel2.add(cancelButton);
        managePanel.add(btnPanel2, gbc2);

        // Clear button action
        clearJobIdsButton.addActionListener(e -> jobIdsField.setText(""));

        bottomPanel.add(reorderPanel);
        bottomPanel.add(managePanel);

        // Status Bar
        statusBar = new JLabel(" Ready.");
        statusBar.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(245, 245, 245));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        statusBar.setForeground(Color.DARK_GRAY);

        mainBottomPanel.add(bottomPanel, BorderLayout.CENTER);
        mainBottomPanel.add(statusBar, BorderLayout.SOUTH);

        frame.add(mainBottomPanel, BorderLayout.SOUTH);

        // --- Actions ---
        moveButton.addActionListener(e -> {
            try {
                int jobId = Integer.parseInt(jobIdField.getText().trim());
                int newPos = Integer.parseInt(posField.getText().trim());
                if (newPos < 1 || newPos > jobList.size()) {
                    setStatusMessage("Invalid position!", "error");
                    return;
                }
                if (jobList.moveOrder(jobId, newPos)) {
                    refreshTable();
                    setStatusMessage("Moved Job " + jobId + " to position " + newPos, "success");
                } else {
                    setStatusMessage("Move failed! Check Job ID.", "error");
                }
            } catch (NumberFormatException ex) {
                setStatusMessage("Please enter valid numbers!", "error");
            }
        });

        clearButton.addActionListener(e -> {
            jobIdField.setText("");
            posField.setText("");
            setStatusMessage("Reorder fields cleared", "info");
        });

        resumeButton.addActionListener(e -> {
            handleJobAction("To resume");
        });
        pauseButton.addActionListener(e -> {
            handleJobAction("To pause");
        });
        cancelButton.addActionListener(e -> {
            handleJobAction("Cancel");
        });

        frame.setVisible(true);
    }

    // --- Status Bar with Colors + Auto-Clear ---
    private void setStatusMessage(String message, String type) {
        statusBar.setText(" " + message);

        switch (type.toLowerCase()) {
            case "success": statusBar.setForeground(new Color(0, 128, 0)); break;
            case "error": statusBar.setForeground(Color.RED); break;
            case "info":
            default: statusBar.setForeground(Color.DARK_GRAY); break;
        }

        Timer timer = new Timer(5000, e -> {
            statusBar.setText(" Ready.");
            statusBar.setForeground(Color.DARK_GRAY);
        });
        timer.setRepeats(false);
        timer.start();
    }



    private void handlePrintAction() {
        String selectedPrinter = (String) printerComboBox.getSelectedItem();
        if (selectedPrinter == null || selectedPrinter.isEmpty()) {
            setStatusMessage("Please select a printer first!", "error");
            return;
        }

        if (jobList.getHead() == null) {
            setStatusMessage("No jobs available to print!", "error");
            return;
        }

        Node current = jobList.getHead();
        int successCount = 0;

        while (current != null) {
            try {
                boolean result = adapter.resumeJob(selectedPrinter, current.jobId); // Assuming your adapter handles printing
                if (result) {
                    current.status = "Printed";
                    successCount++;
                } else {
                    current.status = "Error occurred";
                    setStatusMessage("Failed to print Job ID " + current.jobId, "warning");
                }
            } catch (Exception ex) {
                setStatusMessage("Error printing Job ID " + current.jobId + ": " + ex.getMessage(), "error");
            }
            current = current.next;
        }

        setStatusMessage("Printed " + successCount + " job(s) successfully on " + selectedPrinter + ".", "success");
        refreshTable();
    }

    private void handleApplyAction() {
        String selectedPrinter = (String) printerComboBox.getSelectedItem();
        if (selectedPrinter == null || selectedPrinter.isEmpty()) {
            setStatusMessage("Please select a printer first!", "error");
            return;
        }

        List<Integer> removedJobs = jobList.getRemovedJobIds();
        if (removedJobs.isEmpty()) {
            System.out.println("No removed jobs.");
        } else {
            for (int jobId : removedJobs) {
                try {
                    boolean result = adapter.cancelJob(selectedPrinter, jobId);
                    if (!result) {
                        setStatusMessage("Failed to cancel Job ID " + jobId, "warning");
                    }
                } catch (Exception ex) {
                    setStatusMessage("Error cancelling Job ID " + jobId + ": " + ex.getMessage(), "error");
                }
            }
        }

        if (jobList.getHead() == null) {
            setStatusMessage("No jobs in queue to apply!", "error");
            return;
        }

        Node current = jobList.getHead();
        int position = 1;
        int successCount = 0;

        while (current != null) {
            try {
                boolean reorder = adapter.moveJob(selectedPrinter, current.jobId, position);
                if (!reorder) {
                    setStatusMessage("Failed to reorder Job ID " + current.jobId, "warning");
                }

                String status = current.status.trim().toLowerCase();
                if (status.equals("to resume")) {
                    boolean result = adapter.resumeJob(selectedPrinter, current.jobId);
                    if (result) {
                        successCount++;
                    } else {
                        setStatusMessage("Failed to print Job ID " + current.jobId, "warning");
                    }
                } else if (status.equals("to pause")) {
                    boolean result = adapter.pauseJob(selectedPrinter, current.jobId);
                    if (result) {
                        successCount++;
                    } else {
                        setStatusMessage("Failed to pause Job ID " + current.jobId, "warning");
                    }
                } else {
                    successCount++;
                }
            } catch (Exception ex) {
                setStatusMessage("Error applying Job ID " + current.jobId + ": " + ex.getMessage(), "error");
            }
            current = current.next;
            position++;
        }

        setStatusMessage("Applied status check for " + successCount + " job(s) on printer " + selectedPrinter + ".", "success");
        loadSelectedPrinterJobs();
    }

    private void handleJobAction(String action) {
        List<Integer> jobIds = collectSelectedJobIds();
        if (jobIds.isEmpty()) {
            setStatusMessage("No valid jobs selected!", "error");
            return;
        }

        int successCount = 0;
        boolean isRemove = action.equalsIgnoreCase("Cancel");

        for (int jobId : jobIds) {
            boolean result = isRemove ? jobList.removeJob(jobId) : jobList.updateStatus(jobId, action);
            if (result) successCount++;
            else setStatusMessage("Job ID " + jobId + " not found!", "warning");
        }

        String message = isRemove
                ? "Removed " + successCount + " job(s)."
                : "Updated status to '" + action + "' for " + successCount + " job(s).";

        setStatusMessage(message, "success");
        refreshTable();
    }

    private List<Integer> collectSelectedJobIds() {
        String idsInput = jobIdsField.getText().trim();
        List<Integer> jobIds = new ArrayList<>();

        if (!idsInput.isEmpty()) {
            try {
                // Split on any non-digit character (except minus for negative IDs, if needed)
                jobIds = Arrays.stream(idsInput.split("[^\\d]+"))
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                setStatusMessage("Invalid job ID format!", "error");
                return Collections.emptyList();
            }
        } else {
            int[] selectedRows = jobsTable.getSelectedRows();
            for (int row : selectedRows) {
                int jobId = (int) tableModel.getValueAt(row, 1);
                jobIds.add(jobId);
            }
        }

        return jobIds;
    }

    // Load jobs of selected printer
    private void loadSelectedPrinterJobs() {
        String printer = (String) printerComboBox.getSelectedItem();
        if (printer == null || printer.isEmpty()) return;
        try {
            setStatusMessage("Loading jobs for " + printer + "...", "info");
            jobList.clearAll();
            adapter.loadJobsIntoList(printer, jobList);
            refreshTable();
            setStatusMessage("Loaded jobs for " + printer, "success");
        } catch (Exception ex) {
            setStatusMessage("Failed to load jobs: " + ex.getMessage(), "error");
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        Node current = jobList.getHead();
        int pos = 1;
        while (current != null) {
            tableModel.addRow(new Object[]{pos, current.jobId, current.documentName, current.status});
            current = current.next;
            pos++;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new Main();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
