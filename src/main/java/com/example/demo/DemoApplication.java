package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.sql.*;
import java.util.*;

@SpringBootApplication
public class DemoApplication {
	public static void main(String[] args) throws SQLException {
		SpringApplication.run(DemoApplication.class, args);
		/*
		while(true){
			System.out.println("rolling");
		}
		*/

		ExecutorService executor = Executors.newFixedThreadPool(10);
		String csvFilePath = "app/products.csv";
		List<Task> taskList = new ArrayList<Task>();
		taskList.add(new Task(
				"User1", csvFilePath, Arrays.asList("David Wright", "John Buckley")));
		taskList.add(new Task(
				"User2", csvFilePath, Arrays.asList("Anthony Burch", "Albert Einstein")));

		for(Task task:taskList){
			executor.submit(task);
		}
		executor.shutdown();
		System.out.println("terminate now");

	}
}
