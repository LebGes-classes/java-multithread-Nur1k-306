package org.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Task {
    private static final String TASKS_FILE_PATH = "tasks.xlsx";
    private static int taskCounter = 1;

    public int number; // Порядковый номер задачи
    private int workerId; // ID исполнителя
    public int remainingHours; // Время в часах, выделенное на исполнение задачи (>=1 && <= 16)
    public boolean status; // Статус задачи (false, если задача выполнена)

    public Task(int workerId) {
        this.workerId = workerId;
        this.status = true;
        this.number = taskCounter++;

        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите время, выделенное на выполнение задачи (от 1 до 16 часов):");
        while (true) {
            int time = scanner.nextInt();
            if (time >= 1 && time <= 16) {
                this.remainingHours = time;
                break;
            } else {
                System.out.println("Неверное время, попробуйте снова:");
            }
        }

        addTaskToFile(this);
    }

    public static void addTask(int workerId) {
        if (Worker.exists(workerId) && Worker.isActive(workerId)) {
            Worker worker = Worker.getWorkerById(workerId);
            if (worker != null) {
                worker.addTask(new Task(workerId));
            } else {
                System.out.println("Работник с номером " + workerId + " не найден");
            }
        } else {
            System.out.println("Работник с номером " + workerId + " не существует или уволен");
        }
    }

    private static void addTaskToFile(Task task) {
        try (FileInputStream fis = new FileInputStream(TASKS_FILE_PATH);
             Workbook workbook = new XSSFWorkbook(fis);
             FileOutputStream fos = new FileOutputStream(TASKS_FILE_PATH)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            Row newRow = sheet.createRow(lastRowNum + 1);

            newRow.createCell(0).setCellValue(task.number);
            newRow.createCell(1).setCellValue(task.workerId);
            newRow.createCell(2).setCellValue(task.remainingHours);
            newRow.createCell(3).setCellValue(task.status);

            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void changeTaskStatus(int taskNumber) {
        try (FileInputStream fis = new FileInputStream(TASKS_FILE_PATH);
             Workbook workbook = new XSSFWorkbook(fis);
             FileOutputStream fos = new FileOutputStream(TASKS_FILE_PATH)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                Cell numberCell = row.getCell(0);
                if (numberCell != null && numberCell.getNumericCellValue() == taskNumber) {
                    Cell statusCell = row.getCell(3);
                    statusCell.setCellValue(!statusCell.getBooleanCellValue());
                    break;
                }
            }
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
