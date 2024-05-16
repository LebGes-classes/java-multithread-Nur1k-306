package org.example;


import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.LinkedList;

public class Worker implements Runnable {
    public static LinkedList<Worker> listOfWorkers = new LinkedList<>();
    private static final String WORKERS_FILE_PATH = "workers.xlsx";
    private static final int MAX_WORK_HOURS = 8;
    private static final int SLEEP_TIME = 12;

    private String name;
    private int id;
    private boolean status;
    private int workedHours;
    private int idleHours;
    private LinkedList<Task> tasks;

    public Worker(String name, int id) {
        this.name = name;
        this.id = id;
        this.status = true;
        this.workedHours = 0;
        this.idleHours = 0;
        this.tasks = new LinkedList<>();
    }

    @Override
    public void run() {
        while (!tasks.isEmpty()) {
            Task currentTask = tasks.peekFirst();
            int hoursToWork = Math.min(currentTask.remainingHours, MAX_WORK_HOURS - workedHours);

            while (currentTask.status) {
                workedHours += hoursToWork;
                currentTask.remainingHours -= hoursToWork;

                if (currentTask.remainingHours == 0) {
                    currentTask.status = false;
                    tasks.pollFirst();
                    Task.changeTaskStatus(currentTask.number);
                }

                if (workedHours >= MAX_WORK_HOURS) {
                    logDailyWork();
                    resetDailyHours();
                    sleep();
                }
            }
        }
        logCompletion();
    }

    private void logDailyWork() {
        System.out.println(name + " завершил рабочий день. Рабочие часы: " + workedHours + ", Часы простоя: " + idleHours);
    }

    private void resetDailyHours() {
        idleHours += SLEEP_TIME;
        workedHours = 0;
    }

    private void sleep() {
        try {
            Thread.sleep(SLEEP_TIME * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void logCompletion() {
        System.out.println(name + " выполнил все задачи. Общее время на работе: " + (workedHours + idleHours));
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public static void addWorker(String name, int id) {
        if (!exists(id)) {
            Worker worker = new Worker(name, id);
            listOfWorkers.add(worker);
            addWorkerToFile(worker);
        } else {
            System.out.println("Работник с id " + id + " уже существует");
        }
    }

    private static void addWorkerToFile(Worker worker) {
        try (FileInputStream fis = new FileInputStream(WORKERS_FILE_PATH);
             Workbook workbook = new XSSFWorkbook(fis);
             FileOutputStream fos = new FileOutputStream(WORKERS_FILE_PATH)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            Row newRow = sheet.createRow(lastRowNum + 1);

            newRow.createCell(0).setCellValue(worker.name);
            newRow.createCell(1).setCellValue(worker.id);
            newRow.createCell(2).setCellValue(worker.status);

            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Worker getWorkerById(int id) {
        return listOfWorkers.stream().filter(worker -> worker.id == id).findFirst().orElse(null);
    }

    public static boolean exists(int id) {
        return listOfWorkers.stream().anyMatch(worker -> worker.id == id);
    }

    public static boolean isActive(int id) {
        Worker worker = getWorkerById(id);
        return worker != null && worker.status;
    }
}