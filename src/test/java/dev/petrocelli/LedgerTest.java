package dev.petrocelli;

import java.math.BigDecimal;
import java.time.*;

public final class LedgerTest {
    private static void check(boolean value) { if(!value) throw new AssertionError(); }
    private static Ledger.Entry entry(String id,String date,String category,Ledger.Type type,String amount) {
        return new Ledger.Entry(id,LocalDate.parse(date),category,type,new BigDecimal(amount));
    }
    private static void rejects(Class<? extends Throwable> type,Runnable action) {
        try { action.run(); } catch(Throwable e) { if(type.isInstance(e)) return; throw e; }
        throw new AssertionError("Expected "+type);
    }
    public static void main(String[] args) {
        var ledger=new Ledger(); var month=YearMonth.of(2026,10);
        var salary=entry("1","2026-10-01","Salary",Ledger.Type.INCOME,"1000");
        ledger.append(salary); ledger.append(salary);
        ledger.append(entry("2","2026-10-02","Food",Ledger.Type.EXPENSE,"0.10"));
        ledger.append(entry("3","2026-10-03","Food",Ledger.Type.EXPENSE,"0.20"));
        ledger.append(entry("4","2026-11-01","Food",Ledger.Type.EXPENSE,"50"));
        check(ledger.balance(month).equals(new BigDecimal("999.70")));
        check(ledger.expensesByCategory(month).get("Food").equals(new BigDecimal("0.30")));
        check(ledger.balance(YearMonth.of(2025,1)).equals(new BigDecimal("0.00")));
        rejects(IllegalArgumentException.class,()->ledger.append(entry("1","2026-10-01","Salary",Ledger.Type.INCOME,"999")));
        rejects(ArithmeticException.class,()->entry("5","2026-10-01","Food",Ledger.Type.EXPENSE,"1.001"));
        rejects(IllegalArgumentException.class,()->entry("5","2026-10-01","Food",Ledger.Type.EXPENSE,"0"));
        rejects(UnsupportedOperationException.class,()->ledger.expensesByCategory(month).clear());
        ledger.append(entry("=ID","2026-10-01","=SUM(1,2)\"\n",Ledger.Type.EXPENSE,"1"));
        check(ledger.exportCsv().contains("\"'=ID\""));
        check(ledger.exportCsv().contains("\"'=SUM(1,2)\"\"\n\""));
        System.out.println("Ledger: OK");
    }
}
