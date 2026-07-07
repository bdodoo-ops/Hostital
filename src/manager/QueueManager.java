package manager;

import base.Manager;
import model.QueuePatient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class QueueManager extends Manager<QueuePatient> {

    public static final String[] DEPARTMENTS = {"General","Emergency","Pediatrics","Cardiology","Orthopedics","Neurology"};
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

    public QueueManager() { super("q_patients.txt"); }

    @Override public QueuePatient fromFileString(String l){ return QueuePatient.fromFile(l); }
    @Override public String getIdPrefix(){ return "Q"; }

    public void addPatient(String name,int age,String gender,String dept,int priority){
        String time = LocalDateTime.now().format(FMT);
        QueuePatient p = new QueuePatient(nextId(),name,age,gender,dept,priority,time,"Waiting");
        add(p);
    }

    /** Serve next waiting patient in a department (highest priority first). */
    public QueuePatient serveNext(String dept){
        List<QueuePatient> waiting = items.stream()
            .filter(p -> p.getDepartment().equals(dept) && "Waiting".equals(p.getStatus()))
            .sorted(Comparator.comparingInt(QueuePatient::getPriority))
            .collect(Collectors.toList());
        if(waiting.isEmpty()) return null;
        QueuePatient next = waiting.get(0);
        next.setStatus("Served");
        saveToFile();
        return next;
    }

    public List<QueuePatient> getByDept(String dept){
        return items.stream()
            .filter(p -> p.getDepartment().equals(dept) && "Waiting".equals(p.getStatus()))
            .sorted(Comparator.comparingInt(QueuePatient::getPriority))
            .collect(Collectors.toList());
    }
}
