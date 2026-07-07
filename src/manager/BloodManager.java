package manager;

import base.Manager;
import model.*;
import util.FileUtil;
import java.time.LocalDate;
import java.util.*;

public class BloodManager {
    public final Manager<Donor> donors = new Manager<Donor>("bl_donors.txt"){
        @Override public Donor fromFileString(String l){ return Donor.fromFile(l); }
        @Override public String getIdPrefix(){ return "DN"; }
    };
    public final Manager<BloodRequest> requests = new Manager<BloodRequest>("bl_requests.txt"){
        @Override public BloodRequest fromFileString(String l){ return BloodRequest.fromFile(l); }
        @Override public String getIdPrefix(){ return "BR"; }
    };

    private static final String STOCK_FILE = "bl_stock.txt";
    private static final String[] TYPES = {"A+","A-","B+","B-","O+","O-","AB+","AB-"};
    private final Map<String,Integer> stock = new LinkedHashMap<>();

    public BloodManager(){
        for(String t:TYPES) stock.put(t,0);
        for(String l:FileUtil.readLines(STOCK_FILE)){
            String[] p=l.split("\\|",-1);
            if(p.length==2) stock.put(p[0],Integer.parseInt(p[1]));
        }
    }

    public int getStock(String type){ return stock.getOrDefault(type,0); }

    public void addStock(String type, int units){
        stock.put(type, stock.getOrDefault(type,0)+units);
        saveStock();
    }

    public boolean dispense(String type, int units){
        int cur=stock.getOrDefault(type,0);
        if(cur<units) return false;
        stock.put(type,cur-units);
        saveStock();
        return true;
    }

    public Map<String,Integer> getAllStock(){ return Collections.unmodifiableMap(stock); }
    public String[] getTypes(){ return TYPES; }

    public void recordDonation(String donorId, int units){
        Donor d=donors.findById(donorId);
        if(d==null) throw new IllegalArgumentException("Donor not found.");
        d.setLastDonation(LocalDate.now().toString());
        donors.update(d);
        addStock(d.getBloodType(),units);
    }

    public void fulfilRequest(String requestId){
        BloodRequest r=requests.findById(requestId);
        if(r==null||!"Pending".equals(r.getStatus())) throw new IllegalArgumentException("Pending request not found.");
        if(!dispense(r.getBloodType(),r.getUnits()))
            throw new IllegalArgumentException("Insufficient stock of "+r.getBloodType());
        r.setStatus("Fulfilled");
        requests.update(r);
    }

    private void saveStock(){
        List<String> lines=new ArrayList<>();
        for(Map.Entry<String,Integer> e:stock.entrySet()) lines.add(e.getKey()+"|"+e.getValue());
        FileUtil.writeLines(STOCK_FILE,lines);
    }
}
