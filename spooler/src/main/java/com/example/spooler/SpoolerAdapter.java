package com.example.spooler;

import java.io.*;
import java.util.*;

import com.google.gson.*;

public class SpoolerAdapter {
    private static final String PYTHON_EXE = "python";
    private static final String SCRIPT_PATH = "D:\\PDSA_cw\\python_service\\spooler.py";
    private final Gson gson = new Gson();

    private String readJson(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return reader.readLine();
        }
    }

    public List<String> listPrinters() throws IOException {
        Process process = new ProcessBuilder(PYTHON_EXE, SCRIPT_PATH, "list_printers").start();
        String json = readJson(process);
        return json == null ? Collections.emptyList() : Arrays.asList(gson.fromJson(json, String[].class));
    }

    public void loadJobsIntoList(String printerName, DoublyLinkedList list) throws IOException {
        Process process = new ProcessBuilder(PYTHON_EXE, SCRIPT_PATH, "list_jobs", printerName).start();
        String json = readJson(process);
        if (json == null || json.isEmpty()) return;

        JsonElement parsed = JsonParser.parseString(json);
        if (!parsed.isJsonArray()) return;

        JsonArray jobsArray = parsed.getAsJsonArray();
        for (JsonElement jobElem : jobsArray) {
            JsonObject jobObj = jobElem.getAsJsonObject();
            int jobId = jobObj.get("jobId").getAsInt();
            String docName = jobObj.get("documentName").getAsString();
            int statusInt = jobObj.get("status").getAsInt();
            String statusStr = parseStatus(statusInt);

            list.addLast(jobId, docName, statusStr);
        }
    }

    private String parseStatus(int pythonStatus) {
        if ((pythonStatus & 0x00000001) != 0) return "Paused";
        if ((pythonStatus & 0x00000002) != 0) return "Error";
        if ((pythonStatus & 0x00000004) != 0) return "Deleting";
        if ((pythonStatus & 0x00000008) != 0) return "Spooling";
        if ((pythonStatus & 0x00000010) != 0) return "Printing";
        if ((pythonStatus & 0x00000020) != 0) return "Offline";
        if ((pythonStatus & 0x00000040) != 0) return "PaperOut";
        if ((pythonStatus & 0x00000080) != 0) return "Printed";
        if ((pythonStatus & 0x00000100) != 0) return "Deleted";
        if ((pythonStatus & 0x00000200) != 0) return "BlockedDevQ";
        if ((pythonStatus & 0x00000400) != 0) return "UserIntervention";
        if ((pythonStatus & 0x00000800) != 0) return "Restart";
        if ((pythonStatus & 0x00001000) != 0) return "Retained";
        if ((pythonStatus & 0x00002000) != 0) return "Complete";
        return "Unknown";
    }



    /* public List<JobInfo> listJobs(String printerName) throws IOException {
        Process process = new ProcessBuilder(PYTHON_EXE, SCRIPT_PATH, "list_jobs", printerName).start();
        String json = readJson(process);
        return json == null ? Collections.emptyList() : Arrays.asList(gson.fromJson(json, JobInfo[].class));
    }*/

    public boolean moveJob(String printerName, int jobId, int newPosition) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(PYTHON_EXE, SCRIPT_PATH, "move_job",
                String.valueOf(jobId), String.valueOf(newPosition), printerName).start();
        return process.waitFor() == 0;
    }

    public boolean cancelJob(String printerName, int jobId) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(PYTHON_EXE, SCRIPT_PATH, "cancel_job",
                String.valueOf(jobId), printerName).start();
        return process.waitFor() == 0;
    }

    public boolean pausePrinter(String printerName) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(PYTHON_EXE, SCRIPT_PATH,
                "pause_printer", printerName).start();
        return process.waitFor() == 0;
    }

    public boolean resumePrinter(String printerName) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(PYTHON_EXE, SCRIPT_PATH,
                "resume_printer", printerName).start();
        return process.waitFor() == 0;
    }

    public boolean pauseJob(String printerName, int jobId) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(PYTHON_EXE, SCRIPT_PATH,
                "pause_job", String.valueOf(jobId), printerName).start();
        return process.waitFor() == 0;
    }

    public boolean resumeJob(String printerName, int jobId) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(PYTHON_EXE, SCRIPT_PATH,
                "resume_job", String.valueOf(jobId), printerName).start();
        return process.waitFor() == 0;
    }
}
