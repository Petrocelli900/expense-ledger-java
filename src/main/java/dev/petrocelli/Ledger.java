package dev.petrocelli;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;

/** Thread-safe, append-only ledger in one currency chosen by the caller. */
public final class Ledger {
    public enum Type { INCOME, EXPENSE }
    public record Entry(String id, LocalDate date, String category, Type type, BigDecimal amount) {
        public Entry {
            if(id==null || id.isBlank() || category==null || category.isBlank())
                throw new IllegalArgumentException("Identifiers cannot be blank");
            Objects.requireNonNull(date); Objects.requireNonNull(type); Objects.requireNonNull(amount);
            amount=amount.setScale(2,RoundingMode.UNNECESSARY);
            if(amount.signum()<=0) throw new IllegalArgumentException("Amount must be positive");
        }
    }
    private final Map<String,Entry> entries=new LinkedHashMap<>();
    public synchronized Entry append(Entry entry) {
        Objects.requireNonNull(entry);
        var old=entries.get(entry.id());
        if(old!=null && !old.equals(entry)) throw new IllegalArgumentException("Conflicting entry id");
        if(old==null) entries.put(entry.id(),entry);
        return entry;
    }
    public synchronized BigDecimal balance(YearMonth month) {
        Objects.requireNonNull(month);
        return entries.values().stream().filter(e->YearMonth.from(e.date()).equals(month))
            .map(e->e.type()==Type.INCOME?e.amount():e.amount().negate())
            .reduce(new BigDecimal("0.00"),BigDecimal::add);
    }
    public synchronized Map<String,BigDecimal> expensesByCategory(YearMonth month) {
        Objects.requireNonNull(month); var result=new TreeMap<String,BigDecimal>();
        entries.values().stream().filter(e->e.type()==Type.EXPENSE && YearMonth.from(e.date()).equals(month))
            .forEach(e->result.merge(e.category(),e.amount(),BigDecimal::add));
        return Collections.unmodifiableMap(result);
    }
    private static String csv(String value) {
        // Defend against formula interpretation when a CSV is opened by a spreadsheet.
        String stripped=value.stripLeading();
        if(!stripped.isEmpty() && "=+-@".indexOf(stripped.charAt(0))>=0) value="'"+value;
        return "\""+value.replace("\"","\"\"")+"\"";
    }
    public synchronized String exportCsv() {
        var out=new StringBuilder("id,date,category,type,amount\r\n");
        for(var e:entries.values()) out.append(csv(e.id())).append(',').append(e.date()).append(',')
            .append(csv(e.category())).append(',').append(e.type()).append(',')
            .append(e.amount().toPlainString()).append("\r\n");
        return out.toString();
    }
}
