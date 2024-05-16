package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        // Создаем список сотрудников
        Worker.addWorker("Азазель", 23);
        Worker.addWorker("Хасбик", 98);

        // Назначаем задачи для сотрудников
        Task.addTask(23);
        Task.addTask(98);

        // Запускаем потоки для каждого сотрудника
        ExecutorService executor = Executors.newFixedThreadPool(Worker.listOfWorkers.size());
        for (Worker worker : Worker.listOfWorkers) {
            executor.execute(worker);
        }
        executor.shutdown();
    }
}