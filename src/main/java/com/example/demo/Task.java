package com.example.demo;

import java.util.List;

class Task implements Runnable{
    private String username, csvFilePath;
    private List<String> requestedQueryNames;

    public Task(String username, String csvFilePath, List<String> requestedQueryNames) {
        this.username = username;
        this.csvFilePath = csvFilePath;
        this.requestedQueryNames = requestedQueryNames;
    }

    @Override
    public void run() {
        try {
            LargeFileProcessor largeFileProcessor = new LargeFileProcessor(username);
            largeFileProcessor.insertFromCsvFile(csvFilePath);
            largeFileProcessor.queryRequestedNames(requestedQueryNames);
            largeFileProcessor.closeSession(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}