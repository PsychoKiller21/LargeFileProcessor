package com.example.demo;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.sql.*;
import java.util.*;


class LargeFileProcessor {

    private Connection connection;
    private static Set<String> sharedSkuSet = new HashSet<>();
    public String username;

    public LargeFileProcessor(String username) throws SQLException, ClassNotFoundException {
        this.username = username;
        establishDbConnection();

        if(sharedSkuSet.isEmpty()){
            loadProductDataFromDB();
        }
    }

    private int getProductCount(String productNameSearched) throws SQLException {
        int productCount = 0;
        String query =String.format("SELECT count from freqtable where name='%s'", productNameSearched);
        PreparedStatement preparedStatement =connection.prepareStatement(query);
        ResultSet resultSet =preparedStatement.executeQuery();
        if(resultSet.next()){
            productCount = resultSet.getInt("count");
        }
        resultSet.close();
        return productCount;
    }

    private CSVParser getProductDataFromFile(String csvFilePath) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFilePath));
        return CSVParser.parse(bufferedReader, CSVFormat.EXCEL.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
    }
    private void loadProductDataFromDB() throws SQLException {
        String query = "SELECT name,sku,description FROM product";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet resultSet =preparedStatement.executeQuery();

        while(resultSet.next())
        {
            String sku =resultSet.getString("sku");//enum
            synchronized (sharedSkuSet){
                sharedSkuSet.add(sku);
            }
        }
        resultSet.close();
    }

    private boolean isSkuUnique(String sku, Set<String> skuSet){
        if(skuSet.contains(sku)){
            return false;
        }
        synchronized (sharedSkuSet){
            if(sharedSkuSet.contains(sku)){
                return false;
            }
            sharedSkuSet.add(sku);
        }
        skuSet.add(sku);
        return true;
    }
    public void insertFromCsvFile(String csvFilePath) {
        try {
            checkEmptyString(csvFilePath);
            System.out.println("reading file now");
            CSVParser csvParser = getProductDataFromFile(csvFilePath);
            System.out.println("file reading complete");
            ArrayList<Product> products = new ArrayList<>();
            Set<String> skuSet = new HashSet<>();
            HashMap<String, Integer> productNameFrequencyHashMap = new HashMap<>();

            for (CSVRecord record : csvParser) {
                String sku = record.get(1);
                if (isSkuUnique(sku, skuSet)) {
                    addProductRecord(record, productNameFrequencyHashMap, products);
                }
            }
            writeProductsToDB(products);
            insertIntoFrequencyTable(productNameFrequencyHashMap);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void checkEmptyString(String csvFilePath) throws Exception{
        if(csvFilePath==null || csvFilePath.length()==0){
            throw new Exception();
        }
    }

    private void addProductRecord(CSVRecord record, HashMap<String,Integer> productNameFrequencyHashMap, ArrayList<Product> products){
        Product product = new Product(record);
        String productName = record.get(0);
        if(productNameFrequencyHashMap.containsKey(productName))
            productNameFrequencyHashMap.put((productName), productNameFrequencyHashMap.get(productName)+1);
        else
            productNameFrequencyHashMap.put(productName,1);
        products.add(product);
    }

    private void writeProductsToDB(ArrayList<Product> products) throws SQLException {
        String insertQueryString = "INSERT INTO product(name,sku,description) VALUES (?,?,?)";
        PreparedStatement insertStatement = connection.prepareStatement(insertQueryString);
        for (Product product : products) {
            insertStatement.setString(1, product.getName());
            insertStatement.setString(2, product.getSku());
            insertStatement.setString(3,product.getDescription());
            insertStatement.addBatch();
        }
        insertStatement.executeBatch();
        connection.commit();
        System.out.printf("inserted %d rows into product table for user %s.%n", products.size(), username);
    }

    private synchronized void insertIntoFrequencyTable(HashMap<String, Integer> productNameFrequencyHashMap) throws SQLException {
        String sql = "INSERT INTO freqtable(name,count) VALUES (?,?) on duplicate key UPDATE count = count + ?";
        PreparedStatement upsertStmt = connection.prepareStatement(sql);

        for(Map.Entry<String,Integer> entry: productNameFrequencyHashMap.entrySet()){
            String productName = entry.getKey();
            Integer productCount = entry.getValue();
            upsertStmt.setString(1, productName);
            upsertStmt.setInt(2, productCount);
            upsertStmt.setInt(3, productCount);
            upsertStmt.addBatch();
        }

        upsertStmt.executeBatch();
        connection.commit();
        System.out.printf("upserted %d rows into frequency table for user %s.%n", productNameFrequencyHashMap.size(), username);
    }
    private void establishDbConnection() throws ClassNotFoundException, SQLException {
        System.out.println("Creating connection for user " + username);
        Class.forName("com.mysql.cj.jdbc.Driver");
        // connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/productdata", "root", "root");
        connection = DriverManager.getConnection("jdbc:mysql://host.docker.internal:3306/mysql", "root", "password");
        System.out.println("connection successful for user " + username);
        connection.setAutoCommit(false);
    }

    public void closeSession(String username) throws SQLException {
        connection.close();
        System.out.println("Closed session for user "+username);
    }

    public void queryRequestedNames(List<String> requestedQueryNames) {
        try {
            checkNullList(requestedQueryNames);
            for (String requestedQueryName : requestedQueryNames) {
                int count = getProductCount(requestedQueryName);
                System.out.println("For user " + username + " for name " + requestedQueryName + " count=" + count);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void checkNullList(List<String> requestedQueryNames) throws Exception {
        if(requestedQueryNames == null){
            throw new Exception();
        }
    }
}
